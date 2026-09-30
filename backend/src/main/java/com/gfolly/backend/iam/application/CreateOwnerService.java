package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOwnerService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User createOwnerInNewTenant(User templateUser, String tenantId) {
        User newOwner = new User();
        newOwner.setEmail(templateUser.getEmail());
        newOwner.setPasswordHash(templateUser.getPasswordHash());
        newOwner.setFirstName(templateUser.getFirstName());
        newOwner.setLastName(templateUser.getLastName());
        newOwner.setRole(Role.OWNER);
        newOwner.setEmailVerified(true);
        
        User saved = userRepository.save(newOwner);
        userRepository.fixTenantId(saved.getPublicId(), tenantId);
        return saved;
    }
}

