package com.business.frontend.controller;

import com.business.frontend.util.ApiHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ui")
public class CustomerUiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8085/api";

    @GetMapping("/customers")
    public String getCustomers(
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model, HttpSession session) {

        model.addAttribute("activeTab", "country");
        model.addAttribute("country", country);

        if (country == null || country.isBlank()) return "customers";

        try {
            String url = BASE_URL + "/customers?country=" + country + "&page=" + page + "&size=" + size;
            HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<?, ?> r = resp.getBody();
            model.addAttribute("customers",   r.get("content"));
            model.addAttribute("totalPages",  r.get("totalPages"));
            model.addAttribute("currentPage", r.get("number"));
        } catch (Exception e) {
            model.addAttribute("error", "Could not load customers: " + e.getMessage());
        }
        return "customers";
    }

    @GetMapping("/top")
    public String getTopCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {

        model.addAttribute("activeTab", "top");

        try {
            String url = BASE_URL + "/customers/top?page=" + page + "&size=" + size;
            HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));
            ResponseEntity<List<Map>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<Map>>() {});
            model.addAttribute("customers", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load top customers: " + e.getMessage());
        }
        return "customers";
    }

    @GetMapping("/customers/{id}/orders")
    public String getOrdersByCustomer(
            @PathVariable Integer id, Model model, HttpSession session) {

        model.addAttribute("customerId", id);
        HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));

        try {
            ResponseEntity<List<Map>> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/orders",
                    HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map>>() {});
            model.addAttribute("orders", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/payment/amount",
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("spending", resp.getBody());
        } catch (Exception ignored) {}

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/support",
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("support", resp.getBody());
        } catch (Exception ignored) {}

        return "customer-orders";
    }
    // API 3 - Customer Orders by ID
    @GetMapping("/customer-lookup")
    public String customerLookup(
            @RequestParam(required = false) Integer id,
            Model model, HttpSession session) {

        model.addAttribute("activeTab", "customer-orders");
        if (id == null) return "customer-lookup";

        model.addAttribute("customerId", id);
        HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));

        try {
            // Use Object to handle whatever the backend returns
            ResponseEntity<Object> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/orders",
                    HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Object>() {});
            Object body = resp.getBody();
            if (body instanceof List) {
                model.addAttribute("orders", body);
            } else {
                model.addAttribute("orders", List.of(body));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }
        return "customer-lookup";
    }

    // API 4 - Orders by status for a customer
    @GetMapping("/customer-status-lookup")
    public String customerStatusLookup(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String status,
            Model model, HttpSession session) {

        model.addAttribute("activeTab", "orders-status");
        if (id == null) return "customer-status-lookup";

        model.addAttribute("customerId", id);
        model.addAttribute("selectedStatus", status);
        HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));

        try {
            String url = BASE_URL + "/customers/" + id + "/orders_status"
                    + (status != null ? "?status=" + status : "");
            ResponseEntity<List<Map>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map>>() {});
            model.addAttribute("orders", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load: " + e.getMessage());
        }
        return "customer-status-lookup";
    }

    // API 5 - Customer support rep
    @GetMapping("/customer-support-lookup")
    public String customerSupportLookup(
            @RequestParam(required = false) Integer id,
            Model model, HttpSession session) {

        model.addAttribute("activeTab", "support");
        if (id == null) return "customer-support-lookup";

        model.addAttribute("customerId", id);
        HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/support",
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("support", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load: " + e.getMessage());
        }
        return "customer-support-lookup";
    }

    // API 6 - Customer spending
    @GetMapping("/customer-spending-lookup")
    public String customerSpendingLookup(
            @RequestParam(required = false) Integer id,
            Model model, HttpSession session) {

        model.addAttribute("activeTab", "spending");
        if (id == null) return "customer-spending-lookup";

        model.addAttribute("customerId", id);
        HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/payment/amount",
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("spending", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load: " + e.getMessage());
        }
        return "customer-spending-lookup";
    }

    @GetMapping("/customers/{id}/orders_status")
    public String getOrdersByStatus(
            @PathVariable Integer id,
            @RequestParam(required = false) String status,
            Model model, HttpSession session) {

        model.addAttribute("customerId", id);
        model.addAttribute("selectedStatus", status);

        if (status == null || status.isBlank())
            return "redirect:/ui/customers/" + id + "/orders";

        HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));

        try {
            ResponseEntity<List<Map>> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/orders_status?status=" + status,
                    HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map>>() {});
            model.addAttribute("orders", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/support",
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("support", resp.getBody());
        } catch (Exception ignored) {}

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/customers/" + id + "/payment/amount",
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("spending", resp.getBody());
        } catch (Exception ignored) {}

        return "customer-orders";
    }
}