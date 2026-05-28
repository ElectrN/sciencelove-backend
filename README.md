# ScienceLove Backend

Backend API для платформы подбора научных менторов и консультантов.

## 📋 Описание

**ScienceLove** — это платформа, которая соединяет студентов с квалифицированными научными экспертами для помощи с курсовыми, ВКР и исследованиями.

### Основные возможности

- 🔍 Умный подбор менторов по научным интересам (совпадение 90%+)
- 💬 Чат между студентом и ментором
- 📅 Планирование встреч и консультаций
- 📊 Статистика и аналитика для менторов
- 🔐 Безопасная аутентификация и авторизация
- 📁 Загрузка и обмен файлами

---

## 🛠 Стек технологий

| Компонент | Технология |
|-----------|------------|
| **Язык** | Java 17 |
| **Фреймворк** | Spring Boot 3.2.5 |
| **База данных** | PostgreSQL 17 |
| **ORM** | Spring Data JPA + Hibernate |
| **Безопасность** | Spring Security + JWT |
| **API Документация** | SpringDoc OpenAPI (Swagger) |
| **Чат** | WebSocket (STOMP) |
| **Сборка** | Maven |
| **Хранение файлов** | Локальное / S3 (настраивается) |

---

## 🚀 Быстрый старт

### Требования

- Java 17 или выше
- PostgreSQL 17
- Maven 3.8+

### 1. Клонирование репозитория

```bash
git clone https://github.com/твой-username/sciencelove-backend.git
cd sciencelove-backend
```
### 2. Настройка базы данных
```sql
# Создай базу данных
psql -U postgres
CREATE DATABASE sciencelove_dev;
\q
```
### 3. Конфигурация приложения
Создай файл src/main/resources/application.yml:
```yaml
spring:
  application:
    name: sciencelove-backend
  
  datasource:
    url: jdbc:postgresql://localhost:5432/sciencelove_dev
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  flyway:
    enabled: false
  
  servlet:
    multipart:
      max-file-size: 25MB
      max-request-size: 25MB

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production-min-32-chars}
  expiration: 86400000

app:
  storage:
    path: ./uploads

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### 4. Запуск приложения
```bash
# Через Maven
mvn spring-boot:run

# Или через IDE (запусти ScienceLoveApplication.java)```
```
## 📁 Структура проекта
```text
sciencelove-backend/
├── src/
│   ├── main/
│   │   ├── java/com/sciencelove/
│   │   │   ├── ScienceLoveApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   ├── security/
│   │   │   ├── exception/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
├── pom.xml
├── .gitignore
└── README.md
```
## 👥 Команда проекта

| Имя           | Роль        |
|---------------|-------------|
| **Константин Назаров**      | Разработчик |
| **Екатерина Зверева** | Дизайнер    |
