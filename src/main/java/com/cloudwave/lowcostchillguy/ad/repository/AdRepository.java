package com.cloudwave.lowcostchillguy.ad.repository;

import com.cloudwave.lowcostchillguy.ad.domain.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdRepository extends JpaRepository<Ad, Long> {
	Optional<Ad> findByPlace(String place);  // Method to find ad by place
}
