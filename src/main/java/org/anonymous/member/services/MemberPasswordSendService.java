package org.anonymous.member.services;


import lombok.RequiredArgsConstructor;
import org.anonymous.global.libs.Utils;
import org.anonymous.global.repositories.CodeValueRepository;
import org.anonymous.global.rests.JSONData;
import org.anonymous.member.entities.Member;
import org.anonymous.member.exceptions.MemberNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberPasswordSendService {
    private final MemberInfoService memberInfoService;
    private final RestTemplate restTemplate;
    private final Utils utils;

    public boolean sendEmail(String email) {
        Member member = memberInfoService.get(email);
        if (member == null) {
            throw new MemberNotFoundException();
        }
        String url = "/auth/";
        return addInfo(url, "send", email, null) == HttpStatus.NO_CONTENT;
    }

    public boolean sendVerify(Integer code) {
        String url = "/verify?authCode=";

        boolean checkCodeValue = addInfo(url,"verify",null, code) == HttpStatus.NO_CONTENT;

        if (checkCodeValue) {
            utils.saveValue(utils.getUserHash() + "_password", code);
        }

        return checkCodeValue;
    }

    public HttpStatusCode addInfo(String url, String mode, String email, Integer code) {
        mode = StringUtils.hasText(mode) ? mode : "verify";

        String token = utils.getAuthToken();
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(token)) headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String apiUrl;
        if (StringUtils.hasText(mode) && mode.equals("verify")) {
            apiUrl = utils.serviceUrl("email-service", url + code);
        } else {
            apiUrl = utils.serviceUrl("email-service", url + email);
        }
        ResponseEntity<Void> item = restTemplate.exchange(apiUrl, HttpMethod.GET, request, Void.class);
        return item.getStatusCode();
    }
}
