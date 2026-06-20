# Guia rapido de deploy

Este repositório publica o frontend em GitHub Pages e o backend Spring Boot no Fly.io.

Frontend (GitHub Pages)
- O workflow `.github/workflows/deploy-frontend-gh-pages.yml` publica diretamente o conteúdo da pasta `MaturidadeTI_Run`.
- O arquivo `MaturidadeTI_Run/index.html` redireciona para `MaturidadeTI_v2_Completo.html`, que continua sendo a tela principal.
- URL esperada após o primeiro deploy: `https://<SEU_USUARIO>.github.io/<NOME_DO_REPO>/`.

Backend (Fly.io)
1. Crie uma conta em https://fly.io e gere um token com `fly auth token`.
2. No repositório GitHub, adicione o secret `FLY_API_TOKEN` com o token gerado.
3. Confirme que o app Fly se chama `maturidade-ti-backend` ou ajuste `backend_completo/backend/fly.toml`.
4. Faça push para `main`; o workflow `.github/workflows/deploy-backend-flyio.yml` faz o build e o deploy automatico do backend.

Secrets e variaveis
- Obrigatorio no GitHub Actions: `FLY_API_TOKEN`
- Automatico do GitHub Pages: `GITHUB_TOKEN`
- Opcionais no Fly, se voce quiser outro banco ou outro ajuste de ambiente: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_DRIVER_CLASS_NAME`, `JWT_SECRET`, `JWT_EXPIRATION`

Observacoes
- O backend sobe com o fallback H2 definido em `application.properties`, mas isso nao oferece persistencia duravel no Fly sem volume ou banco externo.
- O `frontend/frontend-api.js` usa `http://localhost:8080/api/v1` no desenvolvimento local e a API publicada no Fly fora do ambiente local.
