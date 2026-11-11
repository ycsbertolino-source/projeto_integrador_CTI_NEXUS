from typing import Any


def brl(valor: float) -> str:
    formatado = f"{valor:,.2f}".replace(",", "X").replace(".", ",").replace("X", ".")
    return f"R$ {formatado}"


def gerar_insights(estatisticas: dict[str, Any]) -> list[str]:
    frases = []
    segmentos = estatisticas["porSegmento"]
    servicos = estatisticas["porServico"]
    resumo = estatisticas["resumo"]

    if segmentos:
        principal = max(segmentos, key=lambda item: item["faturamentoTotal"])
        frases.append(
            f'O segmento {principal["segmento"]} concentra {brl(principal["faturamentoTotal"])} em faturamento.'
        )
    if servicos:
        frases.append(f'O serviço mais contratado é {servicos[0]["servico"]}.')
    if resumo["media"]:
        frases.append(f'O faturamento médio por cliente é {brl(resumo["media"])}.')
    if estatisticas["moda"]["nivel"]:
        frases.append(f'O nível de cliente mais frequente é {estatisticas["moda"]["nivel"]}.')
    return frases
