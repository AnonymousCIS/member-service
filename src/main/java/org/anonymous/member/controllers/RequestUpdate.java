package org.anonymous.member.controllers;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.anonymous.member.constants.Authority;

import java.time.LocalDate;
import java.util.List;

@Data
public class RequestUpdate {

    @NotBlank
    private String email; // 절대 바뀌면 안됨. 그냥 값만 먼저 받고 Member 확인용으로 하려고 하는거

    private String password;

    private String confirmPassword;

    @NotBlank
    private String zipCode;

    @NotBlank
    private String address;

    private String addressSub;

    @NotBlank
    private String phoneNumber;

    // 선택 약관 동의 여부 - 문구 입력
    // 선택 약관은 어떤 약관을 체크했는지 구분할 수 있어야 함
    private List<String> optionalTerms;

    private List<Authority> authorities;
}
