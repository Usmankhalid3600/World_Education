package com.worldedu.worldeducation.security.jwt;

import com.worldedu.worldeducation.auth.repository.UserRepository;
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
                User user = userRepository.findByUserId(userId).orElse(null);
                
                if (user != null && !user.getAccountLocked()) {
                    // Create authentication token with authority matching SecurityConfig
                    // Using user category directly (ADMIN, STUDENT) without "ROLE_" prefix
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getUserCategory().name());
                    UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            user, 
                            null, 
                            Collections.singletonList(authority)
                        );
                    
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    log.debug("Authenticated user: {} with authority: {}", userId, user.getUserCategory());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
