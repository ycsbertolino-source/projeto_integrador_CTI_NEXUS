import os
from pathlib import Path

from dotenv import load_dotenv
from sqlalchemy import URL

raiz_projeto = Path(__file__).resolve().parent.parent
load_dotenv(raiz_projeto / ".env")
load_dotenv(Path(__file__).resolve().parent / ".env", override=True)


def database_url() -> str | URL:
    url = os.getenv("DATABASE_URL")
    if url:
        return url

    # DB_SSLMODE=require é obrigatório no PostgreSQL da Azure (cadastrado nas variáveis do App Service)
    sslmode = os.getenv("DB_SSLMODE", "").strip()
    return URL.create(
        "postgresql+psycopg2",
        username=os.getenv("DB_USER", "postgres"),
        password=os.getenv("DB_PASSWORD", "postgres"),
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "5432")),
        database=os.getenv("DB_NAME", "cti_nexus"),
        query={"sslmode": sslmode} if sslmode else {},
    )
