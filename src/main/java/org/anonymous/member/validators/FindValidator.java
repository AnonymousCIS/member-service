package org.anonymous.member.validators;

import lombok.RequiredArgsConstructor;
import org.anonymous.global.validators.MobileValidator;
import org.anonymous.member.controllers.RequestFindPassword;
import org.anonymous.member.controllers.RequestJoin;
import org.anonymous.member.repositories.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Lazy
@Component
@RequiredArgsConstructor
public class FindValidator implements Validator, MobileValidator {


    private final MemberRepository repository;

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
