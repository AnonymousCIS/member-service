package org.anonymous.member.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.member.controllers.MemberSearch;
import org.anonymous.member.entities.Member;
import org.anonymous.member.exceptions.MemberNotFoundException;
import org.anonymous.member.repositories.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberDeleteService {

    private final MemberRepository memberRepository;
    private final MemberInfoService memberInfoService;

    public Member userDelete(Long seq) {
        Member member = addInfo(seq);

        member.setDeletedAt(LocalDateTime.now());

        memberRepository.saveAndFlush(member);

        return member;
    }

    public Member delete(Long seq) {
        Member member = addInfo(seq);

        memberRepository.delete(member);
        memberRepository.flush();
        return member;
    }

    private Member addInfo(Long seq) {
        Member member = memberInfoService.get(seq);

        if (member == null) {
            throw new MemberNotFoundException();
        }

        return member;
    }
}

















