package com.dall.service;

import com.dall.entity.Ad;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ScrapService {
    private final DateService dateService;

    @Autowired
    public ScrapService(DateService dateService) {
        this.dateService = dateService;
    }

    public ScrapedAd load(String link) {
        try {
            Document document = Jsoup.connect(link).get();
            return new ScrapedAd(document, dateService);
        } catch (IOException e) {
            log.error("Unable to scrap ad.", e);
            return null;
        }
    }

    static class ScrapedAd {
        private static final Map<String, String> transformerLeaseTime = ImmutableMap.of(
            "Minimum 1 Year", "1 Year",
            "Minimum 6 Months", "6 Months",
            "Minimum 3 Months", "3 Months",
            "No Minimum", "No minimum"
        );

        private static final Map<String, String> transformerPer = ImmutableMap.of(
            "Per month", "Month",
            "Per week", "Week"
        );

        private final Document document;
        private final DateService dateService;

        private ScrapedAd(Document document, DateService dateService) {
            this.document = document;
            this.dateService = dateService;
        }

        public String getShortenedLink() {
            return document.select("a[href~=(https://www.daft.ie/)\\d{8}]").first().attr("href");
        }

        public String getAddress() {
            String address = document.select("#address_box h1").first().ownText();
            return address.substring(0, address.lastIndexOf(","));
        }

        public String getDistrict() {
            String address = document.select("#address_box h1").first().ownText();

            Pattern pattern = Pattern.compile("(Dublin )\\d+");
            Matcher matcher = pattern.matcher(address);

            if (matcher.find()) {
                return matcher.group();
            } else {
                log.error("Couldn't find district in the address!");
                return "Unknown district";
            }
        }

        public String getLeaseTime() {
            return transformerLeaseTime.get(document.select(".description_block > div").first().ownText());
        }

        public String getNumberOfBathrooms() {
            String overview = document.select("#overview > ul > li:nth-child(2)").first().text();

            Pattern pattern = Pattern.compile("\\d{1}(?= Bathroom)");
            Matcher matcher = pattern.matcher(overview);

            if (matcher.find()) {
                return matcher.group();
            } else {
                log.error("Couldn't find number of bathrooms in the property overview!");
                return "";
            }
        }

        public String getLastModified() {
            String descriptionExtras = document.select("#description .description_extras").first().html();
            String lastModified = descriptionExtras
                .split("<h3>Date Entered/Renewed:</h3>")[1]
                .split("<h3>Property Views:</h3>")[0]
                .split(" ")[0];

            return dateService.format(lastModified);
        }

        public String getViews() {
            String descriptionExtras = document.select("#description .description_extras").first().html();

            return descriptionExtras
                .split("<h3>Property Views:</h3>")[1]
                .replace(",", "")
                .trim();
        }

        public String getPrice() {
            String priceString = document.select("#smi-price-string").first().text();
            priceString = priceString.substring(1); // Removes the € at the beginning
            priceString = priceString.replace(",", "");

            return priceString.split(" ", 2)[0];
        }

        public String getPer() {
            String priceString = document.select("#smi-price-string").first().text();
            return transformerPer.get(priceString.split(" ", 2)[1]);
        }

        public boolean getRemoved() {
            return !document.select("#agreed").isEmpty() ||
                !document.select(".errorMessages").isEmpty();
        }

        public Ad transform() {
            return new Ad(
                getShortenedLink(),
                getAddress(),
                getDistrict(),
                getLeaseTime(),
                getNumberOfBathrooms(),
                getLastModified(),
                getViews(),
                getPrice(),
                getPer(),
                dateService.getNow(),
                getRemoved()
            );
        }
    }
}
