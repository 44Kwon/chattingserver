package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
// jwt 토큰 생성해주는 클래스
public class JwtTokenProvider {

    private final String secretKey;
    private final int expiration;
    private Key SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, @Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);    //페이로드
        claims.put("role", role);
        Date now = new Date();  //현재시간
        String token = Jwts.builder()   //토큰생성
                .setClaims(claims)
                .setIssuedAt(now)   //발행일자
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))   //만료일자
                .signWith(SECRET_KEY)   //암호화
                .compact();
        return token;
    }
}
