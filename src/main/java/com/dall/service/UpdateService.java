package com.dall.service;

import com.dall.config.SheetsCellConfiguration;
import com.dall.config.SheetsConfiguration;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UpdateService {
    private static final String RANGE_FOR_LINKS_TO_UPDATE_CENTER = "Centre!E3:E103";
    private static final String RANGE_FOR_LINKS_TO_UPDATE_OUTSIDE = "Banlieue!E3:E103";

    private final Sheets.Spreadsheets spreadsheet;
    private final SheetsConfiguration sheetsConfiguration;
    private final ScrapService scrapService;
    private final SheetsService sheetsService;
    private final SheetsCellConfiguration sheetsCellConfiguration;
    private final ConfigurationService configurationService;

    @Autowired
    public UpdateService(
        Sheets sheets,
        SheetsConfiguration sheetsConfiguration,
        ScrapService scrapService,
        SheetsService sheetsService,
        SheetsCellConfiguration sheetsCellConfiguration,
        ConfigurationService configurationService
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.sheetsConfiguration = sheetsConfiguration;
        this.scrapService = scrapService;
        this.sheetsService = sheetsService;
        this.sheetsCellConfiguration = sheetsCellConfiguration;
        this.configurationService = configurationService;
    }

    public void updateSpreadSheet() {
        sheetsCellConfiguration.setCenterSheetStart(3);
        sheetsCellConfiguration.setOutsideSheetStart(3);

        configurationService.saveConfiguration(sheetsCellConfiguration);

        log.info("Starting to update ads");

        getLinksToUpdate(RANGE_FOR_LINKS_TO_UPDATE_CENTER)
            .forEach(link -> {
                log.info(link);
                sheetsService.writeToSheet(scrapService.scrapAd(link));
            });

        log.info("Finished updating the center ads");

        getLinksToUpdate(RANGE_FOR_LINKS_TO_UPDATE_OUTSIDE)
            .forEach(link -> {
                log.info(link);
                sheetsService.writeToSheet(scrapService.scrapAd(link));
            });

        log.info("Finished updating the outside ads");
    }

    @SneakyThrows
    private List<String> getLinksToUpdate(String range) {
        ValueRange result = this.spreadsheet
            .values()
            .get(sheetsConfiguration.getSpreadsheetId(), range)
            .execute();

        return result
            .getValues()
            .stream()
            .flatMap(Collection::stream)
            .map(Object::toString)
            .collect(Collectors.toList());
    }
}
