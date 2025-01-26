package org.anonymous.member.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.member.controllers.RequestStatus;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.MemberStatus;
import org.anonymous.member.entities.MemberStatusId;
import org.anonymous.member.repositories.MemberStatusRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberStatusUpdateService {

    private final MemberInfoService memberInfoService;
    private final MemberStatusRepository memberStatusRepository;

    public MemberStatus status(RequestStatus form) {

        Member member = memberInfoService.get(form.getEmail());

        if (member == null) {
            return null; // 멤버는 무조건 있어야함.
        }

        MemberStatus memberStatus = memberStatus(member, form);
        memberStatus.setMember(member);
        addInfo(memberStatus, form);

        memberStatusRepository.saveAndFlush(memberStatus);

        return memberStatus;
    }

    public List<MemberStatus> statuses(List<RequestStatus> forms) {
        if (forms == null || forms.isEmpty()) {
            return null;
        }
        List<MemberStatus> memberStatuses = new ArrayList<>();

        for (RequestStatus form : forms) {
            Member member = memberInfoService.get(form.getEmail());
            if (member == null) {
                continue; // 동일함. 멤버는 무조건 있어야하기에...
            }
            MemberStatus memberStatus = memberStatus(member, form);
            memberStatus.setMember(member);
            addInfo(memberStatus, form);
            memberStatuses.add(memberStatus);
        }

        memberStatusRepository.saveAllAndFlush(memberStatuses);

        return memberStatuses;
    }

    private void addInfo(MemberStatus memberStatus, RequestStatus form) {

        memberStatus.setBlock(form.isBlock());
        memberStatus.setSeq(form.getSeq());
        memberStatus.setType(form.getType());
    }

    private MemberStatus memberStatus(Member member, RequestStatus form) {
        MemberStatusId memberStatusId = new MemberStatusId(member, form.getType(), form.getSeq());
        return memberStatusRepository.findById(memberStatusId).orElse(new MemberStatus());
    }
}
