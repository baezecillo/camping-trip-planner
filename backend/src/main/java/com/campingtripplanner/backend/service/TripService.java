package com.campingtripplanner.backend.service;

import com.campingtripplanner.backend.domain.ChecklistItem;
import com.campingtripplanner.backend.domain.Trip;
import com.campingtripplanner.backend.domain.User;
import com.campingtripplanner.backend.exception.TripNotFoundException;
import com.campingtripplanner.backend.repository.ChecklistItemRepository;
import com.campingtripplanner.backend.repository.TripRepository;
import com.campingtripplanner.backend.web.dto.TripRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TripService {

    private record SeedItem(String name, String category) {
    }

    private static final List<SeedItem> DEFAULT_CHECKLIST = List.of(
            new SeedItem("Tent", "Shelter & Sleeping"),
            new SeedItem("Sleeping bag", "Shelter & Sleeping"),
            new SeedItem("Sleeping pad", "Shelter & Sleeping"),
            new SeedItem("Pillow", "Shelter & Sleeping"),
            new SeedItem("Camp stove", "Cooking & Food"),
            new SeedItem("Cooler", "Cooking & Food"),
            new SeedItem("Water container", "Cooking & Food"),
            new SeedItem("Matches/lighter", "Cooking & Food"),
            new SeedItem("Food & snacks", "Cooking & Food"),
            new SeedItem("Rain jacket", "Clothing"),
            new SeedItem("Warm layer", "Clothing"),
            new SeedItem("Extra socks", "Clothing"),
            new SeedItem("Sturdy shoes", "Clothing"),
            new SeedItem("First aid kit", "Tools & Safety"),
            new SeedItem("Flashlight/headlamp", "Tools & Safety"),
            new SeedItem("Multi-tool", "Tools & Safety"),
            new SeedItem("Map/compass", "Tools & Safety")
    );

    private final TripRepository tripRepository;
    private final ChecklistItemRepository checklistItemRepository;

    public TripService(TripRepository tripRepository, ChecklistItemRepository checklistItemRepository) {
        this.tripRepository = tripRepository;
        this.checklistItemRepository = checklistItemRepository;
    }

    @Transactional
    public Trip createTrip(User user, TripRequest request) {
        Trip trip = new Trip();
        trip.setUser(user);
        trip.setOrigin(request.origin());
        trip.setDestination(request.destination());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        // Relies on uq_trips_one_active_per_user: a second trip for the same user
        // throws DataIntegrityViolationException here, mapped to 409 globally.
        final Trip savedTrip = tripRepository.save(trip);

        List<ChecklistItem> items = DEFAULT_CHECKLIST.stream()
                .map(seed -> {
                    ChecklistItem item = new ChecklistItem();
                    item.setTrip(savedTrip);
                    item.setItemName(seed.name());
                    item.setCategory(seed.category());
                    item.setPacked(false);
                    return item;
                })
                .toList();
        checklistItemRepository.saveAll(items);

        return savedTrip;
    }

    @Transactional(readOnly = true)
    public Trip getCurrentTrip(User user) {
        return tripRepository.findByUserId(user.getId())
                .orElseThrow(() -> new TripNotFoundException("No active trip for user " + user.getUsername()));
    }

    @Transactional
    public void deleteCurrentTrip(User user) {
        Trip trip = getCurrentTrip(user);
        // ON DELETE CASCADE on checklist_items.trip_id removes the items at the DB level.
        tripRepository.delete(trip);
    }
}
