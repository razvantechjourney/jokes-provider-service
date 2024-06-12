package com.razvanb.jokes.service.repository;

import com.razvanb.jokes.service.domain.JokeDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JokesRepository {
    private final MongoTemplate mongoTemplate;

    public JokesRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveAll(List<JokeDocument> jokeDocuments) {
        mongoTemplate.insertAll(jokeDocuments);
    }
}
