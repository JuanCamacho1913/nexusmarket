package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(String id);

    boolean existsByEmail(String email);

    boolean existsByDocumentId(String documentId);
}
