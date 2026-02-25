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
```

## API Endpoints

*Note: Most endpoints require a valid JWT token provided in the `Authorization` header as `Bearer <Token>`.*

### Authentication & Password Reset (`/api/auth`)
- `POST /api/auth/forgot-password` - Request an OTP for password reset.
- `POST /api/auth/verify-otp` - Verify the received OTP.
- `POST /api/auth/reset-password` - Set a new password after successful OTP verification.

### User Management (`/api/users`)
- `POST /api/users/register` - Register a new user account.
- `POST /api/users/login` - Authenticate using username and password.
- `POST /api/users/firebase-login` - Authenticate using a Firebase token.
- `GET /api/users/me` - Retrieve current authenticated user profile.
- `GET /api/users/get` - Get a list of all users (optionally ordered by points).
- `GET /api/users/my-rank` - Get the current user's leaderboard rank.
- `POST /api/users/update-fcm-token` - Update Firebase Cloud Messaging token for push notifications.
- `POST /api/users/update-userStats` - Sync user statistics to Firebase.
- `GET /api/users/get-chartData` - Retrieve user activity chart data.
- `POST /api/users/update-avatar` - Update the user's profile avatar URL.

### Locations & Discovery (`/api/locations`)
- `GET /api/locations/get-list` - Retrieve a list of locations with filters (lat, lng, tag, mostVisited, nearest, limit).
- `GET /api/locations/get-detail-by-id` - Get detailed information by location ID.
- `GET /api/locations/get-detail-by-address` - Get detailed information by exact address.
- `GET /api/locations/get-tags-by-id` - Retrieve tags associated with a specific location.
- `GET /api/locations/search-autocomplete` - Auto-complete search suggestions based on keyword.

### Routing & AI Travel (`/api/directions`, `/api/ai`)
- `GET /api/directions` - Get map directions between origin and destination coordinates.
- `POST /api/ai/routes` - Generate an AI-suggested travel plan based on user preferences.

### Checkpoints & Gamification (`/api/checkpoints`, `/api/achievements`)
- `GET /api/checkpoints/enable-checkin` - Get a list of nearby locations available for check-in.
- `POST /api/checkpoints/checkin` - Verify and check-in at a specific location.
- `GET /api/checkpoints/me` - Retrieve the current user's check-in history.
- `GET /api/achievements/me` - Get achievements unlocked by the current user.
- `GET /api/achievements/my-total` - Get the total count of user's achievements.

### Reviews & Social (`/api/reviews`)
- `GET /api/reviews/get-list` - Get reviews for a specific location.
- `POST /api/reviews/add` - Submit a new review for a location.
- `POST /api/reviews/update` - Update an existing review.
- `POST /api/reviews/delete` - Delete a user's review.
- `POST /api/reviews/like` - Like/Unlike a review.
- `GET /api/reviews/get-liked-reviews` - Retrieve a list of reviews liked by the user.

### Bookmarks & Personal Collections (`/api/bookmarks`, `/api/bookmark-lists`)
- `POST /api/bookmark-lists/create` - Create a new custom list for saving places.
- `GET /api/bookmark-lists/my-lists` - Get all custom bookmark lists of the user.
- `PUT /api/bookmark-lists/{listId}` - Update a bookmark list's details.
- `DELETE /api/bookmark-lists/{listId}` - Delete a custom bookmark list.
- `POST /api/bookmarks/add` - Add a location to a specific bookmark list.
- `DELETE /api/bookmarks/remove` - Remove a location from a bookmark list.
- `GET /api/bookmarks/list/{listId}` - Get all locations saved within a specific list.
- `GET /api/bookmarks/all` - Get all bookmarked locations across all lists.
- `GET /api/bookmarks/check` - Check if a specific location is saved in a given list.
- `GET /api/bookmarks/count/{listId}` - Count the number of saved places in a specific list.

## Project demo: 
https://drive.google.com/file/d/1uYMwnlz-EZ56rIext6w7q7DR5Jumq0q8/view?usp=drive_link
