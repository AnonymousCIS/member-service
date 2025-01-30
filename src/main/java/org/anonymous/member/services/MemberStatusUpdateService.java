package org.anonymous.member.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.exceptions.BadRequestException;
import org.anonymous.global.libs.Utils;
import org.anonymous.global.paging.ListData;
import org.anonymous.member.constants.DomainStatus;
import org.anonymous.member.constants.MemberCondition;
import org.anonymous.member.controllers.MemberStatusSearch;
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
        String token = utils.getAuthToken();
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(token)) headers.setBearerAuth(token);
        HttpEntity<RequestStatus> request = new HttpEntity<>(headers);
        String apiUrl = utils.serviceUrl("board-service", "/admin/block/" + email);
        ResponseEntity<RequestStatus> item = restTemplate.exchange(apiUrl, HttpMethod.PATCH, request, RequestStatus.class);

        if (item.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new BadRequestException(utils.getMessage("Member.block"));
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
        return members;
    }

    public void statusUnblock(String email, DomainStatus status) {

        Member member = memberInfoService.get(email); // 이메일로 해당멤버 확인
        member.setMemberCondition(MemberCondition.ACTIVE); // 멤버 ACTIVE 로 unblock 처리
        List<MemberStatus> memberStatusBoard = memberStatusInfoService.getStatus("board"); // block 되어있는 데이터 다 가져오기
        List<Long> seqBoardList = memberStatusBoard.stream().map(MemberStatus::getSeq).toList(); // 해당 board에 되어있는 seq 다 가져오기
        String url = "/board/status?seq=";
        ResponseEntity<RequestStatus> boardItem = addInfo(url, seqBoardList, status);

        if (boardItem.getStatusCode() != HttpStatus.OK) {
            throw new BadRequestException(utils.getMessage("Member.board.status"));
        }

        List<MemberStatus> memberStatusComment = memberStatusInfoService.getStatus("comment"); // block 되어있는 데이터 다 가져오기
        List<Long> seqCommentList = memberStatusComment.stream().map(MemberStatus::getSeq).toList(); // 해당 board에 되어있는 seq 다 가져오기

        url = "/comment/status?seq=";
        ResponseEntity<RequestStatus> commentItem = addInfo(url, seqCommentList, status);

        if (commentItem.getStatusCode() != HttpStatus.OK) {
            throw new BadRequestException(utils.getMessage("Member.comment.status"));
        }

        memberRepository.saveAndFlush(member);
    }

    public void statusUnblocks(List<String> emails, DomainStatus status) {
        for (String email : emails) {
            statusUnblock(email, status);
        }
    }

    private ResponseEntity<RequestStatus> addInfo(String url, List<Long> seq, DomainStatus status) {
        HttpHeaders headers = new HttpHeaders(); // 헤더 생성
        if (StringUtils.hasText(utils.getAuthToken())) headers.setBearerAuth(utils.getAuthToken());
        HttpEntity<RequestStatus> request = new HttpEntity<>(headers);
        String apiUrl = utils.serviceUrl("board-service", url + seq + "&status=" + status);
        ResponseEntity<RequestStatus> item = restTemplate.exchange(apiUrl, HttpMethod.PATCH, request, RequestStatus.class);

        return item;
    }

}

















