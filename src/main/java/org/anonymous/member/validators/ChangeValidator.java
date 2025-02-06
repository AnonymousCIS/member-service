package org.anonymous.member.validators;

import org.anonymous.global.validators.PasswordValidator;
import org.anonymous.member.controllers.RequestChangePassword;
import org.anonymous.member.controllers.RequestUpdate;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ChangeValidator implements Validator, PasswordValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestChangePassword.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        RequestChangePassword request = (RequestChangePassword) target;

        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (password.length() < 8) {
            errors.rejectValue("password","Size");
            return;
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
    }
}
