package com.increff.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"barcode"}),
    indexes = {
        @Index(name = "idx_product_barcode", columnList = "barcode")
    }
)
public class ProductEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String barcode;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private BigDecimal mrp;
} 