package org.anonymous.member.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "회원 목록 조회", method="GET", description = "data - 조회된 회원목록, pagination - 페이징 기초 데이터")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name="page", description = "페이지 번호", example = "1"),
            @Parameter(name="limit", description = "한페이지당 레코드 갯수", example = "20"),
            @Parameter(name="sopt", description = "검색옵션", example = "ALL"),
            @Parameter(name="skey", description = "검색키워드"),
            @Parameter(name="email", description = "이메일별로 검색"),
            @Parameter(name="authority", description = "권한별 검색, 상수.", examples = {
                    @ExampleObject(name = "Authority", value = "USER"),
                    @ExampleObject(name = "Authority", value = "ADMIN")
            }), // 여기 권한 뭐뭐있는지 설명
            @Parameter(name="dateType", description = "날짜별 검색", examples = {
                    @ExampleObject(name = "createdAt", value = "생성날짜기준"),
                    @ExampleObject(name = "deletedAt", value = "탈퇴일 기준"),
                    @ExampleObject(name = "credentialChangedAt", value = "비밀번호 변경날짜 기준")
            }), // 여기 날짜 뭐뭐있는지 설명
            @Parameter(name="sDate", description = "시작날짜"),
            @Parameter(name="dDate", description = "끝날짜"),
    })
    @GetMapping("/list")
    public JSONData list(@ModelAttribute MemberSearch search) {
        ListData<Member> memberList = memberInfoService.getList(search);
        return new JSONData(memberList);
    }

    /**
     * 회원 상태 단일 수정 처리 - 도메인별
     * @return
     */

    @Operation(summary = "회원 상태 단일 수정 처리 - 도메인별", method = "PATCH")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name = "email", description = "이메일"),
            @Parameter(name = "DomainStatus", description = "도메인 Status", examples = {
                    @ExampleObject(name = "DomainStatus", value = "ALL"),
                    @ExampleObject(name = "DomainStatus", value = "SECRET"),
                    @ExampleObject(name = "DomainStatus", value = "BLOCK"),
            }),
            @Parameter(name = "type", description = "도메인별", examples = {
                    @ExampleObject(name = "String", value = "Board"),
                    @ExampleObject(name = "String", value = "Message"),
            }),
            @Parameter(name = "seq", description = "도베인별 seq", examples = {
                    @ExampleObject(name = "Long", value = "Board 쪽 block seq"),
            }),
    })
    @PatchMapping("/status")
    public JSONData statusUpdate(@RequestBody RequestStatus form) {

        MemberStatus memberStatus = memberStatusUpdateService.status(form);
        return new JSONData(memberStatus);
    }

    /**
     * 회원 상태 목록 수정 처리 - 도메인별
     * @return
     */
    @Operation(summary = "회원 상태 목록 수정 처리 - List<RequestStatus>도메인별", method = "PATCH")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name = "email", description = "이메일"),
            @Parameter(name = "DomainStatus", description = "도메인 Status", examples = {
                    @ExampleObject(name = "DomainStatus", value = "ALL"),
                    @ExampleObject(name = "DomainStatus", value = "SECRET"),
                    @ExampleObject(name = "DomainStatus", value = "BLOCK"),
            }),
            @Parameter(name = "type", description = "도메인별", examples = {
                    @ExampleObject(name = "String", value = "Board"),
                    @ExampleObject(name = "String", value = "Message"),
            }),
            @Parameter(name = "seq", description = "도베인별 seq", examples = {
                    @ExampleObject(name = "Long", value = "Board 쪽 block seq"),
            }),
    })
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
    @Operation(summary = "회원 상태 단일 조회 - DomainStatus", description = "회원 1명의 도메인 별 Status 조회", method = "GET")
    @Parameter(name = "email", description = "이메일")
    @ApiResponse(responseCode = "200")
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
    @Operation(summary = "회원 상태 목록 조회- DomainStatus", method="GET", description = "data - 조회된 회원목록, pagination - 페이징 기초 데이터")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name="page", description = "페이지 번호", example = "1"),
            @Parameter(name="limit", description = "한페이지당 레코드 갯수", example = "20"),
            @Parameter(name="sopt", description = "검색옵션", example = "ALL"),
            @Parameter(name="skey", description = "검색키워드"),
            @Parameter(name="email", description = "이메일별로 검색"),
            @Parameter(name="type", description = "도메인 별 검색"),
            @Parameter(name = "DomainStatus", description = "도메인 Status", examples = {
                    @ExampleObject(name = "DomainStatus", value = "ALL"),
                    @ExampleObject(name = "DomainStatus", value = "SECRET"),
                    @ExampleObject(name = "DomainStatus", value = "BLOCK"),
            }),
            @Parameter(name="dateType", description = "날짜별 검색", examples = {
                    @ExampleObject(name = "createdAt", value = "생성날짜기준"),
                    @ExampleObject(name = "deletedAt", value = "탈퇴일 기준"),
                    @ExampleObject(name = "credentialChangedAt", value = "비밀번호 변경날짜 기준")
            }), // 여기 날짜 뭐뭐있는지 설명
            @Parameter(name="sDate", description = "시작날짜"),
            @Parameter(name="dDate", description = "끝날짜"),
    })
    @GetMapping("/statuses")
    public JSONData statusList(@ModelAttribute MemberStatusSearch form) {
        ListData<MemberStatus> items = memberStatusInfoService.statuses(form);
        return new JSONData(items);
    }

    /**
     * 회원 수정 업데이트 처리
     * 얘도 필요없을듯..?
     * @return
     */
    @Operation(summary = "회원 정보 수정", method = "PATCH", description = "관리자가 회원의 정보를 수정할 수 있다.")
    @ApiResponse(responseCode = "200", description = "수정 완료 됬을 시 200")
    @Parameters({
            @Parameter(name = "email", required = true, description = "이메일"),
            @Parameter(name = "password", description = "비밀번호"),
            @Parameter(name = "confirmPassword", description = "비밀번호 확인"),
            @Parameter(name = "zipCode", required = true, description = "우편번호"),
            @Parameter(name = "address", required = true, description = "집주소"),
            @Parameter(name = "addressSub", description = "나머지주소"),
            @Parameter(name = "phoneNumber", required = true, description = "휴대폰번호"),
            @Parameter(name = "optionalTerms", description = "선택 약관 동의 여부"),
            @Parameter(name = "authorities", description = "유저 권한.", examples = @ExampleObject(
                    name="authorities", value = "{USER,ADMIN}"
            )),
            @Parameter(name = "mode", description = "edit", examples = @ExampleObject(
                    name="mode", value = "edit", description = "수정이면 edit, 패스워드 찾기면 password")),
    })
    @PatchMapping("/update")
    public JSONData edit(@RequestBody RequestUpdate update, Errors errors) {

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }
        Member member = memberUpdateService.process(update, update.getAuthorities());
        return new JSONData(member);
    }

    /**
     * 리얼 찐 삭제
     * @param seq
     * @return
     */
    @Operation(summary = "회원 삭제", description = "deleteAt이 아닌 DB 내에서 진짜로 삭제한다.", method = "DELETE")
    @Parameter(name = "seq", required = true, description = "회원번호")
    @ApiResponse(responseCode = "200")
    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {
        Member member = memberDeleteService.delete(seq);
        return new JSONData(member);
    }

    /**
     * 단일 관리자 차단 도메인별로 차단해야할듯.
     * @return
     */
    @Operation(summary = "회원 단일 강퇴", description = "회원 단일 강퇴", method = "PATCH")
    @Parameter(name = "email", required = true, description = "이메일")
    @ApiResponse(responseCode = "200")
    @PatchMapping("/block/{email}")
    public JSONData block(@PathVariable("email") String email) {
        Member member = memberStatusUpdateService.statusBlock(email);
        return new JSONData(member);
    }

    @Operation(summary = "회원 목록 강퇴", description = "관리자의 회원 목록 강퇴", method = "PATCH")
    @Parameter(name = "email", required = true, description = "이메일")
    @ApiResponse(responseCode = "200")
    @PatchMapping("/blocks")
    public JSONData blocks(@RequestBody List<String> emails) {
        List<Member> members = memberStatusUpdateService.statusBlocks(emails);
        return new JSONData(members);
    }

    @Operation(summary = "회원 단일 강퇴 취소", description = "관리자의 회원 단일 강퇴 취소", method = "PATCH")
    @Parameter(name = "email", required = true, description = "이메일")
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/unblock/{email}")
    public void unblock(@PathVariable("email") String email, @RequestParam("status") DomainStatus status) {
        memberStatusUpdateService.statusUnblock(email, status);
    }

    @Operation(summary = "회원 목록 강퇴 취소", description = "관리자의 회원 목록 강퇴 취소", method = "PATCH")
    @Parameter(name = "email", required = true, description = "이메일")
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/unblocks")
    public void unblocks(@RequestBody List<String> emails, @RequestParam("status") DomainStatus status) {
        memberStatusUpdateService.statusUnblocks(emails, status);
    }
}
