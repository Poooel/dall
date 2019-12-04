package com.dall;

import com.dall.service.FillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DallApplication implements CommandLineRunner {
	private final FillService fillService;

	@Autowired
	public DallApplication(FillService fillService) {
		this.fillService = fillService;
	}

	public static void main(String[] args) {
		SpringApplication.run(DallApplication.class, args);
	}

	@Override
	public void run(String... args) {
		fillService.fillSpreadSheet();
	}
}
