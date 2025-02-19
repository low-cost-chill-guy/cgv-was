package com.cloudwave.lowcostchillguy.movie.repository;

import com.cloudwave.lowcostchillguy.movie.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}