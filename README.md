# Vault Keeper API (Java)

API REST do Vault Keeper: Spring Boot 3, Java 17, JPA, Spring Security com JWT.

## Rodar localmente

A API **não sobe sem estas variáveis de ambiente** (os segredos nunca ficam no código):

| Variável | O que é |
|---|---|
| `APP_ADMIN_USER` | nome do usuário dono |
| `APP_ADMIN_PASSWORD_HASH` | hash **BCrypt** da senha (nunca a senha em texto) |
| `APP_JWT_SECRET` | segredo para assinar tokens, 32+ caracteres |
| `APP_CORS_ORIGINS` | sites autorizados (padrão: `http://localhost:5173`) |

Gerar o hash da senha no Mac:

```bash
htpasswd -bnBC 10 "" 'SUA_SENHA' | tr -d ':\n'
```

Gerar um segredo:

```bash
openssl rand -hex 32
```

```bash
mvn spring-boot:run
mvn test
```

## Rotas

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/auth/login` | livre |
| GET | `/api/public/magazines` | livre |
| GET/POST | `/api/magazines` | token |
| PUT/DELETE | `/api/magazines/{id}` | token |
| PATCH | `/api/magazines/{id}/public` | token |

## Ligando ao Postgres do Supabase

O perfil `supabase` (arquivo `application-supabase.yml`) troca o H2 pelo banco real:

```bash
export SPRING_PROFILES_ACTIVE=supabase
export SUPABASE_DB_URL='jdbc:postgresql://<host>:5432/postgres?sslmode=require'
export SUPABASE_DB_USER='postgres.<ref-do-projeto>'
export SUPABASE_DB_PASSWORD='...'   # painel do Supabase > Database. Nunca no código nem no chat.
export APP_OWNER_ID='<uuid do seu usuário em auth.users>'
```

- `ddl-auto: validate`: a API só **confere** se as colunas existem; nunca altera o banco.
- A coluna `condition` é texto (`'Excelente'`, `'Bom'`...): o `ConditionConverter` traduz para o enum.
- `user_id` é obrigatório no banco: a API grava `APP_OWNER_ID` nas revistas novas.
- **Atenção:** a API conecta como dono do banco e **ignora a RLS**. A proteção é o filtro por
  `user_id` em `MagazineRepository` (há testes garantindo que revista de outro usuário não aparece nem é alterada).
