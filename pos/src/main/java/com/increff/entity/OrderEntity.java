package com.increff.entity;

import javax.persistence.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import com.increff.model.enums.OrderStatus;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderEntity extends AbstractEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "invoice_path")
    private String invoicePath;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "client_id")
    private Long clientId;
} 