# MediSync Core Service
[![MediSync Core CI](https://github.com/Bhaumik182001/medisync-core/actions/workflows/ci-pipeline.yml/badge.svg)](https://github.com/Bhaumik182001/medisync-core/actions)

The **Core Business Service** is the heavy-lifting engine of the MediSync healthcare ecosystem. It manages the complex domains of Provider scheduling, TimeSlot generation, and secure Appointment booking. Built with high concurrency in mind, it utilizes database-level locking and distributed caching to ensure data integrity at scale.

## 🛠️ Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot 4.0.5
* **Relational Database:** PostgreSQL 15 (Spring Data JPA)
* **Distributed Cache:** Redis (Spring Data Redis)
* **Security:** Stateless JWT Verification (JJWT 0.11.5)
* **Build Tool:** Maven
* **CI/CD:** GitHub Actions

## 🏗️ Architectural Patterns & Triumphs

* **Concurrency Control (Pessimistic Locking):** Implements `@Lock(LockModeType.PESSIMISTIC_WRITE)` at the repository level during the appointment booking flow. This prevents race conditions and guarantees that two patients cannot simultaneously book the exact same TimeSlot, even under heavy load.
* **Optimized Reads via Redis Cache-Aside:** Provider schedules are aggressively cached using Spring's `@Cacheable("availableSlots")`. State-mutating operations (booking, generating schedules, canceling) utilize `@CacheEvict` to automatically invalidate stale data, resulting in sub-millisecond read times for patients browsing available times.
* **Native Chaos Engineering:** Features a built-in `NativeChaosMonkey` filter that can be toggled on-the-fly to inject randomized network latency (1000ms - 5000ms) or simulate total 500-level service failures. This validates the resilience and timeout configurations of the upstream Orchestrator service.
* **Saga Compensation Ready:** Exposes specific endpoints designed for the Orchestrator's rollback mechanisms, safely releasing TimeSlots if downstream payment or notification services fail.

## 📡 Key API Endpoints

All endpoints are prefixed with `/api/v1` and require a valid Bearer JWT.

### Providers & Scheduling
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/providers/profile` | Creates or updates a Doctor/Provider profile linked to their Identity email. |
| `POST` | `/schedules/generate` | Dynamically generates `TimeSlot` intervals for a specific `ScheduleDate`. |
| `GET` | `/schedules/provider/{id}/available` | Fetches open TimeSlots (Serviced primarily from **Redis Cache**). |

### Appointments (Concurrency Protected)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/appointments/book` | Locks the TimeSlot and creates an Appointment record. |
| `DELETE` | `/appointments/cancel/{slotId}` | Compensating transaction: Frees the TimeSlot and deletes the Appointment. |

### Chaos Engineering (God Mode)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/chaos/toggle` | Wakes/Sleeps the Latency Injector (1s - 5s request delays). |
| `POST` | `/chaos/toggle-killswitch` | Simulates a catastrophic service crash (Returns 500s). |

## 🚀 Getting Started

### Prerequisites
* **Java 21** installed locally.
* **Docker & Docker Compose** (to run the PostgreSQL and Redis containers).

### 1. Start the Infrastructure
The core service requires both Postgres and Redis to be running:
```bash
docker-compose up -d postgres redis
```

### 2. Run the Application
You can run the application using the Maven wrapper:
```bash
./mvnw spring-boot:run
```
*The service will start on `http://localhost:8082`.*

### Environment Variables
The application relies on the following configuration block mapped in `application.yml`:

```yaml
server:
  port: 8082
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/medisync_core
    username: postgres
    password: rootpassword
  data:
    redis:
      host: 127.0.0.1
      port: 6379
  cache:
    type: redis
```