package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueObjects.UserRole;
import com.nexusmarket.domain.valueObjects.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {

    private String id;

    private String fullName;

    private String documentId;

    private String email;

    private UserRole role;

    private UserStatus status = UserStatus.ACTIVE;
}
