# Sistema de Gestão Financeira Pessoal Gamificado (Finanzas)

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://oracle.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.5-blue.svg)](https://typescriptlang.org)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-38bdf8.svg)](https://tailwindcss.com)

Aplicação estilo **SaaS Financeiro Premium** com motor de gamificação ética integrado (XP, Níveis, Conquistas, Missões e Saúde Financeira), projetada e desenvolvida com **Clean Architecture** e isolamento absoluto de dados por usuário.

---

## 🚀 Tecnologias Utilizadas

### Backend
- **Linguagem/Framework**: Java 21 + Spring Boot 3.3.4
- **Segurança**: Spring Security 6 + JWT (Access Token + Refresh Token), senhas criptografadas com BCrypt
- **Persistência**: Spring Data JPA + Hibernate (Banco Relacional PostgreSQL / Supabase)
- **Migrações**: Flyway versionando o schema e populando seed inicial de categorias e conquistas
- **Documentação de API**: Springdoc OpenAPI (Swagger UI disponível em `/api/v1/swagger-ui.html`)
- **Gerenciador de Build**: Maven (`pom.xml`)
- **Testes**: JUnit 5 + Mockito + Testcontainers para PostgreSQL

### Frontend
- **SPA Framework**: React 18 + TypeScript + Vite
- **Estilização**: Tailwind CSS com paleta SaaS Dark Mode (grafite, emerald green, rose red, gold)
- **Animações & Microinterações**: Framer Motion
- **Gráficos**: Recharts
- **Gerenciamento de Requisições & Cache**: Axios + TanStack Query (React Query)
- **Ícones**: Lucide React

---

## 📐 Estrutura do Projeto

```
app-financeiro/
├── backend/                  # API REST em Java 21 / Spring Boot 3.3
│   ├── src/main/java/com/appfinanceiro/
│   │   ├── config/           # Security, Swagger, CORS
│   │   ├── controller/       # Controllers REST versionados (/api/v1/...)
│   │   ├── domain/           # Entidades JPA (User, Account, Transaction, Goal, etc.)
│   │   ├── dto/              # Record DTOs de Request/Response imutáveis
│   │   ├── exception/        # Tratamento centralizado de exceções (@ControllerAdvice)
│   │   ├── repository/       # Interfaces Spring Data JPA por userId
│   │   ├── security/         # Filtro JWT, TokenProvider, UserPrincipal
│   │   └── service/          # Serviços financeiro e GamificationService
│   └── src/main/resources/
│       ├── db/migration/     # Migrações Flyway V1__initial_schema.sql
│       └── application.yml   # Configurações com suporte a PostgreSQL local / Supabase
├── frontend/                 # SPA em React 18 + TypeScript + Vite
│   ├── src/
│   │   ├── components/       # Layout, Navbar, ProtectedRoute
│   │   ├── context/          # AuthContext com gerenciamento de sessão JWT
│   │   ├── pages/            # Dashboard, Contas, Transações, Cartões, Metas, Gamificação, Relatórios
│   │   ├── services/         # Cliente HTTP Axios com interceptors de JWT
│   │   └── types/            # Tipos TypeScript alinhados ao backend
│   └── vite.config.ts
├── docker-compose.yml        # PostgreSQL 16 local para desenvolvimento
└── README.md
```

---

## 🎮 Motor de Gamificação Ética

O sistema recompensa **apenas** disciplina e hábitos financeiros saudáveis:
- **+10 XP**: Ao registrar qualquer lançamento financeiro no dia.
- **+20 XP**: Ao pagar uma fatura ou conta no prazo.
- **+200 XP**: Ao atingir 100% de uma meta financeira estipulada.
- **+300 XP**: Ao encerrar o mês com todas as categorias dentro do orçamento planejado.

Fórmula progressiva de Níveis:
`Nível N = N * 200 XP`

---

## ⚡ Como Rodar o Projeto

### Pré-requisitos
- **Java 21 LTS**
- **Node.js v18+** & npm
- **Docker & Docker Compose** (para banco de dados local) ou conta no **Supabase**

---

### 1. Subir o Banco de Dados Local (PostgreSQL)

```bash
docker-compose up -d
```
*O container subirá um banco PostgreSQL na porta 5432 com as credenciais padrão.*

> **Nota sobre o Supabase**: Se desejar utilizar o Supabase, defina as variáveis de ambiente antes de iniciar a API:
> ```bash
> export SPRING_DATASOURCE_URL=jdbc:postgresql://<SEU-SUPABASE-HOST>:5432/postgres
> export SPRING_DATASOURCE_USERNAME=postgres
> export SPRING_DATASOURCE_PASSWORD=<SUA-SENHA-SUPABASE>
> ```

---

### 2. Rodar o Backend (Spring Boot)

Navegue até a pasta `backend/`:
```bash
cd backend
mvn spring-boot:run
```
*A API REST estará rodando em `http://localhost:8080/api/v1`.*  
*Swagger UI disponível em: `http://localhost:8080/api/v1/swagger-ui.html`*

---

### 3. Rodar os Testes Automatizados

```bash
mvn test
```

---

### 4. Rodar o Frontend (React + Vite)

Em um novo terminal, navegue até a pasta `frontend/`:
```bash
cd frontend
npm install
npm run dev
```
*Acesse o sistema no navegador em `http://localhost:3000`.*

---

## 🔑 Credenciais e Testes Rápidos
Ao rodar a aplicação, você pode se cadastrar pela tela `/register` ou fazer login na tela `/login`. Todas as tabelas, categorias padrão e conquistas são auto-inicializadas pelo Flyway no primeiro boot.
