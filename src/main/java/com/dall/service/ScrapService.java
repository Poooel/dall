package com.dall.service;

import com.dall.entity.Ad;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ScrapService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d/M/u");

    private static final Map<String, String> transformerLeaseTime = Map.of(
        "Minimum 1 Year", "1 Year",
        "Minimum 6 Months", "6 Months",
        "Minimum 3 Months", "3 Months",
        "No Minimum", "No minimum"
    );

    private static final Map<String, String> transformerPer = Map.of(
        "Per month", "Month",
        "Per week", "Week"
    );

    Ad scrapAd(String link) {
        try {
            Document document = Jsoup.connect(link).get();

            if (getRemoved(document)) {
                return new Ad(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDate.now().toString(),
                    true
                );
            } else {
                return new Ad(
                    getShortenedLink(document),
                    getAddress(document),
                    getDistrict(document),
                    getLeaseTime(document),
                    String.valueOf(getNumberOfBathrooms(document)),
                    getLastModified(document).toString(),
                    String.valueOf(getViews(document)),
                    String.valueOf(getPrice(document)),
                    getPer(document),
                    LocalDate.now().toString(),
                    false
                );
            }
        } catch (IOException e) {
            log.error("Unable to get document", e);
            return null;
        }
    }

    private String getShortenedLink(Document document) {
        return document.select("a[href~=(https://www.daft.ie/)\\d{8}]").first().attr("href");
    }

    private String getAddress(Document document) {
        String address = document.select("#address_box h1").first().ownText();
        return address.substring(0, address.lastIndexOf(","));
    }

    private String getDistrict(Document document) {
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

    private String getLeaseTime(Document document) {
        return transformerLeaseTime.get(document.select(".description_block > div").first().ownText());
    }

    private int getNumberOfBathrooms(Document document) {
        String overview = document.select("#overview > ul > li:nth-child(2)").first().text();

        Pattern pattern = Pattern.compile("\\d{1}(?= Bathroom)");
        Matcher matcher = pattern.matcher(overview);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            log.error("Couldn't find number of bathrooms in the property overview!");
            return 0;
        }
    }

    private LocalDate getLastModified(Document document) {
        String descriptionExtras = document.select("#description .description_extras").first().html();
        String lastModified = descriptionExtras
            .split("<h3>Date Entered/Renewed:</h3>")[1]
            .split("<h3>Property Views:</h3>")[0]
            .split(" ")[0];

        return LocalDate.parse(lastModified, FORMATTER);
    }

    private int getViews(Document document) {
        String descriptionExtras = document.select("#description .description_extras").first().html();
        String views = descriptionExtras
            .split("<h3>Property Views:</h3>")[1]
            .replace(",", "")
            .trim();

        return Integer.parseInt(views);
    }

    private int getPrice(Document document) {
        String priceString = document.select("#smi-price-string").first().text();
        priceString = priceString.substring(1); // Removes the € at the beginning
        priceString = priceString.replace(",", "");
        String price = priceString.split(" ", 2)[0];

        return Integer.parseInt(price);
    }

    private String getPer(Document document) {
        String priceString = document.select("#smi-price-string").first().text();
        return transformerPer.get(priceString.split(" ", 2)[1]);
    }

    private boolean getRemoved(Document document) {
        return !document.select("#agreed").isEmpty() ||
            !document.select(".errorMessages").isEmpty();
    }
}
