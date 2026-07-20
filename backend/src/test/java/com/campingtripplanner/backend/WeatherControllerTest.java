package com.campingtripplanner.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the four scenarios from docs/step2-extension/02-design.md's Test Plan by mocking the
 * RestTemplate's underlying HTTP layer (via MockRestServiceServer) so the suite never calls the
 * live Open-Meteo APIs, while still exercising the real geocoding-response/forecast-response
 * parsing code.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate weatherRestTemplate;

    private MockRestServiceServer mockServer;
    private int userCounter;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(weatherRestTemplate).build();
        userCounter++;
    }

    private String uniqueUsername() {
        return "weatheruser" + userCounter + "_" + System.nanoTime();
    }

    private MockHttpSession registerAndLogin() throws Exception {
        String username = uniqueUsername();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "password123"}
                                """.formatted(username)))
                .andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "password123"}
                                """.formatted(username))
                        .session(session))
                .andExpect(status().isOk());
        return session;
    }

    private void createTrip(MockHttpSession session, LocalDate startDate, LocalDate endDate) throws Exception {
        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"origin": "Pittsburgh, PA", "destination": "Cook Forest State Park, PA", "startDate": "%s", "endDate": "%s"}
                                """.formatted(startDate, endDate))
                        .session(session))
                .andExpect(status().isCreated());
    }

    private void expectGeocodingSuccess() {
        mockServer.expect(request -> assertThat(request.getURI().toString())
                        .contains("geocoding-api.open-meteo.com"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"results": [{"latitude": 41.34, "longitude": -79.22}]}
                        """, MediaType.APPLICATION_JSON));
    }

    private void expectGeocodingZeroResults() {
        mockServer.expect(request -> assertThat(request.getURI().toString())
                        .contains("geocoding-api.open-meteo.com"))
                .andRespond(withSuccess("""
                        {"results": []}
                        """, MediaType.APPLICATION_JSON));
    }

    private void expectForecastSuccess(LocalDate matchingDate) {
        String time = LocalDate.now().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        mockServer.expect(request -> assertThat(request.getURI().toString())
                        .contains("api.open-meteo.com/v1/forecast"))
                .andRespond(withSuccess("""
                        {
                          "daily": {
                            "time": ["%s"],
                            "weather_code": [3],
                            "temperature_2m_max": [24.5],
                            "temperature_2m_min": [15.2],
                            "precipitation_probability_max": [20]
                          }
                        }
                        """.formatted(matchingDate), MediaType.APPLICATION_JSON));
    }

    private void expectForecastFailure() {
        mockServer.expect(request -> assertThat(request.getURI().toString())
                        .contains("api.open-meteo.com/v1/forecast"))
                .andRespond(withServerError());
    }

    @Test
    void tripWithin16Days_returnsRealForecast() throws Exception {
        MockHttpSession session = registerAndLogin();
        LocalDate startDate = LocalDate.now().plusDays(3);
        createTrip(session, startDate, startDate.plusDays(2));

        expectGeocodingSuccess();
        expectForecastSuccess(startDate);

        mockMvc.perform(get("/api/trips/current/weather").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.date").value(startDate.toString()))
                .andExpect(jsonPath("$.weatherCode").value(3))
                .andExpect(jsonPath("$.temperatureMaxC").value(24.5))
                .andExpect(jsonPath("$.temperatureMinC").value(15.2))
                .andExpect(jsonPath("$.precipitationProbabilityMax").value(20));

        mockServer.verify();
    }

    @Test
    void tripBeyond16Days_returnsUnavailable() throws Exception {
        MockHttpSession session = registerAndLogin();
        LocalDate startDate = LocalDate.now().plusDays(30);
        createTrip(session, startDate, startDate.plusDays(2));

        expectGeocodingSuccess();
        // Forecast horizon never includes a date 30 days out - only today's date is returned.
        expectForecastSuccess(LocalDate.now());

        mockMvc.perform(get("/api/trips/current/weather").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.date").doesNotExist());

        mockServer.verify();
    }

    @Test
    void geocodingZeroResults_returnsUnavailable() throws Exception {
        MockHttpSession session = registerAndLogin();
        LocalDate startDate = LocalDate.now().plusDays(3);
        createTrip(session, startDate, startDate.plusDays(2));

        expectGeocodingZeroResults();

        mockMvc.perform(get("/api/trips/current/weather").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        mockServer.verify();
    }

    @Test
    void forecastApiFailure_returnsUnavailableNot500() throws Exception {
        MockHttpSession session = registerAndLogin();
        LocalDate startDate = LocalDate.now().plusDays(3);
        createTrip(session, startDate, startDate.plusDays(2));

        expectGeocodingSuccess();
        expectForecastFailure();

        mockMvc.perform(get("/api/trips/current/weather").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        mockServer.verify();
    }

    @Test
    void weatherEndpoint_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/trips/current/weather"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void weatherEndpoint_withoutActiveTrip_returns404() throws Exception {
        MockHttpSession session = registerAndLogin();

        mockMvc.perform(get("/api/trips/current/weather").session(session))
                .andExpect(status().isNotFound());
    }
}
