package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.ReturnStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // La validación de que este User tenga role = ADMINISTRATOR se realiza en la
    // capa de servicio (no en la entidad): la entidad no conoce reglas de autorización.
    @ManyToOne
    @JoinColumn(name = "administrator_id", nullable = false)
    private User administrator;

    public ReturnRequest(String reason, Order order, User administrator) {
        if (order == null) {
            throw new IllegalArgumentException("La orden no puede ser nula");
        }
        if (administrator == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }
        this.reason = reason;
        this.order = order;
        this.administrator = administrator;
        this.status = ReturnStatus.REQUESTED;
    }
}
