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
import com.google.api.services.sheets.v4.Sheets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleSheetsConfiguration {
    @Bean
    NetHttpTransport getNetHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    JsonFactory getJsonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    Credential getCredentials(
        NetHttpTransport httpTransport,
        JsonFactory jsonFactory,
        GoogleCredentialsConfiguration googleCredentialsConfiguration
    ) throws IOException {
        InputStream in = getClass()
            .getResourceAsStream(googleCredentialsConfiguration.getCredentialsFilePath());

        if (in == null) {
            throw new FileNotFoundException(
                "Credentials not found: " + googleCredentialsConfiguration.getCredentialsFilePath()
            );
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets
            .load(jsonFactory, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
            .Builder(
                httpTransport,
                jsonFactory,
                clientSecrets,
                googleCredentialsConfiguration.getScopes()
            )
            .setDataStoreFactory(
                new FileDataStoreFactory(
                    new java.io.File(
                        googleCredentialsConfiguration.getTokenDirectoryPath()
                    )
                )
            )
            .setAccessType("offline")
            .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Bean
    Sheets getSheets(
        NetHttpTransport httpTransport,
        JsonFactory jsonFactory,
        Credential credentials,
        GoogleCredentialsConfiguration googleCredentialsConfiguration
    ) {
        return new Sheets
            .Builder(httpTransport, jsonFactory, credentials)
            .setApplicationName(googleCredentialsConfiguration.getApplicationName())
            .build();
    }
}
