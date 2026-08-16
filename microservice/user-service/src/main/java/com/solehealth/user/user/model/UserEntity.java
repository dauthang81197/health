package com.solehealth.user.user.model;

import com.solehealth.user.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity {
    @Column()
    private String username;

    @Column()
    private String password;

    @Column()
    private String email;

    @Column()
    private String role;

    @Column()
    private String status;

    @Column()
    private String token;

    @Column()
    private String refreshToken;

    @Column()
    private String profileImage;

    @Column()
    private String phoneNumber;

    @Column()
    private String address;

    @Column()
    private String gender;

}