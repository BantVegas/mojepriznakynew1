package com.bantvegas.mojepriznakynew.repository;

import com.bantvegas.mojepriznakynew.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
