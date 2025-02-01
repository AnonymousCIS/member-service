package org.anonymous.member.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.anonymous.member.annotations.MockMember;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.constants.DomainStatus;
import org.anonymous.member.jwt.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberSendEmailController {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    @Test
    @MockMember(authority = {Authority.ADMIN, Authority.USER})
    void unblocksTest() throws Exception {
/*        mockMvc.perform(get("/send/kks940104@naver.com"))
                .andDo(print());*/

        String userKey = "" + Objects.hash("userHash");

        Cookie cookie = new Cookie(userKey, "10");

        mockMvc.perform(get("/send/kks940104@naver.com")
                .cookie(cookie))
                .andDo(print());
    }

}
