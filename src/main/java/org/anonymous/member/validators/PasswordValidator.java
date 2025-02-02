package org.anonymous.member.validators;

import lombok.RequiredArgsConstructor;
import org.anonymous.global.libs.Utils;
import org.anonymous.member.controllers.RequestPassword;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Lazy
@Component
@RequiredArgsConstructor
public class PasswordValidator implements Validator, org.anonymous.global.validators.PasswordValidator {

    private final Utils utils;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestPassword.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if(errors.hasErrors()) {
            return;
        }

        RequestPassword request = (RequestPassword) target;
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        Integer code = Integer.valueOf(request.getCode());

        if (password.length() < 8) {
            errors.rejectValue("password","Size");
        }

        if (!StringUtils.hasText(confirmPassword)) { // 비밀번호 확인이 있는지 확인.
            errors.rejectValue("confirmPassword","NotBlank");
            return;
        }

        // region 1. 비밀번호 복잡성

        if (!alphaCheck(password, true) || !numberCheck(password) || !specialCharsCheck(password)) {
            errors.rejectValue("password", "Complexity");
        }

        // endregion

        // region 2. 비밀번호, 비밀번호 확인 일치 여부

        if (!password.equals(confirmPassword)) {
            errors.rejectValue("confirmPassword","Mismatch");
        }

        // endregion

        // region 3. 확실하게 인증코드 발급 후 들어온건지 확인.

        if (utils.getValue(utils.getUserHash() + "_password") == null && !utils.getValue(utils.getUserHash() + "_password").equals(code)) {
            System.out.println("틀렸습니다.");
            errors.rejectValue("confirmPassword","Mismatch");
        }

        // endregion
    }
}
