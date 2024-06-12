package com.razvanb.jokes.service.client;

import com.razvanb.jokes.service.model.JokeData;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class JokesClient {
    private final String jokeApiUrl;
    private final RestTemplate restTemplate;

    public JokesClient(@Value("${app.joke-api.url}") String jokeApiUrl, RestTemplate restTemplate) {
        this.jokeApiUrl = jokeApiUrl;
        this.restTemplate = restTemplate;
    }

    @Retry(name = "jokeApiRetry")
    @RateLimiter(name = "jokeApiRateLimiter")
    public JokeData getRandomJoke() {
        return restTemplate.getForObject(jokeApiUrl, JokeData.class);
    }
}
