package org.anonymous.member.services;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.paging.ListData;
import org.anonymous.global.paging.Pagination;
import org.anonymous.member.MemberInfo;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.controllers.MemberSearch;
import org.anonymous.member.entities.Authorities;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.QMember;
import org.anonymous.member.repositories.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
     * 회원 목록
     * @param search
     * @return
     */
    public ListData<Member> getList(MemberSearch search) {
        int page = Math.max(search.getPage(), 1);
        int limit = search.getLimit();
        limit = limit < 1 ? 20 : limit;
        int offset = (page - 1) * limit;
        QMember member = QMember.member;

        // region 검색 처리

        BooleanBuilder andBuilder = new BooleanBuilder();

        // region 키워드 검색

        String sopt = search.getSopt(); // 검색 옵션
        String skey = search.getSkey(); // 검색 키워드
        sopt = StringUtils.hasText(sopt) ? sopt : "ALL";

        /**
         * sopt - ALL : 통합 검색 - 이메일 + 회원명
         *       NAME : 회원명
         *      EMAIL : 이메일
         */
        if (StringUtils.hasText(skey)) {
            skey = skey.trim();
            StringExpression condition;
            if (sopt.equals("EMAIL")) {
                condition = member.email;
            } else if (sopt.equals("NAME")) {
                condition = member.name;
            } else {
                condition = member.email.concat(member.name);
            }

            andBuilder.and(condition.contains(skey));
        }

        // endregion

        // region 이메일 검색

        List<String> emails = search.getEmail();

        if (emails != null && !emails.isEmpty()) {
            andBuilder.and(member.email.in(emails));
        }

        // endregion

        // region 권한 검색

        List<Authority> authorities = search.getAuthority();
        if (authorities != null && !authorities.isEmpty()) {
            andBuilder.and(member.authorities.any().authority.in(authorities));
        }

        // endregion

        // region 날짜 검색

        String dateType = search.getDateType();
        dateType = StringUtils.hasText(dateType) ? dateType : "createdAt"; // 가입일 기준
        LocalDate sDate = search.getSDate();
        LocalDate eDate = search.getEDate();

        DateTimePath<LocalDateTime> condition;
        if (dateType.equals("deletedAt")) condition = member.deletedAt; // 탈퇴일 기준
        else if (dateType.equals("credentialChangedAt")) condition = member.credentialChangedAt; // 비밀번호 변경 기준
        else condition = member.createdAt; // 가입일 기준

        if (sDate != null) {
            andBuilder.and(condition.after(sDate.atStartOfDay()));
        }

        if (eDate != null) {
            andBuilder.and(condition.before(eDate.atTime(LocalTime.of(23,59,59))));
        }

        // endregion

        // endregion

        List<Member> items = queryFactory.selectFrom(member)
                .leftJoin(member.authorities)
                .fetchJoin()
                .where(andBuilder)
                .orderBy(member.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();

        long total = memberRepository.count(andBuilder); // 총 회원 수
        Pagination pagination = new Pagination(page, (int)total, 10, limit, request);

        return new ListData<>(items, pagination);
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