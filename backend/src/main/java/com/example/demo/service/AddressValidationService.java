package com.example.demo.service;

import com.example.demo.dto.AddressRequest;
import com.example.demo.dto.AddressValidationResponse;
import com.example.demo.dto.GeoCodioResponse;
import com.example.demo.dto.GeoCodioResponse.AddressComponents;
import com.example.demo.dto.GeoCodioResponse.GeoCodioResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Calls Geocodio's Address Validation API before an order is created. */
@Service
public class AddressValidationService {
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AddressValidationService(@Value("${geocodio.api.key}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public AddressValidationResponse isValid(AddressRequest addressRequest) {
        // Keep local development usable when no Geocodio key is configured.
        if (apiKey.isBlank()) return new AddressValidationResponse(true, null, null, null, null, null);

        try {
            String url = UriComponentsBuilder.fromUriString("https://api.geocod.io/v2.1/geocode")
                .queryParam("street", addressRequest.street())
                .queryParam("city", addressRequest.city())
                .queryParam("state", addressRequest.state())
                .queryParam("postal_code", addressRequest.zip())
                .queryParam("api_key", apiKey)
                .toUriString();

            HttpRequest request = HttpRequest.newBuilder(
                    URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            GeoCodioResponse geoResponse = objectMapper.readValue(response.body(), GeoCodioResponse.class);
                        
            if (geoResponse.results() == null || geoResponse.results().isEmpty()) {
                return new AddressValidationResponse(false, "Could not verify exact street address, no address available.", null, null, null, null);
            }

            GeoCodioResult bestMatch = geoResponse.results().get(0);

            if (bestMatch.accuracy() < 0.8) {
                return new AddressValidationResponse(false, "Could not verify exact street address, too far from existing addresses.", null, null, null, null);
            }

            AddressComponents components = bestMatch.addressComponents();
            String cleanStreet = components.number() + " " + components.formattedStreet();

            return new AddressValidationResponse(
                true,
                "Address verified.",
                cleanStreet,
                components.city(),
                components.state(),
                components.zip()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Geocodio address validation failed", exception);
        }
    }
}
