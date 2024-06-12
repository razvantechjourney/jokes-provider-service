package com.razvanb.jokes.service.configuration;

import com.razvanb.jokes.service.interceptor.JokesRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JokesRequestInterceptor jokesRequestInterceptor;

    public WebMvcConfig(JokesRequestInterceptor jokesRequestInterceptor) {
        this.jokesRequestInterceptor = jokesRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jokesRequestInterceptor);
    }
}