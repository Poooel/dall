package com.dall;

import com.dall.service.FillService;
import com.dall.service.UpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DallApplication implements CommandLineRunner {
    private final FillService fillService;
    private final UpdateService updateService;

    @Autowired
    public DallApplication(FillService fillService, UpdateService updateService) {
        this.fillService = fillService;
        this.updateService = updateService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DallApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length > 1) {
            log.error("Only one mode can be specified.");
        } else if (args.length == 0) {
            log.error("One mode has to be specified (-update or -fill).");
        } else if (args[0].equals("-update") || args[0].equals("-u")) {
            log.info("Updating spreadsheets.");
            updateService.updateSpreadSheet();
            log.info("Update has been run successfully.");
        } else if (args[0].equals("-fill") || args[0].equals("-f")) {
            log.info("Filling spreadsheets.");
            fillService.fillSpreadSheet();
            log.info("Fill has been run successfully.");
        } else {
            log.error("Unknown argument.");
        }
    }
}
