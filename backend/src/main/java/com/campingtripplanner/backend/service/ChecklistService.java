package com.campingtripplanner.backend.service;

import com.campingtripplanner.backend.domain.ChecklistItem;
import com.campingtripplanner.backend.domain.User;
import com.campingtripplanner.backend.repository.ChecklistItemRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChecklistService {

    private final ChecklistItemRepository checklistItemRepository;

    public ChecklistService(ChecklistItemRepository checklistItemRepository) {
        this.checklistItemRepository = checklistItemRepository;
    }

    @Transactional(readOnly = true)
    public List<ChecklistItem> getItemsForTrip(Long tripId) {
        return checklistItemRepository.findByTripIdOrderById(tripId);
    }

    @Transactional
    public ChecklistItem updatePacked(User user, Long itemId, boolean isPacked) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                // A non-existent item "doesn't belong to the caller's trip" either,
                // per the API contract, so both cases map to the same 403.
                .orElseThrow(() -> new AccessDeniedException("Checklist item not accessible"));

        if (!item.getTrip().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Checklist item not accessible");
        }

        item.setPacked(isPacked);
        return item;
    }
}
