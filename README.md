# Campus Lost & Found Board

## Description

Campus Lost & Found Board is a web application that allows students to report found items, browse reported items, and submit ownership claims. Luddy admins can review claims and decide whether to approve, decline, or request more information.

This project was developed for CSCI-P466.

## Team Members

- Heer Soni

## Requirements

- Java 17 or newer
- Maven (included through the Maven wrapper)

## How to Run

1. Clone the repository:

   git clone https://github.com/p466-heer/campus-lost-and-found.git

2. Open the project folder:

   cd campus-lost-and-found

3. Run the automated tests:

   ./mvnw clean test

4. Start the application:

   ./mvnw spring-boot:run

5. Open http://localhost:8080/items in a browser.

## Student Features

- Register a student account at http://localhost:8080/register
- Sign in at http://localhost:8080/login
- Browse reported found items
- Report a found item while signed in
- Submit an ownership claim with identifying information
- Allow multiple students to submit claims for the same item

## Admin Features

Luddy admins can access the claims dashboard at:

http://localhost:8080/admin/claims

The dashboard allows admins to:

- View pending claims
- Approve claims
- Decline claims
- Request more information
- Prevent a second claim for the same item from being approved

### Demo Admin Account

For grading and local testing, the application automatically creates a demo admin account when it starts.

Email: admin@example.com
Password: Admin123!

This account is intended for local demonstration only.

## Database

The application uses an H2 in-memory database. Tables are created automatically when the application starts.

No additional database setup is required. Data resets whenever the application stops.

## Automated Testing

Run the full test suite with:

./mvnw clean test

At the end of Sprint 2, all 13 automated tests passed.

## Sprint 1 Features

- Report found items
- Browse reported items
- Validate required form fields

## Sprint 2 Features

- Student registration and sign-in
- Link found item reports to student accounts
- Submit ownership claims
- Admin claims dashboard
- Approve and decline claims
- Request-more-information status
- Prevent multiple approved claims for the same item

## Known Limitations

- Automatic admin notifications are not implemented yet.
- The request-more-information feature changes the claim status but does not provide messaging between admins and students.
- The H2 in-memory database does not preserve records after restarting.
- Simultaneous approval requests are not protected by database-level concurrency control.

These limitations are planned for future development.
