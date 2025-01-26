package org.anonymous.member.services;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.paging.ListData;
import org.anonymous.global.paging.Pagination;
import org.anonymous.member.constants.MemberDomainStatus;
import org.anonymous.member.controllers.MemberStatusSearch;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.MemberStatus;
import org.anonymous.member.entities.QMemberStatus;
import org.anonymous.member.repositories.MemberStatusRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberStatusInfoService {

    private final HttpServletRequest request;
    private final JPAQueryFactory queryFactory;
    private final MemberInfoService memberInfoService;
    private final MemberStatusRepository memberStatusRepository;


    public List<MemberStatus> status(String email) {
        Member member = memberInfoService.get(email);
        QMemberStatus qMemberStatus = QMemberStatus.memberStatus1;

        return queryFactory.selectFrom(qMemberStatus)
                .where(qMemberStatus.member.in(member))
                .fetch();
    }

    public ListData<MemberStatus> statuses(MemberStatusSearch search) {
        int page = Math.max(search.getPage(), 1);
        int limit = search.getLimit();
        limit = limit < 1 ? 20 : limit;
        int offset = (page - 1) * limit;

        QMemberStatus qMemberStatus = QMemberStatus.memberStatus1;

        BooleanBuilder andBuilder = new BooleanBuilder();

        // region 키워드 검색

        String sopt = search.getSopt(); // 검색 옵션
        String skey = search.getSkey(); // 검색 키워드
        sopt = StringUtils.hasText(sopt) ? sopt : "ALL";

        /**
         * sopt - ALL : 통합 검색 - 이메일 + 회원명 + 닉네임
         *       NAME : 회원명 + 닉네임
         *      EMAIL : 이메일
         */
        if (StringUtils.hasText(skey)) {
            skey = skey.trim();
            StringExpression condition;
            if (sopt.equals("EMAIL")) {
                condition = qMemberStatus.member.email;
            } else if (sopt.equals("TYPE")) {
                condition = qMemberStatus.type;
            } else {
                condition = qMemberStatus.member.name.concat(qMemberStatus.type);
            }

            andBuilder.and(condition.contains(skey));
        }

        // endregion

        // region 이메일 검색

        List<String> emails = search.getEmail();

        if (emails != null && !emails.isEmpty()) {
            andBuilder.and(qMemberStatus.member.email.in(emails));
        }

        // endregion

        // region 도메인 검색

        List<String> types = search.getType();
        if (types != null && !types.isEmpty()) {
            andBuilder.and(qMemberStatus.type.in(types));
        }

        // endregion

        // region block 검색

        List<MemberDomainStatus> domainStatuses = search.getDomainStatuses();
        if (domainStatuses != null && !domainStatuses.isEmpty()) {
            andBuilder.and(qMemberStatus.memberStatus.in(domainStatuses));
        }

        // endregion

        // region 날짜 검색

/*        String dateType = search.getDateType();
        dateType = StringUtils.hasText(dateType) ? dateType : "createdAt"; // 블락당한 생성날짜 기준 근데 무조건 이것밖에 없을듯?* 근데 사용 안하네..*/
        LocalDate sDate = search.getSDate();
        LocalDate eDate = search.getEDate();

        DateTimePath<LocalDateTime> condition = qMemberStatus.createdAt; // 생성일 기준. 무조건 이걸로 통할거같음.

        if (sDate == null) { // 만약 날짜가 없다라는 가정이라면
            Date epochDate = new Date(0L);
            sDate = epochDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }

        if (eDate == null) {
            eDate = LocalDate.now();
        }

        andBuilder.and(condition.after(sDate.atStartOfDay()));
        andBuilder.and(condition.before(eDate.atTime(LocalTime.of(23,59,59))));

        List<MemberStatus> items = queryFactory.selectFrom(qMemberStatus)
                .leftJoin(qMemberStatus.member)
                .fetchJoin()
                .where(andBuilder)
                .offset(offset)
                .limit(limit)
                .fetch();

        long total = memberStatusRepository.count(andBuilder);
        Pagination pagination = new Pagination(page, (int)total, 10, limit, request);

        return new ListData<>(items, pagination);

        // endregion
    }
}



















