package pbl2.sub119.backend.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pbl2.sub119.backend.common.enumerated.UserRole;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.AuthException;

import static pbl2.sub119.backend.auth.constant.JwtConstants.*;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtResolver jwtResolver;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        final String token = extractToken(request);

        if (token == null) {
            setGuestAttributes(request);
            return true;
        }

        if (!jwtResolver.isValid(token)) {
            throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        setUserAttributes(request, token);
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX_LENGTH);
    }

    private void setUserAttributes(HttpServletRequest request, String token) {
        request.setAttribute(REQUEST_ATTR_USER_ID, jwtResolver.extractUserId(token));
        request.setAttribute(REQUEST_ATTR_EMAIL, jwtResolver.extractSocialId(token));
        request.setAttribute(REQUEST_ATTR_USER_ROLE, jwtResolver.extractUserRole(token));
    }

    private void setGuestAttributes(HttpServletRequest request) {
        request.setAttribute(REQUEST_ATTR_USER_ROLE, UserRole.GUEST);
    }
}