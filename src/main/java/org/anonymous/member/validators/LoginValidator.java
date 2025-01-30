package org.anonymous.member.validators;

import lombok.RequiredArgsConstructor;
import org.anonymous.member.constants.MemberCondition;
import org.anonymous.member.controllers.RequestLogin;
import org.anonymous.member.entities.Member;
import org.anonymous.member.repositories.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Token 발급 전 검증
 */
@Lazy
@Component
@RequiredArgsConstructor
public class LoginValidator implements Validator {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestLogin.class);
    }

    /**
     * 1. 회원이 존재 여부 체크
     * 2. 존재하면 비밀번호 일치 여부 체크
     */
    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) return;

        RequestLogin form = (RequestLogin) target;

        String email = form.getEmail();
        String password = form.getPassword();

        Member member = memberRepository.findByEmail(email).orElse(null);

        // 존재하지 않는 회원
        if (member == null) {
            errors.reject("Mismatch.login");
            return;
        }

        // 비밀번호 불일치
        if (!passwordEncoder.matches(password, member.getPassword())) {
            errors.reject("Mismatch.login");
            return;
        }

        if (member.getMemberCondition() == MemberCondition.BLOCK) { // 블락확인.
            errors.reject("Member.blocked");
            return;
        }

        if (member.getDeletedAt() != null) { // 탈퇴된 멤버.
            errors.reject("Failure.disabled.login");
        }

    }
}