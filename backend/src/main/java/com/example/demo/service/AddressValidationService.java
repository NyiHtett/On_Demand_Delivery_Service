package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Calls Google's Address Validation API before an order is created. */
@Service
public class AddressValidationService {
    private static final String VALIDATION_URL = "https://addressvalidation.googleapis.com/v1:validateAddress";

    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AddressValidationService(@Value("${google.maps.api-key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public boolean isValid(String address) {
        // Keep local development usable when no Google key is configured.
        if (apiKey.isBlank()) return true;

        try {
            ObjectNode body = objectMapper.createObjectNode();
            ObjectNode postalAddress = body.putObject("address");
            ArrayNode addressLines = postalAddress.putArray("addressLines");
            addressLines.add(address);
            postalAddress.put("regionCode", "US");
            body.put("enableUspsCass", true);

            String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create(VALIDATION_URL + "?key=" + encodedKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) return false;

            JsonNode result = objectMapper.readTree(response.body()).path("result");
            JsonNode verdict = result.path("verdict");
            return verdict.path("addressComplete").asBoolean(false)
                && !verdict.path("hasUnconfirmedComponents").asBoolean(false)
                && !verdict.path("hasInferredComponents").asBoolean(false);
        } catch (Exception exception) {
            throw new IllegalStateException("Google address validation failed", exception);
        }
    }
}
