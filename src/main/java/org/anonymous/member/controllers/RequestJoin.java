package org.anonymous.member.controllers;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RequestJoin {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name; // 회원명

    @Size(min=8, max=40)
    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    // 필수 약관 동의 여부
    @AssertTrue
    private boolean requiredTerms1;

    @AssertTrue
    private boolean requiredTerms2;

    @AssertTrue
    private boolean requiredTerms3;

    // 선택 약관 동의 여부 - 문구 입력
    // 선택 약관은 어떤 약관을 체크했는지 구분할 수 있어야 함
    private List<String> optionalTerms;
}