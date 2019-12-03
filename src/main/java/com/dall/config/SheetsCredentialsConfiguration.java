package com.dall.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

@Configuration
public class SheetsCredentialsConfiguration {
    private final SheetsConfiguration sheetsConfiguration;

    @Autowired
    public SheetsCredentialsConfiguration(SheetsConfiguration sheetsConfiguration) {
        this.sheetsConfiguration = sheetsConfiguration;
    }

    @Bean
    public NetHttpTransport getNetHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    public Credential getCredentials(NetHttpTransport httpTransport, JsonFactory jsonFactory) throws IOException {
        InputStream in = getClass().getResourceAsStream(sheetsConfiguration.getCredentialsFilePath());

        if (in == null) {
            throw new FileNotFoundException("Credentials not found: " + sheetsConfiguration.getCredentialsFilePath());
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
            .Builder(httpTransport, jsonFactory, clientSecrets, sheetsConfiguration.getScopes())
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(sheetsConfiguration.getTokenDirectoryPath())))
            .setAccessType("offline")
            .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
