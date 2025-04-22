package com.bantvegas.mojepriznakynew.model;

import com.bantvegas.mojepriznakynew.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // ⚠️ Fix na problém s rezervovaným slovom
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private SubscriptionTier subscriptionTier;

    private int aiUsageCount;

    private boolean enabled;

    private String referringDoctorCode;
}
