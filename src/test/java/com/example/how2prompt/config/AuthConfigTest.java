package com.example.how2prompt.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthConfigTest {

    @Test
    void googleIdTokenVerifier_NoClientId() {
        AuthConfig config = new AuthConfig();
        AuthProperties props = new AuthProperties();
        props.setGoogleClientId(null);
        
        GoogleIdTokenVerifier verifier = config.googleIdTokenVerifier(props);
        assertNull(verifier);
        
        props.setGoogleClientId("");
        verifier = config.googleIdTokenVerifier(props);
        assertNull(verifier);
    }

    @Test
    void googleIdTokenVerifier_WithClientId() {
        AuthConfig config = new AuthConfig();
        AuthProperties props = new AuthProperties();
        props.setGoogleClientId("test-client-id");
        
        GoogleIdTokenVerifier verifier = config.googleIdTokenVerifier(props);
        assertNotNull(verifier);
        assertEquals("test-client-id", verifier.getAudience().iterator().next());
    }
}
