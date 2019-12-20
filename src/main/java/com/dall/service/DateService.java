package com.dall.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class DateService {
    private final DateTimeFormatter dateTimeFormatter;

    DateService() {
        dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/u", Locale.FRANCE);
    }

    public String getNow() {
        return LocalDate.now().format(dateTimeFormatter);
    }

    public String format(String date) {
        LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
        return localDate.format(dateTimeFormatter);
    }
}
