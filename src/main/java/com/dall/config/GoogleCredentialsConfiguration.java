package com.dall.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.sheets")
public class GoogleCredentialsConfiguration {
    private String applicationName;
    private String tokenDirectoryPath;
    private String credentialsFilePath;
    private String spreadsheetId;
    private List<String> scopes;
}
