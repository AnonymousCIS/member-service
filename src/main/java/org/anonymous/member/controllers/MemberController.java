package org.anonymous.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.exceptions.BadRequestException;
import org.anonymous.global.libs.Utils;
import org.anonymous.global.rests.JSONData;
import org.anonymous.member.MemberInfo;
import org.anonymous.member.jwt.TokenService;
import org.anonymous.member.services.MemberUpdateService;
import org.anonymous.member.validators.JoinValidator;
import org.anonymous.member.validators.LoginValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 인증/인가 API")
@RestController
@RequiredArgsConstructor
public class MemberController { ////

    @Value("${front.domain}")
    private  String frontDomain;

    private final Utils utils;

    private final TokenService tokenService;

    private final JoinValidator joinValidator;

    private final LoginValidator loginValidator;

    private final MemberUpdateService updateService;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED) // 201
    public void join(@RequestBody @Valid RequestJoin form, Errors errors) {

        joinValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        updateService.process(form);
    }

    /**
     * 로그인 성공시 Token 발급
     *
     * @param form
     * @param errors
     */
    @PostMapping("/login")
    public JSONData login(@RequestBody @Valid RequestLogin form, Errors errors, HttpServletResponse response) {

        loginValidator.validate(form, errors);

        if (errors.hasErrors()) throw new BadRequestException(utils.getErrorMessages(errors));

        String email = form.getEmail();
        String token = tokenService.create(email);

        if (StringUtils.hasText(frontDomain)) {

            String[] domains = frontDomain.split(",");

            for (String domain : domains) {

                /*
                Cookie cookie = new Cookie("token", token);

                // 전체 경로 가능
                cookie.setPath("/");
                cookie.setDomain(domain);
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
                 */

                // SameSite 정책 None = 다른 서버에서도 쿠키 설정 가능, Https 필수!
                response.setHeader("Set-Cookie", String.format("token=%s; Path=/; Domain=%s; Secure; HttpOnly; SameSite=None", token, domain));
            }
        }

        return new JSONData(token);
    }

    /**
     * 로그인한 회원정보 조회
     *
     * @return
     */
    @GetMapping("/")
    public JSONData info(@AuthenticationPrincipal MemberInfo memberInfo) {

        return new JSONData(memberInfo.getMember());
    }

    // 회원 전용 접근 테스트
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/test")
    public void test(@AuthenticationPrincipal MemberInfo memberInfo) {

        System.out.println(memberInfo);
        System.out.println("회원 전용 URL");
    }
}