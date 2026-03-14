package pbl2.sub119.backend.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.auth.constant.JwtConstants;
import pbl2.sub119.backend.common.enumerated.UserRole;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;

    public String createAccessToken(final Long userId, final String email, final UserRole role) {
        final Date now = new Date();
        final Date expiredDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMillis());

        return Jwts.builder()
                .setSubject(email) // 식별자로 이메일 사용
                .claim(JwtConstants.USER_ID, userId)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
