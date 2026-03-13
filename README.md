# SkillMentor

A full-stack mentoring platform connecting students with mentors. Users can browse mentors by subject, book sessions, manage profiles, and handle payments. Includes role-based access (Student, Mentor, Admin), Clerk authentication, and a REST API backed by Spring Boot and PostgreSQL.

---

## Features

- **Authentication (Clerk)** – Sign up / sign in with Clerk; JWT validated by backend
- **Role-based access** – Roles: Student, Mentor, Admin with protected routes
- **Mentors** – Public listing and profile pages; pagination and filtering; admin create/update/delete
- **Subjects** – List subjects; filter mentors by subject; admin CRUD for subjects
- **User profile** – Get/update current user (`/me`), profile setup after first login
- **Sessions / bookings** – Create sessions (admin), enroll in sessions (student), list my sessions
- **Payments** – Upload payment proof for a session; admin confirm payment; meeting link (admin)
- **Reviews** – Students can create/update/delete reviews for mentors; list reviews by mentor
- **Students** – CRUD for students (admin/student); get current student profile
- **Admin dashboard** – Overview, manage subjects, create mentors, manage bookings
- **CORS** – Configurable allowed origins for frontend (Vercel + localhost)
- **API docs** – Swagger/OpenAPI at `/swagger-ui.html`

---

## Tech Stack

| Layer      | Technologies |
|-----------|--------------|
| Frontend  | React 19, TypeScript, Vite 7, React Router 7, Tailwind CSS 4, Radix UI, React Hook Form + Zod, Clerk (React), Lucide React, Sonner |
| Backend   | Java 17, Spring Boot 4, Spring Web MVC, Spring Data JPA, Spring Security |
| Database  | PostgreSQL |
| Auth      | Clerk (JWT); backend validates via JWKS |
| API docs  | Springdoc OpenAPI (Swagger UI) |
| Build     | npm (frontend), Maven (backend) |

---

## Getting Started (Local Development)

### Prerequisites

- Node.js 18+ and npm
- Java 17
- Maven
- PostgreSQL (local or remote URL)
- Clerk account (for auth)

### Backend

1. Clone the repo and go to the backend:
   ```bash
   cd "SkillMentor_Backend"
   ```
2. Set environment variables (or use defaults in `application.properties` / `application-dev.properties`):
   - `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` – PostgreSQL connection
   - `CLERK_JWKS_URL`, `CLERK_SECRET_KEY` – Clerk JWKS and secret (see Environment Variables)
   - Optional: `CORS_ALLOWED_ORIGINS` for local frontend URLs
3. Run with Maven (dev profile uses port 8082 and dev CORS):
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   Or set `spring.profiles.active=dev` in `application.properties` and run from your IDE.
4. API: `http://localhost:8082`  
   Swagger UI: `http://localhost:8082/swagger-ui.html`

### Frontend

1. Go to the frontend:
   ```bash
   cd "skillmentor-frontend"
   ```
2. Install dependencies and run:
   ```bash
   npm install
   npm run dev
   ```
3. Set env vars (e.g. in `.env` or `.env.local`):
   - `VITE_CLERK_PUBLISHABLE_KEY` – required
   - `VITE_API_URL` – optional; defaults to `http://localhost:8080` (change to `http://localhost:8082` if backend runs on 8082)
4. Open the app at the URL Vite prints (e.g. `http://localhost:5173`).

---

## Environment Variables

### Frontend (Vite)

| Variable | Required | Description |
|----------|----------|-------------|
| `VITE_CLERK_PUBLISHABLE_KEY` | Yes | Clerk publishable key for the frontend |
| `VITE_API_URL` | No | Backend base URL (default: `http://localhost:8080`) |

### Backend (Spring)

| Variable | Required | Description |
|----------|----------|-------------|
| `DATABASE_URL` | Yes (prod) | PostgreSQL JDBC URL, e.g. `jdbc:postgresql://host:5432/dbname` |
| `DB_USERNAME` | Yes (prod) | Database username |
| `DB_PASSWORD` | Yes (prod) | Database password |
| `CLERK_JWKS_URL` | Yes (prod) | Clerk JWKS URL (e.g. `https://<your-clerk>.clerk.accounts.dev/.well-known/jwks.json`) |
| `CLERK_SECRET_KEY` | Yes (prod) | Clerk secret key for backend JWT verification |
| `CORS_ALLOWED_ORIGINS` | No | Comma-separated origins (e.g. frontend URL + localhost); default in prod includes Vercel URL |
| `PORT` | No | Server port (Render sets this; default 8082) |
| `SPRING_PROFILES_ACTIVE` | No | `dev` or `prod` (default in repo: `prod`) |
| `PAYMENT_UPLOAD_DIR` | No | Directory for payment proof uploads (default: `uploads/payment-proofs`) |

---

## API Documentation

Key endpoints (base path: `/api`). Full interactive docs: **Swagger UI** (see Deployed Links).

### Users (`/api/v1/users`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/me` | Authenticated | Get current user profile |
| POST | `/setup` | Authenticated | Profile setup (e.g. after first login) |

### Mentors (`/api/v1/mentors`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Public | List mentors (paginated; query: `page`, `size`) |
| GET | `/{id}` | Public | Get mentor by ID |
| GET | `/profile/{mentorId}` | Public | Get mentor by Clerk `mentorId` |
| POST | `/` | Admin | Create mentor |
| PUT | `/{id}` | Admin, Mentor | Update mentor |
| DELETE | `/{id}` | Admin | Delete mentor |

### Subjects (`/api/v1/subjects`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Public | List subjects (paginated) |
| GET | `/{id}` | Public | Get subject by ID |
| GET | `/mentor/{mentorId}` | Public | Get subjects by mentor |
| POST | `/` | Admin | Create subject |
| PUT | `/{id}` | Admin | Update subject |
| DELETE | `/{id}` | Admin | Delete subject |

### Sessions (`/api/v1/sessions`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Admin | List all sessions |
| GET | `/{id}` | Admin, Mentor | Get session by ID |
| POST | `/` | Admin | Create session |
| PUT | `/{id}` | Admin | Update session |
| DELETE | `/{id}` | Admin | Delete session |
| POST | `/enroll` | Student | Enroll in a session |
| GET | `/my-sessions` | Student | List my enrolled sessions |
| POST | `/{id}/payment-proof` | Student | Upload payment proof (multipart) |
| GET | `/{id}/payment-proof` | Authenticated | Get payment proof |
| PATCH | `/{id}/confirm-payment` | Admin | Confirm payment |
| PATCH | `/{id}/complete` | Admin | Mark session complete |
| PATCH | `/{id}/meeting-link` | Admin | Set meeting link |

### Reviews (`/api/reviews`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/mentor/{mentorId}` | Public | List reviews for a mentor |
| POST | `/` | Student | Create review |
| PUT | `/{id}` | Student | Update review |
| DELETE | `/{id}` | Student | Delete review |

### Students (`/api/v1/students`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Admin, Student | List students |
| GET | `/{id}` | Admin, Student | Get student by ID |
| GET | `/me` | Student | Get current student profile |
| POST | `/` | Admin, Student | Create student |
| PUT | `/{id}` | Admin, Student | Update student |
| DELETE | `/{id}` | Admin | Delete student |

---

## Deployed Links

- **Frontend:** https://skillmentor-frontend-one.vercel.app  
- **Backend API:** https://skillmentor-backend-d2gy.onrender.com  
- **Backend Swagger UI:** https://skillmentor-backend-d2gy.onrender.com/swagger-ui.html  

---

## Project Structure

```
skillmentor project/
├── README.md
├── skillmentor-frontend/          # React + Vite frontend
│   ├── src/
│   │   ├── components/            # Reusable UI (Navbar, Footer, MentorCard, BookingModal, etc.)
│   │   ├── components/ui/        # shadcn-style components (button, card, input, etc.)
│   │   ├── contexts/              # UserRoleContext, etc.
│   │   ├── lib/                   # api.ts, utils
│   │   ├── pages/                 # HomePage, MentorProfilePage, DashboardPage, SessionsPage, etc.
│   │   │   └── admin/             # AdminOverviewPage, ManageSubjectsPage, CreateMentorPage, etc.
│   │   ├── types/                 # TypeScript types
│   │   ├── App.tsx
│   │   ├── Layout.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── vite.config.ts
│
└── SkillMentor_Backend/           # Spring Boot backend
    ├── src/main/java/com/stemlink/skillmentor/
    │   ├── configs/               # CorsConfig, SecurityConfig
    │   ├── controllers/           # UserController, MentorController, SubjectController, SessionController, ReviewController, StudentController
    │   ├── security/              # AuthenticationFilter, Clerk JWT validation, entry point, access denied handler
    │   ├── dto/                   # Request/response DTOs
    │   ├── entities/              # JPA entities
    │   ├── repositories/         # Spring Data JPA repositories
    │   └── services/              # Business logic
    ├── src/main/resources/
    │   ├── application.properties
    │   ├── application-dev.properties
    │   └── application-prod.properties
    └── pom.xml
```
