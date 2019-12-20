package com.dall.service;

import com.dall.config.GoogleCredentialsConfiguration;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeleteService {
    private final Sheets.Spreadsheets spreadsheet;
    private final GoogleCredentialsConfiguration googleCredentialsConfiguration;

    DeleteService(
        Sheets sheets,
        GoogleCredentialsConfiguration googleCredentialsConfiguration
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.googleCredentialsConfiguration = googleCredentialsConfiguration;
    }

    @SneakyThrows
    void deleteRange(String range, int linksToDelete) {
        spreadsheet
            .values()
            .update(
                googleCredentialsConfiguration.getSpreadsheetId(),
                range,
                new ValueRange().setValues(getDeleteObject())
            )
            .setValueInputOption("USER_ENTERED")
            .execute();

        log.info("Deleted {} link{}.", linksToDelete, linksToDelete > 1 ? "s" : "");
    }

    private List<List<Object>> getDeleteObject() {
        List<List<Object>> deleteObject = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            deleteObject.add(new ArrayList<>(Collections.singletonList("")));
        }

        return deleteObject;
    }
}
