# Design Decisions — Sprint 2

## 1. Student Accounts and Security

I added student accounts and Spring Security so students can register, sign in, and submit reports or claims under their own accounts. Passwords are stored using BCrypt instead of plain text.

I considered handling login manually with session variables, but decided to use Spring Security because it already provides authentication and role-based access control.

## 2. Ownership Claims

I created a separate ItemClaim class, repository, and database table to store ownership claims. Each claim is linked to a found item and a student.

I chose this approach because multiple students may claim the same item. Keeping claims separate from found items allows the application to store and review each claim individually.

## 3. Admin Review

I added an admin controller and dashboard for reviewing pending claims. Admins can approve, decline, or request more information.

The controller checks a claim's status before updating it. I also added a check to prevent a second claim for the same item from being approved.

## 4. Architecture and PoEAA Patterns

I kept the existing Browser → Controller → Domain/Repository → Database structure.

The application uses the following Patterns of Enterprise Application Architecture (PoEAA):

- **Transaction Script:** Controller actions handle operations such as submitting and reviewing claims.
- **Table Data Gateway:** Spring Data JDBC repositories provide methods for accessing stored records, although the repositories are framework-generated rather than manually implemented gateways.
- **Domain Model:** Classes such as FoundItem, AppUser, and ItemClaim represent the main concepts in the application.

I kept the design simple because this is a small application, and I wanted the new features to fit into the structure I already built during Sprint 1.

## 5. Remaining Work

Automatic notifications and the communication process for requesting more information are not finished yet. I plan to complete these in Sprint 3.

The application currently uses an H2 in-memory database, so stored data is lost when the application restarts. A persistent database would be needed to retain data between restarts.
