package org.anonymous.member.annotations;

import org.anonymous.member.constants.Authority;
import org.anonymous.member.entities.Authorities;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockSecurityContextFactory.class)
public @interface MockMember {
    long seq() default 10L;
    String email() default "user01@test.org";
    String name() default "사용자01";
    Authority[] authority() default {Authority.USER};
}
