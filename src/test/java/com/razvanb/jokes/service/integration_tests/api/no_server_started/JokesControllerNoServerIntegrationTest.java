package com.razvanb.jokes.service.integration_tests.api.no_server_started;

import com.razvanb.jokes.service.domain.JokeDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JokesControllerNoServerIntegrationTest {
    private final String BASE_URL = "/jokes-service/public";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${app.jokes-limit}")
    private int jokesLimit;

    @BeforeEach
    void dbCleanup() {
        mongoTemplate.remove(new Query(), JokeDocument.class);
        assertTrue(mongoTemplate.findAll(JokeDocument.class).isEmpty());
    }

    @Test
    public void givenCountNotProvided_whenGetJokes_returnDefaultNumberOfJokes() throws Exception {
        mockMvc.perform(get(STR."\{BASE_URL}/v1/jokes")
                        .param("count", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].type").isString())
                .andExpect(jsonPath("$[0].setup").isString())
                .andExpect(jsonPath("$[0].punchline").isString());
    }

    @Test
    public void givenJokesCountNotExceedingLimit_whenGetJokes_thenReturnRequestedNumberOfJokes() throws Exception {
        mockMvc.perform(get(STR."\{BASE_URL}/v1/jokes")
                        .param("count", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void givenJokesCountExceedsTheLimit_whenGetJokes_throwError() throws Exception {
        int exceedingLimitCount = jokesLimit + 1;
        mockMvc.perform(get(STR."\{BASE_URL}/v1/jokes")
                        .param("count", String.valueOf(exceedingLimitCount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(STR."You can get no more than \{jokesLimit} jokes at a time.")));
    }

    @Test
    public void givenJokesCountLessThanOne_whenGetJokes_throwError() throws Exception {
        mockMvc.perform(get(STR."\{BASE_URL}/v1/jokes")
                        .param("count", "-3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Requested less than one joke.")));
    }

    @Test
    public void givenJInvalidJokesCount_whenGetJokes_throwError() throws Exception {
        mockMvc.perform(get(STR."\{BASE_URL}/v1/jokes")
                        .param("count", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("An unexpected error occurred")));
    }

}
