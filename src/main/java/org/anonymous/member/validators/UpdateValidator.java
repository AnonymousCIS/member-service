package org.anonymous.member.validators;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.validators.PasswordValidator;
import org.anonymous.member.controllers.RequestUpdate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Lazy
@Component
@RequiredArgsConstructor
public class UpdateValidator implements Validator, PasswordValidator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestUpdate.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        RequestUpdate request = (RequestUpdate) target;

        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        String mode = request.getMode();


        /**
         * 비밀번호 수정 할 때 회원정보 수정이면 비밀번호 있는지 확인.
         * 만약 없으면 그냥 return하게 됨.
         * 또한 비밀번호 수정이 아니라 비밀번호 찾기라면 비밀번호를 체크해야함.
         */
        if (mode.equals("edit")) {
            if (!StringUtils.hasText(password)) { // 비밀번호 있는지 확인.
                return;
            }
        }

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
    }
}
