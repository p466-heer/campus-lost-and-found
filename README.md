# Campus Lost & Found Board

## Description

Campus Lost & Found Board is a web application that helps students report and browse items found on campus. For Sprint 1, users can submit information about a found item, including its name, description, and location. They can also view items that others have reported.

## Team Members

- Heer Soni

## Requirements

- Java 17
- Maven (included through the Maven wrapper)

## How to Run

1. Clone the repository.
2. Open the project folder in a terminal.
3. Run the tests:

   ./mvnw test

4. Start the application:

   ./mvnw spring-boot:run

5. Open http://localhost:8080/items in a browser.

## Database

The application uses an H2 in-memory database. The database is created when the application starts, and the data resets when the application stops.

No additional database setup is needed.

## Sprint 1 Features

- Report a found item
- Browse reported items
- Validate required form fields
