package com.dall.service;

import com.dall.config.SheetsCellConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ConfigurationService {
    private final ObjectMapper objectMapper;

    @Autowired
    public ConfigurationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public void saveConfiguration(SheetsCellConfiguration sheetsCellConfiguration) {
        objectMapper.writeValue(new File("src/main/resources/sheets-configuration.yaml"), sheetsCellConfiguration);
    }
}
