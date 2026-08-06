package com.example.how2prompt.config;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtConfigTest {

    @Test
    void jwtKeyPair_FallbackToEphemeral() throws Exception {
        JwtConfig config = new JwtConfig();
        JwtProperties props = new JwtProperties();
        
        KeyPair keyPair = config.jwtKeyPair(props);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPrivate());
        assertNotNull(keyPair.getPublic());
        
        RSAPrivateKey privateKey = config.jwtPrivateKey(keyPair);
        RSAPublicKey publicKey = config.jwtPublicKey(keyPair);
        assertNotNull(privateKey);
        assertNotNull(publicKey);
    }
    
    @Test
    void jwtKeyPair_WithValidKeys() throws Exception {
        JwtConfig config = new JwtConfig();
        JwtProperties props = new JwtProperties();
        
        // Generate valid keys for test
        java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        
        String pubPem = "-----BEGIN PUBLIC KEY-----\n" + 
                        Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()) +
                        "\n-----END PUBLIC KEY-----";
        String privPem = "-----BEGIN PRIVATE KEY-----\n" + 
                         Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()) +
                         "\n-----END PRIVATE KEY-----";
                         
        props.setPublicKeyPem(pubPem);
        props.setPrivateKeyPem(privPem);
        
        KeyPair keyPair = config.jwtKeyPair(props);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPrivate());
        assertNotNull(keyPair.getPublic());
    }
    
    @Test
    void jwtKeyPair_PartialKeys() throws Exception {
        JwtConfig config = new JwtConfig();
        JwtProperties props = new JwtProperties();
        
        props.setPublicKeyPem("some-key");
        props.setPrivateKeyPem(null);
        
        KeyPair keyPair = config.jwtKeyPair(props);
        assertNotNull(keyPair);
        
        props.setPublicKeyPem(null);
        props.setPrivateKeyPem("some-key");
        
        KeyPair keyPair2 = config.jwtKeyPair(props);
        assertNotNull(keyPair2);
    }
}
