package com.business.frontend.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class ApiHelper {

    private ApiHelper() {}

    public static HttpHeaders bearerHeaders(HttpSession session) {
        String token = (String) session.getAttribute("jwt_token");
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    public static <T> T get(RestTemplate rt, String url, Class<T> type, HttpSession session) {
        HttpEntity<Void> entity = new HttpEntity<>(bearerHeaders(session));
        ResponseEntity<T> resp = rt.exchange(url, HttpMethod.GET, entity, type);
        return resp.getBody();
    }
}