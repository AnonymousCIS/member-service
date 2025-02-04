package org.anonymous.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestFindPassword {
    @NotBlank
    private String name; // 회원명

    @NotBlank
    private String phoneNumber; // 휴대전화번호

    @NotBlank
    private String origin; // 프론트엔드 주소
}
