package com.myservers.backend.security.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.security.auth.dataTypes.AuthenticationResponse;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Autowired
    UserRepository userRepository;
    private static final String SECRET_KEY="tSaJMLRUW0ZOmvlmH9eOai1DyIaIGeAWIqEs3lWy9Ghgx7hUNjJ8Iv9JBjEg536O3lwou2SZssZxOFxYP7kSS/XGs0";

    public String extractUserEmail(String token) {
        return String.valueOf(extractClaim(token, Claims::getSubject));
    }
    public  String generateToken(UserDetails userDetails){
        return generateToken( new HashMap<>(), userDetails);
    }
    public  String generateToken(Map<String,Object> extraClaims, UserDetails userDetails){
       return Jwts.builder()
               .claims(extraClaims)
               .subject(userDetails.getUsername())
               .issuedAt(new Date(System.currentTimeMillis()))
               .expiration(new Date(System.currentTimeMillis()+1000*60*24*30*3))
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

    public boolean isTokenValid(String Token,UserDetails userDetails){
        String username=extractUserEmail(Token);
//        System.out.println("isTokenValid:"+(username.equals(userDetails.getUsername())&& !isTokenExpired(Token)));

        return(username.equals(userDetails.getUsername())&& !isTokenExpired(Token));

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
        if( jwtdecoded.getExpiresAt().before(new Date())) {
          return true;
        }
        return  false;
    }


    public User getUser() {
        return (User) userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()-> new ApiRequestException("user not exist",HttpStatus.NOT_FOUND));
    }

    public Admin getAdmin() {
        return (Admin) userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()-> new ApiRequestException("admin not exist",HttpStatus.NOT_FOUND));
    }

    public String generatePasswordResetToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // Token valid for 15 minutes
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
