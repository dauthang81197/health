package com.solehealth.user.common.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    public String createdAt;

    @Column(name = "updated_at")
    public String updatedAt;

    @Column(name = "created_date")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdDate = Instant.now();

    @Column(name = "updated_date")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant updatedDate;

    @PrePersist()
    protected void onCreate() {
        this.createdDate = Instant.now();
        this.createdAt = null;
    }

    @PreUpdate()
    protected void onUpdate() {
        this.updatedDate = Instant.now();
        this.updatedAt = null;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof BaseEntity)) return false;
        return id.equals(((BaseEntity) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
