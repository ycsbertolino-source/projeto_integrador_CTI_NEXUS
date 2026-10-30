# CTI Nexus

Projeto Integrador · SENAI · ADS

```
CTI_NEXUS/
├── backend/    API em Spring Boot 3.5 + Java 21 (porta 8080)
└── frontend/   Site em Vue 3 + Vite (porta 5173)
```

O front e o backend rodam separados, cada um no seu terminal. O front chama a API em `http://localhost:8080`.

## Antes da primeira vez

1. **Backend:** copie `backend/.env.example` para `backend/.env` e preencha `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mínimo 32 caracteres) e `ADMIN_PASSWORD`.
2. **Frontend:** instale as dependências:
   ```
   cd frontend
   npm install
   ```

## Para rodar (2 terminais)

**Terminal 1 · Backend**
```
cd backend
.\mvnw.cmd spring-boot:run
```
Teste rápido: http://localhost:8080/api/health → `{"status":"ok"}`

**Terminal 2 · Frontend**
```
cd frontend
npm run dev
```
Abra http://localhost:5173

## Primeiro acesso

Entre com o e-mail de `ADMIN_EMAIL` (padrão `admin@cti.local`) e a senha de `ADMIN_PASSWORD`.
Novos usuários se cadastram em `/cadastro` e só entram depois que o administrador aprovar em **Usuários**.
