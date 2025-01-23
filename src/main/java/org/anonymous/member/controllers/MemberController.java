package org.anonymous.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.exceptions.BadRequestException;
import org.anonymous.global.libs.Utils;
import org.anonymous.global.rests.JSONData;
import org.anonymous.member.MemberInfo;
import org.anonymous.member.entities.Member;
import org.anonymous.member.jwt.TokenService;
import org.anonymous.member.repositories.MemberRepository;
import org.anonymous.member.services.MemberDeleteService;
import org.anonymous.member.services.MemberInfoService;
import org.anonymous.member.services.MemberUpdateService;
import org.anonymous.member.validators.JoinValidator;
import org.anonymous.member.validators.LoginValidator;
import org.anonymous.member.validators.UpdateValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 인증/인가/정보수정 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberInfoService memberInfoService;
    @Value("${front.domain}")
    private  String frontDomain;
    private final Utils utils;
    private final TokenService tokenService;
    private final JoinValidator joinValidator;
    private final LoginValidator loginValidator;
    private final UpdateValidator updateValidator;
    private final MemberUpdateService updateService;
    private final MemberRepository memberRepository;
    private final MemberDeleteService memberDeleteService;

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
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public JSONData info(@AuthenticationPrincipal MemberInfo memberInfo) {

        return new JSONData(memberInfo.getMember());
    }

    /**
     * 회원 탈퇴. 진짜 지우는게 아니라 deleteAt 업데이트만 하면 됨.
     * @return
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/delete/{seq}")
    public JSONData delete(@PathVariable Long seq) {
        Member member = memberDeleteService.delete(seq);
        return new JSONData(member);
    }

    /**
     * 회원 정보 수정
     * @return
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/edit")
    public JSONData edit(@RequestBody @Valid RequestUpdate update, Errors errors) {

        update.setMode("edit");
        updateValidator.validate(update, errors);

        if(errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        // 이제 여기 수정 넣으면 됨. 더 추가될거 있으면 추가하면됨.

        return null;
    }

    /**
     * 비밀번호 찾기 후 수정.
     * @param update
     * @return
     */
    @PatchMapping("/password")
    public JSONData password(@RequestBody @Valid RequestUpdate update, Errors errors) {
        update.setMode("change");
        updateValidator.validate(update, errors);

        if(errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        // 수정처리 ㄱㄱ

        return null;
    }

    /*********** 강사님추가 S  *************/
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info/{email}")
    public JSONData info(@PathVariable("email") String email) {
        Member member = null;
        try {
            Long seq = Long.valueOf(email);
            member = memberInfoService.get(seq);
        } catch (Exception e) {
            // 이메일
            member = memberInfoService.get(email);
        }
        return new JSONData(member);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exists/{email}")
    public ResponseEntity<Void> exists(@PathVariable("email") String email) {
        HttpStatus status = memberRepository.exists(email) ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).build();
    }
    /*********** 강사님추가 E  *************/

    // 회원 전용 접근 테스트
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/test")
    public void test(@AuthenticationPrincipal MemberInfo memberInfo) {

        System.out.println(memberInfo);
        System.out.println("회원 전용 URL");
    }
}