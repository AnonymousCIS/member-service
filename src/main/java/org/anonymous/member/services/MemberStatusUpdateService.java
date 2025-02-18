package org.anonymous.member.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.global.exceptions.BadRequestException;
import org.anonymous.global.libs.Utils;
import org.anonymous.member.constants.DomainStatus;
import org.anonymous.member.constants.MemberCondition;
import org.anonymous.member.controllers.RequestStatus;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.MemberStatus;
import org.anonymous.member.entities.MemberStatusId;
import org.anonymous.member.exceptions.MemberNotFoundException;
import org.anonymous.member.repositories.MemberRepository;
import org.anonymous.member.repositories.MemberStatusRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberStatusUpdateService {

    private final Utils utils;
    private final RestTemplate restTemplate;
    private final MemberInfoService memberInfoService;
    private final MemberStatusRepository memberStatusRepository;
    private final MemberStatusInfoService memberStatusInfoService;
    private final MemberRepository memberRepository;

    public MemberStatus status(RequestStatus form) {

        Member member = memberInfoService.get(form.getMember().getEmail());

        if (member == null) {
            throw new MemberNotFoundException();
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
            Member member = memberInfoService.get(form.getMember().getEmail());
            if (member == null) {
                throw new MemberNotFoundException();
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
        memberStatus.setMemberStatus(form.getStatus());
        memberStatus.setSeq(form.getSeq());
        memberStatus.setType(form.getType());
    }

    private MemberStatus memberStatus(Member member, RequestStatus form) {
        MemberStatusId memberStatusId = new MemberStatusId(member, form.getType(), form.getSeq());
        return memberStatusRepository.findById(memberStatusId).orElse(new MemberStatus());
    }

    public Member statusBlock(String email) {
        Member member = memberInfoService.get(email);

        if (member == null) {
            throw new MemberNotFoundException();
        }

        member.setMemberCondition(MemberCondition.BLOCK);
        HttpEntity<Void> request = request();
        String apiUrl = utils.serviceUrl("board-service", "/admin/block/" + email);
        ResponseEntity<Void> item = restTemplate.exchange(apiUrl, HttpMethod.PATCH, request, Void.class);

        if (item.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new BadRequestException(utils.getMessage("Member.block"));
        }

        if (!messageStatus(List.of(email), true)) {
            throw new BadRequestException(utils.getMessage("Member.message.status"));
        }

        memberRepository.saveAndFlush(member);
        return member;
    }

    public List<Member> statusBlocks(List<String> emails) {
        List<Member> members = new ArrayList<>();
        for (String email : emails) {
            Member member = statusBlock(email);
            members.add(member);
        }

        if (!messageStatus(emails, true)) {
            throw new BadRequestException(utils.getMessage("Member.message.status"));
        }

        return members;
    }

    public void statusUnblock(String email, DomainStatus status) {
        Member member = memberInfoService.get(email); // 이메일로 해당멤버 확인
        member.setMemberCondition(MemberCondition.ACTIVE); // 멤버 ACTIVE 로 unblock 처리
        List<MemberStatus> memberStatusBoard = memberStatusInfoService.getStatus("board", email); // block 되어있는 데이터 다 가져오기
        if (!memberStatusBoard.isEmpty()) {
            List<Long> seqBoardList = memberStatusBoard.stream().map(MemberStatus::getSeq).toList(); // 해당 board에 되어있는 seq 다 가져오기
            String url = "/status?";
            if (!seqBoardList.isEmpty()) {
                ResponseEntity<Void> boardItem = addInfo(url, seqBoardList, status);
                if (boardItem.getStatusCode() != HttpStatus.OK) {
                    throw new BadRequestException(utils.getMessage("Member.board.status"));
                }
            }
        }

        List<MemberStatus> memberStatusComment = memberStatusInfoService.getStatus("comment", email); // block 되어있는 데이터 다 가져오기
        if (!memberStatusComment.isEmpty()) {
            List<Long> seqCommentList = memberStatusComment.stream().map(MemberStatus::getSeq).toList(); // 해당 board에 되어있는 seq 다 가져오기


            if (!seqCommentList.isEmpty()) {
                String url = "/comment/status?";
                ResponseEntity<Void> commentItem = addInfo(url, seqCommentList, status);

                if (commentItem.getStatusCode() != HttpStatus.OK) {
                    throw new BadRequestException(utils.getMessage("Member.comment.status"));
                }
            }
        }

        List<MemberStatus> memberStatusMessage = memberStatusInfoService.getStatus("message", email);
        if (!memberStatusMessage.isEmpty()) {
            if (!messageStatus(List.of(email), false)) {
                // unblock thr
            }
        }


        memberRepository.saveAndFlush(member);
    }

    public void statusUnblocks(List<String> emails, DomainStatus status) {
        for (String email : emails) {
            statusUnblock(email, status);
        }
    }

    private ResponseEntity<Void> addInfo(String url, List<Long> seq, DomainStatus status) {
        HttpEntity<Void> request = request();
        String result = seq.stream()
                .map(a -> "seq=" + a)
                .collect(Collectors.joining("&"));
        String apiUrl = utils.serviceUrl("board-service", url + result + "&status=" + status);
        ResponseEntity<Void> item = restTemplate.exchange(apiUrl, HttpMethod.PATCH, request, Void.class);

        return item;
    }

    private boolean messageStatus(List<String> emails, boolean status) {
        for (String email : emails) {
            Member member = memberInfoService.get(email);
            if (member == null) {
                throw new MemberNotFoundException();
            }
        }

        HttpHeaders headers = new HttpHeaders(); // 헤더 생성
        if (StringUtils.hasText(utils.getAuthToken())) headers.setBearerAuth(utils.getAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<String>> request = new HttpEntity<>(emails, headers);
        String apiUrl = utils.serviceUrl("message-service", "/admin/status?status=" + status);
        ResponseEntity<Void> item = restTemplate.exchange(apiUrl, HttpMethod.PATCH, request, Void.class);

        return item.getStatusCode() == HttpStatus.OK;
    }

    private HttpEntity<Void> request() {
        HttpHeaders headers = new HttpHeaders(); // 헤더 생성
        if (StringUtils.hasText(utils.getAuthToken())) headers.setBearerAuth(utils.getAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(headers);
    }

}

















