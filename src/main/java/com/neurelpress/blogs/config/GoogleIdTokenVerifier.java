package com.neurelpress.blogs.config;

import com.neurelpress.blogs.config.properties.NeuralPressGoogleProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleIdTokenVerifier {

    private static final String GOOGLE_JWKS_URL =
            "https://www.googleapis.com/oauth2/v3/certs";
    private static final String GOOGLE_ISSUER_1 = "https://accounts.google.com";
    private static final String GOOGLE_ISSUER_2 = "accounts.google.com";

    private final NeuralPressGoogleProperties googleProperties;

    public Optional<JWTClaimsSet> verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }
        try {
            ConfigurableJWTProcessor<SecurityContext> processor = buildProcessor();
            JWTClaimsSet claims = processor.process(idToken, null);

            String issuer = claims.getIssuer();
            if (!GOOGLE_ISSUER_1.equals(issuer) && !GOOGLE_ISSUER_2.equals(issuer)) {
                log.warn("Google ID token has unexpected issuer: {}", issuer);
                return Optional.empty();
            }

            if (!claims.getAudience().contains(googleProperties.clientId())) {
                log.warn("Google ID token audience mismatch. Expected: {}", googleProperties.clientId());
                return Optional.empty();
            }

            log.debug("Google ID token valid for subject: {}", claims.getSubject());
            return Optional.of(claims);

        } catch (Exception e) {
            log.warn("Google ID token verification failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private ConfigurableJWTProcessor<SecurityContext> buildProcessor() throws Exception {
        ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        var jwkSource = JWKSourceBuilder
                .create(URI.create(GOOGLE_JWKS_URL).toURL())
                .refreshAheadCache(true)
                .build();
        processor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource)
        );
        return processor;
    }
}
