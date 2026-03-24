package pbl2.sub119.backend.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pbl2.sub119.backend.auth.constant.JwtConstants;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.AuthException;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtResolver jwtResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) return true;

        if (!jwtResolver.isValid(token)) {
            throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        request.setAttribute(JwtConstants.REQUEST_ATTR_USER_ID, jwtResolver.extractUserId(token));
        request.setAttribute(JwtConstants.REQUEST_ATTR_EMAIL, jwtResolver.extractEmail(token));
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(JwtConstants.AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(JwtConstants.BEARER_PREFIX)) {
            return null;
        }
        return header.substring(JwtConstants.BEARER_PREFIX_LENGTH);
    }
}