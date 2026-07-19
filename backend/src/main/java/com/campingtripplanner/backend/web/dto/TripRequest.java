package com.campingtripplanner.backend.web.dto;

import java.time.LocalDate;

public record TripRequest(String origin, String destination, LocalDate startDate, LocalDate endDate) {
}
