package org.anonymous.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.exceptions.BadRequestException;
import org.anonymous.global.libs.Utils;
import org.anonymous.global.paging.ListData;
import org.anonymous.global.rests.JSONData;
import org.anonymous.member.constants.DomainStatus;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.MemberStatus;
import org.anonymous.member.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AdminMember", description = "회원 관리 API")
@RequestMapping("/admin")
@RestController
@RequiredArgsConstructor
public class AdminMemberController {


    private final Utils utils;
    private final MemberInfoService memberInfoService;
    private final MemberDeleteService memberDeleteService;
    private final MemberUpdateService memberUpdateService;
    private final MemberStatusInfoService memberStatusInfoService;
    private final MemberStatusUpdateService memberStatusUpdateService;

//    /**
//     * 회원 단일 조회
//     * 사실 여기에 필요가 없을거같은데..
//     * @return
//     */
//    @GetMapping("/info/{email}")
//    public JSONData info(@PathVariable("email") String email) {
//        Member member = null;
//        try {
//            Long seq = Long.valueOf(email);
//            member = memberInfoService.get(seq);
//        } catch (Exception e) {
//            // 이메일
//            member = memberInfoService.get(email);
//        }
//        return new JSONData(member);
//    }

    /**
     * 회원 목록 조회
     * @return
     */
    @GetMapping("/list")
    public JSONData list(@ModelAttribute MemberSearch search) {
        ListData<Member> memberList = memberInfoService.getList(search);
        return new JSONData(memberList);
    }

    /**
     * 회원 상태 단일 수정 처리 - 도메인별
     * @return
     */
    @PatchMapping("/status")
    public JSONData statusUpdate(@RequestBody RequestStatus form) {

        MemberStatus memberStatus = memberStatusUpdateService.status(form);
        return new JSONData(memberStatus);
    }

    /**
     * 회원 상태 목록 수정 처리 - 도메인별
     * @return
     */
    @PatchMapping("/statuses")
    public JSONData statusUpdate(@RequestBody List<RequestStatus> form) {

        List<MemberStatus> memberStatus = memberStatusUpdateService.statuses(form);
        return new JSONData(memberStatus);
    }

    /**
     * 회원 상태 단일 조회 -> 멤버 한개가 어떤 것들이 block되어 있는지...
     * @param email
     * @return
     */
    @GetMapping("/status/{email}")
    public JSONData status(@PathVariable("email") String email) {
        
        List<MemberStatus> items = memberStatusInfoService.status(email);
        return new JSONData(items);
    }

    /**
     * 회원 상태 목록 조회
     * 
     * @return
     */
    public JSONData statusList(@ModelAttribute MemberStatusSearch form) {
        ListData<MemberStatus> items = memberStatusInfoService.statuses(form);
        return new JSONData(items);
    }

    /**
     * 회원 수정 업데이트 처리
     * 얘도 필요없을듯..?
     * @return
     */
    @PatchMapping("/update")
    public JSONData edit(@RequestBody RequestUpdate update, Errors errors) {

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }
        Member member = memberUpdateService.process(update);
        return new JSONData(member);
    }

    /**
     * 리얼 찐 삭제
     * @param seq
     * @return
     */
    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {
        Member member = memberDeleteService.delete(seq);
        return new JSONData(member);
    }

    /**
     * 단일 관리자 차단 도메인별로 차단해야할듯.
     * @return
     */
    @PatchMapping("/block/{email}")
    public JSONData block(@PathVariable("email") String email) {
        Member member = memberStatusUpdateService.statusBlock(email);
        return new JSONData(member);
    }

    @PatchMapping("/blocks")
    public JSONData blocks(@RequestBody List<String> emails) {
        List<Member> members = memberStatusUpdateService.statusBlocks(emails);
        return new JSONData(members);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/unblock")
    public void unblock(@RequestParam("email") String email, @RequestParam("status") DomainStatus status) {
        memberStatusUpdateService.statusUnblock(email, status);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/unblocks")
    public void unblocks(@RequestBody List<String> emails, @RequestParam("status") DomainStatus status) {
        memberStatusUpdateService.statusUnblocks(emails, status);
    }
}
