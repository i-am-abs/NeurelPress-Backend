package com.neurelpress.blogs.security.jwt;

import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import io.jsonwebtoken.Claims;

@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                try {
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    String email = claims.get("email", String.class);
                    if (email != null) {
                        User existingUser = userRepository.findByEmail(email).orElse(null);
                        if (existingUser != null && !existingUser.getId().equals(userId)) {
                            log.info("Deleting existing user record with old ID {} to link to Supabase UUID {}", existingUser.getId(), userId);
                            userRepository.delete(existingUser);
                            existingUser.setId(userId);
                            existingUser.setProvider(com.neurelpress.blogs.constants.AuthProvider.LOCAL);
                            existingUser.setVerified(true);
                            user = userRepository.save(existingUser);
                        } else if (existingUser == null) {
                            String username = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
                            if (userRepository.existsByUsername(username)) {
                                username = username + "-" + UUID.randomUUID().toString().substring(0, 6);
                            }

                            String displayName = username;
                            java.util.Map<String, Object> userMetadata = claims.get("user_metadata", java.util.Map.class);
                            if (userMetadata != null) {
                                if (userMetadata.containsKey("displayName")) {
                                    displayName = (String) userMetadata.get("displayName");
                                } else if (userMetadata.containsKey("display_name")) {
                                    displayName = (String) userMetadata.get("display_name");
                                } else if (userMetadata.containsKey("full_name")) {
                                    displayName = (String) userMetadata.get("full_name");
                                }
                                if (userMetadata.containsKey("username")) {
                                    String metaUsername = (String) userMetadata.get("username");
                                    if (metaUsername != null && !metaUsername.isBlank() && !userRepository.existsByUsername(metaUsername)) {
                                        username = metaUsername;
                                    }
                                }
                            }

                            user = User.builder()
                                    .id(userId)
                                    .username(username)
                                    .email(email)
                                    .displayName(displayName)
                                    .provider(com.neurelpress.blogs.constants.AuthProvider.LOCAL)
                                    .verified(true)
                                    .build();
                            user = userRepository.save(user);
                            log.info("Auto-provisioned new user in MongoDB for email: {}, UUID: {}", email, userId);
                        } else {
                            user = existingUser;
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to auto-provision user from Supabase claims", e);
                }
            }

            if (user != null) {
                UserPrincipal userPrincipal = UserPrincipal.from(user);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                log.info("Authentication successful for user: {}", userId);
            }
        }
        filterChain.doFilter(request, response);
    }

    private @Nullable String extractToken(@NonNull HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
