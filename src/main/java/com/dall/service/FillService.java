package com.dall.service;

import com.dall.config.DallConfiguration;
import com.dall.config.GoogleCredentialsConfiguration;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    FillService(
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

    /**
     * Get the links from the Links sheet and follow each one to scrap the ad from daft.ie
     * and then insert the scraped ad in the corresponding sheet
     */
    public void fillSpreadSheet() {
        List<String> links = getLinksToScrap();

        if (links.isEmpty()) {
            log.error("No links to scrap.");
        } else {
            int adCounter = 0;

            for (String link : links) {
                ScrapService.ScrapedAd scrapedAd = scrapService.load(link);

                if (scrapedAd.getRemoved()) {
                    log.error("Ad has been removed from website: {}.", link);
                } else if (duplicateService.exists(scrapedAd.getShortenedLink())) {
                    log.error("Duplicate ad: {}.", link);
                } else {
                    writeService.writeToSheet(scrapedAd.transform());
                    log.info("Added {}.", scrapedAd.getShortenedLink());
                    adCounter++;
                }
            }

            log.info("Added {} ad{}.", adCounter, adCounter > 1 ? "s" : "");
            deleteService.deleteRange(dallConfiguration.getLinksRange(), links.size());
        }
    }

    @SneakyThrows
    private List<String> getLinksToScrap() {
        ValueRange result = spreadsheet.values()
            .get(
                googleCredentialsConfiguration.getSpreadsheetId(),
                dallConfiguration.getLinksRange()
            )
            .execute();

        if (result.getValues() == null) {
            return new ArrayList<>();
        } else {
            return result.getValues()
                .stream()
                .flatMap(Collection::stream)
                .map(Object::toString)
                .collect(Collectors.toList());
        }
    }
}
