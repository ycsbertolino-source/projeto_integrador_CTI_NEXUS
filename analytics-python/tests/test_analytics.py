import unittest
from unittest.mock import patch

from sqlalchemy import create_engine, select

from analise import calcular
from banco import criar_tabelas, obter_dashboard, upload_lote
from api import Pedido, dashboard, etl, lotes
from limpeza import tratar
from main import processar_lote


class AnalyticsTests(unittest.TestCase):
    def setUp(self):
        self.engine = create_engine("sqlite:///:memory:")
        criar_tabelas(self.engine)

    def tearDown(self):
        self.engine.dispose()

    def test_normalizes_and_aggregates_slideshow_sample(self):
        linhas = [{
            "codigo_cliente": "cti001",
            "nome_cliente": "metalúrgica horizonte",
            "consultor": "ana souza",
            "segmento": "IND.",
            "nivel_cliente": "a",
            "faturamento_anual": "R$ 1.850.000,00",
            "servicos_contratados": "Internet Dedicada;Firewall",
            "data_contratacao": "15/01/2025",
        }]

        resultado = tratar(linhas)
        estatisticas = calcular(resultado.tratado, len(linhas))

        self.assertEqual(resultado.rejeitadas, [])
        self.assertEqual(resultado.tratado.iloc[0]["consultor"], "Ana Souza")
        self.assertEqual(resultado.tratado.iloc[0]["segmento"], "Indústria")
        self.assertEqual(estatisticas["resumo"]["media"], 1850000)
        self.assertEqual(estatisticas["totalContratos"], 2)

    def test_processes_and_persists_lot_and_statistics(self):
        payload = {
            "arquivoNome": "clientes.csv",
            "recebidas": 1,
            "invalidas": 0,
            "comAviso": 0,
            "linhas": [{
                "codigo_cliente": "CTI001",
                "nome_cliente": "Metalúrgica Horizonte",
                "consultor": "Ana Souza",
                "segmento": "Indústria",
                "nivel_cliente": "A",
                "faturamento_anual": 1850000,
                "servicos_contratados": "Internet Dedicada;Firewall",
                "data_contratacao": "2025-01-15",
            }],
        }

        resultado = processar_lote(self.engine, payload)

        self.assertEqual(resultado["status"], "PROCESSADO")
        lote = obter_dashboard(self.engine, resultado["loteId"])
        self.assertEqual(lote["status"], "PROCESSADO")
        self.assertEqual(lote["estatisticas"]["totalClientes"], 1)
        with self.engine.connect() as conexao:
            self.assertEqual(conexao.execute(select(upload_lote.c.status)).scalar_one(), "PROCESSADO")

    def test_api_routes_process_and_read_a_persisted_lot(self):
        pedido = Pedido.model_validate({
            "arquivoNome": "clientes.csv",
            "recebidas": 1,
            "linhas": [{
                "codigo_cliente": "CTI002",
                "nome_cliente": "Empresa Beta",
                "consultor": "Carlos Lima",
                "segmento": "Comercio",
                "nivel_cliente": "B",
                "faturamento_anual": "420000.00",
                "servicos_contratados": "Backup;Firewall",
                "data_contratacao": "2025-02-15",
            }],
        })

        with patch("api._engine", return_value=self.engine):
            resposta = etl(pedido, None)
            lotes_salvos = lotes()
            painel = dashboard(resposta["loteId"])

        self.assertTrue(resposta["ok"])
        self.assertEqual(lotes_salvos[0]["status"], "PROCESSADO")
        self.assertEqual(painel["estatisticas"]["totalClientes"], 1)

    def test_upload_updates_existing_client_without_breaking_schema_contract(self):
        first = {
            "arquivoNome": "primeiro.csv",
            "recebidas": 1,
            "invalidas": 0,
            "comAviso": 0,
            "linhas": [{
                "codigo_cliente": "CTI003",
                "nome_cliente": "Empresa Gama",
                "consultor": "Ana Souza",
                "segmento": "Tecnologia",
                "nivel_cliente": "A",
                "faturamento_anual": 500000,
                "servicos_contratados": "Cloud;Firewall",
                "data_contratacao": "2025-03-15",
            }],
        }
        first_result = processar_lote(self.engine, first)
        second = {
            **first,
            "arquivoNome": "segundo.csv",
            "linhas": [{
                **first["linhas"][0],
                "nome_cliente": "Empresa Gama Atualizada",
                "faturamento_anual": 750000,
            }],
        }
        second_result = processar_lote(self.engine, second)

        self.assertNotEqual(first_result["loteId"], second_result["loteId"])
        with self.engine.connect() as connection:
            customers = connection.execute(
                select(upload_lote.c.linhas_validas)
            ).scalars().all()
            self.assertEqual(customers, [1, 1])
        latest = obter_dashboard(self.engine)
        self.assertEqual(latest["id"], second_result["loteId"])


if __name__ == "__main__":
    unittest.main()
