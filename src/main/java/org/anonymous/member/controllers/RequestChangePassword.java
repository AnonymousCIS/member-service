package org.anonymous.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestChangePassword {

    @NotBlank
    private String token; // 임시 토큰

    @NotBlank
    private String password; // 변경할 비밀번호

    @NotBlank
    private String confirmPassword; // 변경할 비밀번호
}
