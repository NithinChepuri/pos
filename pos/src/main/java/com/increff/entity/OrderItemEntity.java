package com.increff.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"orderId", "productId"}),
    indexes = {
        @Index(name = "idx_order_item_orderId", columnList = "orderId"),
        @Index(name = "idx_order_item_productId", columnList = "productId")
    }
)
public class OrderItemEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;
}