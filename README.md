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

Stripe test card: `4242 4242 4242 4242`, any future expiry, any CVC. Seed logins (password `P@ssw0rd!`): customers `anthony.k.130550@gmail.com`, `maria.lopez@example.com`, `sam.patel@example.com`; employees `nyi.htet@sjsu.edu`, `johnathan.aye@sjsu.edu`.

The manual steps below still work if you prefer them.

## Running the Project

### 1. Start Docker Desktop

Make sure Docker Desktop is open before running the following commands.

### 2. Start the MySQL Container

From the project directory, run:

```bash
docker compose up -d
```

The `-d` option runs the MySQL container in the background.

Check whether the container is running:

```bash
docker compose ps
```

View the MySQL startup logs:

```bash
docker compose logs mysql
```

Wait until the logs show:

```text
ready for connections
```

### 3. Access the MySQL Database

Enter the MySQL command-line client:

```bash
docker compose exec mysql mysql -u delivery_user -p on_demand_delivery_db
```

When prompted for the password, enter:

```text
delivery_password
```

After logging in, you should see:

```text
mysql>
```

You can now execute SQL commands, such as:

```sql
SHOW TABLES;
```

To leave MySQL:

```sql
exit;
```

### 4. Start the Spring Boot Application

Give the Maven wrapper permission to run. This command only needs to be executed once:

```bash
cd backend
chmod u+x mvnw
```

Start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

The backend will run at:

```text
http://localhost:8080
```

### 5. Start the React Frontend

```bash
cd ..
cd frontend
npm install
npm run dev
```

## Recommended Startup Order

```text
1. Open Docker Desktop
2. Start MySQL with Docker Compose
3. Check the MySQL container
4. Start the Spring Boot application
5. Start the React Frontend
```

Commands:

```bash
docker compose up -d
docker compose ps
cd backend
./mvnw spring-boot:run
cd ..
cd frontend
npm install
npm run dev
```

## Stopping the Project

Stop Spring Boot by pressing:

```text
Ctrl + C
```

Stop the MySQL container:

```bash
docker compose down
```

The MySQL data is preserved in the Docker volume unless the volume is manually deleted.

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

