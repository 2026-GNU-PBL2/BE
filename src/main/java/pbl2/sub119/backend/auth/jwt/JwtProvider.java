package pbl2.sub119.backend.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.auth.enumerated.TokenType;
import pbl2.sub119.backend.common.enumerated.UserRole;

import java.util.Date;

import static pbl2.sub119.backend.auth.constant.JwtConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(final Long userId, final String socialId, final UserRole userRole) {
        return createToken(
                userId,
                socialId,
                jwtProperties.getAccessTokenExpirationMillis(),
                TokenType.ACCESS_TOKEN,
                userRole
        );
    }

    // TODO: 추후 사용
    public String createRefreshToken(final Long userId, final String socialId, final UserRole userRole) {
        return createToken(
                userId,
                socialId,
                jwtProperties.getAccessTokenExpirationMillis(),
                TokenType.REFRESH_TOKEN,
                userRole
        );
    }

    private String createToken(
            final Long userId,
            final String socialId,
            final long expirationMillis,
            final TokenType tokenType,
            final UserRole userRole
    ) {
        final Date now = new Date();
        final Date expiredDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(socialId)
                .claim(USER_ID, userId)
                .claim(USER_ROLE, userRole.name())
                .claim(TOKEN_TYPE, tokenType.name())
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}