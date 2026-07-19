package com.campingtripplanner.backend.repository;

import com.campingtripplanner.backend.domain.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findByTripIdOrderById(Long tripId);
}
