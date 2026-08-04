package com.example.how2prompt.modules.identity;

import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.modules.identity.service.GoogleIdTokenService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleIdTokenServiceTest extends IdentityIntegrationTestBase {

    @Autowired
    private GoogleIdTokenService googleIdTokenService;

    @MockitoBean
    private GoogleIdTokenVerifier verifier;

    @Test
    void testVerify_Success() throws Exception {
        String idTokenString = "valid-id-token";
        
        Header header = new Header();
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("test-sub");
        payload.setEmail("test@example.com");
        payload.setEmailVerified(true);
        payload.set("name", "Test User");
        
        GoogleIdToken idToken = new GoogleIdToken(header, payload, new byte[0], new byte[0]);
        when(verifier.verify(idTokenString)).thenReturn(idToken);

        GoogleIdToken.Payload resultPayload = googleIdTokenService.verify(idTokenString);
        assertNotNull(resultPayload);
        assertEquals("test-sub", resultPayload.getSubject());
        assertEquals("test@example.com", resultPayload.getEmail());
        
        Map<String, Object> rawProfile = googleIdTokenService.toRawProfile(resultPayload);
        assertEquals("test-sub", rawProfile.get("sub"));
        assertEquals("test@example.com", rawProfile.get("email"));
        assertEquals(true, rawProfile.get("email_verified"));
        assertEquals("Test User", rawProfile.get("name"));
    }
    
    @Test
    void testVerify_Failure() throws Exception {
        String idTokenString = "invalid-id-token";
        when(verifier.verify(idTokenString)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            googleIdTokenService.verify(idTokenString);
        });
    }
}
