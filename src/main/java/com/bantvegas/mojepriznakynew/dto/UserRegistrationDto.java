package com.bantvegas.mojepriznakynew.dto;

import com.bantvegas.mojepriznakynew.enums.SubscriptionTier;
import lombok.Data;

@Data
public class UserRegistrationDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private SubscriptionTier subscriptionTier = SubscriptionTier.PACIENT; // default
    private String referralCode;
}
