package org.anonymous.member.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Value("${front.domain}")
    private String frontDomain;

    private final Utils utils;
    private final TokenService tokenService;
    private final JoinValidator joinValidator;
    private final LoginValidator loginValidator;
    private final UpdateValidator updateValidator;
    private final MemberUpdateService updateService;
    private final MemberRepository memberRepository;
    private final MemberInfoService memberInfoService;
    private final MemberDeleteService memberDeleteService;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED) // 201
    @Operation(summary = "회원 가입", method = "POST")
    @ApiResponse(responseCode = "201", description = "회원가입 성공 시 201")
    @Parameters({
            @Parameter(name = "email", required = true, description = "이메일"),
            @Parameter(name = "name", required = true, description = "이름"),
            @Parameter(name = "password", required = true, description = "비밀번호"),
            @Parameter(name = "confirmPassword", required = true, description = "비밀번호 확인"),
            @Parameter(name = "zipCode", required = true, description = "우편번호"),
            @Parameter(name = "address", required = true, description = "집주소"),
            @Parameter(name = "addressSub", description = "나머지주소"),
            @Parameter(name = "phoneNumber", required = true, description = "휴대폰번호"),
            @Parameter(name = "requiredTerms1", required = true, description = "필수 약관 동의 여부1"),
            @Parameter(name = "requiredTerms2", required = true, description = "필수 약관 동의 여부2"),
            @Parameter(name = "requiredTerms3", required = true, description = "필수 약관 동의 여부3"),
            @Parameter(name = "optionalTerms", description = "선택 약관 동의 여부"),
            @Parameter(name = "gender", required = true, description = "성별"),
            @Parameter(name = "birthDt", required = true, description = "생년월일", examples =  @ExampleObject(name="birthDt", value = "1994-01-04")),
    })
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
    @Operation(summary = "인증 및 토큰 발급 후 로그인", method = "POST")
    @ApiResponse(responseCode = "200", description = "로그인 성공 시 200")
    @Parameters({
            @Parameter(name = "email", required = true, description = "이메일"),
            @Parameter(name = "password", required = true, description = "비밀번호"),
    })
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
    @Operation(summary = "인증(로그인)한 회원 정보 조회", method = "GET")
    @ApiResponse(responseCode = "200", description = "로그인 한 회원 정보 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public JSONData info(@AuthenticationPrincipal MemberInfo memberInfo) {

        return new JSONData(memberInfo.getMember());
    }

    /**
     * 회원 탈퇴. 진짜 지우는게 아니라 deleteAt 업데이트만 하면 됨.
     * @return
     */
    @Operation(summary = "회원 탈퇴", method = "PATCH")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴. 실제로 DB 내에서는 지워지는게 아님.")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/delete/{seq}")
    public JSONData delete(@PathVariable Long seq) {
        Member member = memberDeleteService.userDelete(seq);
        return new JSONData(member);
    }

    /**
     * 회원 정보 수정
     * @return
     */
    @Operation(summary = "회원 정보 수정", method = "Patch")
    @ApiResponse(responseCode = "200", description = "수정 완료 됬을 시 200")
    @Parameters({
            @Parameter(name = "email", required = true, description = "이메일"),
            @Parameter(name = "password", description = "비밀번호"),
            @Parameter(name = "confirmPassword", description = "비밀번호 확인"),
            @Parameter(name = "zipCode", required = true, description = "우편번호"),
            @Parameter(name = "address", required = true, description = "집주소"),
            @Parameter(name = "addressSub", description = "나머지주소"),
            @Parameter(name = "phoneNumber", required = true, description = "휴대폰번호"),
            @Parameter(name = "optionalTerms", description = "선택 약관 동의 여부"),
            @Parameter(name = "authorities", description = "유저 권한.", examples = @ExampleObject(
                    name="authorities", value = "{USER,ADMIN}"
            )),
            @Parameter(name = "mode", description = "edit", examples = @ExampleObject(
                    name="mode", value = "edit", description = "수정이면 edit, 패스워드 찾기면 password")),
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/edit")
    public JSONData edit(@RequestBody @Valid RequestUpdate update, Errors errors) {

        updateValidator.validate(update, errors);

        if(errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        Member member = updateService.process(update);


        return new JSONData(member);
    }

    // region 사용하지 않는 코드

//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @GetMapping("/send/{to}")
//    public void passwordSend(@PathVariable("to") String to) {
//        if (!memberPasswordSendService.sendEmail(to)) {
//            throw new BadRequestException(utils.getMessage("Member.password.send"));
//        }
//    }
//
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @GetMapping("/verify")
//    public void passwordVerify(@RequestParam(name="authCode", required = false) Integer authCode) {
//        if (!memberPasswordSendService.sendVerify(authCode)) {
//            throw new BadRequestException(utils.getMessage("Member.password.verify"));
//        }
//    }
//
//    /**
//     * 비밀번호 찾기 후 수정.
//     * @param update
//     * @return
//     */
//    @Operation(summary = "회원 정보 수정", method = "PATCH")
//    @ApiResponse(responseCode = "200", description = "수정 완료 됬을 시 200")
//    @Parameters({
//            @Parameter(name = "email", required = true, description = "이메일"),
//            @Parameter(name = "password", required = true, description = "비밀번호"),
//            @Parameter(name = "confirmPassword", required = true, description = "비밀번호 확인"),
//            @Parameter(name = "mode", description = "password", examples = @ExampleObject(
//                    name="mode", value = "edit", description = "수정이면 edit, 패스워드 찾기면 password")),
//    })
//    @PatchMapping("/password")
//    public JSONData password(@RequestBody @Valid RequestPassword update, Errors errors) {
//        passwordValidator.validate(update, errors);
//
//        RequestUpdate requestUpdate = modelMapper.map(update, RequestUpdate.class);
//        if(errors.hasErrors()) {
//            throw new BadRequestException(utils.getErrorMessages(errors));
//        }
//
//        Member member = updateService.process(requestUpdate);
//
//        return new JSONData(member);
//    }

    // endregion

    /*********** 강사님추가 S  *************/
    @Operation(summary = "회원 조회", method = "GET")
    @ApiResponse(responseCode = "200", description = "회원 조회, Member 객체를 return 해준다.")
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

    @Operation(summary = "회원 유무 판단", method = "GET")
    @ApiResponse(responseCode = "200", description = "회원 유무 판단, boolean 값으로 return 해준다.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exists/{email}")
    public ResponseEntity<Void> exists(@PathVariable("email") String email) {
        HttpStatus status = memberRepository.exists(email) ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).build();
    }
    /*********** 강사님추가 E  *************/


    @Operation(summary = "비밀번호 찾기", method = "GET" , description = "이메일로 비밀번호 변경 url을 토큰 실어서 전달해준다.")
    @ApiResponse(responseCode = "200", description = "회원 유무 판단, boolean 값으로 return 해준다.")
    @Parameters({
            @Parameter(name="name", description = "회원명", example = "이이름"),
            @Parameter(name="phoneNumber", description = "휴대전화번호", example = "010-1234-5678"),
            @Parameter(name="origin", description = "프론트엔드 주소", example = "https://pintech.onedu.blue"),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/password/find")
    public void findPassword(@RequestBody @Valid RequestFindPassword form, Errors errors) {
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        updateService.issueToken(form);
    }

    /**
     *
     * @param form
     * @param errors
     */
    @Operation(summary = "비밀번호 변경", method = "GET" , description = "비밀번호를 변경한다.")
    @ApiResponse(responseCode = "200", description = "회원 유무 판단, boolean 값으로 return 해준다.")
    @Parameters({
            @Parameter(name="token", description = "토큰 값"),
            @Parameter(name="password", description = "변경할 비밀번호"),
            @Parameter(name="confirmPassword", description = "변경할 비밀번호 확인"),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/password/change")
    public void changePassword(@RequestBody @Valid RequestChangePassword form, Errors errors) {
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        updateService.changePassword(form);
    }

//    // 회원 전용 접근 테스트
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/test")
//    public void test(@AuthenticationPrincipal MemberInfo memberInfo) {
//
//        System.out.println(memberInfo);
//        System.out.println("회원 전용 URL");
//    }
}