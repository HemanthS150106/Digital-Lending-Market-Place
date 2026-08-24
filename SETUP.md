# Setup & Run Instructions

## Prerequisites
- Docker & Docker Compose
- Java 21 (optional, if you want to run outside Docker)
- Maven 3.9+ (optional, if you want to build locally outside Docker)

## Step-by-Step Guide

### 1. Start the Environment
Run the following command from the project root. This will build the Spring Boot application and start PostgreSQL and Adminer.
```bash
docker compose up --build -d
```

### 2. Verify Health
Check if the application started successfully:
```bash
curl http://localhost:8080/actuator/health
```
You should see: `{"status":"UP"}`

### 3. Hit Sample Endpoints

**View empty loan applications:**
```bash
curl http://localhost:8080/api/v1/loan-applications
```

**Submit an application:**
```bash
curl -X POST http://localhost:8080/api/v1/loan-applications \
-H "Content-Type: application/json" \
-d '{
    "lendingApp": {"id": 1},
    "borrowerId": "B-1001",
    "amount": 5000.00,
    "tenureMonths": 12,
    "monthlyIncome": 2500.00
}'
```

**Check the generated decision trail for the application:**
```bash
curl http://localhost:8080/api/v1/loan-applications/1/decision-trail
```

### 4. Database Access
You can inspect the tables using Adminer:
- **URL**: http://localhost:8081
- **System**: PostgreSQL
- **Server**: postgres
- **Username**: postgres
- **Password**: postgres
- **Database**: lendingdb

### 5. Running Tests
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
