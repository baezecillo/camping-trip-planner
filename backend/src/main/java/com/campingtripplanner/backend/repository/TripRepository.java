package com.campingtripplanner.backend.repository;

import com.campingtripplanner.backend.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Optional<Trip> findByUserId(Long userId);
}
