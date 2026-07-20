package com.campingtripplanner.backend.web;

import com.campingtripplanner.backend.domain.Trip;
import com.campingtripplanner.backend.domain.User;
import com.campingtripplanner.backend.security.CurrentUserProvider;
import com.campingtripplanner.backend.service.TripService;
import com.campingtripplanner.backend.service.WeatherService;
import com.campingtripplanner.backend.web.dto.WeatherResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class WeatherController {

    private final TripService tripService;
    private final WeatherService weatherService;
    private final CurrentUserProvider currentUserProvider;

    public WeatherController(TripService tripService, WeatherService weatherService,
                              CurrentUserProvider currentUserProvider) {
        this.tripService = tripService;
        this.weatherService = weatherService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/current/weather")
    public ResponseEntity<WeatherResponse> getCurrentTripWeather(Authentication authentication) {
        User user = currentUserProvider.require(authentication);
        Trip trip = tripService.getCurrentTrip(user);
        return ResponseEntity.ok(weatherService.getForecastForTrip(trip));
    }
}
