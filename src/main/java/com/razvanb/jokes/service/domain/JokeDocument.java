package com.razvanb.jokes.service.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jokes")
public record JokeDocument(@Id String id, long externalId, String type, String setup, String punchline) {
}
