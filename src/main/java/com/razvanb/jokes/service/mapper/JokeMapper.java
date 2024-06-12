package com.razvanb.jokes.service.mapper;

import com.razvanb.jokes.service.domain.JokeDocument;
import com.razvanb.jokes.service.model.JokeData;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JokeMapper {
    public JokeDocument mapToDocument(JokeData jokeData) {
        return new JokeDocument(
                UUID.randomUUID().toString(),
                jokeData.id(),
                jokeData.type(),
                jokeData.setup(),
                jokeData.punchline());
    }
}
