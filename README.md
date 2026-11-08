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

## Versão HTML + React + Java

Uma só aplicação Spring Boot entrega três coisas:

| Parte | Tecnologia | Endereço |
|---|---|---|
| **Catálogo público** | **HTML** gerado no servidor (Thymeleaf), com CSS e um JS mínimo só para o aviso 18+ | `/catalogo` |
| **Painel do dono** | **React** + TypeScript (pasta `painel/`), compilado para dentro de `static/painel` | `/painel/` |
| **API** | **Java** 17, Spring Boot, JPA, JWT | `/api/**` |

Por que assim: o catálogo público já chega pronto no HTML (carrega rápido, funciona sem JavaScript e é
fácil de indexar), e o React fica onde ele faz sentido: uma área logada, cheia de interação.

### Rodar em desenvolvimento (tudo fictício, sem configurar nada)

```bash
npm --prefix painel install
npm --prefix painel run build                      # gera src/main/resources/static/painel
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run     # H2 em memória + 30 revistas fictícias
```

Abra http://localhost:8080/catalogo e http://localhost:8080/painel/ (usuário `dono`, senha `senha-de-teste-123`;
valores só do perfil `dev`, nunca use fora do seu computador).

Para mexer no painel com recarga automática: `npm --prefix painel run dev` (porta 5173, com proxy de `/api` para a 8080).

### Com o Postgres do Supabase
Use o perfil `supabase` (veja a seção acima) e as variáveis `APP_ADMIN_USER`, `APP_ADMIN_PASSWORD_HASH` e `APP_JWT_SECRET`.

### Capas
No painel, o formulário aceita **JPEG, PNG, WebP ou AVIF de até 5 MB** (ou, como alternativa, o endereço https de uma imagem).
A API confere o **conteúdo** do arquivo (os primeiros bytes), e não o nome nem o tipo declarado, grava com nome UUID e serve em `/capas/<arquivo>`.
Trocar a capa ou apagar a revista remove o arquivo antigo. No perfil `dev`, as revistas de exemplo ganham capas SVG geradas pelo código.

### Testes
`mvn test` (41 testes Java: API, filtro, páginas HTML, segurança, envio de capas) e `npm --prefix painel test` (14 testes React: API, login, CRUD, envio de capa).

### Limitações conhecidas
- **As capas ficam no disco do servidor** (`APP_STORAGE_DIR`, padrão `./data/capas`). Em hospedagens com disco efêmero (Render gratuito, por exemplo) elas somem a cada deploy: para produção, use um volume persistente ou o Supabase Storage (que exige a chave de serviço no servidor).
- O painel precisa ser compilado antes de rodar (`npm --prefix painel run build`); a pasta compilada não vai para o Git.
- Não foi publicada nem testada contra o banco real do Supabase (falta a senha do banco, que fica só em variável de ambiente).
