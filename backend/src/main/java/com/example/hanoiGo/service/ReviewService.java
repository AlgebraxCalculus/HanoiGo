package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.ReviewRequest;
import com.example.hanoiGo.dto.response.ReviewResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.model.Review;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.model.UserLike;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.repository.ReviewRepository;
import com.example.hanoiGo.repository.UserRepository;
import com.example.hanoiGo.repository.LocationDetailRepository;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.example.hanoiGo.mapper.UserMapper;
import com.google.cloud.firestore.DocumentSnapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final LocationDetailRepository locationDetailRepository;
    private final com.example.hanoiGo.repository.UserLikeRepository userLikeRepository;
    private final FirebaseService firebaseService;
    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Return list of reviews for a location address. sortType: best|worst|newest|oldest
     */
    public List<ReviewResponse> getListReview(String address, String sortType) {
        LocationDetail loc = locationDetailRepository.findByAddress(address)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        List<Review> reviews = reviewRepository.findByLocationId(loc.getId());
        List<ReviewResponse> responses = new ArrayList<>();

        for (Review r : reviews) {
            UserResponse userRes = null;
            try {
                userRes = userService.getUserById(r.getUser().getId());
            } catch (Exception ex) {
                // fallback: try to build minimal user response
                try {
                    Optional<User> uOpt = userRepository.findById(r.getUser().getId());
                    if (uOpt.isPresent()) {
                        User u = uOpt.get();
                        userRes = userMapper.toUserResponse(u);
                    }
                } catch (Exception ignore) {}
            }

            List<String> pics = firebaseService.getReviewPictures(r.getId());

        long likeCount = userLikeRepository.countByReviewId(r.getId());

        ReviewResponse resp = ReviewResponse.builder()
            .userResponse(userRes)
            .rating(r.getRating() == null ? 0 : r.getRating())
            .createdAt(r.getCreatedAt())
            .content(r.getContent())
            .pictureUrl(pics)
            .likeCount(likeCount)
            .build();

            responses.add(resp);
        }

        if ("most approved".equalsIgnoreCase(sortType)) {
            responses.sort(Comparator.comparingLong(ReviewResponse::getLikeCount).reversed());
        } else if ("highest rate".equalsIgnoreCase(sortType)) {
            responses.sort(Comparator.comparingInt(ReviewResponse::getRating).reversed());
        } else if ("newest".equalsIgnoreCase(sortType)) {
            responses.sort(Comparator.comparing(ReviewResponse::getCreatedAt).reversed());
        } else if ("lowest rate".equalsIgnoreCase(sortType)) {
            responses.sort(Comparator.comparing(ReviewResponse::getRating));
        }

        return responses;
    }

    /**
     * Add a review for a location. Username identifies the user.
     */
    public void addReview(ReviewRequest request, String username) {
        // find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // find location
        LocationDetail loc = locationDetailRepository.findByAddress(request.getLocationAddress())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // validate rating
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // check if existing review by this user for this location
        if(reviewRepository.findByUserIdAndLocationId(user.getId(), loc.getId()).isPresent()){
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review();
        review.setUser(user);
        review.setLocation(loc);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        // createdAt will be set by @CreationTimestamp

        Review saved = reviewRepository.save(review);

        // create Firestore document for this review with pictureUrl (empty list if none) and maxLikeCount = 0
        try {
            Firestore db = FirestoreClient.getFirestore();
            List<String> pics = request.getPictureUrls() != null ? request.getPictureUrls() : new ArrayList<>();
            Map<String, Object> doc = Map.of(
                    "pictureUrl", pics,
                    "maxLikeCount", 0
            );
            db.collection("reviews").document(saved.getId().toString()).set(doc).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to create review document in Firestore: " + e.getMessage());
        }

        // Add points for review only if user hasn't already been awarded for this place
        int addedPoints = 5;
        boolean shouldAwardPoints = true;
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot userDoc = db.collection("userReviewPlaces")
                    .document(user.getId().toString())
                    .get()
                    .get();

            String placeIdStr = loc.getId().toString();
            if (userDoc.exists()) {
                @SuppressWarnings("unchecked")
                List<String> placeList = (List<String>) userDoc.get("PlaceId");
                if (placeList != null && placeList.contains(placeIdStr)) {
                    // user has already received points for this place before -> do not award again
                    shouldAwardPoints = false;
                } else {
                    // add this place id to the array (merge)
                    placeList.add(placeIdStr);
                    db.collection("userReviewPlaces").document(user.getId().toString())
                            .set(Map.of("PlaceId", placeList), com.google.cloud.firestore.SetOptions.merge()).get();
                }
            } else {
                // create document with this place id
                db.collection("userReviewPlaces").document(user.getId().toString())
                        .set(Map.of("PlaceId", List.of(placeIdStr))).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to check/update userReviewPlaces: " + e.getMessage());
            shouldAwardPoints = true;
        }

        if (shouldAwardPoints) {
            int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) + addedPoints;
            user.setPoints(newPoints);
            userRepository.save(user);

            // Push updated points to Firestore userStats
            try {
                firebaseService.pushUserStatsData(user.getId(), "points", newPoints);
            } catch (Exception ex) {
                System.err.println("Failed to push user stats to Firestore: " + ex.getMessage());
            }

            // Send FCM notification
            String fcm = user.getFcmToken();
            if (fcm != null && !fcm.isBlank()) {
                String title = "Post review successful!";
                String body = "You have posted a review at " + loc.getAddress() + " and received +" + addedPoints + " points.";
                firebaseService.sendNotification(fcm, title, body);
            }
        }
    }

    public void updateReview(ReviewRequest request, String username) {
        // find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // find location
        LocationDetail loc = locationDetailRepository.findByAddress(request.getLocationAddress())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // validate rating
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        // find existing review by this user for this location
        Review existing = reviewRepository.findByUserIdAndLocationId(user.getId(), loc.getId())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // update fields
        existing.setRating(request.getRating());
        existing.setContent(request.getContent());

        // persist update
        Review saved = reviewRepository.save(existing);

        if (request.getPictureUrls() != null) {
            try {
                Firestore db = FirestoreClient.getFirestore();
                db.collection("reviews").document(saved.getId().toString())
                        .set(Map.of("pictureUrl", request.getPictureUrls()), com.google.cloud.firestore.SetOptions.merge()).get();
            } catch (InterruptedException | ExecutionException e) {
                // log and continue
                System.err.println("Failed to update review pictures: " + e.getMessage());
            }
        }
    }

    public void deleteReview(String address, String username) {
        // find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // find location
        LocationDetail loc = locationDetailRepository.findByAddress(address)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // find review
        Review review = reviewRepository.findByUserIdAndLocationId(user.getId(), loc.getId())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // delete review
        reviewRepository.delete(review);

        // delete user_likes associated with this review
        List<UserLike> likes = userLikeRepository.findAllByReviewId(review.getId());
        userLikeRepository.deleteAll(likes);

        // delete pictures from Firestore
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("reviews").document(review.getId().toString()).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to delete review pictures: " + e.getMessage());
        }
    }

    public String likeReview(String address, String authorName, String username) {
        // find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // find author
        User author = userRepository.findByUsername(authorName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // find location
        LocationDetail loc = locationDetailRepository.findByAddress(address)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // find review
        Review review = reviewRepository.findByUserIdAndLocationId(author.getId(), loc.getId())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        UserLike userLike = userLikeRepository.findByUserIdAndReviewId(user.getId(), review.getId());
        if (userLike != null) {
            userLikeRepository.delete(userLike);
            return "Bỏ thích";
        }else{
            userLike = new UserLike();
            userLike.setUser(user);
            userLike.setReview(review);
            userLikeRepository.save(userLike);
            long likeCount = userLikeRepository.countByReviewId(review.getId());
            long maxLikeCount = likeCount;
            try {
                Firestore db = FirestoreClient.getFirestore();
                DocumentSnapshot doc = db.collection("reviews")
                        .document(review.getId().toString())
                        .get()
                        .get();
                if (doc.exists()) {
                    @SuppressWarnings("unchecked")
                    long num = (Long) doc.get("maxLikeCount");
                    maxLikeCount = num;
                }
            } catch (Exception e) {
                System.err.println("Error getting maxLikeCount for " + review.getId() + ": " + e.getMessage());
            }

            if(likeCount > maxLikeCount){
                try {
                    //Cập nhật maxLikeCount
                    maxLikeCount = likeCount;
                    Firestore db = FirestoreClient.getFirestore();
                    db.collection("reviews").document(review.getId().toString())
                            .set(Map.of("maxLikeCount", maxLikeCount), com.google.cloud.firestore.SetOptions.merge()).get();

                    // Cộng điểm nếu maxLikeCount ở mốc 5,10,15,20,25.
                    if(maxLikeCount % 5 == 0 && maxLikeCount <= 25){
                        int pointsToAdd = 1; // điểm cộng mỗi mốc
                        int newPoints = (author.getPoints() == null ? 0 : author.getPoints()) + pointsToAdd;
                        author.setPoints(newPoints);
                        userRepository.save(author);

                        // Push updated points to Firestore userStats
                        try {
                            firebaseService.pushUserStatsData(author.getId(), "points", newPoints);
                        } catch (Exception ex) {
                            System.err.println("Failed to push user stats to Firestore: " + ex.getMessage());
                        }

                        // Send FCM notification
                        String fcm = author.getFcmToken();
                        if (fcm != null && !fcm.isBlank()) {
                            String title = "Your review is getting popular!";
                            String body = "Your review at " + loc.getAddress() + " has received " + maxLikeCount + " likes. You earned +" + pointsToAdd + " points!";
                            firebaseService.sendNotification(fcm, title, body);
                        }
                    }

                    // Cập nhật most_likes nếu maxLikeCount lớn hơn most_likes hiện tại
                    DocumentSnapshot doc = db.collection("userStats")
                            .document(author.getId().toString())
                            .get()
                            .get();
                    if (doc.exists()) {
                        @SuppressWarnings("unchecked")
                        long num = (Long) doc.get("most_likes");
                        if(maxLikeCount > num){
                            db.collection("userStats").document(author.getId().toString())
                                    .set(Map.of("most_likes", maxLikeCount), com.google.cloud.firestore.SetOptions.merge()).get();
                        } 
                    }
                } catch (InterruptedException | ExecutionException e) {
                    // log and continue
                    System.err.println("Failed to update review maxLikeCount: " + e.getMessage());
                }
            }

            return "Thích";
        }
        
    }

    /**
     * Get all reviews for a location that the user has liked
     */
    public List<ReviewResponse> getLikedReviews(String address, String username) {
        // find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // find location
        LocationDetail loc = locationDetailRepository.findByAddress(address)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // get all reviews for this location
        List<Review> allReviews = reviewRepository.findByLocationId(loc.getId());
        List<ReviewResponse> likedReviews = new ArrayList<>();

        for (Review r : allReviews) {
            // check if user has liked this review
            UserLike like = userLikeRepository.findByUserIdAndReviewId(user.getId(), r.getId());
            if (like == null) {
                continue; // user hasn't liked this review, skip
            }

            // build ReviewResponse for liked reviews
            UserResponse userRes = null;
            try {
                userRes = userService.getUserById(r.getUser().getId());
            } catch (Exception ex) {
                // fallback: try to build minimal user response
                try {
                    Optional<User> uOpt = userRepository.findById(r.getUser().getId());
                    if (uOpt.isPresent()) {
                        User u = uOpt.get();
                        userRes = userMapper.toUserResponse(u);
                    }
                } catch (Exception ignore) {}
            }

            List<String> pics = firebaseService.getReviewPictures(r.getId());
            long likeCount = userLikeRepository.countByReviewId(r.getId());

            ReviewResponse resp = ReviewResponse.builder()
                    .userResponse(userRes)
                    .rating(r.getRating() == null ? 0 : r.getRating())
                    .createdAt(r.getCreatedAt())
                    .content(r.getContent())
                    .pictureUrl(pics)
                    .likeCount(likeCount)
                    .build();

            likedReviews.add(resp);
        }

        return likedReviews;
    }
}
