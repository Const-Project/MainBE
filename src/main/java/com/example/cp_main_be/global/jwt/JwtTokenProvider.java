package com.example.cp_main_be.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    return Jwts.parser()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  // 예외를 그대로 던지도록 수정
  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException e) {
      // 만료된 토큰 - 예외를 다시 던짐
      throw e;
    } catch (JwtException e) {
      // 유효하지 않은 토큰 (서명 오류, 형식 오류 등) - 예외를 다시 던짐
      throw e;
    } catch (Exception e) {
      // 기타 예외 - JwtException으로 래핑해서 던짐
      throw new JwtException("Token validation failed", e);
    }
  }

  public LocalDateTime getExpirationLocalDateTime(String token) {
    // 1. parserBuilder()로 시작합니다.
    Claims claims =
        Jwts.parser()
            // 2. 서명 키를 설정합니다.
            .setSigningKey(getSigningKey())
            // 3. 파서를 빌드합니다.
            .build()
            // 4. 토큰을 파싱하여 Claims(내용)를 가져옵니다.
            .parseClaimsJws(token)
            .getBody();

    // Claims에서 만료 시간을 가져옵니다.
    Date expiration = claims.getExpiration();
    Instant expInstant = expiration.toInstant();

    // Instant를 시스템 기본 시간대의 LocalDateTime으로 변환합니다.
    return LocalDateTime.ofInstant(expInstant, ZoneId.systemDefault());
  }
}
