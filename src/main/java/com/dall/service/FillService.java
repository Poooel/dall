package com.dall.service;

import com.dall.config.SheetsConfiguration;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FillService {
    private static final String RANGE_FOR_LINKS_TO_FOLLOW = "Links!A1:A100";

    private final Sheets.Spreadsheets spreadsheet;
    private final SheetsConfiguration sheetsConfiguration;
    private final ScrapService scrapService;
    private final SheetsService sheetsService;

    @Autowired
    public FillService(
        Sheets sheets,
        SheetsConfiguration sheetsConfiguration,
        ScrapService scrapService,
        SheetsService sheetsService
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.sheetsConfiguration = sheetsConfiguration;
        this.scrapService = scrapService;
        this.sheetsService = sheetsService;
    }

    public void fillSpreadSheet() {
        getLinksToFollow().forEach(link -> sheetsService.writeToSheet(scrapService.scrapAd(link)));
        sheetsService.deleteRange(RANGE_FOR_LINKS_TO_FOLLOW);
    }

    @SneakyThrows
    private List<String> getLinksToFollow() {
        ValueRange result = this.spreadsheet
            .values()
            .get(sheetsConfiguration.getSpreadsheetId(), RANGE_FOR_LINKS_TO_FOLLOW)
            .execute();

        return result
            .getValues()
            .stream()
            .flatMap(Collection::stream)
            .map(Object::toString)
            .collect(Collectors.toList());
    }
}
