package com.increff.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "inventory",
    indexes = {
        @Index(name = "idx_inventory_product_id", columnList = "productId")
    }
)
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "productId", nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private Long quantity;
    
    @Version
    @Column(nullable = false)
    private Integer version = 0; // Set default value to 0
} 