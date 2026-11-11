from datetime import datetime, timezone
import json
from typing import Any

from sqlalchemy import (
    BigInteger, Column, Date, DateTime, ForeignKey, Identity, Integer, MetaData,
    Numeric, String, Table, Text, UniqueConstraint, create_engine, delete, insert,
    select, update,
)
from sqlalchemy.engine import Engine

from config import database_url

metadata = MetaData()
ID_TYPE = BigInteger().with_variant(Integer, "sqlite")

upload_lote = Table(
    "upload_lote", metadata,
    Column("id", ID_TYPE, Identity(), primary_key=True),
    Column("arquivo_nome", String(255), nullable=False),
    Column("linhas_recebidas", Integer, nullable=False, default=0),
    Column("linhas_validas", Integer, nullable=False, default=0),
    Column("linhas_invalidas", Integer, nullable=False, default=0),
    Column("linhas_com_aviso", Integer, nullable=False, default=0),
    Column("status", String(20), nullable=False, default="PROCESSANDO"),
    Column("mensagem_erro", String(2000)),
    Column("criado_em", DateTime, nullable=False),
)
consultor = Table(
    "consultor", metadata,
    Column("id", ID_TYPE, Identity(), primary_key=True),
    Column("nome", String(120), nullable=False, unique=True),
)
segmento = Table(
    "segmento", metadata,
    Column("id", ID_TYPE, Identity(), primary_key=True),
    Column("nome", String(80), nullable=False, unique=True),
)
servico = Table(
    "servico", metadata,
    Column("id", ID_TYPE, Identity(), primary_key=True),
    Column("nome", String(120), nullable=False, unique=True),
)
cliente = Table(
    "cliente", metadata,
    Column("id", ID_TYPE, Identity(), primary_key=True),
    Column("codigo", String(60), nullable=False, unique=True),
    Column("nome", String(255), nullable=False),
    Column("consultor_id", BigInteger, ForeignKey("consultor.id")),
    Column("segmento_id", BigInteger, ForeignKey("segmento.id")),
    Column("nivel", String(20), nullable=False),
    Column("faturamento_anual", Numeric(15, 2), nullable=False),
    Column("data_contratacao", Date, nullable=False),
    Column("cidade", String(120)),
    Column("uf", String(10)),
    Column("upload_lote_id", BigInteger, ForeignKey("upload_lote.id")),
    Column("atualizado_em", DateTime, nullable=False),
)
contrato = Table(
    "contrato", metadata,
    Column("id", ID_TYPE, Identity(), primary_key=True),
    Column("cliente_id", BigInteger, ForeignKey("cliente.id", ondelete="CASCADE"), nullable=False),
    Column("servico_id", BigInteger, ForeignKey("servico.id"), nullable=False),
    UniqueConstraint("cliente_id", "servico_id"),
)
estatistica_lote = Table(
    "estatistica_lote", metadata,
    Column("id", Integer, Identity(), primary_key=True),
    Column("upload_lote_id", BigInteger, ForeignKey("upload_lote.id", ondelete="CASCADE"), nullable=False),
    Column("indicador", String(80), nullable=False),
    Column("valor", Numeric(18, 2)),
    Column("detalhe", String(255)),
)
dashboard_lote = Table(
    "dashboard_lote", metadata,
    Column("upload_lote_id", BigInteger, ForeignKey("upload_lote.id", ondelete="CASCADE"), primary_key=True),
    Column("dados_json", Text, nullable=False),
    Column("criado_em", DateTime, nullable=False),
)


def _agora() -> datetime:
    return datetime.now(timezone.utc).replace(tzinfo=None)


def criar_engine() -> Engine:
    return create_engine(database_url(), pool_pre_ping=True)


def criar_tabelas(engine: Engine) -> None:
    metadata.create_all(engine)


def _buscar_ou_criar(conn, tabela: Table, nome: str) -> int:
    existente = conn.execute(select(tabela.c.id).where(tabela.c.nome == nome)).scalar_one_or_none()
    if existente is not None:
        return existente
    return conn.execute(insert(tabela).values(nome=nome).returning(tabela.c.id)).scalar_one()


def criar_lote(
    engine: Engine,
    arquivo_nome: str,
    recebidas: int,
    invalidas: int,
    com_aviso: int,
) -> int:
    with engine.begin() as conn:
        return conn.execute(insert(upload_lote).values(
            arquivo_nome=arquivo_nome,
            linhas_recebidas=recebidas,
            linhas_validas=0,
            linhas_invalidas=invalidas,
            linhas_com_aviso=com_aviso,
            status="PROCESSANDO",
            criado_em=_agora(),
        ).returning(upload_lote.c.id)).scalar_one()


def gravar_resultado(
    engine: Engine,
    lote_id: int,
    invalidas: int,
    com_aviso: int,
    linhas: list[dict[str, Any]],
    rejeitadas: list[dict[str, Any]],
    estatisticas: dict[str, Any],
) -> None:
    agora = _agora()
    with engine.begin() as conn:
        for linha in linhas:
            consultor_id = _buscar_ou_criar(conn, consultor, linha["consultor"])
            segmento_id = _buscar_ou_criar(conn, segmento, linha["segmento"])
            dados_cliente = {
                "nome": linha["nome_cliente"],
                "consultor_id": consultor_id,
                "segmento_id": segmento_id,
                "nivel": linha["nivel_cliente"],
                "faturamento_anual": linha["faturamento_anual"],
                "data_contratacao": linha["data_contratacao"],
                "cidade": linha["cidade"] or None,
                "uf": linha["uf"] or None,
                "upload_lote_id": lote_id,
                "atualizado_em": agora,
            }
            cliente_id = conn.execute(
                select(cliente.c.id).where(cliente.c.codigo == linha["codigo_cliente"])
            ).scalar_one_or_none()
            if cliente_id is None:
                cliente_id = conn.execute(insert(cliente).values(
                    codigo=linha["codigo_cliente"], **dados_cliente
                ).returning(cliente.c.id)).scalar_one()
            else:
                conn.execute(update(cliente).where(cliente.c.id == cliente_id).values(**dados_cliente))
                conn.execute(delete(contrato).where(contrato.c.cliente_id == cliente_id))

            for nome_servico in linha["servicos_contratados"]:
                servico_id = _buscar_ou_criar(conn, servico, nome_servico)
                conn.execute(insert(contrato).values(
                    cliente_id=cliente_id,
                    servico_id=servico_id,
                ))

        dados = {
            **estatisticas,
            "rejeitadas": rejeitadas,
            "linhasInvalidas": invalidas + len(rejeitadas),
            "comAviso": com_aviso,
        }
        conn.execute(insert(dashboard_lote).values(
            upload_lote_id=lote_id,
            dados_json=json.dumps(dados, ensure_ascii=False, default=str),
            criado_em=agora,
        ))
        resumo = estatisticas.get("resumo", {})
        indicadores = [
            ("total_clientes", estatisticas.get("totalClientes"), None),
            ("total_contratos", estatisticas.get("totalContratos"), None),
            ("faturamento_total", resumo.get("total"), None),
            ("faturamento_medio", resumo.get("media"), None),
        ]
        conn.execute(insert(estatistica_lote), [
            {
                "upload_lote_id": lote_id,
                "indicador": indicador,
                "valor": valor,
                "detalhe": detalhe,
            }
            for indicador, valor, detalhe in indicadores
        ])
        conn.execute(update(upload_lote).where(upload_lote.c.id == lote_id).values(
            linhas_validas=len(linhas),
            linhas_invalidas=invalidas + len(rejeitadas),
            linhas_com_aviso=com_aviso,
            status="PROCESSADO",
            mensagem_erro=None,
        ))


def marcar_erro(engine: Engine, lote_id: int, mensagem: str) -> None:
    with engine.begin() as conn:
        conn.execute(update(upload_lote).where(upload_lote.c.id == lote_id).values(
            status="ERRO",
            mensagem_erro=mensagem[:2000],
        ))


def listar_lotes(engine: Engine) -> list[dict[str, Any]]:
    with engine.connect() as conn:
        linhas = conn.execute(
            select(upload_lote).order_by(upload_lote.c.criado_em.desc(), upload_lote.c.id.desc())
        ).mappings().all()
    return [dict(linha) for linha in linhas]


def obter_dashboard(engine: Engine, lote_id: int | None = None) -> dict[str, Any] | None:
    with engine.connect() as conn:
        consulta = select(upload_lote)
        if lote_id is not None:
            consulta = consulta.where(upload_lote.c.id == lote_id)
        lote = conn.execute(
            consulta.order_by(upload_lote.c.criado_em.desc(), upload_lote.c.id.desc()).limit(1)
        ).mappings().one_or_none()
        if lote is None:
            return None
        estatisticas = conn.execute(
            select(dashboard_lote.c.dados_json).where(
                dashboard_lote.c.upload_lote_id == lote["id"]
            )
        ).scalar_one_or_none()
    resultado = dict(lote)
    resultado["estatisticas"] = json.loads(estatisticas) if estatisticas else None
    return resultado
