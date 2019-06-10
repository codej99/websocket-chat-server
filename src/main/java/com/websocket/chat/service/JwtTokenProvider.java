package com.websocket.chat.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

    public String generateToken(String userId, String nickname) {
        Claims claims = Jwts.claims().setId(userId);
        claims.put("nickname", nickname);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // 데이터
                .setIssuedAt(now) // 토큰 발행일자
                .setExpiration(new Date(now.getTime() + tokenValidMilisecond)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
                .compact();
    }

    public String validateToken(String jwt) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt); // jwt 복호화
        if (claims.getBody().getExpiration().before(new Date())) // jwt 유효시간 체크
            return null;

        return claims.getBody().get("nickname", String.class);
    }
}
