package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.CommercialStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buyer_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "shippingAddresses")
public class BuyerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String mainAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommercialStatus commercialStatus;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToMany(mappedBy = "buyerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShippingAddress> shippingAddresses = new ArrayList<>();

    public BuyerProfile(User user, String mainAddress) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario asociado no puede ser nulo");
        }
        this.user = user;
        this.mainAddress = mainAddress;
        this.commercialStatus = CommercialStatus.ACTIVE;
        this.shippingAddresses = new ArrayList<>();
    }

    /**
     * Agrega una dirección de envío manteniendo la invariante de que solo puede
     * existir una única dirección marcada como default a la vez: si la nueva
     * dirección es default, se desmarcan todas las existentes.
     */
    public void addShippingAddress(ShippingAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("La dirección no puede ser nula");
        }
        if (address.isDefault()) {
            shippingAddresses.forEach(existing -> existing.setDefault(false));
        }
        address.setBuyerProfile(this);
        shippingAddresses.add(address);
    }
}
