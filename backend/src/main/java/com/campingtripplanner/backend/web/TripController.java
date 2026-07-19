package com.campingtripplanner.backend.web;

import com.campingtripplanner.backend.domain.ChecklistItem;
import com.campingtripplanner.backend.domain.Trip;
import com.campingtripplanner.backend.domain.User;
import com.campingtripplanner.backend.security.CurrentUserProvider;
import com.campingtripplanner.backend.service.ChecklistService;
import com.campingtripplanner.backend.service.TripService;
import com.campingtripplanner.backend.web.dto.TripRequest;
import com.campingtripplanner.backend.web.dto.TripResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;
    private final ChecklistService checklistService;
    private final CurrentUserProvider currentUserProvider;

    public TripController(TripService tripService, ChecklistService checklistService,
                           CurrentUserProvider currentUserProvider) {
        this.tripService = tripService;
        this.checklistService = checklistService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@RequestBody TripRequest request, Authentication authentication) {
        User user = currentUserProvider.require(authentication);
        Trip trip = tripService.createTrip(user, request);
        List<ChecklistItem> items = checklistService.getItemsForTrip(trip.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TripResponse.from(trip, items));
    }

    @GetMapping("/current")
    public ResponseEntity<TripResponse> getCurrentTrip(Authentication authentication) {
        User user = currentUserProvider.require(authentication);
        Trip trip = tripService.getCurrentTrip(user);
        List<ChecklistItem> items = checklistService.getItemsForTrip(trip.getId());
        return ResponseEntity.ok(TripResponse.from(trip, items));
    }

    @DeleteMapping("/current")
    public ResponseEntity<Void> deleteCurrentTrip(Authentication authentication) {
        User user = currentUserProvider.require(authentication);
        tripService.deleteCurrentTrip(user);
        return ResponseEntity.noContent().build();
    }
}
