 package com.example.hanoiGo.util;

 import io.jsonwebtoken.Claims;
 import io.jsonwebtoken.Jwts;
 import io.jsonwebtoken.SignatureAlgorithm;
 import io.jsonwebtoken.security.Keys;
 import org.springframework.stereotype.Component;

 import javax.crypto.SecretKey;
 import java.util.Date;
 import java.util.UUID;

 @Component
 public class JwtUtil {
    
     private static final String SECRET_KEY = "mySecretKey123456789012345678901234567890"; // 32 ký tự
     private static final int EXPIRATION_TIME = 86400000; // 24 giờ (milliseconds)
    
     private SecretKey getSigningKey() {
         return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
     }
    
        // Tạo JWT token có cả username + userId
        public String generateToken(String username, UUID userId) {
            return Jwts.builder()
                    .setSubject(username)
                    .claim("userId", userId.toString()) 
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        }

        // Lấy username từ token
        public String getUsernameFromToken(String token) {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        }

        // Lấy userId trực tiếp từ token
        public UUID extractUserId(String token) {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String userIdStr = claims.get("userId", String.class);
            return UUID.fromString(userIdStr);
        }

     // Kiểm tra token có hợp lệ không
     public boolean validateToken(String token, String username) {
         try {
             String tokenUsername = getUsernameFromToken(token);
             return (username.equals(tokenUsername) && !isTokenExpired(token));
         } catch (Exception e) {
             return false;
         }
     }
    
     // Kiểm tra token đã hết hạn chưa
     private boolean isTokenExpired(String token) {
         try {
             Claims claims = Jwts.parserBuilder()
                     .setSigningKey(getSigningKey())
                     .build()
                     .parseClaimsJws(token)
                     .getBody();
             return claims.getExpiration().before(new Date());
         } catch (Exception e) {
             return true;
         }
     }

     public String extractToken(String authHeader) {
         if (authHeader != null && authHeader.startsWith("Bearer ")) {
             return authHeader.substring(7);
         }
         throw new IllegalArgumentException("Invalid Authorization header");
     }
 }
