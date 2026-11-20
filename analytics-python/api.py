import logging
import os
from typing import Any

from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy.exc import SQLAlchemyError

from banco import criar_engine, criar_tabelas, listar_lotes, obter_dashboard
from main import processar_lote
from limpeza import tratar
from analise import calcular

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="CTI Insights - ETL em Python")


class Pedido(BaseModel):
    arquivoNome: str = "planilha"
    recebidas: int = Field(ge=0)
    invalidas: int = Field(default=0, ge=0)
    comAviso: int = Field(default=0, ge=0)
    linhas: list[dict[str, Any]]


def _engine():
    try:
        engine = criar_engine()
        criar_tabelas(engine)
        return engine
    except SQLAlchemyError as error:
        logger.exception("Falha ao conectar ou preparar o PostgreSQL")
        raise HTTPException(status_code=503, detail="Banco de dados indisponível.") from error


def _validar_chave(chave_enviada: str | None) -> None:
    chave_configurada = os.getenv("ETL_API_KEY", "").strip()
    if chave_configurada and chave_enviada != chave_configurada:
        raise HTTPException(status_code=401, detail="Chave ETL inválida.")


@app.get("/saude")
@app.get("/health", include_in_schema=False)
def saude():
    return {"status": "ok"}


@app.post("/etl")
def etl(
    pedido: Pedido,
    x_etl_key: str | None = Header(default=None, alias="X-ETL-Key"),
):
    _validar_chave(x_etl_key)
    engine = _engine()
    try:
        return processar_lote(engine, pedido.model_dump())
    except (ValueError, SQLAlchemyError) as error:
        logger.exception("Falha ao processar o lote do arquivo %s", pedido.arquivoNome)
        raise HTTPException(status_code=500, detail=f"Falha no ETL: {error}") from error


@app.post("/testar")
def testar(linhas: list[dict[str, Any]]):
    resultado = tratar(linhas)
    return {
        "linhas": resultado.tratado.to_dict(orient="records"),
        "rejeitadas": resultado.rejeitadas,
        "estatisticas": calcular(resultado.tratado, len(linhas)),
    }


# /lotes e /dashboard também exigem a chave: com o endereço público na internet,
# sem ela qualquer pessoa leria os dados. O Java não usa estas rotas (ele lê o banco direto).
@app.get("/lotes")
def lotes(x_etl_key: str | None = Header(default=None, alias="X-ETL-Key")):
    _validar_chave(x_etl_key)
    return listar_lotes(_engine())


@app.get("/dashboard")
@app.get("/dashboard/{lote_id}")
def dashboard(
    lote_id: int | None = None,
    x_etl_key: str | None = Header(default=None, alias="X-ETL-Key"),
):
    _validar_chave(x_etl_key)
    try:
        resultado = obter_dashboard(_engine(), lote_id)
    except SQLAlchemyError as error:
        logger.exception("Falha ao consultar o lote %s", lote_id)
        raise HTTPException(status_code=500, detail="Falha ao consultar as estatísticas.") from error
    if resultado is None:
        raise HTTPException(status_code=404, detail="Lote não encontrado ou ainda sem estatísticas.")
    return resultado
