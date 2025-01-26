package org.anonymous.member.annotations;

import org.anonymous.member.MemberInfo;
import org.anonymous.member.entities.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.List;

public class MockSecurityContextFactory implements WithSecurityContextFactory<MockMember> {
    @Override
    public SecurityContext createSecurityContext(MockMember annotation) {
        Member member = new Member();
        member.setEmail(annotation.email());
        member.setSeq(annotation.seq());
        member.setName(annotation.name());
/*        member.setAuthorities(Arrays.stream(annotation.authority()))*/
        MemberInfo _member = MemberInfo.builder().member(member)._authorities(List.of(annotation.authority()))
                .email(member.getEmail()).build();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(member, null, _member.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication); // 로그인 처리

        return context;
    }
}
