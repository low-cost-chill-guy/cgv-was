package com.cloudwave.lowcostchillguy.user.repository;


import com.cloudwave.lowcostchillguy.user.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
}