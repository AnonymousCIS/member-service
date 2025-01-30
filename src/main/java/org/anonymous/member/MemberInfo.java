package org.anonymous.member;

import lombok.Builder;
import lombok.Data;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.entities.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * UserDetails Interface - DTO
 *
 * 구현체
 *
 */
@Data
@Builder
public class MemberInfo implements UserDetails {

    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Member member;
    private List<Authority> _authorities;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // 인가 기능(페이지 접근 제한)
        return authorities;
    }

    @Override
    public String getPassword() {

        return password;
    }

    @Override
    public String getUsername() {

        return email;
    }

    // Account 만료 여부 -> 휴면여부 체크 하면 됨
    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    // Account 잠김 여부 -> deleteAt 체크 하면 됨.
    @Override
    public boolean isAccountNonLocked() {

        return member.getDeletedAt() != null;
    }

    // 비밀번호 만료 여부
    // EX) 일정 기간 지나면 비밀번호 변경 팝업
    @Override
    public boolean isCredentialsNonExpired() {

        LocalDateTime credentialChangedAt = member.getCredentialChangedAt();
        // ★ 비밀번호 변경 일시가 Null 이 아니고 30일이 지나지 않았을 경우 이용 가능
        // 아닐 경우 LoginFailureHandler 에서 비밀번호 변경 주소로 이동 처리 ★
        return credentialChangedAt != null &&
                credentialChangedAt.isAfter(LocalDateTime.now().minusMonths(1L));
    }

    // False 시 탈퇴한 회원
    @Override
    public boolean isEnabled() {

        return member.getDeletedAt() == null;
    }
}