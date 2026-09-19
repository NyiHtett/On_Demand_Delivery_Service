# On-Demand Delivery Backend

## Technology Stack

- Java
- Spring Boot
- Maven
- Docker
- MySQL
- JDBC

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

## Recommended Startup Order

```text
1. Open Docker Desktop
2. Start MySQL with Docker Compose
3. Check the MySQL container
4. Start the Spring Boot application
```

Commands:

```bash
docker compose up -d
docker compose ps
./mvnw spring-boot:run
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
