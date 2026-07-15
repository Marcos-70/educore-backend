package com.api.educore.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, int[]> attempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (request.getRequestURI().equals("/api/auth/login") && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = request.getRemoteAddr();
            long now = System.currentTimeMillis();
            int[] record = attempts.compute(ip, (key, val) -> {
                if (val == null || now - val[1] > 60000) {
                    return new int[]{1, (int) now};
                }
                val[0]++;
                val[1] = (int) now;
                return val;
            });

            if (record[0] > 5) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Muitas tentativas. Tente novamente em 1 minuto.\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
