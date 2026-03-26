package pbl2.sub119.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.auth.constant.JwtConstants;
import pbl2.sub119.backend.common.enumerated.UserRole;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.AuthException;

@Component
@RequiredArgsConstructor
public class JwtResolver {
    private final JwtProperties jwtProperties;

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new AuthException(ErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get(JwtConstants.USER_ID, Long.class);
    }

    public String extractSocialId(String token) {
        return parseClaims(token).getSubject();
    }

    public UserRole extractRole(String token) {
        try {
            final String role = parseClaims(token).get(JwtConstants.USER_ROLE, String.class);
            if (role == null) {
                throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
            }
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (AuthException e) {
            return false;
        }
    }
}