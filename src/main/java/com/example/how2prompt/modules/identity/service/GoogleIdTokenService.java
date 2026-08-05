package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.config.AuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Verify Google ID token (GIS) bằng GoogleIdTokenVerifier chính thức.
 * Kiểm tra chữ ký (JWKS), audience, issuer; caller kiểm tra email_verified.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleIdTokenService {

    private final AuthProperties authProperties;

    @Autowired(required = false)
    private GoogleIdTokenVerifier verifier;

    public GoogleIdToken.Payload verify(String idToken) {
        if (!StringUtils.hasText(authProperties.getGoogleClientId()) || verifier == null) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Google OAuth chưa được cấu hình.");
        }
        if (!StringUtils.hasText(idToken)) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "id_token không hợp lệ.");
        }

        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Google id_token không hợp lệ.");
            }
            return token.getPayload();
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Google id_token verify failed: {}", e.getMessage());
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Google id_token không hợp lệ.");
        }
    }

    public Map<String, Object> toRawProfile(GoogleIdToken.Payload payload) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("sub", payload.getSubject());
        profile.put("email", payload.getEmail());
        profile.put("email_verified", payload.getEmailVerified());
        profile.put("name", payload.get("name"));
        profile.put("picture", payload.get("picture"));
        profile.put("given_name", payload.get("given_name"));
        profile.put("family_name", payload.get("family_name"));
        profile.put("locale", payload.get("locale"));
        profile.put("hd", payload.get("hd"));
        return profile;
    }
}
