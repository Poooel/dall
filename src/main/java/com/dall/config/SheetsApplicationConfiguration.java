package com.dall.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class SheetsApplicationConfiguration {
    private final SheetsConfiguration sheetsConfiguration;

    @Autowired
    public SheetsApplicationConfiguration(SheetsConfiguration sheetsConfiguration) {
        this.sheetsConfiguration = sheetsConfiguration;
    }

    @Bean
    public Sheets getSheets(NetHttpTransport httpTransport, JsonFactory jsonFactory, Credential credentials) {
        return new Sheets
            .Builder(httpTransport, jsonFactory, credentials)
            .setApplicationName(sheetsConfiguration.getApplicationName())
            .build();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    @SneakyThrows
    public SheetsCellConfiguration getSheetsCellConfiguration(ObjectMapper objectMapper) {
        return objectMapper.readValue(
            new File("src/main/resources/sheets-configuration.yaml"), SheetsCellConfiguration.class
        );
    }
}
