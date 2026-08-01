# Log Ingestion Service

The **Log Ingestion Service** is the core processing engine of the **LogPulse** platform. It consumes log events from Apache Kafka, validates and processes incoming data, persists logs into PostgreSQL, indexes searchable data in Elasticsearch, aggregates traffic metrics, and exposes REST APIs for dashboards, analytics, and monitoring.

It is responsible for transforming raw log events into meaningful insights for centralized application monitoring.

---

# Architecture

```
Apache Kafka
      │
Consume Events
      ▼
Log Ingestion Service
      │
      ├───────────────┐
      │               │
      ▼               ▼
PostgreSQL     Elasticsearch
      │               │
      └──────┬────────┘
             ▼
     Analytics & Dashboard APIs
             │
             ▼
      LogPulse Web Dashboard
```

---

# Features

- Consumes log events from Apache Kafka
- Processes HTTP, Exception, and Application logs
- Persists transactional data into PostgreSQL
- Indexes searchable data into Elasticsearch
- Server-side filtering, pagination, and sorting
- Full-text log search using Elasticsearch
- Multi-tenant architecture with schema isolation
- Dashboard analytics and traffic insights
- Geo-location based traffic analysis
- API performance monitoring
- Exception analytics
- Client IP tracking
- API Key management
- Secure REST APIs using JWT & Keycloak

---

# Technology Stack

- Java 21
- Spring Boot
- Spring Kafka
- Spring Data JPA
- Spring Data Elasticsearch
- PostgreSQL
- Elasticsearch
- Flyway
- Keycloak
- Spring Security
- Docker
- Maven

---

# Processing Flow

```
Kafka Topic
      │
      ▼
Kafka Consumer
      │
      ▼
Log Validation
      │
      ▼
Log Processing
      │
      ├──────────────┐
      │              │
      ▼              ▼
PostgreSQL    Elasticsearch
      │              │
      └──────┬───────┘
             ▼
Dashboard APIs
```

---

# Responsibilities

- Consume Kafka events
- Process incoming logs
- Store transactional data
- Index searchable logs
- Generate dashboard analytics
- Aggregate traffic metrics
- Manage API Keys
- Handle multi-tenant data isolation
- Expose analytics APIs

---

# Dashboard Analytics

The service provides analytics for:

- Total API Requests
- Total APIs
- Total Exceptions
- Total Users
- HTTP Status Distribution
- Log Level Distribution
- Request Trend
- Traffic Insights
- Top 10 Client IPs
- Peak Traffic Hour
- Top 10 Slowest APIs
- Top 10 Most Called APIs
- Top Services by Request Count
- Average Response Time by API
- Average Response Time by Service
- Geo-location Traffic Analysis

---

# Search Capabilities

Powered by Elasticsearch:

- Full-text log search
- Service name search
- API path search
- Exception search
- Trace ID search
- Correlation ID search
- Time-based filtering
- Log level filtering

---

# REST APIs

| Method | Description |
|----------|------------|
| GET | Dashboard APIs |
| GET | Traffic Insights |
| GET | Log Search |
| GET | Analytics |
| GET | API Key Management |
| POST | API Key Generation |
| PUT | API Key Regeneration |
| GET | Health Check |

> Update the endpoint paths according to your implementation.

---

# Kafka

### Consumer Topic

```
log-trace-events
```

### Event Flow

```
Kafka
    │
Consume
    ▼
Log Ingestion Service
    │
    ├── PostgreSQL
    └── Elasticsearch
```

---

# Database

## PostgreSQL

Stores:

- Log Metadata
- HTTP Trace
- Exception Trace
- User Information
- API Keys
- Traffic Statistics
- Dashboard Aggregations

---

## Elasticsearch

Indexes:

- Searchable Logs
- HTTP Logs
- Exception Logs
- Application Logs
- Dashboard Search Data

---

# Security

- JWT Authentication
- Keycloak Integration
- API Key Management
- Multi-Tenant Schema Isolation
- Role-Based Authorization

---

# Project Structure

```
src
├── controller
├── service
├── kafka
├── elasticsearch
├── repository
├── entity
├── dto
├── security
├── configuration
├── analytics
├── dashboard
├── traffic
├── exception
└── util
```

---

# Configuration

Example:

```properties
server.port=8090
server.servlet.context-path=/log-ingestion-service

spring.kafka.bootstrap-servers=localhost:9092

consumer.topic=log-trace-events

spring.datasource.url=...

spring.elasticsearch.uris=http://localhost:9200
```

---

# Running

```bash
mvn clean install

mvn spring-boot:run
```

or

```bash
java -jar log-ingestion-service.jar
```

---

# Dependencies

- Apache Kafka
- PostgreSQL
- Elasticsearch
- Flyway
- Keycloak
- Log Producer Service

---

# Future Enhancements

- Real-time Dashboard Updates (WebSocket)
- Distributed Tracing
- Alert Engine
- Email & Slack Notifications
- Log Retention Policies
- Machine Learning Anomaly Detection
- Prometheus Metrics
- Grafana Dashboards
- OpenTelemetry Integration
- Dead Letter Queue (DLQ)

---

# License

This project is part of the **LogPulse** centralized application monitoring and log analytics platform.
