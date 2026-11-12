# On-Demand Delivery Backend

## Technology Stack

- Java
- Spring Boot
- Maven
- Docker
- MySQL
- JDBC

## Quick Start

Requires Docker Desktop (running), Node, and Java 17+. From the project root:

```bash
npm run dev
```

This starts MySQL in Docker, resets the database from `backend/db/schema.sql` + `seed.sql`, then runs the Spring Boot backend (http://localhost:8080) and the React frontend (http://localhost:5173). Press `Ctrl + C` to stop the backend and frontend; MySQL keeps running (`docker compose down` to stop it).

```bash
npm run dev -- --keep-db   # same, but keep the current database data
npm run db:reset           # only reset the database
npm test                   # reset the database, then run backend tests and Playwright tests
npm test -- --backend      # backend tests only
npm test -- --e2e          # Playwright tests only
```

If another MySQL is already using port 3306, the scripts use 3307 automatically. If Playwright's Chromium won't launch, set `PW_CHANNEL=chrome` to use your installed Chrome.

Settings go in a `.env` file in the project root (it is gitignored, never commit keys):

```bash
STRIPE_SECRET_KEY=sk_test_...      # without it, checkout skips Stripe and marks orders paid right away
GOOGLE_MAPS_API_KEY=...            # robot routes + map; without it robots use 5 min per stop and no map
ROBOT_QUEUE_UNIT_SECONDS=10        # demo speed: robot timer counts 10 s "minutes" (default 60)
ROBOT_TIME_SCALE=20                # demo speed: robots drive 20x faster (default 1)
```

Stripe test card: `4242 4242 4242 4242`, any future expiry, any CVC. Seed logins (password `P@ssw0rd!`): customers `maria.lopez@example.com` (has a sample cart and order history), `sam.patel@example.com`, `leah.ni@example.com`; employees `abe.jun@example.com`, `joel.east@example.com`, `po.li@example.com`.

## User's Guide

### Prerequisites

Install and start:

- Docker Desktop
- Node.js and npm
- Java 17 or later

### Start the Application

From the project root, run:

```bash
npm install
npm run dev
```

`npm run dev` automatically:

1. Starts MySQL with Docker Compose.
2. Waits for MySQL to become available.
3. Resets the database using `backend/db/schema.sql` and `backend/db/seed.sql`.
4. Installs frontend dependencies if needed.
5. Starts the Spring Boot backend.
6. Starts the React frontend.

Open the application at:

```text
http://localhost:5173
```

The backend API runs at:

```text
http://localhost:8080
```

The first startup may take a few minutes while dependencies and services are prepared.

### Preserve Existing Database Data

By default, `npm run dev` resets the database. To keep the existing database data, run:

```bash
npm run dev -- --keep-db
```

### Demo Accounts

All seeded accounts use the password:

```text
P@ssw0rd!
```

Customer accounts:

- `maria.lopez@example.com`
- `sam.patel@example.com`
- `leah.ni@example.com`

Employee accounts:

- `abe.jun@example.com`
- `joel.east@example.com`
- `po.li@example.com`

### Environment Settings

Optional settings can be added to a `.env` file in the project root:

```bash
STRIPE_SECRET_KEY=sk_test_...
GOOGLE_MAPS_API_KEY=...
ROBOT_QUEUE_UNIT_SECONDS=10
ROBOT_TIME_SCALE=20
```

The `.env` file is ignored by Git and should never contain production credentials.

### Stop the Application

Press:

```text
Ctrl + C
```

This stops the backend and frontend. MySQL continues running in Docker.

To stop MySQL as well, run:

```bash
docker compose down
```

### Reset Only the Database

To start MySQL and reseed the database without starting the backend or frontend:

```bash
npm run db:reset
```

> The usernames and passwords in this document are for local development only. Do not use these credentials in production.


## Customers Endpoint

Retrieves all customers from the `customers` table.

### Request

```http
GET http://localhost:8080/api/customers
```

## Testing

### Backend tests

From the project root:

```powershell
cd backend
.\mvnw.cmd test
```

### Playwright end-to-end tests

Make sure Docker Desktop is running. From the project root:

```powershell
cd frontend
npm install
npx playwright install chromium
npm run test:e2e
```

The Playwright setup automatically starts:

- MySQL through Docker Compose
- The Spring Boot backend
- The Vite frontend

### Complete test suite

From the project root:

```powershell
docker compose up -d

cd backend
.\mvnw.cmd test

cd ..\frontend
npm run test:e2e
```

### Run a specific Playwright test

```powershell
cd frontend
npx playwright test tests/loginAndSignUp/testRunner.spec.js
```

### Run Playwright with the browser visible

```powershell
cd frontend
npx playwright test --headed
```

### View the Playwright report

```powershell
cd frontend
npx playwright show-report
```
