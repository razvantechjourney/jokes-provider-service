package com.razvanb.jokes.service.unit_tests.exception_handler;

import com.razvanb.jokes.service.exception.JokesClientRequestRateExceededException;
import com.razvanb.jokes.service.exception_handler.CustomResponseErrorHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomResponseErrorHandlerUnitTest {
    @InjectMocks
    CustomResponseErrorHandler errorHandler;

    @Mock
    private ClientHttpResponse response;

    @Test
    public void givenResponseStatusIsTooManyRequests_whenHandleError_throwJokesClientRequestRateExceededException() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        when(response.getStatusText()).thenReturn("Too Many Requests");
        when(response.getBody()).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        assertThrows(JokesClientRequestRateExceededException.class, () -> errorHandler.handleError(response));
    }

    @Test
    public void givenResponseStatusIsInternalServerError_whenHandleError_throwHttpClientErrorException() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(response.getStatusText()).thenReturn("Internal Server Error");

        assertThrows(HttpClientErrorException.class, () -> errorHandler.handleError(response));
    }
}
