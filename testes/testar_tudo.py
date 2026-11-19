"""
testar_tudo.py — confere se o Python, o Java e o dashboard estão no ar.

Uso (na raiz do projeto):
  python testes/testar_tudo.py                                   # testa o seu computador (localhost)
  python testes/testar_tudo.py --back https://cti-api-SEUNOME.azurewebsites.net \
                               --python https://cti-python-SEUNOME.azurewebsites.net

O login usa ADMIN_EMAIL e ADMIN_PASSWORD (variáveis de ambiente ou backend/.env).
Se não encontrar, pergunta a senha no terminal. Nada é gravado: o teste só LÊ.
"""
import argparse
import getpass
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")  # acentos no terminal do Windows
resultados = []  # (ok, nome, detalhe)


def chamar(url, metodo="GET", corpo=None, token=None):
    """Faz o pedido HTTP e devolve (status, json ou texto)."""
    cabecalhos = {"Content-Type": "application/json"}
    if token:
        cabecalhos["Authorization"] = f"Bearer {token}"
    dados = json.dumps(corpo).encode() if corpo is not None else None
    pedido = urllib.request.Request(url, data=dados, method=metodo, headers=cabecalhos)
    try:
        with urllib.request.urlopen(pedido, timeout=90) as resposta:  # a Azure gratuita pode demorar para "acordar"
            texto = resposta.read().decode()
            status = resposta.status
    except urllib.error.HTTPError as erro:
        texto, status = erro.read().decode(errors="replace"), erro.code
    except (urllib.error.URLError, TimeoutError) as erro:
        return 0, str(getattr(erro, "reason", erro))
    try:
        return status, json.loads(texto)
    except ValueError:
        return status, texto


def conferir(nome, ok, detalhe=""):
    resultados.append((ok, nome, detalhe))
    print(f"  {'OK   ' if ok else 'FALHA'}  {nome}" + (f"  ->  {detalhe}" if detalhe else ""))
    return ok


def ler_env_backend():
    arquivo = RAIZ / "backend" / ".env"
    valores = {}
    if arquivo.exists():
        for linha in arquivo.read_text(encoding="utf-8").splitlines():
            if "=" in linha and not linha.lstrip().startswith("#"):
                chave, valor = linha.split("=", 1)
                valores[chave.strip()] = valor.strip()
    return valores


def main():
    args = argparse.ArgumentParser(description="Testa Python, Java e dashboard.")
    args.add_argument("--back", default="http://localhost:8080", help="endereço do Java (sem /api)")
    args.add_argument("--python", default="http://localhost:8000", help="endereço do Python")
    opcoes = args.parse_args()
    back = opcoes.back.rstrip("/").removesuffix("/api")
    python = opcoes.python.rstrip("/")

    env = ler_env_backend()
    email = os.getenv("ADMIN_EMAIL") or env.get("ADMIN_EMAIL") or "admin@cti.local"
    senha = os.getenv("ADMIN_PASSWORD") or env.get("ADMIN_PASSWORD") or getpass.getpass(f"Senha do {email}: ")

    print("\n1) Python")
    status, corpo = chamar(f"{python}/saude")
    conferir("GET /saude", status == 200 and isinstance(corpo, dict) and corpo.get("status") == "ok", f"HTTP {status}")

    print("\n2) Java")
    status, corpo = chamar(f"{back}/api/health")
    conferir("GET /api/health", status == 200, f"HTTP {status}")

    print("\n3) Login")
    status, corpo = chamar(f"{back}/api/auth/login", "POST", {"email": email, "senha": senha})
    token = corpo.get("token") if isinstance(corpo, dict) else None
    conferir(f"POST /api/auth/login ({email})", status == 200 and bool(token), f"HTTP {status}")

    print("\n4) Dashboard")
    if not token:
        conferir("GET /api/dashboard", False, "sem login, não dá para testar")
    else:
        status, lotes = chamar(f"{back}/api/dashboard/lotes", token=token)
        conferir("GET /api/dashboard/lotes", status == 200 and isinstance(lotes, list),
                 f"{len(lotes)} lote(s)" if isinstance(lotes, list) else f"HTTP {status}")
        status, dash = chamar(f"{back}/api/dashboard", token=token)
        if status == 404:
            conferir("GET /api/dashboard", True, "banco vazio: envie uma planilha pelo site")
        else:
            est = dash.get("estatisticas") if isinstance(dash, dict) else None
            lote = dash.get("lote", {}) if isinstance(dash, dict) else {}
            total = f"{est.get('resumo', {}).get('total', 0):,.2f}".replace(",", "X").replace(".", ",").replace("X", ".") if est else ""
            detalhe = (f"lote #{lote.get('id')} - {est.get('totalClientes')} clientes - R$ {total}") if est else f"HTTP {status}"
            conferir("GET /api/dashboard", status == 200 and est is not None, detalhe)

    falhas = sum(1 for ok, _, _ in resultados if not ok)
    print(f"\nResultado: {len(resultados) - falhas} OK · {falhas} FALHA\n")
    sys.exit(1 if falhas else 0)


if __name__ == "__main__":
    main()
