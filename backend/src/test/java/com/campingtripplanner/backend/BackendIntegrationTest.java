package com.campingtripplanner.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests driven through MockMvc against the real Spring context (web, security,
 * JPA, Flyway) with the in-memory H2 database configured in src/test/resources/application.yml.
 * Each test registers/logs in its own user(s) so tests don't depend on execution order.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class BackendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private int userCounter;

    @BeforeEach
    void resetCounter() {
        userCounter++;
    }

    private String uniqueUsername() {
        return "user" + userCounter + "_" + System.nanoTime();
    }

    private MockHttpSession registerAndLogin(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s"}
                                """.formatted(username, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username));

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s"}
                                """.formatted(username, password))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        return session;
    }

    private String tripRequestJson() {
        return """
                {"origin": "Pittsburgh, PA", "destination": "Cook Forest State Park, PA", "startDate": "2026-08-01", "endDate": "2026-08-03"}
                """;
    }

    @Test
    void registerAndLogin_succeeds() throws Exception {
        registerAndLogin(uniqueUsername(), "password123");
    }

    @Test
    void login_withBadCredentials_returns401() throws Exception {
        String username = uniqueUsername();
        registerAndLogin(username, "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "wrong"}
                                """.formatted(username)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTrip_seedsChecklistItemsAcrossFourCategories() throws Exception {
        MockHttpSession session = registerAndLogin(uniqueUsername(), "password123");

        // The design doc's checklist table (Shelter & Sleeping: 4, Cooking & Food: 5,
        // Clothing: 4, Tools & Safety: 4) sums to 17 items, though its prose summary
        // says "16 default items" - the table is the authoritative seed data.
        MvcResult result = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson())
                        .session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checklist.length()").value(17))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        long categoryCount = 0;
        var categories = new java.util.HashSet<String>();
        for (JsonNode item : body.get("checklist")) {
            categories.add(item.get("category").asText());
        }
        assertThat(categories).hasSize(4);
        assertThat(categories).containsExactlyInAnyOrder(
                "Shelter & Sleeping", "Cooking & Food", "Clothing", "Tools & Safety");
    }

    @Test
    void secondTripCreation_forSameUser_returns409() throws Exception {
        MockHttpSession session = registerAndLogin(uniqueUsername(), "password123");

        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson())
                        .session(session))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson())
                        .session(session))
                .andExpect(status().isConflict());
    }

    @Test
    void togglingChecklistItem_persistsChange() throws Exception {
        MockHttpSession session = registerAndLogin(uniqueUsername(), "password123");

        MvcResult createResult = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson())
                        .session(session))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long itemId = created.get("checklist").get(0).get("id").asLong();

        mockMvc.perform(patch("/api/checklist/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isPacked": true}
                                """)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPacked").value(true));

        mockMvc.perform(get("/api/trips/current").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checklist[?(@.id == %d)].isPacked".formatted(itemId)).value(true));
    }

    @Test
    void wrapUp_deletesTripAndChecklistItems() throws Exception {
        MockHttpSession session = registerAndLogin(uniqueUsername(), "password123");

        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson())
                        .session(session))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/trips/current").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/trips/current").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotAccessAnotherUsersTripOrChecklistItems() throws Exception {
        MockHttpSession sessionA = registerAndLogin(uniqueUsername(), "password123");
        MockHttpSession sessionB = registerAndLogin(uniqueUsername(), "password123");

        MvcResult createResult = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson())
                        .session(sessionA))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long itemId = created.get("checklist").get(0).get("id").asLong();

        // User B has no trip of their own, so /current is a plain 404, not a cross-user leak.
        mockMvc.perform(get("/api/trips/current").session(sessionB))
                .andExpect(status().isNotFound());

        // But B must not be able to toggle A's checklist item.
        mockMvc.perform(patch("/api/checklist/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isPacked": true}
                                """)
                        .session(sessionB))
                .andExpect(status().isForbidden());

        // And B must not be able to wrap up A's trip either.
        mockMvc.perform(delete("/api/trips/current").session(sessionB))
                .andExpect(status().isNotFound());

        // A's trip/item remain untouched.
        mockMvc.perform(get("/api/trips/current").session(sessionA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checklist[?(@.id == %d)].isPacked".formatted(itemId)).value(false));
    }
}
