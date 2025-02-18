package org.anonymous.member.libs;

import org.anonymous.member.MemberInfo;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.entities.Member;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MemberUtil {

    public boolean isLogin() {

        return getMember() != null;
    }

    // 관리자 여부 체크
    public boolean isAdmin() {

        return isLogin() && getMember().get_authorities().stream().anyMatch(a -> a == Authority.ADMIN);
    }

    // 로그인 회원 정보 조회
    public Member getMember() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MemberInfo) {

            MemberInfo memberInfo = (MemberInfo) authentication.getPrincipal();
            return memberInfo.getMember();
        }

        return null;
    }
}
