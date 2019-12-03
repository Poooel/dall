package com.dall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.sheets")
public class SheetsConfiguration {
    private String applicationName;
    private String tokenDirectoryPath;
    private String credentialsFilePath;
    private String spreadsheetId;
    private List<String> scopes;
    private List<Integer> centerDistricts;
    private List<Integer> outsideDistricts;
}
