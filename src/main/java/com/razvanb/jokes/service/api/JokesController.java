package com.razvanb.jokes.service.api;

import com.razvanb.jokes.service.model.JokeData;
import com.razvanb.jokes.service.service.JokesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jokes-service/public")
@Tag(name = "Jokes Service", description = "API to provide jokes")
public class JokesController {
    private static final Logger logger = LoggerFactory.getLogger(JokesController.class);
    private final JokesService jokesService;

    public JokesController(JokesService jokesService) {
        this.jokesService = jokesService;
    }

    @Operation(summary = "Retrieve random jokes",
            description = "Random jokes are fetched from an external API in a concurrent manner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched jokes",
                    content = @Content(array =
                    @ArraySchema(schema = @Schema(implementation = JokeData.class)))),
            @ApiResponse(responseCode = "400", description = "Either the requested jokes exceeds the maximum limit, or are less than one"),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")
    })
    @GetMapping("/v1/jokes")
    public ResponseEntity<List<JokeData>> getJokes(
            @Parameter(name = "count",
                    description = "Defaults to DESC if not specified",
                    example = "12",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) Integer count) throws Throwable {
        logger.debug("Handling request in thread: {}", Thread.currentThread());

        return ResponseEntity.ok(jokesService.getAndPersistJokes(count));
    }
}
