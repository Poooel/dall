package com.dall;

import com.dall.service.FillService;
import com.dall.service.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
		//fillService.fillSpreadSheet();
		updateService.updateSpreadSheet();
	}
}
