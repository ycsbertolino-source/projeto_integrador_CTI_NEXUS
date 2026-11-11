from sqlalchemy import text

from banco import criar_engine


def main() -> None:
    engine = criar_engine()
    with engine.connect() as conexao:
        conexao.execute(text("SELECT 1"))
    print("Conectou!")


if __name__ == "__main__":
    main()
