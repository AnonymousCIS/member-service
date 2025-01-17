package org.anonymous.member.services;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.anonymous.member.MemberInfo;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.entities.Authorities;
import org.anonymous.member.entities.Member;
import org.anonymous.member.repositories.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 조회 기능
 *
 * UserDetailsService & UserDetailService
 *
 * UserDetailsService
 *
 */
@Lazy // 순환 참조 방지용
@Service
@Transactional
@RequiredArgsConstructor
public class MemberInfoService implements UserDetailsService {

    // 회원 조회 위해 DB
    private final MemberRepository memberRepository;

    private final JPAQueryFactory queryFactory;

    private final HttpServletRequest request;

    private final ModelMapper modelMapper;

    // 회원 조회해서 UserDetails 구현체로 완성해 반환값 내보냄
    // 회원 정보가 필요할때마다 호출됨
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));

        List<Authorities> items = member.getAuthorities();

        if (items == null) {
            // 권한이 null 일땐 기본 권한인 USER 값
            Authorities auth = new Authorities();

            auth.setMember(member);
            auth.setAuthority(Authority.USER);

            items = List.of(auth);
        }

        // private Collection<? extends GrantedAuthority> authorities;이므로 stream 이용해 문자열로 변환
        // 무조건 문자열이어야함
        List<SimpleGrantedAuthority> authorities = items.stream().map(a -> new SimpleGrantedAuthority(a.getAuthority().name())).toList();

        // 추가 정보 처리 (2차 가공)
        addInfo(member);

        return MemberInfo.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .member(member)
                .authorities(authorities)
                .build();
    }

    public UserDetails loadUserBySeq(Long seq)

    {
        Member member = memberRepository.findById(seq).orElse(null);

        List<Authorities> items = member.getAuthorities();

        if (items == null) {
            // 권한이 null 일땐 기본 권한인 USER 값
            Authorities auth = new Authorities();

            auth.setMember(member);
            auth.setAuthority(Authority.USER);

            items = List.of(auth);
        }

        // private Collection<? extends GrantedAuthority> authorities;이므로 stream 이용해 문자열로 변환
        // 무조건 문자열이어야함
        List<SimpleGrantedAuthority> authorities = items.stream().map(a -> new SimpleGrantedAuthority(a.getAuthority().name())).toList();

        // 추가 정보 처리 (2차 가공)
        addInfo(member);

        return MemberInfo.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .member(member)
                .authorities(authorities)
                .build();
    }

    /**
     * email 로 회원 조회
     *
     * @param email
     * @return
     */
    public Member get(String email) {
        MemberInfo memberInfo = (MemberInfo)loadUserByUsername(email);

        return memberInfo.getMember();
    }
    /**
     * 추가 정보 처리 (2차 가공)
     *
     * @param member
     */
    public void addInfo(Member member) {

//        List<FileInfo> files = fileInfoService.getList(member.getEmail(), "profile");
//
//        if (files != null && !files.isEmpty()) {
//
//            member.setProfileImage(files.get(0));
//        }
    }
}