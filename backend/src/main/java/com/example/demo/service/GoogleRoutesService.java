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
    // Last key an employee entered on the tracking page; lets the robot dispatcher draw routes
    // when GOOGLE_MAPS_API_KEY isn't set on the server.
    private volatile String rememberedApiKey = "";
    // Why the most recent route request failed (null after a success), shown on the tracking page.
    private volatile String lastError;

    public GoogleRoutesService(
        @Value("${google.maps.api-key:}") String apiKey
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public void rememberApiKey(String requestApiKey) {
        if (requestApiKey != null && !requestApiKey.isBlank()) {
            rememberedApiKey = requestApiKey.trim();
        }
    }

    public boolean hasApiKey() {
        return !apiKey.isBlank() || !rememberedApiKey.isBlank();
    }

    public boolean hasServerApiKey() {
        return !apiKey.isBlank();
    }

    public String getLastError() {
        return lastError;
    }

    public GoogleRouteResponse calculateRoute(String requestApiKey, String pickupAddress, List<TrackingOrderResponse> orders) {
        String routeApiKey = requestApiKey != null && !requestApiKey.isBlank()
            ? requestApiKey.trim()
            : !apiKey.isBlank() ? apiKey : rememberedApiKey;

        if (routeApiKey.isBlank() || pickupAddress == null || pickupAddress.isBlank() || orders.isEmpty()) {
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
            // Google rejects optimizeWaypointOrder when there is nothing between origin and destination.
            requestBody.put("optimizeWaypointOrder", orders.size() > 1);
            requestBody.put("polylineQuality", "OVERVIEW");
            requestBody.put("polylineEncoding", "ENCODED_POLYLINE");

            HttpRequest request = HttpRequest.newBuilder(URI.create(ROUTES_URL))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", routeApiKey)
                .header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline,routes.optimizedIntermediateWaypointIndex,routes.legs.endLocation,routes.legs.duration")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                String message = objectMapper.readTree(response.body()).path("error").path("message").asText(response.body());
                throw new IllegalStateException("Google Routes API returned HTTP " + response.statusCode() + ": " + message);
            }

            JsonNode route = objectMapper.readTree(response.body()).path("routes").path(0);
            List<Integer> optimizedIndexes = new ArrayList<>();
            route.path("optimizedIntermediateWaypointIndex").forEach(node -> optimizedIndexes.add(node.asInt()));
            List<RoutePoint> stopLocations = new ArrayList<>();
            List<Long> legDurations = new ArrayList<>();
            JsonNode legs = route.path("legs");
            for (int index = 0; index < orders.size(); index++) {
                JsonNode location = legs.path(index).path("endLocation").path("latLng");
                if (location.has("latitude") && location.has("longitude")) {
                    stopLocations.add(new RoutePoint(location.path("latitude").asDouble(), location.path("longitude").asDouble()));
                }
                legDurations.add(parseSeconds(legs.path(index).path("duration").asText("0s")));
            }

            lastError = null;
            return new GoogleRouteResponse(
                route.path("distanceMeters").asLong(),
                route.path("duration").asText(null),
                route.path("polyline").path("encodedPolyline").asText(null),
                optimizedIndexes,
                stopLocations,
                legDurations
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Google Routes request was interrupted.", exception);
        } catch (Exception exception) {
            String details = exception.getMessage();
            lastError = details;
            throw new IllegalStateException(
                "Google Routes request failed" + (details == null || details.isBlank() ? "." : ": " + details),
                exception
            );
        }
    }

    // Google returns durations like "754s".
    static long parseSeconds(String duration) {
        try {
            return Math.round(Double.parseDouble(duration.replace("s", "")));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private ObjectNode addressPoint(String address) {
        ObjectNode point = objectMapper.createObjectNode();
        point.put("address", address);
        return point;
    }
}
