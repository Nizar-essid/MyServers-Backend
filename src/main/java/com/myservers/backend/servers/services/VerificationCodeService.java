package com.myservers.backend.servers.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.servers.repositories.CodeRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class VerificationCodeService {
    @Autowired
    CodeRepository codeRepository;
    private static final String SECRET_KEY="tSaJMLRUW0ZOmvlmH9eOai1DyIaIGeAWIqEs3lWy9Ghgx7hUNjJ8Iv9JBjEg536O3lwou2SZssZxOFxYP7kSS/XGs0";

    public Long extractCodeID(String token) {
        return Long.valueOf(extractClaim(token, Claims::getSubject));
    }
    public  String generateToken(Long id_code){

        return Jwts.builder()
                .subject(id_code.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000*60*24*30*7))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public<T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims=extractAllClaims(token);
        return  claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) throws AuthenticationException {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();


    }

    private Key getSigningKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String Token,Long code_id){
        Long username=extractCodeID(Token);
//        System.out.println("isTokenValid:"+(username.equals(userDetails.getUsername())&& !isTokenExpired(Token)));

        if (username.intValue()==code_id) {
            return isTokenExpired(Token);
        }
        return false;

    }
    public boolean isTokenExpired(String token){
        //System.out.println("isTokenExpired:"+extractExpiration(token).before(new Date()));
        return extractExpiration(token).before(new Date());

    }

    private Date extractExpiration(String token) {
        return (Date) extractClaim(token,Claims::getExpiration);
    }

    protected Boolean isTokenExpiredV1(String jwt){
        DecodedJWT jwtdecoded = JWT.decode(jwt);
        return jwtdecoded.getExpiresAt().before(new Date());
    }
    public  String generateTokenForCodeValue(String code){

        return Jwts.builder()
                .subject(code)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000*60*60*24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


}
