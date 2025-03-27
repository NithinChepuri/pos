package com.increff.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(indexes = {
    @Index(name = "idx_inventory_productId", columnList = "productId")
})
public class InventoryEntity extends AbstractEntity {
    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private Long quantity;
} 