package com.websocket.chat.service;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    private long tokenValidMilisecond = 1000L * 60 * 60; // 1시간만 토큰 유효

    /**
     * 이름으로 Jwt Token을 생성한다.
     */
    public String generateToken(String name) {
        Date now = new Date();
        return Jwts.builder()
                .setId(name)
                .setIssuedAt(now) // 토큰 발행일자
                .setExpiration(new Date(now.getTime() + tokenValidMilisecond)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
                .compact();
    }

    /**
     * Jwt Token을 복호화 하여 이름을 얻는다.
     */
    public String getUserNameFromJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getId();
    }

    /**
     * Jwt Token의 유효성을 체크한다.
     */
    public boolean validateToken(String jwt) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}
