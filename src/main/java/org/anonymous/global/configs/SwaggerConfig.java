package org.anonymous.global.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API 설정
 *
 * - API 문서 자동 생성 도구
 *
 */
// 제목 & 설명
@Configuration
@OpenAPIDefinition(info = @Info(title = "회원 인증/인가 API", description = "회원 가입 및 로그인, 회원 인가 체크에 대한 API 제공"))
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi openApiGroup() {

        return GroupedOpenApi.builder()
                .group("회원 인증/인가 API v1") // Group 이름 -> group("설명")
                .pathsToMatch("/**") // 경로 패턴 지정 (api 문서에 속하는 모든 경로)
                .build();
    }
}