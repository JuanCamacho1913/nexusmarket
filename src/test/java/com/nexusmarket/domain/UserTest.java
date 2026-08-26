package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.UserRole;
import com.nexusmarket.valueObjects.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void fullNameEnBlancoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("   ", "juan@nexus.com", "secret123", UserRole.COMPRADOR));
    }

    @Test
    void emailEnBlancoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Juan Perez", "", "secret123", UserRole.COMPRADOR));
    }

    @Test
    void statusPorDefectoEsActivo() {
        User user = new User("Juan Perez", "juan@nexus.com", "secret123", UserRole.COMPRADOR);

        assertEquals(UserStatus.ACTIVO, user.getStatus());
    }
}
