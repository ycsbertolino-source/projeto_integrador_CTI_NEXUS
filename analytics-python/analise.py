from typing import Any

import pandas as pd
from scipy.stats import pearsonr

from insights import gerar_insights


def _registros(agrupado: pd.DataFrame, nome: str) -> list[dict[str, Any]]:
    return agrupado.rename(columns={agrupado.columns[0]: nome}).to_dict(orient="records")


def calcular(resultado: pd.DataFrame, linhas_recebidas: int) -> dict[str, Any]:
    if resultado.empty:
        return {
            "totalClientes": 0,
            "totalContratos": 0,
            "linhasRecebidas": linhas_recebidas,
            "resumo": {
                "media": 0, "mediana": 0, "desvioPadrao": 0,
                "minimo": 0, "maximo": 0, "q1": 0, "q3": 0,
            },
            "porSegmento": [],
            "porNivel": [],
            "porConsultor": [],
            "porServico": [],
            "porMes": [],
            "topClientes": [],
            "correlacao": {"servicosEFaturamento": None},
            "moda": {"segmento": None, "nivel": None, "servico": None},
            "insights": [],
        }

    faturamento = resultado["faturamento_anual"].astype(float)
    resumo = {
        "media": float(faturamento.mean()),
        "mediana": float(faturamento.median()),
        "desvioPadrao": float(faturamento.std(ddof=1)) if len(faturamento) > 1 else 0,
        "minimo": float(faturamento.min()),
        "maximo": float(faturamento.max()),
        "q1": float(faturamento.quantile(0.25)),
        "q3": float(faturamento.quantile(0.75)),
        "total": float(faturamento.sum()),
    }

    por_segmento = resultado.groupby("segmento", as_index=False).agg(
        clientes=("codigo_cliente", "count"), faturamentoTotal=("faturamento_anual", "sum"),
        media=("faturamento_anual", "mean"),
    ).sort_values("clientes", ascending=False)
    por_nivel = resultado.groupby("nivel_cliente", as_index=False).agg(
        clientes=("codigo_cliente", "count"), faturamentoTotal=("faturamento_anual", "sum"),
    ).rename(columns={"nivel_cliente": "nivel"})
    por_consultor = resultado.groupby("consultor", as_index=False).agg(
        clientes=("codigo_cliente", "count"), faturamentoTotal=("faturamento_anual", "sum"),
    )

    servicos = resultado[["codigo_cliente", "servicos_contratados"]].explode("servicos_contratados")
    por_servico = servicos.groupby("servicos_contratados", as_index=False).agg(
        clientes=("codigo_cliente", "nunique"),
    ).rename(columns={"servicos_contratados": "servico"}).sort_values("clientes", ascending=False)
    por_mes = resultado.assign(
        mes=pd.to_datetime(resultado["data_contratacao"]).dt.strftime("%Y-%m")
    ).groupby("mes", as_index=False).agg(
        contratacoes=("codigo_cliente", "count"), faturamentoTotal=("faturamento_anual", "sum"),
    ).sort_values("mes")

    correlacao = None
    contagem_servicos = servicos.groupby("codigo_cliente").size().reindex(resultado["codigo_cliente"])
    if len(resultado) > 1 and contagem_servicos.nunique() > 1 and faturamento.nunique() > 1:
        correlacao = float(pearsonr(contagem_servicos.to_numpy(), faturamento.to_numpy()).statistic)

    contagens_servicos = servicos["servicos_contratados"].value_counts()
    moda = {
        "segmento": resultado["segmento"].mode().iat[0],
        "nivel": resultado["nivel_cliente"].mode().iat[0],
        "servico": contagens_servicos.index[0] if not contagens_servicos.empty else None,
    }
    top_clientes = resultado.nlargest(5, "faturamento_anual")[[
        "codigo_cliente", "nome_cliente", "faturamento_anual",
    ]].rename(columns={
        "codigo_cliente": "codigoCliente",
        "nome_cliente": "nomeCliente",
        "faturamento_anual": "faturamentoAnual",
    }).to_dict(orient="records")

    dados = {
        "totalClientes": int(len(resultado)),
        "totalContratos": int(len(servicos)),
        "linhasRecebidas": int(linhas_recebidas),
        "resumo": resumo,
        "porSegmento": _registros(por_segmento, "segmento"),
        "porNivel": _registros(por_nivel, "nivel"),
        "porConsultor": _registros(por_consultor, "consultor"),
        "porServico": por_servico.to_dict(orient="records"),
        "porMes": por_mes.to_dict(orient="records"),
        "topClientes": top_clientes,
        "correlacao": {"servicosEFaturamento": correlacao},
        "moda": moda,
    }
    dados["insights"] = gerar_insights(dados)
    return dados
