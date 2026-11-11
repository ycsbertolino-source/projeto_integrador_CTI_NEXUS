import logging
from typing import Any

from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.engine import Engine

from analise import calcular
from banco import criar_lote, gravar_resultado, marcar_erro
from limpeza import tratar

logger = logging.getLogger(__name__)


def processar_lote(engine: Engine, pedido: dict[str, Any]) -> dict[str, Any]:
    arquivo_nome = pedido.get("arquivoNome") or "planilha"
    recebidas = int(pedido["recebidas"])
    rejeitadas = []

    lote_id = criar_lote(
        engine,
        arquivo_nome,
        recebidas,
        int(pedido.get("invalidas", 0)),
        int(pedido.get("comAviso", 0)),
    )
    try:
        resultado = tratar(pedido.get("linhas", []))
        rejeitadas = resultado.rejeitadas
        estatisticas = calcular(resultado.tratado, recebidas)
        linhas = resultado.tratado.to_dict(orient="records")
        gravar_resultado(
            engine, lote_id, int(pedido.get("invalidas", 0)),
            int(pedido.get("comAviso", 0)), linhas, rejeitadas, estatisticas,
        )
        return {
            "ok": True,
            "loteId": lote_id,
            "arquivoNome": arquivo_nome,
            "status": "PROCESSADO",
            "recebidas": recebidas,
            "invalidas": int(pedido.get("invalidas", 0)) + len(rejeitadas),
            "comAviso": int(pedido.get("comAviso", 0)),
            "validas": len(linhas),
            "estatisticas": estatisticas,
        }
    except (ValueError, SQLAlchemyError) as error:
        logger.exception("Erro ao gravar o lote %s", lote_id)
        marcar_erro(engine, lote_id, str(error))
        raise
