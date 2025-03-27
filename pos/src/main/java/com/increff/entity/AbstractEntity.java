package com.increff.entity;

import javax.persistence.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)  
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
} 