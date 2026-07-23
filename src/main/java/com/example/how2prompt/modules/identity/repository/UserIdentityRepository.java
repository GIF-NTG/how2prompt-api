package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    Optional<UserIdentity> findByProviderAndProviderUid(String provider, String providerUid);
}
