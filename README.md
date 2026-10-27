# Campus Lost & Found Board

## Description

Campus Lost & Found Board is a Spring Boot web application developed for CSCI-P466 at Indiana University.

Students can report found items, search for items, and submit ownership claims. Luddy administrators can review claims, request additional information, approve or decline claims, and mark returned items as resolved.

The application is deployed using Docker and Render, with PostgreSQL for production data storage.

## Team Members

- Heer Soni

## Live Application

https://campus-lost-and-found-5zeb.onrender.com

The application is hosted on Render's free tier, so the first request may take additional time if the service has been inactive.

## Requirements

- Java 17 or newer
- Maven wrapper (included)
- Docker (optional, for containerized execution)

## Running Locally

1. Clone the repository:

   ```bash
   git clone https://github.com/p466-heer/campus-lost-and-found.git
   ```

2. Enter the project directory:

   ```bash
   cd campus-lost-and-found
   ```

3. Run automated tests:

   ```bash
   ./mvnw clean test
   ```

4. Start the application:

   ```bash
   ./mvnw spring-boot:run
   ```

5. Open:

   http://localhost:8080/items

The local application uses an H2 in-memory database. Local data is reset when the application restarts.

## Demo Accounts

### Administrator

- Email: admin@example.com
- Password: Admin123!

The demo administrator can access:

https://campus-lost-and-found-5zeb.onrender.com/admin/claims

### Student

Students can register their own accounts using:

https://campus-lost-and-found-5zeb.onrender.com/register

## Student Features

- Register and sign in
- Report found items
- Browse and search found items
- View individual item details
- Submit ownership claims
- View submitted claims and their statuses
- Respond when an administrator requests more information
- Receive approval or decline notifications through the application
- View recently resolved items
- Cannot claim an item they originally reported
- Cannot submit new claims for resolved items

## Administrator Features

- View submitted ownership claims
- Receive notifications of new claims through the application
- Approve or decline claims
- Request additional information from claimants
- Review student responses
- Prevent multiple claims for the same item from being approved
- Mark returned items as resolved
- Record the resolution date

## Database

The application uses different databases for development and production.

### Local Development

- H2 in-memory database
- No external database installation required
- Data resets when the application restarts

### Production

- PostgreSQL hosted on Render
- Database connection settings supplied through environment variables
- Persistent storage for users, found items, and ownership claims

Database credentials are not hardcoded in the repository.

## Concurrency Control

The application uses PostgreSQL row-level locking to prevent two administrators from approving different claims for the same item simultaneously.

The approval operation runs inside a transaction and uses SQL `SELECT ... FOR UPDATE` to lock the relevant database rows.

After acquiring the lock, the application checks whether another claim has already been approved.

If an approved claim already exists, the application rejects the conflicting approval with HTTP 409 Conflict.

This protects against concurrent approval attempts and ensures that at most one claim per item can be approved.

## Docker

The project includes a multi-stage Dockerfile.

- Stage 1: Build the Spring Boot application using Java 17 and Maven.
- Stage 2: Run the packaged application using Java 17 JRE.

Build the Docker image:

```bash
docker build -t campus-lost-and-found .
```

Run the container locally:

```bash
docker run --rm -p 8080:8080 campus-lost-and-found
```

Open:

http://localhost:8080/items

The Docker image starts the application using:

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Continuous Integration (CI)

GitHub Actions automatically runs on:

- Pushes
- Pull requests

The workflow is defined in:

`.github/workflows/ci.yml`

The pipeline:

1. Checks out the repository.
2. Sets up Java 17.
3. Runs automated Maven tests.
4. Builds the Docker image.

If the Maven tests fail, the workflow fails and the Docker build step does not execute.

## Continuous Deployment (CD)

The application is deployed through Render.

The infrastructure configuration is defined in:

`render.yaml`

Render provides:

- A Docker-based web service
- A PostgreSQL database
- Production environment variables
- Health monitoring through `/actuator/health`
- Automatic deployment after successful GitHub checks

The configuration uses:

```yaml
autoDeployTrigger: checksPass
```

This configures Render to wait for passing checks before automatically deploying new changes.

## Production Configuration

The application uses the Spring `prod` profile in production.

Render supplies the following environment variables:

- `SPRING_PROFILES_ACTIVE`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

Sensitive database credentials are supplied by Render instead of being committed to GitHub.

## Automated Testing

Run the automated test suite:

```bash
./mvnw clean test
```

Tests cover application functionality and help detect regressions before deployment.

GitHub Actions runs the tests automatically for every push and pull request.

## Sprint 1 Features

- Report found items
- Browse reported items
- Validate required form fields

## Sprint 2 Features

- Student registration and sign-in
- Associate found item reports with student accounts
- Submit ownership claims
- Admin claims dashboard
- Approve and decline claims
- Request-more-information claim status
- Prevent multiple approved claims for the same item

## Sprint 3 Features

- Notify administrators when new claims are submitted (CLFB-13)
- Allow administrators to request and receive more information (CLFB-21)
- Search found items (CLFB-10)
- Notify students when claims are approved (CLFB-16)
- Mark returned items as resolved (CLFB-17)
- View recently resolved items (CLFB-18)
- Prevent students from claiming items they reported
- PostgreSQL database-level locking for claim approvals
- Docker containerization
- GitHub Actions CI
- Render deployment with PostgreSQL

## Known Limitations

- Notifications are displayed within the application rather than sent by email.
- The Render free-tier service may take additional time to start after inactivity.
- Item resolution is performed manually by an administrator.

## Release

Sprint 3 represents the final working increment of the Campus Lost & Found Board application.

The production application is available at:

https://campus-lost-and-found-5zeb.onrender.com
