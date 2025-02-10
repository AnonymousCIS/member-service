package org.anonymous.member.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.anonymous.global.libs.Utils;
import org.anonymous.member.constants.TokenAction;
import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.TempToken;
import org.anonymous.member.exceptions.MemberNotFoundException;
import org.anonymous.member.exceptions.TempTokenExpiredException;
import org.anonymous.member.repositories.MemberRepository;
import org.anonymous.member.repositories.TempTokenRepository;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Lazy
@Service
@RequiredArgsConstructor
public class TempTokenService {

    private final Utils utils;
    private final ObjectMapper om;
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final TempTokenRepository tempTokenRepository;

    /**
     * 임시 접근 토큰 발급
     * @return
     */
    public TempToken issue(String email, TokenAction action, String origin) {

        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        TempToken token = TempToken.builder()
                .token(UUID.randomUUID().toString())
                .member(member)
                .action(action)
                .expiredTime(LocalDateTime.now().plusMinutes(3L))
                .origin(origin) // 유입된 프론트엔드 도메인 주소 예) http://pintech.koreait.xyz
                .build();

        tempTokenRepository.saveAndFlush(token);

        return token;
    }

    /**
     * 발급 받은 토큰으로 접근 가능한 주소 생성 후 메일 전송
     * @param token
     */
    public boolean sendEmail(String token) {
        TempToken tempToken = get(token);

        Member member = tempToken.getMember();
        String email = member.getEmail();

        String tokenUrl = tempToken.getOrigin() + "?" + tempToken.getToken();
        String subject = tempToken.getAction() == TokenAction.PASSWORD_CHANGE ? "비밀번호 변경 안내입니다." : "....";

        Map<String, Object> data = new HashMap<>();

        data.put("to", List.of(email));
        data.put("subject", subject);
        data.put("content", tokenUrl);

        try {
            String emailUrl = utils.serviceUrl("email-service","/tpl/general");
            String params = om.writeValueAsString(data);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(params, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(URI.create(emailUrl), request, Void.class);

            return response.getStatusCode().is1xxInformational();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public TempToken get(String token) {
        TempToken tempToken = tempTokenRepository.findByToken(token).orElseThrow(TempTokenExpiredException::new);

        if (tempToken.isExpired()) {
            throw new TempTokenExpiredException();
        }

        return tempToken;
    }
}























