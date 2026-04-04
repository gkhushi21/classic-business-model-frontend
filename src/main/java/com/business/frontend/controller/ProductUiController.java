package com.business.frontend.controller;

import com.business.frontend.util.ApiHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ui/products")
public class ProductUiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8085/api";

    private static final List<String> PRODUCT_LINES = Arrays.asList(
            "Classic Cars", "Motorcycles", "Planes",
            "Ships", "Trains", "Trucks and Buses", "Vintage Cars");

    @GetMapping
    public String getProducts(
            @RequestParam(required = false) String productLine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model, HttpSession session) {

        model.addAttribute("productLines", PRODUCT_LINES);
        model.addAttribute("selectedLine", productLine);

        if (productLine == null || productLine.isBlank()) return "products";

        try {
            String encoded = URLEncoder.encode(productLine, StandardCharsets.UTF_8);
            String url = BASE_URL + "/products?productLine=" + encoded + "&page=" + page + "&size=" + size;
            HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));
            ResponseEntity<Object> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            Object result = resp.getBody();
            if (result instanceof List) {
                model.addAttribute("products", result);
            } else if (result instanceof Map) {
                Object content = ((Map<?, ?>) result).get("content");
                model.addAttribute("products", content != null ? content : result);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Could not load products: " + e.getMessage());
        }
        return "products";
    }
}