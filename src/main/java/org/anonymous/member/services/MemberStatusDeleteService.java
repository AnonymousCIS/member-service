package org.anonymous.member.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.global.libs.Utils;
import org.anonymous.member.controllers.RequestStatus;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.MemberStatus;
import org.anonymous.member.entities.MemberStatusId;
import org.anonymous.member.exceptions.MemberNotFoundException;
import org.anonymous.member.repositories.MemberRepository;
import org.anonymous.member.repositories.MemberStatusRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberStatusDeleteService {

    private final MemberInfoService memberInfoService;
    private final MemberStatusRepository memberStatusRepository;

    public List<MemberStatus> deletes(List<RequestStatus> forms) {

        if(forms == null || forms.isEmpty()){
            return null;
        }

        List<MemberStatus> memberStatuses = new ArrayList<>();
        for (RequestStatus form : forms) {
            Member member = memberInfoService.get(form.getEmail());

            if (member == null) {
                throw new MemberNotFoundException();
            }
            MemberStatus memberStatus = memberStatus(member, form);
            memberStatuses.add(memberStatus);
        }
        memberStatusRepository.deleteAll(memberStatuses);
        memberStatusRepository.flush();

        return memberStatuses;
    }

    private MemberStatus memberStatus(Member member, RequestStatus form) {
        MemberStatusId memberStatusId = new MemberStatusId(member, form.getType(), form.getSeq());
        return memberStatusRepository.findById(memberStatusId).orElse(new MemberStatus());
    }
}

















