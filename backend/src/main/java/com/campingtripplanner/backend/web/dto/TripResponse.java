package com.campingtripplanner.backend.web.dto;

import com.campingtripplanner.backend.domain.ChecklistItem;
import com.campingtripplanner.backend.domain.Trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record TripResponse(
        Long id,
        String origin,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        long daysUntilStart,
        List<ChecklistItemResponse> checklist
) {

    public static TripResponse from(Trip trip, List<ChecklistItem> items) {
        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), trip.getStartDate());
        List<ChecklistItemResponse> checklist = items.stream()
                .map(ChecklistItemResponse::from)
                .toList();

        return new TripResponse(
                trip.getId(),
                trip.getOrigin(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                daysUntilStart,
                checklist
        );
    }
}
