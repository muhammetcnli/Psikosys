# Psikosys

[![Java](https://img.shields.io/badge/Java-21-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1)](https://www.mysql.com/)

Psikosys is an AI-assisted psychological chat platform built with Spring Boot. It combines personality-based prompt strategies, bilingual UX (`tr` / `en`), and secure authentication with both form login and Google OAuth2.

## Key Features

- Personality-driven AI chat with configurable prompt files
- Multi-language experience with cookie-based and profile-based language preferences
- User registration and login with Spring Security
- Google OAuth2 sign-in support
- Profile management (language + password update)
- REST API endpoints for AI and personality data

## Tech Stack

- `Java 21`
- `Spring Boot 3.4.3`
- `Spring Web`, `Spring Security`, `Spring Data JPA`, `Thymeleaf`
- `Spring AI` (OpenAI-compatible endpoint)
- `MySQL`
- `Maven`

## Pages and Their Purpose

- `GET /` (`index.html`): Landing page, introduction, and language selection entry point
- `GET /login` (`login.html`): Form login and Google OAuth2 login access
- `GET /register` (`register.html`): New account creation flow
- `GET /chat` (`chat.html`): Main chat workspace, personality selection, chat list
- `GET /chat/{id}` (`chat.html`): Existing conversation detail with message history
- `GET /profile` (`user-profile.html`): Account profile, language preference, password management

## Getting Started

### 1) Prerequisites

- JDK 21
- MySQL 8+
- Maven (or Maven Wrapper)

### 2) Database Setup

- Create a MySQL database named `psikosys`.
- Ensure your database credentials in `src/main/resources/application.properties` match your local MySQL user.
- Default values in this project are currently:
  - `spring.datasource.url=jdbc:mysql://localhost:3306/psikosys`
  - `spring.datasource.username=springstudent`
  - `spring.datasource.password=springstudent`

### 3) Environment Variables (`.env`)

Create a `.env` file in the project root (same level as `pom.xml`):

```dotenv
SPRING_AI_OPENAI_API_KEY=your_api_key_here
SPRING_AI_OPENAI_BASE_URL=https://api.groq.com/openai
SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=llama-3.3-70b-versatile

GOOGLE_AUTH_CLIENT_ID=your_google_client_id
GOOGLE_AUTH_CLIENT_SECRET=your_google_client_secret
GOOGLE_AUTH_CLIENT_URI=http://localhost:8080/login/oauth2/code/google
```

`application.properties` imports `.env` via `spring.config.import=optional:file:.env`.

### 4) Run the Application

Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Application URL: `http://localhost:8080`

## Running Tests

Windows (PowerShell):

```powershell
.\mvnw.cmd test
```

macOS/Linux:

```bash
./mvnw test
```

## API Overview

- `GET /api/ai/generate?message=...` - single AI response
- `GET /api/ai/generateStream?message=...` - streaming AI response
- `GET /api/personalities` - available personality definitions
- `GET /api/debug/token` - OAuth token debug information

## Screenshots

Place your screenshots under `src/main/resources/static/images/` with the following file names:

```text
src/main/resources/static/images/
  chat.png
  google_oauth.png
  index.png
  index-en.png
  jungian-analysis-en.png
  login.png
  profile.png
```

Preview blocks:

![Index](src/main/resources/static/images/index.png)
![Index EN](src/main/resources/static/images/index-en.png)
![Login](src/main/resources/static/images/login.png)
![Google OAuth](src/main/resources/static/images/google_oauth.png)
![Chat](src/main/resources/static/images/chat.png)
![Jungian Analysis EN](src/main/resources/static/images/jungian-analysis-en.png)
![Profile](src/main/resources/static/images/profile.png)

## Project Structure

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
  application.properties
  messages_en.properties
  messages_tr.properties
  personalities.json
  personalities-en.json
  personalities-tr.json
  static/
    css/
    images/
    js/
  templates/
```

## Security Notes

- Never commit real API keys or OAuth secrets.
- Keep `.env` local and rotate keys immediately if exposed.

## License

Add your preferred license (for example: MIT or Apache-2.0).

