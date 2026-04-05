# Psikosys

Psikosys is a Spring Boot web application that combines AI chat, Jungian-style personality prompts, and user account management (form login + Google OAuth2).

## Features

- AI-supported chat flow with personality-based prompt selection
- Multi-language support (`tr` / `en`) with cookie + user preference sync
- User registration, login, profile management, and password update
- Google OAuth2 login integration
- Thymeleaf-based UI pages (`index`, `login`, `register`, `chat`, `user-profile`)
- REST endpoints for AI generation and personality listing

## Tech Stack

- Java 21
- Spring Boot 3.4.3
- Spring Web, Spring Security, Spring Data JPA, Thymeleaf
- Spring AI (OpenAI-compatible API via Groq)
- MySQL
- Maven

## Prerequisites

- JDK 21
- MySQL 8+
- Maven (or Maven Wrapper)

## Setup

### 1) Database setup

Run these SQL commands in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS psikosys
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'springstudent'@'localhost'
IDENTIFIED BY 'springstudent';

GRANT ALL PRIVILEGES ON psikosys.* TO 'springstudent'@'localhost';
FLUSH PRIVILEGES;
```

### 2) Environment variables (`.env`)

Create a `.env` file in project root (same level as `pom.xml`):

```dotenv
SPRING_AI_OPENAI_API_KEY=your_api_key_here
SPRING_AI_OPENAI_BASE_URL=https://api.groq.com/openai
SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=llama-3.3-70b-versatile

GOOGLE_AUTH_CLIENT_ID=your_google_client_id
GOOGLE_AUTH_CLIENT_SECRET=your_google_client_secret
GOOGLE_AUTH_CLIENT_URI=http://localhost:8080/login/oauth2/code/google
```

> `application.properties` already imports `.env` via `spring.config.import=optional:file:.env`.

### 3) Run the application

#### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run
```

#### macOS/Linux

```bash
./mvnw spring-boot:run
```

App URL:

- `http://localhost:8080`

## Run Tests

### Windows (PowerShell)

```powershell
.\mvnw.cmd test
```

### macOS/Linux

```bash
./mvnw test
```

## Important Routes

### UI routes

- `GET /` -> landing page
- `GET /login` -> login page
- `GET /register` -> register page
- `GET /chat` -> chat page
- `GET /profile` -> user profile

### API routes

- `GET /api/ai/generate?message=...`
- `GET /api/ai/generateStream?message=...`
- `GET /api/personalities`
- `GET /api/debug/token`

## Screenshots (Placeholders)

Put your images under `docs/images/` with the names below.

```text
docs/images/
  chat.png
  google_oauth.png
  index.png
  index-en.png
  jungian-analysis-en.png
  login.png
  profile.png
```

Markdown references (already ready):

![Index](docs/images/index.png)
![Index EN](docs/images/index-en.png)
![Login](docs/images/login.png)
![Google OAuth](docs/images/google_oauth.png)
![Chat](docs/images/chat.png)
![Jungian Analysis EN](docs/images/jungian-analysis-en.png)
![Profile](docs/images/profile.png)

## Project Structure (Short)

```text
src/main/java/com/atlas/Psikosys
  configuration/
  controller/
  dto/
  entity/
  repository/
  security/
  service/

src/main/resources
  static/
  templates/
  personalities.json
  personalities-tr.json
  personalities-en.json
  application.properties
```

## Security Note

- Do not commit real API keys or OAuth secrets.
- Keep `.env` local and rotate keys if accidentally exposed.

## License

Add your preferred license here (MIT, Apache-2.0, etc.).

