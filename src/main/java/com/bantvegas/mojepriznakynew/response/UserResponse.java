package com.bantvegas.mojepriznakynew.response;

import com.bantvegas.mojepriznakynew.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String subscriptionTier;

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .subscriptionTier(user.getSubscriptionTier().name())
                .build();
    }
}
