# HowardAI

Howard AI is an AI-powered chatbot application that makes chatting with [Howard Roark](https://en.wikipedia.org/wiki/The_Fountainhead#:~:text=The%20novel%27s%20protagonist%2C-,Howard%20Roark,-%2C%20is%20an%20intransigent) a reality. Built using Java Spring Boot. 

The project consists of two main components:

1. **REST API** – Backend service integrated with Groq API (llama3-70b-8192 model)
2. **Web Interface** – A simple user interface where users can chat with the bot

## Features
- User authentication (Google OAuth2 integration)
- AI-powered chat with Howard Roark
- Chat history management
- Dynamic chat titles

## Technologies
- **Backend**: Java 21, Spring Boot, Spring Web, Spring AI, Spring Security, Hibernate, JPA
- **Frontend**: Thymeleaf
- **Database**: MySQL
- **AI**: Groq API (llama3-70b-8192 model)

## Setup
1. Clone the project:
   ```bash
   git clone https://github.com/muhammetcnli/howardAI.git

## Screenshots

### Chat Page
![Chat Page](https://github.com/muhammetcnli/howardAI/blob/main/howardAI/src/main/resources/static/images/chat.png?raw=true)

### Chating Example
![Chating](https://github.com/muhammetcnli/howardAI/blob/main/howardAI/src/main/resources/static/images/chatting.png?raw=true)

### Login Page
![Login Page](https://github.com/muhammetcnli/howardAI/blob/main/howardAI/src/main/resources/static/images/login_default.png?raw=true)

### Google OAuth2 Page
![OAuth2 Page](https://github.com/muhammetcnli/howardAI/blob/main/howardAI/src/main/resources/static/images/google_oauth2.png?raw=true)

### Index Page
![Index Page](https://raw.githubusercontent.com/muhammetcnli/howardAI/main/howardAI/src/main/resources/static/images/index.png)

### API Example
![API](https://github.com/muhammetcnli/howardAI/blob/main/howardAI/src/main/resources/static/images/api.png?raw=true)

### To-Do 
- Dockerize 
- Deployment (Railway / Render / Vercel / AWS)
