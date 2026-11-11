# CTI Nexus

Projeto Integrador · SENAI · ADS

```
CTI_NEXUS/
├── backend/          API Spring Boot 3.5 + Java 21 (porta 8080)
├── frontend/         Vue 3 + Vite (porta 5173)
└── analytics-python/ ETL, estatísticas e API FastAPI (porta 8000)
```

O Java valida a planilha e encaminha as linhas válidas ao serviço Python. O Python persiste os clientes, contratos, estatísticas e o snapshot de cada painel no PostgreSQL. O Java expõe o lote e o painel por rotas protegidas por JWT.

## Requisitos

- Java 21
- Node.js e npm
- Python 3.10+
- Docker com Docker Compose, para o PostgreSQL local

## Preparar o banco local

Na raiz do projeto:

```powershell
docker compose up -d postgres
```

O banco local de desenvolvimento usa `ctiinsights`, usuário `postgres` e senha `cti-nexus-local`. Esses valores são apenas para desenvolvimento local; use credenciais próprias nos ambientes compartilhados.

## Configuração

1. Copie `backend/.env.example` para `backend/.env`.
2. No `backend/.env`, configure `DB_URL=jdbc:postgresql://localhost:5432/ctiinsights`, `DB_USER=postgres`, `DB_PASSWORD=cti-nexus-local`, um `JWT_SECRET` aleatório com pelo menos 32 caracteres e `ADMIN_PASSWORD`.
3. Copie `analytics-python/.env.example` para `analytics-python/.env`. Use os mesmos dados PostgreSQL. Se preencher `ETL_API_KEY`, configure o mesmo valor em `ETL_KEY` no `backend/.env`.
4. Instale as dependências do frontend:

```powershell
cd frontend
npm install
cd ..
```

## Executar localmente

Inicie cada serviço em um terminal separado, a partir da raiz do projeto.

**Terminal 1 — ETL e API Python**

```powershell
cd analytics-python
py -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn api:app --host 0.0.0.0 --port 8000
```

**Terminal 2 — Backend Java**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Terminal 3 — Frontend**

```powershell
cd frontend
npm run dev
```

Abra http://localhost:5173. O backend executa `schema.sql` ao iniciar, criando as tabelas necessárias no PostgreSQL.

## API analítica

- `POST /api/uploads/validadas` — recebe o upload validado (requer autenticação).
- `GET /api/dashboard` — retorna as estatísticas persistidas do lote processado mais recente (requer autenticação).
- `GET /api/dashboard?loteId=<id>` — retorna as estatísticas de um lote específico (requer autenticação).
- `GET /api/lotes` — lista o histórico de processamento (requer autenticação).
- `GET http://localhost:8000/saude` — verifica se o serviço Python está ativo.

As rotas do ETL são destinadas à comunicação entre os serviços; a chave opcional `ETL_API_KEY` deve ser a mesma em `backend/.env` (`ETL_KEY`) e `analytics-python/.env` (`ETL_API_KEY`).

## Testes

```powershell
cd backend
.\mvnw.cmd test
cd ..\analytics-python
py -m unittest discover -s tests
cd ..\frontend
npm run build
```

## Modo local sem PostgreSQL

O perfil Java `local` usa H2 para testar as funcionalidades Java sem Azure ou Docker:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Esse perfil não compartilha o banco com o ETL Python. Para testar o fluxo completo de upload e dashboard, inicie o PostgreSQL e execute os serviços Java e Python com a mesma configuração de banco descrita acima.

## Primeiro acesso

Entre com o e-mail definido em `ADMIN_EMAIL` (padrão `admin@cti.local`) e a senha definida em `ADMIN_PASSWORD`. Novos usuários se cadastram em `/cadastro` e só entram depois que o administrador aprovar em **Usuários**.
