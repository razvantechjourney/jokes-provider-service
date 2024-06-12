package com.razvanb.jokes.service.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port}")
    private String serverPort;

    @Value("${app.host}")
    private String appHost;

    @Value("${app.contact.email}")
    private String contactEmail;

    @Value("${app.contact.name}")
    private String contactName;

    @Value("${app.contact.url}")
    private String contactUrl;

    @Bean
    public GroupedOpenApi petOpenApi() {
        String[] paths = {"/jokes-service/public/**"};
        return GroupedOpenApi.builder()
                .group("jokes-provider-service")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getInfo())
                .servers(List.of(getDevServer()));
    }

    private Server getDevServer() {
        return new Server()
                .url(STR."http://\{appHost}:\{serverPort}/")
                .description("Server URL in Development environment");
    }

    private Info getInfo() {
        return new Info()
                .title("Jokes Provider Service API")
                .version("1.0")
                .description("API to retrieve random jokes.")
                .contact(getContact());
    }

    private Contact getContact() {
        return new Contact()
                .email(contactEmail)
                .name(contactName)
                .url(contactUrl);
    }
}
