package com.increff.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clients", 
    uniqueConstraints = @UniqueConstraint(name = "unique_email", columnNames = {"email"}))
public class ClientEntity extends AbstractEntity {
    //add name as unique
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;
} 