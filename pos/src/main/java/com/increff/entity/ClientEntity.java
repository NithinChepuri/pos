package com.increff.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "name"}),
    indexes = {
        @Index(name = "idx_client_email", columnList = "email"),
        @Index(name = "idx_client_name", columnList = "name")
    }
)
public class ClientEntity extends AbstractEntity {
    //id        
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;
} 