package org.anonymous.member.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.global.exceptions.BadRequestException;
import org.anonymous.global.libs.Utils;
import org.anonymous.global.validators.PasswordValidator;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.constants.MemberCondition;
import org.anonymous.member.constants.TokenAction;
import org.anonymous.member.controllers.RequestChangePassword;
import org.anonymous.member.controllers.RequestFindPassword;
import org.anonymous.member.controllers.RequestJoin;
import org.anonymous.member.controllers.RequestUpdate;
import org.anonymous.member.entities.Authorities;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.QAuthorities;
import org.anonymous.member.entities.TempToken;
import org.anonymous.member.exceptions.MemberNotFoundException;
import org.anonymous.member.exceptions.TempTokenNotFoundException;
import org.anonymous.member.libs.MemberUtil;
import org.anonymous.member.repositories.AuthoritiesRepository;
import org.anonymous.member.repositories.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원 가입 & 정보 수정 기능
 *
 */
// @Lazy = 지연 로딩 - 최초로 해당 Bean 사용할 때 생성
@Lazy
@Service
@RequiredArgsConstructor
@Transactional
public class MemberUpdateService implements PasswordValidator {



    private final AuthoritiesRepository authoritiesRepository;
    private final MemberRepository memberRepository;
    private final TempTokenService tempTokenService;
    private final PasswordEncoder passwordEncoder;
    private final MemberUtil memberUtil;

    // ModelMapper
    // 같은 getter setter 처리시 일괄 처리해주는 Reflection API 편의 기능
    private final ModelMapper modelMapper;
    private final Utils utils;

    /**
     * 메서드 오버로드 - 커맨드 객체의 타입에 따라서
     *
     * RequestJoin 이면 회원 가입 처리
     * RequestProfile 이면 회원 정보 수정 처리
     *
     * @param form
     */
    public Member process(RequestJoin form) {

        // 커맨드 객체 -> Entity 객체로 Data 옮기기
        /*
        Member member = new Member();
        member.setEmail(form.getEmail());
        member.setName(form.getName());
        ..
        ...

         */
        Member member = modelMapper.map(form, Member.class);

        // 선택 약관 처리
        List<String> optionalTerms = form.getOptionalTerms();

        // 선택 약관 값이 있을때에만 -> 약관 항목1||약관 항목2||... 형태로 가공 처리
        if (optionalTerms != null) {

            member.setOptionalTerms(String.join("||", optionalTerms));
        }

        // 비밀번호 해시화 - BCrypt (단방성, 유동 해시)
        String hash = passwordEncoder.encode(form.getPassword());
        member.setPassword(hash);
        // ★ 비밀번호 변경 일자 Null 이 아닌 오늘로 설정 ★
        member.setCredentialChangedAt(LocalDateTime.now());

        // 회원 권한 부여
        Authorities auth = new Authorities();
        auth.setMember(member);
        // 처음 가입시 일반 회원(USER)
        auth.setAuthority(Authority.USER);

        member.setMemberCondition(MemberCondition.ACTIVE);

        save(member, List.of(auth)); // 회원 저장 처리

        return member;
    }

    public Member process(RequestUpdate form, List<Authority> authorities) {
        String email = form.getEmail();

/*        Member member = memberUtil.isAdmin() && StringUtils.hasText(email) ? memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email)) : memberUtil.getMember(); // 로그인한 사용자의 정보를 가지고온다.*/
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        String password = form.getPassword();

        if (StringUtils.hasText(password)) {
            String hash = passwordEncoder.encode(password);
            member.setPassword(hash);
            member.setCredentialChangedAt(LocalDateTime.now());
        }
        member.setZipCode(form.getZipCode());
        member.setAddress(form.getAddress());
        member.setAddressSub(form.getAddressSub());
        member.setPhoneNumber(form.getPhoneNumber());
        List<String> optionalTerms = form.getOptionalTerms();
        if (optionalTerms != null) {
            member.setOptionalTerms(String.join("||", optionalTerms));
        }

        List<Authorities> _authorities = null;

        if (authorities != null && memberUtil.isAdmin()) {
            _authorities = authorities.stream().map(a -> {
                Authorities auth = new Authorities();
                auth.setAuthority(a);
                auth.setMember(member);
                return auth;
            }).toList();
        }

        save(member, _authorities);

        return member;
    }

    public Member process(RequestUpdate form) {
        return process(form, null);
    }


    /**
     * 회원 정보 추가 OR 수정 완료 처리
     *
     */
    public void save(Member member, List<Authorities> authorities) {

        memberRepository.saveAndFlush(member);

        /* 회원 권한 업데이트 처리 S */
        // 추후 Builder 로 변경

        if (authorities != null) {
            /*
             * 기존 권한을 삭제하고 다시 등록
             */

            QAuthorities qAuthorities = QAuthorities.authorities;

            List<Authorities> items = (List<Authorities>) authoritiesRepository.findAll(qAuthorities.member.eq(member));

            if (items != null) {

                authoritiesRepository.deleteAll(items);

                authoritiesRepository.flush();
            }

            authoritiesRepository.saveAllAndFlush(authorities);
        }
        /* 회원 권한 업데이트 처리 E */
    }

    /**
     * 회원이 입력한 회원명 + 휴대전화 번호로 회원을 찾고 
     * 가입한 이메일로 비번 변경 가능한 임시 토큰을 발급하고 메일을 전송
     * @param form
     */
    public void issueToken(RequestFindPassword form) {
        String name = form.getName();
        String phoneNumber = form.getPhoneNumber();

        Member member = memberRepository.findByNameAndPhoneNumber(name, phoneNumber).orElseThrow(MemberNotFoundException::new);
        String email = member.getEmail();
        String origin = form.getOrigin();

        TempToken token = tempTokenService.issue(email, TokenAction.PASSWORD_CHANGE, origin); // 토큰발급
        tempTokenService.sendEmail(token.getToken()); // 이메일 전송
    }

    /**
     * 비밀번호 변경
     * @param form
     */
    public void changePassword(RequestChangePassword form) {
        String token = form.getToken();
        String password = form.getPassword();


        TempToken tempToken = tempTokenService.get(token);

        if (tempToken.getAction() != TokenAction.PASSWORD_CHANGE) {
            throw new TempTokenNotFoundException();
        }

        // 비밀번호 자리수 체크

        if (password.length() < 8) {
            throw new BadRequestException(utils.getMessage("Size.requestJoin.password"));
        }

        // 비밀번호 복잡성
        if (!alphaCheck(password, false) || !numberCheck(password) || !specialCharsCheck(password)) {
            throw new BadRequestException(utils.getMessage("Complexity.requestJoin.password"));
        }

        Member member = tempToken.getMember();
        
        String hash = passwordEncoder.encode(password);
        member.setPassword(hash);
        member.setCredentialChangedAt(LocalDateTime.now());
        memberRepository.saveAndFlush(member);
    }
}






















