package com.example.demo.service;

import com.example.demo.dto.GoogleRouteResponse;
import com.example.demo.dto.TrackingOrderResponse;
import com.example.demo.dto.RoutePoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleRoutesService {
    private static final String ROUTES_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GoogleRoutesService(
        @Value("${google.maps.api-key:}") String apiKey
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public GoogleRouteResponse calculateRoute(String pickupAddress, List<TrackingOrderResponse> orders) {
        if (apiKey.isBlank() || pickupAddress == null || pickupAddress.isBlank() || orders.isEmpty()) {
            return null;
        }

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("origin", addressPoint(pickupAddress));
            TrackingOrderResponse finalOrder = orders.get(orders.size() - 1);
            requestBody.set("destination", addressPoint(finalOrder.deliveryAddress()));

            ArrayNode intermediates = requestBody.putArray("intermediates");
            for (int index = 0; index < orders.size() - 1; index++) {
                TrackingOrderResponse order = orders.get(index);
                intermediates.add(addressPoint(order.deliveryAddress()));
            }

            requestBody.put("travelMode", "DRIVE");
            requestBody.put("routingPreference", "TRAFFIC_AWARE");
            requestBody.put("optimizeWaypointOrder", true);
            requestBody.put("polylineQuality", "OVERVIEW");
            requestBody.put("polylineEncoding", "ENCODED_POLYLINE");

            HttpRequest request = HttpRequest.newBuilder(URI.create(ROUTES_URL))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline,routes.optimizedIntermediateWaypointIndex,routes.legs.endLocation")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Google Routes API returned HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode route = objectMapper.readTree(response.body()).path("routes").path(0);
            List<Integer> optimizedIndexes = new ArrayList<>();
            route.path("optimizedIntermediateWaypointIndex").forEach(node -> optimizedIndexes.add(node.asInt()));
            List<RoutePoint> stopLocations = new ArrayList<>();
            JsonNode legs = route.path("legs");
            for (int index = 0; index < orders.size(); index++) {
                JsonNode location = legs.path(index).path("endLocation").path("latLng");
                if (location.has("latitude") && location.has("longitude")) {
                    stopLocations.add(new RoutePoint(location.path("latitude").asDouble(), location.path("longitude").asDouble()));
                }
            }

            return new GoogleRouteResponse(
                route.path("distanceMeters").asLong(),
                route.path("duration").asText(null),
                route.path("polyline").path("encodedPolyline").asText(null),
                optimizedIndexes,
                stopLocations
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Google Routes request was interrupted.", exception);
        } catch (Exception exception) {
            String details = exception.getMessage();
            throw new IllegalStateException(
                "Google Routes request failed" + (details == null || details.isBlank() ? "." : ": " + details),
                exception
            );
        }
    }

    private ObjectNode addressPoint(String address) {
        ObjectNode point = objectMapper.createObjectNode();
        point.put("address", address);
        return point;
    }
}
