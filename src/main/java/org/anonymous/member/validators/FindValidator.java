package org.anonymous.member.validators;

import org.anonymous.global.validators.MobileValidator;
import org.anonymous.member.controllers.RequestFindPassword;
import org.anonymous.member.controllers.RequestJoin;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class FindValidator implements Validator, MobileValidator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestFindPassword.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) return;

        RequestFindPassword form = (RequestFindPassword) target;

        String phoneNumber = form.getPhoneNumber();

        if (!checkMobile(phoneNumber)) {
            errors.rejectValue("phoneNumber", "Complexity");
        }
    }
}
