package org.anonymous.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.rests.JSONData;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminMember", description = "회원 관리 API")
@RequestMapping("/admin")
@RestController
@RequiredArgsConstructor
public class AdminMemberController {


    /**
     * 회원 단일 조회
     * @return
     */
    @GetMapping("/info/{seq}")
    public JSONData info(@PathVariable Long seq) {

        return null;
    }

    /**
     * 회원 목록 조회
     * @return
     */
    @GetMapping("/list")
    public JSONData list(MemberSearch search) {

        return null;
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
    public JSONData edit(@RequestParam RequestUpdate update, Errors errors) {

        if(errors.hasErrors()) {

        }

        return null;
    }
}
