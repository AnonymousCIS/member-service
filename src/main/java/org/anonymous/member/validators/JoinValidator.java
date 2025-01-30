package org.anonymous.member.validators;

import lombok.RequiredArgsConstructor;
import org.anonymous.global.validators.PasswordValidator;
import org.anonymous.member.controllers.RequestJoin;
import org.anonymous.member.repositories.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Lazy
@Component
@RequiredArgsConstructor
public class JoinValidator implements Validator, PasswordValidator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.isAssignableFrom(RequestJoin.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) return;

        RequestJoin form = (RequestJoin) target;

            /**
             * 1. 이메일 중복 여부 체크
             * 1-1. 이메일 인증
             * 2. 비밀번호 복잡성 - 8 ~ 40글자 & 영어 대소문자 각각 1개 이상 & 숫자 1개 이상 & 특수 문자 포함 필수 (global_validators : interface - 비회원 이용등 다른 곳에서도 사용하기 때문에)
             * 3. 비밀번호 - 비밀번호 확인 일치 여부
             */

            String email = form.getEmail();
            String password = form.getPassword();
            String confirmPassword = form.getConfirmPassword();

            // 1. 이메일 중복 여부 체크 S

            if (memberRepository.exists(email)) {

                errors.rejectValue("email", "Duplicated");
            }

            // 1. 이메일 중복 여부 체크 E

            // 2. 비밀번호 복잡성 S

            if (!alphaCheck(password, false) || !numberCheck(password) || !specialCharsCheck(password)) {

                errors.rejectValue("password", "Complexity");
            }

            // 2. 비밀번호 복잡성 E

            // 3. 비밀번호, 비밀번호 확인 일치 여부 S

            if (!password.equals(confirmPassword)) {

                errors.rejectValue("confirmPassword", "Mismatch");
            }
            // 3. 비밀번호, 비밀번호 확인 일치 여부 E
    }
}
