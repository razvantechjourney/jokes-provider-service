package com.razvanb.jokes.service.exception_handler;

import com.razvanb.jokes.service.exception.JokesClientRequestRateExceededException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().value() == 429) {
            throw new JokesClientRequestRateExceededException(STR."""
            \{response.getStatusText()}
            \{new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)}
            """, response.getStatusCode().value());
        } else {
            throw new HttpClientErrorException(response.getStatusCode(), response.getStatusText());
        }
    }
}