<p align="center">
  <img src="https://d9hhrg4mnvzow.cloudfront.net/lp.3035tech.com/96c1669d-logo-teach-horiz-branco_1000000000000000000028.png" alt="3035tech Logo" width="200"/>
</p>


# Teachgram

Teachgram é uma aplicação de rede social minimalista desenvolvida como desafio final de módulo, com foco na construção de uma stack fullstack moderna. O projeto integra as tecnologias Java com Spring Boot no backend e Next.js no frontend, além de utilizar Docker e PostgreSQL para garantir escalabilidade e portabilidade.

---

## Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Docker
- JUnit & Mockito (testes unitários)

### Frontend
- Next.js
- TypeScript
- TailwindCSS
- Cypress *(em implementação para testes E2E)*

---

## Funcionalidades

- Autenticação e autorização de usuários
- Criação, edição e exclusão de postagens
- Feed com postagens dos usuários
- Perfil com informações e posts do usuário
- Integração entre frontend e backend via API REST

---

## Estrutura do Projeto
```
teachgram/
├── backend/ # API Spring Boot
│ ├── src/
│ └── Dockerfile
├── frontend/ # Aplicação Next.js
├── docker-compose.yml
│ ├── src/
│ └── public/
└── README.md
````
---

## Testes

- **Backend:** testes unitários com JUnit e Mockito
- **Frontend:** testes E2E com Cypress *(em breve)*

---

## Como Executar

> Requisitos: Docker e Docker Compose instalados.

```bash
# Clonar o repositório
git clone https://github.com/seuusuario/teachgram.git
cd teachgram

# Subir containers
docker-compose up --build
