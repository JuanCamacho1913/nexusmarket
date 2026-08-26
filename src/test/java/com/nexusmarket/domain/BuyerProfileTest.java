package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuyerProfileTest {

    @Test
    void agregarSegundaDireccionDefaultDesmarcaLaAnterior() {
        User user = new User("Ana Gomez", "ana@nexus.com", "secret123", UserRole.COMPRADOR);
        BuyerProfile buyerProfile = new BuyerProfile(user, "Calle Falsa 123");

        ShippingAddress first = ShippingAddress.builder()
                .addressDetails("Av. Siempreviva 742")
                .city("Springfield")
                .zipCode("1000")
                .isDefault(true)
                .build();
        buyerProfile.addShippingAddress(first);

        ShippingAddress second = ShippingAddress.builder()
                .addressDetails("Calle Nueva 456")
                .city("Springfield")
                .zipCode("2000")
                .isDefault(true)
                .build();
        buyerProfile.addShippingAddress(second);

        assertFalse(first.isDefault());
        assertTrue(second.isDefault());
        assertEquals(2, buyerProfile.getShippingAddresses().size());
    }
}
