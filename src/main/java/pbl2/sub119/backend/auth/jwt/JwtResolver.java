package pbl2.sub119.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.auth.enumerated.TokenType;
import pbl2.sub119.backend.common.enumerated.UserRole;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.AuthException;

import static pbl2.sub119.backend.auth.constant.JwtConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtResolver {

    private final JwtProperties jwtProperties;

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get(USER_ID, Long.class);
    }

    public String extractSocialId(String token) {
        return parseClaims(token).getSubject();
    }

    public UserRole extractUserRole(String token) {
        String value = parseClaims(token).get(USER_ROLE, String.class);
        return UserRole.valueOf(value);
    }

    // TODO: 추후 사용
    public TokenType extractTokenType(String token) {
        String value = parseClaims(token).get(TOKEN_TYPE, String.class);
        return TokenType.valueOf(value);
    }

    private Claims parseClaims(String token) {
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
}