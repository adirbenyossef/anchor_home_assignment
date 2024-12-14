package com.sheet.cellflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.sheet.cellflow.config.CellFlowProperties;

@EnableConfigurationProperties(CellFlowProperties.class)
@SpringBootApplication
public class CellflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(CellflowApplication.class, args);
	}

}