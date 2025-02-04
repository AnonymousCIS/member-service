package org.anonymous.member.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.converters.Auto;
import org.anonymous.member.annotations.MockMember;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.constants.DomainStatus;
import org.anonymous.member.constants.Gender;
import org.anonymous.member.entities.Member;
import org.anonymous.member.jwt.TokenService;
import org.anonymous.member.repositories.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
// @ActiveProfiles({"default", "jwt"})
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MemberRepository memberRepository;

    // region 회원가입 & 로그인 테스트

    @Test
    @DisplayName("회원가입")
    void join() {
        RequestJoin form = new RequestJoin();
        for (int i = 1; i <= 20; i++) {
            form.setEmail("user" + i + "@test.org");
            form.setName("사용자" + i);
            form.setPassword("_aA123456");
            form.setConfirmPassword(form.getPassword());
            form.setPhoneNumber("010-0000-0000");
            form.setGender(Gender.MALE);
            form.setBirthDt(LocalDate.now());
            form.setZipCode("12345");
            form.setAddress("12345");
            form.setAddressSub("12345");
            form.setRequiredTerms1(true);
            form.setRequiredTerms2(true);
            form.setRequiredTerms3(true);

            try {
                String body = om.writeValueAsString(form);

                mockMvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andDo(print());
            } catch (Exception e) {}
        }
    }

    @Test
    @DisplayName("회원가입 & 로그인 테스트")
    void joinTest() throws Exception {
        // 환경 변수
        // config.server=https://config-service.onedu.blue

        // 회원 가입
        RequestJoin form = new RequestJoin();

        form.setEmail("user01@test.org");
        form.setName("사용자01");
        form.setPassword("_aA123456");
        form.setConfirmPassword(form.getPassword());
        form.setRequiredTerms1(true);
        form.setRequiredTerms2(true);
        form.setRequiredTerms3(true);
        form.setOptionalTerms(List.of("advertisement"));

        String body = om.writeValueAsString(form);

        mockMvc.perform(post("/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)).andDo(print());

        // 로그인 테스트 - Token 발급
        RequestLogin loginForm = new RequestLogin();

        loginForm.setEmail(form.getEmail());
        loginForm.setPassword(form.getPassword());

        String loginBody = om.writeValueAsString(loginForm);

        String body3 = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)).andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        // Token 으로 로그인 처리 테스트
        Map<String, String> data = om.readValue(body3, new TypeReference<>() {});

        String token = data.get("data");

        mockMvc.perform(get("/test")
                .header("Authorization", "Bearer " + token))
                .andDo(print());
    }

    // endregion

    @Test
    void blockTest() throws Exception {
        String token = tokenService.create("user01@test.org");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        /**
         * {
         *   "email":"user04@test.org",
         *   "status":"BLOCK",
         *   "type":"board",
         *   "seq":"1"
         * }
         */

        RequestStatus form = new RequestStatus();
        form.setEmail("user04@test.org");
        form.setStatus(DomainStatus.BLOCK);
        form.setType("board");
        form.setSeq(2L);
        String params = om.writeValueAsString(form);

        HttpEntity<String> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(URI.create("http://localhost:3011/admin/status"), HttpMethod.PATCH, request, String.class);
        System.out.println(response);
    }

    @Test
    @MockMember(authority = {Authority.ADMIN, Authority.USER})
    void blockTest2() throws Exception {
        String token = tokenService.create("user1@test.org");

        System.out.println("token : " + token);

        mockMvc.perform(patch("/admin/block/user4@test.org")
                .header("Authorization", "Bearer " + token))
                .andDo(print());
    }

    @Test
    @MockMember(authority = {Authority.ADMIN, Authority.USER})
    void unblockTest() throws Exception {
        String token = tokenService.create("user1@test.org");

        mockMvc.perform(patch("/admin/unblock?email=user4@test.org&status=ALL")
                .header("Authorization", "Bearer " + token))
                .andDo(print());
    }

    @Test
    @MockMember(authority = {Authority.ADMIN, Authority.USER})
    void blocksTest() throws Exception {
        String token = tokenService.create("user01@test.org");
        List<String> memberList = List.of("user10@test.org", "user11@test.org");

        mockMvc.perform(patch("/admin/blocks")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .content(om.writeValueAsString(memberList)))
                .andDo(print());
    }

    @Test
    @MockMember(authority = {Authority.ADMIN, Authority.USER})
    void unblocksTest() throws Exception {
        String token = tokenService.create("user01@test.org");
        List<String> memberList = List.of("user10@test.org", "user11@test.org", "user04@test.org");

        mockMvc.perform(patch("/admin/unblocks")
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(memberList))
                        .param("status", String.valueOf(DomainStatus.ALL))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

}





























