# HanoiGo 

## Description
HanoiGo is a comprehensive Spring Boot application designed to power the HanoiGo travel and exploration platform. It provides robust RESTful APIs for managing user authentication, location discovery, AI-driven travel routing, user reviews, customizable bookmarks, and an engaging gamification system featuring checkpoints and achievements.

## Architecture
The codebase strictly follows a standard Layered Architecture pattern common in modern backend development, ensuring a clean separation of concerns, scalability, and maintainability.

### Model Layer
Core business entities and data structures mapped directly to the database:
- **User Management:** `User`, `PasswordResetToken`
- **Locations & Discovery:** `LocationDetail`, `LocationTag`, `Tag`
- **Gamification:** `Achievement`, `Checkpoint`, `UserAchievement`
- **User Interaction:** `Review`, `Bookmark`, `BookmarkList`, `UserLike`

### Service Layer
Contains the core business logic and orchestrates operations between the API delivery and data access components:
- **UserService / FirebaseService:** Manages user lifecycles, traditional authentication, and third-party login.
- **LocationService:** Handles the retrieval, filtering, and management of point-of-interest data in Hanoi.
- **TravelAIService / DirectionService:** Processes routing algorithms and AI-generated travel plans.
- **ReviewService / BookmarkService:** Manages user-generated content, feedback, and personalized saved places.
- **AchievementService / CheckpointService:** Implements the verification logic for unlocking badges and tracking user exploration progress.

### Controller Layer
REST API endpoints that receive HTTP requests, validate incoming payloads via DTOs, and format standard responses:
- `UserController`, `LocationController`, `TravelAIController`, `ReviewController`, `BookmarkController`, `AchievementController`, `DirectionController`, etc.

### Repository Layer
Data access interfaces extending Spring Data JPA for seamless, query-based database interactions (e.g., `UserRepository`, `LocationDetailRepository`, `ReviewRepository`).

### Middleware
Components intercepting and processing requests/responses globally across the application:
- **SecurityConfig:** Defines access rules, CORS policies, and secures protected endpoints.
- **JwtUtil:** Validates JSON Web Tokens (JWT) attached to requests to ensure strict authorization.
- **FirebaseConfig:** Integrates the Firebase Admin SDK for secure mobile authentication synchronization.
- **GlobalExceptionHandler:** Catches exceptions system-wide and returns standardized error formats defined in `AppException` and `ErrorCode`.

## Features
- **Secure Authentication:** Complete JWT-based login/registration system with Firebase integration.
- **Smart Discovery:** Explore locations around Hanoi with detailed tags, ratings, and descriptions.
- **AI Travel Routing:** Generate optimized travel itineraries and directions (integrating external mapping APIs like Goong).
- **Social & Feedback:** Rate, review, and interact with various places.
- **Gamified Experience:** Earn achievements and track progress by visiting specific real-world checkpoints.
- **Personal Collections:** Organize favorite spots into custom bookmark lists.

## Prerequisites
To run this project locally, ensure you have the following installed and configured:
- **Java:** JDK 17 (or compatible version)
- **Build Tool:** Maven
- **Database:** Relational database (e.g., MySQL or PostgreSQL) as configured in `application.properties`
- **External Services:**
  - Firebase Admin SDK credentials 
  - Goong API Key (required for mapping and directions functionality)

## Project Structure

```text
backend/src/main/java/com/example/hanoiGo/
├── config/          # Spring Security, Firebase, and Async configurations
├── controller/      # REST API Controllers (Controller Layer)
├── dto/             # Data Transfer Objects (Requests & Responses)
├── exception/       # Global error handling and custom application errors
├── mapper/          # Object mapping logic (Entity to DTO transformations)
├── model/           # JPA Entities (Model Layer)
├── repository/      # Spring Data JPA interfaces (Repository Layer)
├── service/         # Business logic implementation (Service Layer)
└── util/            # Utility classes (JWT processing, cryptos, etc.)
