package com.dall.service;

import com.dall.config.DallConfiguration;
import com.dall.config.GoogleCredentialsConfiguration;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FillService {
    private final Sheets.Spreadsheets spreadsheet;
    private final GoogleCredentialsConfiguration googleCredentialsConfiguration;
    private final ScrapService scrapService;
    private final WriteService writeService;
    private final DallConfiguration dallConfiguration;
    private final DeleteService deleteService;
    private final DuplicateService duplicateService;

    @Autowired
    public FillService(
        Sheets sheets,
        GoogleCredentialsConfiguration googleCredentialsConfiguration,
        ScrapService scrapService,
        WriteService writeService,
        DallConfiguration dallConfiguration,
        DeleteService deleteService,
        DuplicateService duplicateService
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.googleCredentialsConfiguration = googleCredentialsConfiguration;
        this.scrapService = scrapService;
        this.writeService = writeService;
        this.dallConfiguration = dallConfiguration;
        this.deleteService = deleteService;
        this.duplicateService = duplicateService;
    }

    public void fillSpreadSheet() {
        List<String> links = getLinksToScrap();

        for (String link : links) {
            ScrapService.ScrapedAd scrapedAd = scrapService.load(link);

            if (scrapedAd.getRemoved()) {
                log.error("Ad has been removed: {}.", link);
            } else if (duplicateService.exists(scrapedAd.getShortenedLink())) {
                log.error("Duplicate ad: {}.", link);
            } else {
                writeService.writeToSheet(scrapedAd.transform());
            }
        }

        deleteService.deleteRange(dallConfiguration.getLinksRange());
    }

    @SneakyThrows
    private List<String> getLinksToScrap() {
        ValueRange result = this.spreadsheet
            .values()
            .get(googleCredentialsConfiguration.getSpreadsheetId(), dallConfiguration.getLinksRange())
            .execute();

        return result.getValues()
            .stream()
            .flatMap(Collection::stream)
            .map(Object::toString)
            .collect(Collectors.toList());
    }
}
