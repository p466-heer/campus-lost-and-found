# Design Decisions — Sprints 1–3

## 1. Student Accounts and Security

I added student accounts and Spring Security so students can register, sign in, and submit reports or claims under their own accounts. Passwords are stored using BCrypt instead of plain text.

I considered handling login manually with session variables, but chose Spring Security because it provides authentication and role-based access control.

Students can only perform student actions, while administrator actions are protected by the ADMIN role.

## 2. Ownership Claims

I created a separate ItemClaim class, repository, and database table to store ownership claims. Each claim is linked to a found item and a student.

I chose this approach because multiple students may claim the same item. Keeping claims separate from found items allows the application to store and review each claim individually.

Students cannot claim an item they reported themselves or an item that has already been resolved.

## 3. Admin Review and Communication

I added an admin controller and dashboard for reviewing ownership claims.

Administrators can:

- Approve claims
- Decline claims
- Request additional information
- Review student responses

The application stores the administrator's request and the student's response with the claim.

I chose to keep this communication inside the application rather than implement email messaging because it was simpler and fit the scope of the project.

## 4. Database-Level Concurrency Control

One important design decision was preventing two administrators from approving different claims for the same found item at the same time.

A normal check for an existing approved claim is not enough because two requests could perform the check before either saves its changes.

To address this, I used database row-level locking with SQL `SELECT ... FOR UPDATE`.

The approval operation runs inside a transaction. It locks the found item before checking whether an approved claim already exists.

If another administrator is approving a claim for the same item, the second transaction waits for the first transaction to finish.

After acquiring the lock, the second transaction checks the approval status again. If another claim was approved, the application rejects the request with HTTP 409 Conflict.

I chose database locking because it protects the shared data even when multiple requests reach the application simultaneously.

## 5. Resolved Items

In Sprint 3, I added the ability for administrators to mark returned items as resolved.

Each found item stores:

- A resolved status
- A date and time when it was resolved

Resolved items remain visible for reference but cannot receive new ownership claims.

I also added a public page that displays recently resolved items, ordered by their resolution dates.

I chose to keep resolved items in the database instead of deleting them because they provide a record of returned property.

## 6. Database Configuration

The application uses two database configurations.

### Local Development

I use H2 for local development and automated testing because it requires no external database setup and makes development easier.

### Production

I use PostgreSQL on Render for production because application data needs to remain available across deployments and restarts.

Database connection information is provided through environment variables rather than hardcoded credentials.

I chose separate development and production configurations to keep local development simple while supporting persistent production storage.

## 7. Docker Containerization

I created a multi-stage Dockerfile.

The first stage uses Java 17 JDK and Maven to build the application.

The second stage uses Java 17 JRE to run the packaged application.

I chose a multi-stage build because the production image does not need the full development toolchain.

The container starts the application using `java -jar app.jar`.

## 8. Continuous Integration

I created a GitHub Actions workflow that runs on pushes and pull requests.

The workflow:

1. Checks out the repository.
2. Sets up Java 17.
3. Runs automated Maven tests.
4. Builds the Docker image.

If a test fails, the workflow stops and reports a failure.

I chose this approach to catch errors before deploying new code.

## 9. Continuous Deployment

I deployed the application to Render using the configuration in `render.yaml`.

Render hosts both the Docker-based web application and the PostgreSQL database.

The deployment configuration includes:

- Production environment variables
- Database connection settings
- A health check endpoint
- Automatic deployment after passing GitHub checks

The setting `autoDeployTrigger: checksPass` is intended to prevent automatic deployment until the required checks pass.

I chose Render because it supports Docker deployments and managed PostgreSQL with a straightforward setup.

## 10. Architecture and PoEAA Patterns

I kept the existing:

Browser → Controller → Domain/Repository → Database

architecture.

The application uses the following Patterns of Enterprise Application Architecture (PoEAA):

- **Transaction Script:** Controller actions coordinate operations such as submitting claims, approving claims, and resolving items.
- **Table Data Gateway:** Spring Data JDBC repositories provide database access methods, although the repositories are generated by the framework rather than manually implemented gateways.
- **Domain Model:** FoundItem, AppUser, and ItemClaim represent the main concepts in the application.

I kept the architecture relatively simple because this is a small course project.

## 11. Tradeoffs and Remaining Limitations

Some features were intentionally kept simple:

- Notifications appear inside the application instead of being sent through email.
- Administrators manually mark items as resolved after they are returned.
- The Render free-tier web service may take additional time to respond after inactivity.
- The local H2 database is temporary, while PostgreSQL stores production data persistently.

These choices allowed me to focus on the required user stories, testing, database correctness, and deployment.

## 12. Final Outcome

By the end of Sprint 3, the application supports the planned student and administrator workflows, database-level claim approval locking, resolved-item tracking, automated tests, Docker containerization, GitHub Actions CI, and deployment to Render with PostgreSQL.
