package org.anonymous.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.paging.ListData;
import org.anonymous.global.rests.JSONData;
import org.anonymous.member.entities.Member;
import org.anonymous.member.services.MemberInfoService;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminMember", description = "회원 관리 API")
@RequestMapping("/admin")
@RestController
@RequiredArgsConstructor
public class AdminMemberController {


    private final MemberInfoService memberInfoService;

    /**
     * 회원 단일 조회
     * @return
     */
    @GetMapping("/info/{seq}")
    public JSONData info(@PathVariable Long seq) {
        Member member = (Member) memberInfoService.loadUserBySeq(seq);
        return new JSONData(member);
    }

    /**
     * 회원 단일 조회
     * @return
     */
    @GetMapping("/info/{email}")
    public JSONData info(@PathVariable String email) {
        Member member = (Member) memberInfoService.loadUserByUsername(email);
        return new JSONData(member);
    }

    /**
     * 회원 목록 조회
     * @return
     */
    @GetMapping("/list")
    public JSONData list(MemberSearch search) {
        ListData<Member> memberList = memberInfoService.getList(search);
        return new JSONData(memberList);
    }

    /**
     * 회원 상태 수정 처리
     * @return
     */
    @PatchMapping("/status")
    public JSONData delete() {

        return null;
    }

    /**
     * 회원 수정 업데이트 처리
     * @return
     */
    @PatchMapping("/update")
    public JSONData edit(@RequestBody RequestUpdate update, Errors errors) {

        if (errors.hasErrors()) {

        }

        return null;
    }
}
