package com.increff.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products", 
    uniqueConstraints = @UniqueConstraint(name = "unique_barcode", columnNames = {"barcode"}))
public class ProductEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String barcode;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private BigDecimal mrp;
} 