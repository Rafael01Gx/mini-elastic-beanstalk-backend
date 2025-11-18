# 🚀 Mini Elastic Beanstalk Backend

**Gerenciador de containers Docker executados diretamente no host, com deploy isolado por namespaces, monitoramento completo, autenticação JWT e API documentada com OpenAPI.**

---

<!-- Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/Java-25-007396?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker%20Engine%20API-Active-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16+-0064a5?style=for-the-badge&logo=postgresql&logoColor=white" />
</p>

---

## 🎯 Visão Geral

O **Mini Elastic Beanstalk Backend** é uma plataforma em desenvolvimento, construída em **Java 25 + Spring Boot 3.5.7** que permite gerenciar containers Docker diretamente no host, evitando Docker-in-Docker e oferecendo:

- Deploys isolados por `serverId`
- Controle total dos containers (start / stop / logs / redeploy)
- Upload e extração de artefatos
- Monitoramento em tempo real via WebSocket
- Métricas completas com Micrometer + Prometheus
- Arquitetura escalável, organizada e modular

---

## ⚙️ Funcionalidades Principais

- **Deploy automático** baseado em `docker-compose` gerado dinamicamente
- **Isolamento por namespace** (labels + workspaces por `serverId`)
- **Logs em tempo real** via WebSocket
- **Autenticação e autorização** via JWT + Spring Security
- **Métricas e healthchecks** com Actuator + Prometheus
- **Documentação automática** com OpenAPI / Swagger
- Código modular com camadas bem definidas (config / service / controller / repository)

---

## 🧱 Tecnologias / Versões

**Core**
- Java 25
- Spring Boot 3.5.7
- Maven 3.9+
- docker-java 3.3.6 (Docker Engine API client)

**Banco**
- PostgreSQL 16+ (produção)
- H2 2.2+ (dev/test)
- Flyway (migrations)

**Monitoramento & Observability**
- Micrometer
- Prometheus client
- Logback (logging estruturado)
- Spring Boot Actuator

---

## 🗂️ Estrutura de Pastas Completa

```text
mini-elastic-beanstalk/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── elasticbeanstalk/
│   │   │           ├── MiniElasticBeanstalkApplication.java
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── DockerConfig.java
│   │   │           │   ├── WebSocketConfig.java
│   │   │           │   ├── OpenApiConfig.java
│   │   │           │   ├── CorsConfig.java
│   │   │           │   └── AsyncConfig.java
│   │   │           ├── domain/
│   │   │           │   ├── entity/
│   │   │           │   ├── enums/
│   │   │           │   └── dto/
│   │   │           ├── repository/
│   │   │           ├── service/
│   │   │           ├── controller/
│   │   │           ├── security/
│   │   │           ├── exception/
│   │   │           ├── validator/
│   │   │           └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       ├── templates/
│   │       └── logback-spring.xml
│   └── test/
│       └── java/
│
├── data/
├── docs/
├── scripts/
├── .gitignore
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## 🚀 Como Rodar (Local)

1. **Clonar**
```bash
git clone https://github.com/SEU_USUARIO/mini-elastic-beanstalk-backend.git
cd mini-elastic-beanstalk-backend
```

2. **Configurar** `src/main/resources/application.yml` (DB, JWT secret, Docker socket path etc.)

3. **Executar**
```bash
mvn -DskipTests spring-boot:run
```

4. **Swagger UI**
Abra: `http://localhost:8080/swagger-ui.html`

---

## 📊 Endpoints úteis / Observability

- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/prometheus` (Prometheus scrape endpoint)
- `GET /swagger-ui.html` (Docs)

---

## 🛡️ Segurança

- JWT (acesso + refresh)
- Senhas armazenadas com BCrypt
- Filtros específicos para autenticação e autorização
- Política CORS configurável

---

## 🛠️ Scripts úteis

- `scripts/init-docker.sh` — configura permissões e diretórios
- `scripts/cleanup.sh` — remove containers temporários / imagens
- `scripts/setup-monitoring.sh` — auxilia na instalação de Prometheus/Node Exporter

---

## ✅ Boas práticas / Notas

- A aplicação **não** executa containers dentro de containers — usa o Docker Engine do host (`/var/run/docker.sock`).
- Cada deployment deve usar labels com `serverId` para permitir isolamento lógico e operações em lote.
- Use usuários não-root e roles específicas para operações sensíveis.
- Garanta backup das migrations e do esquema do PostgreSQL em produção.

---

## ✨ Próximos passos (Roadmap)

- Blue/Green e Canary deploys
- Gerenciamento de logs centralizado (ELK/EFK)
- UI de gerenciamento (dashboard)
- Autenticação OAuth2 / SSO opcional
- Suporte a orquestração (k8s) como target opcional

---

