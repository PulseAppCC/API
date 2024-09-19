package cc.pulseapp.api.config;

import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.user.Session;
import cc.pulseapp.api.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @author Braydon
 */
@Configuration @EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSecurityConfig {
    /**
     * The session repository to use.
     */
    @NonNull private final SessionRepository sessionRepository;

    @Autowired
    public WebSecurityConfig(@NonNull SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Bean @NonNull
    public SecurityFilterChain filterChain(@NonNull HttpSecurity http) throws Exception {
        SessionFilter filter = new SessionFilter();
        filter.setAuthenticationManager(authentication -> {
            Session authToken = (Session) authentication.getCredentials();
            if (authToken == null) { // No API key found
                throw new BadCredentialsException(Error.INVALID_ACCESS_TOKEN.name());
            }
            authentication.setAuthenticated(true); // Mark the session as authenticated
            return authentication;
        });
        return http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
                .formLogin(AbstractHttpConfigurer::disable) // Disable form logins
                .securityMatcher("/**") // Require auth for all routes
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class) // Add the auth token filter
                .authorizeHttpRequests(registry -> registry // Except for the following routes
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/error")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/v*/auth/register")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/v*/auth/login")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/v*/user/exists")).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> { // Handle invalid access tokens
                            response.sendError(HttpStatus.FORBIDDEN.value(), Error.INVALID_ACCESS_TOKEN.name());
                        }))
                .build();
    }

    private enum Error implements IGenericResponse {
        INVALID_ACCESS_TOKEN
    }

    /**
     * The filter for authenticating
     * requests with an {@link Session}.
     *
     * @author Braydon
     */
    public final class SessionFilter extends AbstractPreAuthenticatedProcessingFilter {
        @Override
        protected String getPreAuthenticatedPrincipal(@NonNull HttpServletRequest request) {
            return request.getHeader(HttpHeaders.AUTHORIZATION);
        }

        @Override
        protected Session getPreAuthenticatedCredentials(@NonNull HttpServletRequest request) {
            String accessToken = getPreAuthenticatedPrincipal(request); // Get the provided access token
            if (accessToken == null || !accessToken.startsWith("Bearer ")) {
                return null;
            }
            return sessionRepository.findByAccessToken(accessToken.substring(7));
        }
    }
}