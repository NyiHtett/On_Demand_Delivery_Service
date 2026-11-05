package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.dto.GoogleRouteResponse;
import com.example.demo.dto.TrackingOrderResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GoogleRoutesServiceTest {
    private HttpServer fakeGoogleServer;

    @BeforeEach
    void startFakeGoogleServer() throws IOException {
        fakeGoogleServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    }

    @AfterEach
    void stopFakeGoogleServer() {
        if (fakeGoogleServer != null) {
            fakeGoogleServer.stop(0);
        }
    }

    @Test
    void forwardsRequestApiKeyAndParsesGoogleResponse() {
        AtomicReference<String> receivedApiKey = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();

        fakeGoogleServer.createContext("/computeRoutes", exchange -> {
            receivedApiKey.set(exchange.getRequestHeaders().getFirst("X-Goog-Api-Key"));
            receivedBody.set(new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
            ));

            sendResponse(exchange, """
                {
                  "routes": [
                    {
                      "distanceMeters": 12500,
                      "duration": "754s",
                      "polyline": {
                        "encodedPolyline": "test-polyline"
                      },
                      "legs": [
                        {
                          "duration": "754s",
                          "endLocation": {
                            "latLng": {
                              "latitude": 37.3382,
                              "longitude": -121.8863
                            }
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        });
        fakeGoogleServer.start();

        GoogleRoutesService service = new GoogleRoutesService(
            "server-key",
            HttpClient.newHttpClient(),
            "http://localhost:" + fakeGoogleServer.getAddress().getPort() + "/computeRoutes"
        );

        GoogleRouteResponse result = service.calculateRoute(
            "request-key",
            "150 E San Fernando St, San Jose, CA 95112",
            List.of(order("1 Washington Square, San Jose, CA 95192"))
        );

        assertEquals("request-key", receivedApiKey.get());
        assertNotNull(receivedBody.get());
        assertEquals(true, receivedBody.get().contains("150 E San Fernando St"));
        assertEquals(true, receivedBody.get().contains("1 Washington Square"));

        assertEquals(12500, result.distanceMeters());
        assertEquals("754s", result.duration());
        assertEquals("test-polyline", result.encodedPolyline());
        assertEquals(List.of(754L), result.legDurationSeconds());
        assertEquals(37.3382, result.stopLocations().get(0).latitude());
        assertEquals(-121.8863, result.stopLocations().get(0).longitude());
    }

    @Test
    void usesServerApiKeyWhenRequestKeyIsMissing() {
        AtomicReference<String> receivedApiKey = new AtomicReference<>();
        fakeGoogleServer.createContext("/computeRoutes", exchange -> {
            receivedApiKey.set(exchange.getRequestHeaders().getFirst("X-Goog-Api-Key"));
            sendResponse(exchange, minimalRouteResponse());
        });
        fakeGoogleServer.start();

        GoogleRoutesService service = new GoogleRoutesService(
            "server-key",
            HttpClient.newHttpClient(),
            "http://localhost:" + fakeGoogleServer.getAddress().getPort() + "/computeRoutes"
        );

        service.calculateRoute(null, "Store Address", List.of(order("Customer Address")));

        assertEquals("server-key", receivedApiKey.get());
    }

    private TrackingOrderResponse order(String address) {
        return new TrackingOrderResponse(
            1L,
            "Test Customer",
            address,
            "Shipping",
            new BigDecimal("10.0"),
            null,
            null,
            null,
            null
        );
    }

    private static String minimalRouteResponse() {
        return """
            {
              "routes": [
                {
                  "distanceMeters": 1000,
                  "duration": "60s",
                  "legs": [
                    {
                      "duration": "60s",
                      "endLocation": {
                        "latLng": {
                          "latitude": 37.0,
                          "longitude": -121.0
                        }
                      }
                    }
                  ]
                }
              ]
            }
            """;
    }

    private static void sendResponse(HttpExchange exchange, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
