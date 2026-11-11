import re
import unicodedata
from dataclasses import dataclass
from decimal import Decimal, InvalidOperation
from typing import Any

import pandas as pd


@dataclass
class ResultadoTransformacao:
    tratado: pd.DataFrame
    rejeitadas: list[dict[str, Any]]
    avisos: list[str]


def _texto(valor: Any) -> str:
    return "" if valor is None or pd.isna(valor) else str(valor).strip()


def _chave_sem_acento(valor: str) -> str:
    return "".join(
        letra for letra in unicodedata.normalize("NFKD", valor)
        if not unicodedata.combining(letra)
    ).upper()


def titulo(valor: Any) -> str:
    texto = _texto(valor)
    return " ".join(parte.capitalize() for parte in texto.split())


def padronizar_segmento(valor: Any) -> str:
    texto = _texto(valor)
    mapa = {
        "IND": "Indústria",
        "INDUSTRIA": "Indústria",
        "COMERCIO": "Comércio",
        "SERVICO": "Serviços",
        "SERVICOS": "Serviços",
        "SAUDE": "Saúde",
        "EDUCACAO": "Educação",
        "TECNOLOGIA": "Tecnologia",
    }
    return mapa.get(_chave_sem_acento(texto).rstrip("."), titulo(texto))


def padronizar_servicos(valor: Any) -> list[str]:
    return list(dict.fromkeys(titulo(item) for item in re.split(r"[;,]", _texto(valor)) if item.strip()))


def parse_numero(valor: Any) -> Decimal | None:
    if isinstance(valor, (int, float, Decimal)) and not isinstance(valor, bool):
        try:
            numero = Decimal(str(valor))
            return numero if numero.is_finite() else None
        except InvalidOperation:
            return None

    texto = _texto(valor).replace("R$", "").replace(" ", "")
    if not texto:
        return None
    if "," in texto:
        texto = texto.replace(".", "").replace(",", ".")
    try:
        numero = Decimal(texto)
        return numero if numero.is_finite() else None
    except InvalidOperation:
        return None


def _linha_limpa(linha: dict[str, Any], indice: int) -> tuple[dict[str, Any] | None, list[str]]:
    codigo = _texto(linha.get("codigo_cliente", linha.get("codigoCliente"))).upper()
    nome = titulo(linha.get("nome_cliente", linha.get("nomeCliente")))
    consultor = titulo(linha.get("consultor"))
    segmento = padronizar_segmento(linha.get("segmento"))
    nivel = _texto(linha.get("nivel_cliente", linha.get("nivelCliente"))).upper()
    faturamento = parse_numero(
        linha.get("faturamento_anual_num", linha.get("faturamento_anual", linha.get("faturamentoAnual")))
    )
    servicos = padronizar_servicos(
        linha.get("servicos_contratados", linha.get("servicosContratados"))
    )
    data_valor = linha.get("data_contratacao", linha.get("dataContratacao"))
    data_texto = _texto(data_valor)
    if re.fullmatch(r"\d{4}-\d{2}-\d{2}", data_texto):
        data = pd.to_datetime(data_texto, format="%Y-%m-%d", errors="coerce")
    else:
        data = pd.to_datetime(data_valor, dayfirst=True, errors="coerce")

    erros = []
    if not codigo:
        erros.append("codigo_cliente obrigatório")
    if not nome:
        erros.append("nome_cliente obrigatório")
    if not consultor:
        erros.append("consultor obrigatório")
    if not segmento:
        erros.append("segmento obrigatório")
    if nivel not in {"A", "B", "C"}:
        erros.append("nivel_cliente deve ser A, B ou C")
    if faturamento is None or faturamento < 0:
        erros.append("faturamento_anual deve ser um número não negativo")
    if not servicos:
        erros.append("servicos_contratados obrigatório")
    if pd.isna(data):
        erros.append("data_contratacao inválida")

    if erros:
        return None, erros

    linha_limpa = {
        "codigo_cliente": codigo,
        "nome_cliente": nome,
        "consultor": consultor,
        "segmento": segmento,
        "nivel_cliente": nivel,
        "faturamento_anual": float(faturamento),
        "servicos_contratados": servicos,
        "data_contratacao": data.date(),
        "cidade": titulo(linha.get("cidade")),
        "uf": _texto(linha.get("uf")).upper(),
    }
    return linha_limpa, []


def tratar(linhas: list[dict[str, Any]] | pd.DataFrame) -> ResultadoTransformacao:
    brutas = linhas.to_dict(orient="records") if isinstance(linhas, pd.DataFrame) else linhas
    limpas = []
    rejeitadas = []
    codigos = set()

    for indice, linha in enumerate(brutas, start=2):
        numero_linha = linha.get("numero_linha", indice)
        limpa, erros = _linha_limpa(linha, indice)
        if limpa and limpa["codigo_cliente"] in codigos:
            erros.append("codigo_cliente duplicado neste lote")
        if erros:
            rejeitadas.append({"linha": numero_linha, "erros": erros, "dados": linha})
            continue
        codigos.add(limpa["codigo_cliente"])
        limpas.append(limpa)

    return ResultadoTransformacao(
        tratado=pd.DataFrame(limpas),
        rejeitadas=rejeitadas,
        avisos=[],
    )
