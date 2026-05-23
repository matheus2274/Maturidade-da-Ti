# Guia rápido de deploy

Este repositório contém o frontend estático em `MaturidadeTI_Run` e o backend Java (Spring Boot) em `backend/`.

Frontend (GitHub Pages)
- O workflow [`.github/workflows/deploy-frontend-gh-pages.yml`] publica automaticamente o conteúdo da pasta `MaturidadeTI_Run` na branch `gh-pages` usando o `GITHUB_TOKEN`.
- URL esperada (após o primeiro deploy): `https://<SEU_USUARIO>.github.io/<NOME_DO_REPO>/`.

Backend (Fly.io) — passos resumidos
1. Crie uma conta em https://fly.io e gere um token (Dashboard → Personal Access Tokens).
2. No seu repositório GitHub, adicione o secret `FLY_API_TOKEN` com o token gerado.
3. No seu ambiente local (uma vez), execute dentro de `backend/`:
```bash
fly auth login
cd backend
fly launch --name maturidade-ti-backend
```
Isso criará um `fly.toml` com as configurações da app.
4. Commit o `fly.toml` no repositório e faça push; o workflow `.github/workflows/deploy-backend-flyio.yml` fará o deploy automático quando `FLY_API_TOKEN` existir.

Observações
- O deploy do backend requer que a app Fly esteja configurada e que as variáveis de ambiente sensíveis (ex.: `SPRING_DATASOURCE_URL`, `JWT_SECRET`) sejam definidas como secrets no Fly ou no workflow.
- Alternativas gratuitas: Railway, Render — ambos exigem conta e integração semelhante.
