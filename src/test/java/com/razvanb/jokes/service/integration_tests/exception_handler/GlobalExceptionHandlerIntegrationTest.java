package com.razvanb.jokes.service.integration_tests.exception_handler;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class GlobalExceptionHandlerIntegrationTest {
    private final String BASE_URL = "/jokes-service/public";
    @Autowired
    private MockMvc mockMvc;

    @Value("${app.jokes-limit}")
    private int jokesLimit;

    @Test
    public void givenInvalidCount_whenGetJokes_thenReturnInternalServerError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(STR."\{BASE_URL}/v1/jokes?count=abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void givenCountLessThanOne_whenGetStatistics_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(STR."\{BASE_URL}/v1/jokes?count=-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Requested less than one joke."));
    }

    @Test
    public void givenCountExceedUpperLimit_whenGetStatistics_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(STR."\{BASE_URL}/v1/jokes?count=\{1 + jokesLimit}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(STR."You can get no more than \{jokesLimit} jokes at a time."));
    }
}
