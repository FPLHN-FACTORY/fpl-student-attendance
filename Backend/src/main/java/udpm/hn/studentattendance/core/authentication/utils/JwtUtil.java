package udpm.hn.studentattendance.core.authentication.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JwtUtil {

    @Value("${authentication.secret-key}")
    private String SECRET_KEY;

    @Value("${app.config.auth.expiration}")
    private long EXPIRATION_TIME;

    public static String getAuthorization(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    public SecretKey getSecretKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        if (token == null) {
            return false;
        }
        try {
            Jws<Claims> claims = getClaimsFromToken(token);
            Date expirationDate = claims.getBody().getExpiration();
            return !expirationDate.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(String email, AuthUser oauthUser) {
        Map<String, Object> dataUser = new HashMap<>();
        dataUser.put("id", oauthUser.getId());
        dataUser.put("role", oauthUser.getRole());
        dataUser.put("facilityID", oauthUser.getIdFacility());
        dataUser.put("name", oauthUser.getName());
        dataUser.put("code", oauthUser.getCode());
        dataUser.put("picture", oauthUser.getPicture());
        return buildToken(email, dataUser);
    }

    public String generateToken(String token) {
        Claims claims = getClaimsFromToken(token).getBody();
        return buildToken(claims);
    }

    private String buildToken(String email, Map<String, Object> data) {
        return Jwts.builder()
                .setSubject(email)
                .addClaims(data)
                .setIssuedAt(new Date())
                .setExpiration(buildExpiration())
                .signWith(getSecretKey())
                .compact();
    }

    private String buildToken(Claims claims) {
        return buildToken(claims, buildExpiration());
    }

    private String buildToken(Claims claims, Date expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(String token) {
        Jws<Claims> claimsJws = getClaimsFromToken(token);
        Date expirationDate = claimsJws.getBody().getExpiration();
        Instant newExpiration = expirationDate.toInstant().plus(Duration.ofMinutes(1));
        Date updatedExpiration = Date.from(newExpiration);
        return buildToken(claimsJws.getBody(), updatedExpiration);
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKey())
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Jws<Claims> getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token);
    }

    public Set<String> getRoleFromToken(String token) {
        return new HashSet<>(getClaimsFromToken(token).getBody().get("role", List.class));
    }

    public String getFacilityFromToken(String token) {
        return getClaimsFromToken(token).getBody().get("facilityID", String.class);
    }

    private Date buildExpiration() {
        return new Date(System.currentTimeMillis() + EXPIRATION_TIME * 60 * 1000);
    }

}
