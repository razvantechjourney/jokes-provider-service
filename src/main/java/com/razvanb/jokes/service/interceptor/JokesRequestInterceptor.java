package com.razvanb.jokes.service.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JokesRequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        setThreadMetadataToResponseHeaders(response);
        return true;
    }

    private static void setThreadMetadataToResponseHeaders(HttpServletResponse response) {
        Thread currentThread = Thread.currentThread();

        response.addHeader("X-Thread-Is-Virtual", String.valueOf(currentThread.isVirtual()));
        response.addHeader("X-Thread-Name", currentThread.getName());
        response.addHeader("X-Thread-Class", currentThread.getClass().getName());
    }
}
