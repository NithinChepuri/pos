package com.increff.entity;

import javax.persistence.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import com.increff.model.enums.Role;

@Getter
@Setter
@Entity
@Table(
    name = "pos_users", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"email"}),
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role")
    }
)
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    //todo: remove
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
} 