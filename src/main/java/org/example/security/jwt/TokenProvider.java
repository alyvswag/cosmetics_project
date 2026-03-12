package org.example.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.model.dao.Users;
import org.example.security.data.SecurityProperties;
import org.example.utils.PublicPrivateKeyUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.example.constant.TokenConstants.USERNAME_KEY;

@Component
public class TokenProvider implements TokenService<Users, Claims> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TokenProvider.class);
    final SecurityProperties securityProperties;

    public TokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public List<String> generate(Users obj) {
        List<String> tokens = new ArrayList<>();
        tokens.add(0, generateAccessToken(obj));
        tokens.add(1, generateRefreshToken(obj));
        return tokens;
    }

    @Override
    public Claims read(String token) { // access yoxsa refresh ikisini də edir
        return Jwts.parserBuilder()
                .setSigningKey(PublicPrivateKeyUtils.getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String getUsername(String token) {
        return read(token).get(USERNAME_KEY, String.class);
    }

    private String generateAccessToken(Users users) {
        Claims claims = Jwts.claims();
        claims.put(USERNAME_KEY, users.getUsername());

        Date now = new Date();
        Date exp = new Date(now.getTime() + securityProperties.getJwt().getAccessTokenValidityTime());

        return Jwts.builder()
                .setSubject(String.valueOf(users.getId())) //id vurulur
                .setIssuedAt(now)
                .setExpiration(exp)
                .addClaims(claims)
                .signWith(PublicPrivateKeyUtils.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    private String generateRefreshToken(Users users) {
        Claims claims = Jwts.claims();
        claims.put(USERNAME_KEY, users.getUsername());

        Date now = new Date();
        Date exp = new Date(now.getTime() + securityProperties.getJwt().getRefreshTokenValidityTime());

        return Jwts.builder()
                .setSubject(String.valueOf(users.getId()))
                .setIssuedAt(now)
                .setExpiration(exp)
                .addClaims(claims)
                .signWith(PublicPrivateKeyUtils.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }
}