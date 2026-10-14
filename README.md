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
