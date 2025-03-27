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
    uniqueConstraints = @UniqueConstraint(columnNames = {"email"}),
    indexes = {
        @Index(name = "idx_user_email", columnList = "email")
    }
)
public class UserEntity extends AbstractEntity {
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
} 