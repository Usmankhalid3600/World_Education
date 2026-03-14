package com.worldedu.worldeducation.security.jwt;

import com.worldedu.worldeducation.auth.repository.UserRepository;
import com.worldedu.worldeducation.auth.repository.UserSessionRepository;
import com.worldedu.worldeducation.auth.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String userId = null;
        String jwt = null;

        // Extract JWT from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                userId = jwtUtil.extractUserId(jwt);
            } catch (Exception e) {
                log.error("Error extracting userId from token: {}", e.getMessage());
            }
        }

        // Validate token and set authentication
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwt)) {

                // Check if the session embedded in the token is still active.
                // This enforces single-session login: when a student logs in on a new device,
                // all previous sessions are deactivated, making their tokens invalid here.
                try {
                    Long sessionId = jwtUtil.extractSessionId(jwt);
                    if (sessionId != null) {
                        boolean sessionActive = userSessionRepository.existsBySessionIdAndIsActive(sessionId, true);
                        if (!sessionActive) {
                            log.warn("Session {} is no longer active — returning SESSION_TERMINATED for user {}", sessionId, userId);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                "{\"success\":false,\"code\":\"SESSION_TERMINATED\"," +
                                "\"message\":\"Your session was ended because you signed in from another device.\"}"
                            );
                            return; // Short-circuit — do NOT continue the filter chain
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not check session validity, allowing request through: {}", e.getMessage());
                }

                User user = userRepository.findByUserId(userId).orElse(null);

                if (user != null && !user.getAccountLocked()) {
                    // Using user category directly (ADMIN, STUDENT) without "ROLE_" prefix
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getUserCategory().name());
                    UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(authority)
                        );

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    log.debug("Authenticated user: {} with authority: {}", userId, user.getUserCategory());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
