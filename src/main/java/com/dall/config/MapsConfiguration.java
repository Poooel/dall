package com.dall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.maps")
public class MapsConfiguration {
    private String key;
    private String address;
    private String latitude;
    private String longitude;
    private String mode;
    private String units;
    private String format;
}
