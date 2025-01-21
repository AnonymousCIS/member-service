package org.anonymous.member.controllers;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RequestUpdate {

    @Size(min=8, max=40)
    @NotBlank
    private String password;

    @NotBlank
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

    @NotNull
    private LocalDate birthDt;
}
