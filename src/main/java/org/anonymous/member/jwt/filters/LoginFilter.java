package org.anonymous.member.jwt.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.exceptions.UnAuthorizedException;
import org.anonymous.member.jwt.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFilter extends GenericFilterBean {

    private final TokenService tokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (httpRequest.getRequestURI().startsWith("/swagger-ui") || httpRequest.getRequestURI().startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // Token 이 유입되면 로그인 처리
            tokenService.authenticate((HttpServletRequest) request);

        } catch (UnAuthorizedException e) {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
    }
}
