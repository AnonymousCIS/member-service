package org.anonymous.global.configs;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.anonymous.member.jwt.filters.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity // 기본 보안 정책 활성화 - 비정상적인 요청시 밴 등등
@EnableMethodSecurity // @PreAuthorize, @PrePostEnabled 호라성화, 특정 처리 메서드 단위에서 권한 정책 설정
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsFilter corsFilter;

    private final LoginFilter loginFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /**
         * Token 인증 방식은 Session 을 사용하지 않기때문에 추가한 설정
         *
         * SessionCreationPolicy
         * - ALWAYS : 서버가 시작 되었을때부터 Session 을 Cookie 에 생성, Session ID 바로 조회 가능
         * - IF_REQUIRED : Session 이 필요한 시점에 Session 을 Cookie 에 생성 (default 값)
         * - NEVER : Session 생성 X, 기존 생성된 Session 이 존재하는 경우 사용
         * - STATELESS : Session 생성 X, 기존 생성된 Session 도 사용 X
         */
        http.csrf(c -> c.disable())
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // .addFilterAfter() // 특정 Filter 의 앞에
                // .addFilterBefore() // 특정 Filter 의 뒤에
                // .addFilterAt() // 끝에

                // UsernamePasswordAuthenticationFilter (Spring Security 기본)
                // UserName & Password 로 로그인 하는 필터
                // 항상 UsernamePasswordAuthenticationFilter 앞에 corsFilter & loginFilter 동작해야함
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)
                // 인증 실패 예외 발생시
                .exceptionHandling(c -> {
                    // 미로그인 상태에서 접근한 경우
                    c.authenticationEntryPoint((req, res, e) -> {
                       // throw new UnAuthorizedException();
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    });

                    // 로그인 후 권한이 없는 경우
                    c.accessDeniedHandler((req, res, e) -> {
                      // throw new UnAuthorizedException();
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    });
                })
                // 미로그인시 접근 가능한 패턴
                .authorizeHttpRequests( c -> {

                    /**
                     * 접근 제한 통제 설정.
                     * authenticated() : 인증받은 사용자만 접근.
                     * anonymous() : 인증받지 않은 사용자만 접근.
                     * permitAll() : 모든 사용자가 접근 가능.
                     * hasAuthority("권한 명칭") : 하나의 권한을 체크
                     * hasAnyAuthority("권한1", "권한2", ... ) : 나열된 권한 중 하나라도 충족하면 접근 가능
                     * ROLE
                     * ROLE_명칭
                     * hasRole("명칭")
                     * hasAnyRole(...)
                     */
                      // 미로그인도 즉, 전체 접근 가능 패턴

                    c.requestMatchers(
                                    "/join", // GateWay 연동시 /api/v1/member/join 예정
                                    "/login",
                                    "/find/**",
                                    "/change/**",
                                    "/apidocs.html",
                                    "/swagger-ui/**",
                                    "/api-docs/**").permitAll()
                            // 관리자만 접근 가능 패턴
                            .requestMatchers("/admin/**").hasAnyAuthority("ADMIN")

                            // 나머지는 아무 인증, 즉 로그인시 접근 가능 패턴
                            .anyRequest().authenticated();
                });

        // Security 설정 무력화
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

//    public DefaultCookieSerializerCustomizer cookieSerializer() {
//
//
//    }
}