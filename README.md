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

## Modo local (sem Azure)

Para rodar sem o banco do Azure, use o perfil `local`: ele usa um banco H2 salvo em `backend/data/`.
```
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```
No VS Code: Executar e Depurar → **Backend (modo local, sem Azure)**.
Login: `ADMIN_EMAIL` / `ADMIN_PASSWORD` do `backend/.env` (sem ADMIN_PASSWORD, a senha é `Admin12345`).
Os dados continuam lá quando o backend é desligado; para zerar, apague a pasta `backend/data`.

## Validação no backend (aula DTO + Jakarta Validation)

`POST http://localhost:8080/api/clientes/validar` recebe a lista do Pinia, valida no `ClienteDTO`
(`@NotBlank`, `@NotNull`, `@Positive`, `@Pattern`), o `ClienteValidacaoService` confere código repetido e
padroniza, e devolve a lista preparada. Não precisa de token, não grava no banco e não chama o Python.

- Thunder Client: use os JSONs dos slides 26 (válido → 200) e 27 (inválido → 400 `{"mensagem","campo","erro"}`).
- Testes automáticos (não precisam do Azure): `cd backend` e `.\mvnw.cmd test`.

## Se o backend não ligar

`Failed to obtain JDBC Connection` / `Connect timed out` = o banco no Azure recusou a conexão.
No portal do Azure → servidor `nexuscti` → **Rede** → **Adicionar endereço IP do cliente atual** → Salvar.
Toda vez que mudar de rede (casa, SENAI, celular) o IP muda e precisa liberar de novo.
Confira também se o servidor não está **Parado** (Visão geral → Iniciar).

## Primeiro acesso

Entre com o e-mail de `ADMIN_EMAIL` (padrão `admin@cti.local`) e a senha de `ADMIN_PASSWORD`.
Novos usuários se cadastram em `/cadastro` e só entram depois que o administrador aprovar em **Usuários**.
