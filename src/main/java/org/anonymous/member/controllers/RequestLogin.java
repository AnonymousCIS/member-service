package org.anonymous.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestLogin {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}