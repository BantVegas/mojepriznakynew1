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
    private String initials;

    public static UserResponse of(User user) {
        String initials = "Ú";

        if (user.getFirstName() != null && user.getLastName() != null &&
                !user.getFirstName().isBlank() && !user.getLastName().isBlank()) {
            initials = (
                    user.getFirstName().substring(0, 1) +
                            user.getLastName().substring(0, 1)
            ).toUpperCase();
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .subscriptionTier(user.getSubscriptionTier().name())
                .initials(initials)
                .build();
    }
}
