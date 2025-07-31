package com.example.cp_main_be.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token-expiration-milliseconds}")
  private long accessTokenExpirationMilliseconds;

  @Value("${jwt.refresh-token-expiration-milliseconds}")
  private long refreshTokenExpirationMilliseconds;

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateAccessToken(String uuid) {
    return generateToken(uuid, accessTokenExpirationMilliseconds);
  }

  public String generateRefreshToken(String uuid) {
    return generateToken(uuid, refreshTokenExpirationMilliseconds);
  }

  private String generateToken(String uuid, long expirationTime) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationTime);

    return Jwts.builder()
        .setSubject(uuid)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String getUuidFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      // TODO: Handle specific exceptions (ExpiredJwtException, UnsupportedJwtException, etc.)
      return false;
    }
  }
}
