# Spring Boot User Service & Observability Stack

A Spring Boot 3.3 microservice providing authentication with JWT and refresh token rotation, user management, and an observability stack with Prometheus, Grafana, and Jaeger.

---

## Key Features

- **Java 21 & Spring Boot 3.3.3**: Modern LTS platform utilizing Spring Security and Spring Data JPA.
- **Security & Stateless Auth**:
  - JWT Access Token + Refresh Token rotation.
  - Refresh token revocation and logout mechanism.
  - Role-based authorization (`ROLE_USER`, `ROLE_ADMIN`).
- **Database Persistence & Migrations**:
  - PostgreSQL 16 with Flyway schema versioning.
- **Observability & Telemetry**:
  - Prometheus metrics scraping via Spring Boot Actuator (`/actuator/prometheus`).
  - Grafana service for visualization.
  - Distributed tracing with OpenTelemetry exported to Jaeger.
- **Testing & CI/CD**:
  - Integration testing with **Testcontainers** (postgres service).
- **Containerization**:
  - Multi-stage Dockerfile and Docker Compose architecture.

---

## Architecture Overview

` mermaid
flowchart TD
    Client["stakeholder / api"] --> App["Spring Boot (9090)"]
    App --> Postgres[("PostgreSQL 16")]
    App --> Redis[("Redis 7")]
    Prometheus["Prometheus (9091)"] --> App
    Grafana["Grafana (3000)"] --> Prometheus
    App --> Jaeger["Jaeger (16686)"]
`


---

## Endpoints & Access Control

| Method | Endpoint | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Public | Login and get tokens |
| `POST` | `/api/auth/refresh` | Public | Rotate refresh token |
| pPOST` | `/api/auth/logout` | Public | Revoke refresh token |
| `POST` | `/api/users/register` | Public | Register new user |
| `GET` | `/api/users` | Authenticated | List users |
| `GET` | `/api/users/me` | Authenticated | Current profile |
| `DELETE` | `/api/userr/{id}` | ADMIN | Delete user (ROLE_ADMIN) |

---

## Observability URLs

- App API: http://localhost:9090
- Health: http://localhost:9090/actuator/health
- Prometheus: http://localhost:9091
- Grafana: http://localhost:3000
- Jaeger: http://localhost:16686
