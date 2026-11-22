package com.example.demo.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoCodioResponse(List<GeoCodioResult> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoCodioResult(
        double accuracy,
        @JsonProperty("accuracy_type") String accuracyType,
        @JsonProperty("formatted_address") String formattedAddress,
        @JsonProperty("address_components") AddressComponents addressComponents
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressComponents(
        String number,
        @JsonProperty("formatted_street") String formattedStreet,
        @JsonProperty("unit_type") String unitType,
        @JsonProperty("unit_number") String unitNumber,
        String city,
        @JsonProperty("state_province") String state,
        @JsonProperty("postal_code") String zip
    ) {}
}
