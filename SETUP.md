# Setup & Run Instructions

## Prerequisites
- Docker & Docker Compose
- Java 21 (optional, if you want to run outside Docker)
- Maven 3.9+ (optional, if you want to build locally outside Docker)

## Step-by-Step Guide

### 1. Start the Environment
Run the following command from the project root. This will build all containers and start the full stack.
```bash
docker compose up --build -d
```

### 2. Access the Application
| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | React Dashboard (Login, Apply, Nominee Flow) |
| Backend API | http://localhost:8080 | Spring Boot REST API |
| Swagger UI | http://localhost:8080/swagger-ui.html | Interactive API Docs |
| Adminer (DB UI) | http://localhost:8081 | Database Management |

### 3. Demo Accounts
The system ships with three pre-seeded demo accounts:

| User ID | Password | Profile | Purpose |
|---------|----------|---------|---------|
| `alice` | `password` | $8,000/mo income, $200K property | Likely to be approved |
| `bob` | `password` | $2,000/mo income, no property | Likely to be rejected (High Risk) |
| `charlie` | `password` | $15,000/mo income, $500K property | Rich nominee/guarantor |

### 4. Testing the Full Flow
1. Go to **http://localhost:3000** and login as `bob` / `password`.
2. Apply for a **$50,000 loan** with a **24-month tenure**.
3. The ML model will compute a high Probability-of-Default and reject it → status becomes `NOMINEE_REQUIRED`.
4. Click **Refer Nominee** and enter `charlie`.
5. **Logout**, then login as `charlie` / `password`.
6. Charlie sees a **Pending Nominee Request**. Click **Accept & Guarantee Loan**.
7. The system merges Bob + Charlie's financial profiles and re-runs the ML model → status flips to `APPROVED`.
8. Click **Audit Trail** on any application to see the full ML feature-contribution breakdown (XAI).

### 5. Database Access
You can inspect the tables using Adminer:
- **URL**: http://localhost:8081
- **System**: PostgreSQL
- **Server**: postgres
- **Username**: postgres
- **Password**: postgres
- **Database**: lendingdb

### 6. Running Tests
To run unit tests locally (if Java/Maven are installed):
```bash
mvn test
```

## Vercel Deployment Guide

Since this is a Monorepo containing both the Java backend and the React frontend, you need to configure Vercel so it only builds the frontend directory.

1. Create a [Vercel](https://vercel.com/) account and connect your GitHub repository.
2. In the **Import Project** screen, configure the following:
   - **Framework Preset**: Vite
   - **Root Directory**: Click "Edit" and select the `frontend` folder.
3. In the **Environment Variables** section, add:
   - **Key**: `VITE_API_BASE_URL`
   - **Value**: `https://<YOUR_PRODUCTION_BACKEND_URL>/api/v1`
4. Click **Deploy**. Vercel will now automatically build and deploy your React frontend on every push to your main branch!
