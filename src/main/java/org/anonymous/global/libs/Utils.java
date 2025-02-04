package org.anonymous.global.libs;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.anonymous.member.libs.MemberUtil;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.*;
import java.util.stream.Collectors;

@Lazy
@Component
@RequiredArgsConstructor
public class Utils {

    private final MemberUtil memberUtil;
    private final HttpServletRequest request;
    private final MessageSource messageSource;
    private final DiscoveryClient discoveryClient;

    /**
     * 메서지 코드로 조회된 문구
     *
     * @param code
     * @return
     */
    public String getMessage(String code) {
        Locale lo = request.getLocale(); // 사용자 요청 헤더(Accept-Language)

        return messageSource.getMessage(code, null, lo);
    }

    public List<String> getMessages(String[] codes) {

        return Arrays.stream(codes).map(c -> {
            try {
                return getMessage(c);
            } catch (Exception e) {
                return "";
            }
        }).filter(s -> !s.isBlank()).toList();

    }

    /**
     * REST 커맨드 객체 검증 실패시에 에러 코드를 가지고 메세지 추출
     *
     * @param errors
     * @return
     */
    public Map<String, List<String>> getErrorMessages(Errors errors) {
        ResourceBundleMessageSource ms = (ResourceBundleMessageSource) messageSource;


        // 필드별 에러코드 - getFieldErrors()
        // Collectors.toMap
        Map<String, List<String>> messages = errors.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, f -> getMessages(f.getCodes()), (v1, v2) -> v2));

        // 글로벌 에러코드 - getGlobalErrors()
        List<String> gMessages = errors.getGlobalErrors()
                .stream()
                .flatMap(o -> getMessages(o.getCodes()).stream())
                .toList();
        // 글로벌 에러코드 필드 - global
        if (!gMessages.isEmpty()) {
            messages.put("global", gMessages);
        }

        return messages;
    }

    /**
     * 유레카 서버 인스턴스 주소 검색
     *
     *      spring.profiles.active : dev - localhost로 되어 있는 주소를 반환
     *          - 예) member-service : 최대 2가지만 존재, 1 - 실 서비스 도메인 주소, 2. localhost ...
     * @param serviceId
     * @param url
     * @return
     */
    public String  serviceUrl(String serviceId, String url) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            String profile = System.getenv("spring.profiles.active");
            boolean isDev = StringUtils.hasText(profile) && profile.contains("dev");
            String serviceUrl = null;
            for (ServiceInstance instance : instances) {
                String uri = instance.getUri().toString();
                if (isDev && uri.contains("localhost")) {
                    serviceUrl = uri;
                } else if (!isDev && !uri.contains("localhost")) {
                    serviceUrl = uri;
                }
            }

            if (StringUtils.hasText(serviceUrl)) {
                return serviceUrl + url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 요청 헤더 : Authorizaion: Bearer ...
     * @return
     */
    public String getAuthToken() {
        String auth = request.getHeader("Authorization");

        return StringUtils.hasText(auth) ? auth.substring(7).trim() : null;
    }

    /**
     * 전체 주소
     *
     * @param url
     * @return
     */
    public String getUrl(String url) {
        int port = request.getServerPort();
        String _port = port == 80 || port == 443 ? "" : ":" + port;
        return String.format("%s://%s%s%s%s", request.getScheme(), request.getServerName(), _port, request.getContextPath(), url);
    }

    // region Code - Value redis 저장소. 멤버 내에서는 사용 안함.

//    /**
//     * Code - Value 레디스 저장소 저장
//     * @param code
//     * @param value
//     */
//    public void saveValue(String code, Object value) {
//        CodeValue item = new CodeValue();
//        item.setCode(code);
//        item.setValue(value);
//
//        codeValueRepository.save(item);
//    }
//
//    /**
//     * code 값으로 value값 조회
//     * @param code
//     * @return
//     * @param <T>
//     */
//    public <T> T getValue(String code) {
//        CodeValue item = codeValueRepository.findByCode(code);
//
//        return item == null ? null : (T)item.getValue();
//
//    }
//
//    /**
//     * 저장된 값 code로 삭제
//     * @param code
//     */
//    public void deleteValue(String code) {
//        codeValueRepository.deleteById(code);
//    }

    // endregion

    public boolean isMobile() {

        // 요청 헤더 - User-Agent / 브라우저 정보
        String ua = request.getHeader("User-Agent");
        String pattern = ".*(iPhone|iPod|iPad|BlackBerry|Android|Windows CE|LG|MOT|SAMSUNG|SonyEricsson).*";


        return StringUtils.hasText(ua) && ua.matches(pattern);
    }

    /**
     * 요청 헤더
     *  - JWT 토큰이 있으면 자동 추가
     * @return
     */
    public HttpHeaders getRequestHeader() {
        String token = getAuthToken();
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(token)) {
            headers.setBearerAuth(token);
        }

        return headers;
    }

    /**
     * 회원, 비회원 구분 해시
     * 회원 - 회원번호, 비회원 - IP + User-Agent
     * @return
     */
    public int getMemberHash() {
        if (memberUtil.isLogin()) return Objects.hash(memberUtil.getMember().getSeq()); // 회원
        else { // 비회원
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");

            return Objects.hash(ip, ua);
        }
    }

    /**
     * 사용자 구분을 위한 해시값 조회
     */
    public String getUserHash() {
        String userKey = "" + Objects.hash("userHash");
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies){
            if (cookie.getName().equals(userKey)){
                return cookie.getValue();
            }
        }
        return null;
    }
}