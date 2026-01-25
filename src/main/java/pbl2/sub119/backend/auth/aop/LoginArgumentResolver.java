package pbl2.sub119.backend.auth.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import pbl2.sub119.backend.auth.constant.JwtConstants;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.AuthException;

@Component
@RequiredArgsConstructor
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class)
                && parameter.getParameterType().equals(Accessor.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        Long userId = (Long) request.getAttribute(JwtConstants.REQUEST_ATTR_USER_ID);
        String email = (String) request.getAttribute(JwtConstants.REQUEST_ATTR_EMAIL);
        String role = (String) request.getAttribute(JwtConstants.REQUEST_ATTR_USER_ROLE);

        if (userId == null) {
            throw new AuthException(ErrorCode.AUTH_USER_NOT_FOUND);
        }

        return Accessor.user(userId, email, role);
    }
}