//package me.khs.token;
//
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.Date;
//
//import org.springframework.stereotype.Component;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import me.khs.entity.UserEntity;
//
//import java.security.Key;
//
//
//@Component
//public class JwtUtil {
//	
//	private static final String SECRET_KEY = "this-is-private-secret-key-for-budget-project-!!!";
//	private final Key key =Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
//	
//	private final long expireTime = 1000 * 60 * 60;
//	
//	public String generateToken(Long userId) {
//		
//		return Jwts.builder()
//				.setSubject("accessToken")
//				.claim("userId", userId)
//				.claim("role", "user")
//				.setIssuedAt(new Date())
//				.setExpiration(new Date(System.currentTimeMillis() + expireTime))
//				.signWith(key)
//				.compact();
//		
//	}
//
//}
//
