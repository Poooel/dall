package com.dall.config;

import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.maps")
public class MapsConfiguration {
    private String key;
    private String address;
    private double latitude;
    private double longitude;
    private TravelMode mode;
    private Unit units;
}
