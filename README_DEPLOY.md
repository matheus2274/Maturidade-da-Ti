# Guia rapido de deploy

Este repositorio publica o frontend no GitHub Pages via branch `gh-pages` e o backend Spring Boot no Fly.io.

Frontend
- O workflow `.github/workflows/deploy-frontend-gh-pages.yml` publica o conteudo da pasta `MaturidadeTI_Run` na branch `gh-pages`.
- O arquivo `MaturidadeTI_Run/index.html` redireciona para `MaturidadeTI_v2_Completo.html`, que continua sendo a tela principal.
- A URL publica esperada e `https://matheus2274.github.io/Maturidade-da-Ti/`.
- Se o Pages precisar ser revisado, configure em `Settings > Pages` a fonte `Deploy from a branch`, branch `gh-pages` e pasta `/ (root)`.

Backend
1. Crie uma conta em https://fly.io e gere um token com `fly auth token`.
2. No repositorio GitHub, adicione o secret `FLY_API_TOKEN` com o token gerado.
3. Confirme que o app Fly se chama `maturidade-ti-backend` ou ajuste `backend_completo/backend/fly.toml`.
4. Faca push para `main`; o workflow `.github/workflows/deploy-backend-flyio.yml` faz o build e o deploy automatico do backend.

Secrets e variaveis
- Obrigatorio no GitHub Actions: `FLY_API_TOKEN`
- Automatico do GitHub Pages: `GITHUB_TOKEN`
- Opcionais no Fly, se quiser outro banco ou outro ajuste de ambiente: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_DRIVER_CLASS_NAME`, `JWT_SECRET`, `JWT_EXPIRATION`

Observacoes
- No Fly, o `fly.toml` usa H2 em memoria para evitar travas de arquivo; os dados reiniciam quando a maquina reinicia.
- O `fly.toml` força IPv4 no runtime para o Tomcat expor `0.0.0.0:8080`, que e o formato que o Fly precisa para rotear o app.
- O `frontend/frontend-api.js` usa `http://localhost:8080/api/v1` no desenvolvimento local e a API publicada no Fly fora do ambiente local.
