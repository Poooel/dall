package com.dall.service;

import com.dall.config.SheetsCellConfiguration;
import com.dall.config.SheetsConfiguration;
import com.dall.entity.Ad;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SheetsService {
    private final Sheets.Spreadsheets spreadsheet;
    private final SheetsConfiguration sheetsConfiguration;
    private final SheetsCellConfiguration sheetsCellConfiguration;
    private final ConfigurationService configurationService;

    @Autowired
    public SheetsService(
        Sheets sheets,
        SheetsConfiguration sheetsConfiguration,
        SheetsCellConfiguration sheetsCellConfiguration,
        ConfigurationService configurationService
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.sheetsConfiguration = sheetsConfiguration;
        this.sheetsCellConfiguration = sheetsCellConfiguration;
        this.configurationService = configurationService;
    }

    void writeToSheet(Ad ad) {
        int districtNumber = Integer.parseInt(ad.getDistrict().split(" ")[1]);

        if (sheetsConfiguration.getCenterDistricts().contains(districtNumber)) {
            writeToCenter(ad);
        } else {
            writeToOutside(ad);
        }
    }

    @SneakyThrows
    void deleteRange(String range) {
        spreadsheet
            .values()
            .update(sheetsConfiguration.getSpreadsheetId(), range, createValueRange(getDeleteObject()))
            .setValueInputOption("USER_ENTERED")
            .execute();
    }

    private List<List<Object>> getDeleteObject() {
        List<List<Object>> deleteObject = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            deleteObject.add(new ArrayList<>(Collections.singletonList("")));
        }

        return deleteObject;
    }

    private void writeToCenter(Ad ad) {
        String range = String.format("Centre!E%d:N%d", sheetsCellConfiguration.getCenterSheetStart(), sheetsCellConfiguration.getCenterSheetStart());
        sheetsCellConfiguration.setCenterSheetStart(sheetsCellConfiguration.getCenterSheetStart() + 1);
        configurationService.saveConfiguration(sheetsCellConfiguration);
        write(ad, range);
    }

    private void writeToOutside(Ad ad) {
        String range = String.format("Banlieue!E%d:N%d", sheetsCellConfiguration.getOutsideSheetStart(), sheetsCellConfiguration.getOutsideSheetStart());
        sheetsCellConfiguration.setOutsideSheetStart(sheetsCellConfiguration.getOutsideSheetStart() + 1);
        configurationService.saveConfiguration(sheetsCellConfiguration);
        write(ad, range);
    }

    @SneakyThrows
    private void write(Ad ad, String range) {
        List<List<Object>> body = createBodyFromAd(ad);
        ValueRange valueRange = createValueRange(body);

        UpdateValuesResponse response = spreadsheet
            .values()
            .update(sheetsConfiguration.getSpreadsheetId(), range, valueRange)
            .setValueInputOption("USER_ENTERED")
            .execute();

        log.info("Added {}.", ad.getShortenedLink());
    }

    private List<List<Object>> createBodyFromAd(Ad ad) {
        return Collections.singletonList(
            Arrays.asList(
                ad.getShortenedLink(),
                ad.getAddress(),
                ad.getDistrict(),
                ad.getLeaseTime(),
                ad.getBathrooms(),
                ad.getLastModified(),
                ad.getViews(),
                ad.getPrice(),
                ad.getPer(),
                ad.getDateAdded()
            )
        );
    }

    private ValueRange createValueRange(List<List<Object>> body) {
        return new ValueRange().setValues(body);
    }
}
