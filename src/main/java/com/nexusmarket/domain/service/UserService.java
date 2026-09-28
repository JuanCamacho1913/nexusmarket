package com.nexusmarket.domain.service;

import com.nexusmarket.domain.models.BuyerProfile;
import com.nexusmarket.domain.models.SellerProfile;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.BuyerProfileRepositoryPort;
import com.nexusmarket.domain.ports.out.SellerProfileRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.exception.DuplicateEntityException;
import com.nexusmarket.domain.exception.EntityNotFoundException;
import com.nexusmarket.domain.exception.UnauthorizedOperationException;
import com.nexusmarket.domain.exception.ValidationException;
import com.nexusmarket.domain.service.support.IdGenerator;
import com.nexusmarket.domain.valueObjects.UserRole;
import com.nexusmarket.domain.valueObjects.UserStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort userRepository;
    private final BuyerProfileRepositoryPort buyerProfileRepository;
    private final SellerProfileRepositoryPort sellerProfileRepository;
    private final IdGenerator idGenerator;

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEntityException("User with email '" + user.getEmail() + "' already exists");
        }
        if (userRepository.existsByDocumentId(user.getDocumentId())) {
            throw new DuplicateEntityException(
                    "User with documentId '" + user.getDocumentId() + "' already exists");
        }
        user.setId(idGenerator.newId());
        return userRepository.save(user);
    }

    public BuyerProfile registerBuyer(BuyerProfile buyerProfile) {
        if (buyerProfile.getUser() == null) {
            throw new ValidationException("BuyerProfile.user must not be null");
        }
        buyerProfile.setId(idGenerator.newId());
        return buyerProfileRepository.save(buyerProfile);
    }

    public SellerProfile registerSeller(String adminUserId, SellerProfile sellerProfile) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("User '" + adminUserId + "' not found"));
        if (admin.getRole() != UserRole.ADMINISTRATOR || admin.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedOperationException(
                    "User '" + adminUserId + "' is not authorized to register a seller");
        }
        sellerProfile.setId(idGenerator.newId());
        return sellerProfileRepository.save(sellerProfile);
    }
}
