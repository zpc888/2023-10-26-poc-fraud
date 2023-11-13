package com.prot.poc.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.prot.poc")
//@EnableScheduling
public class FraudApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudApplication.class, args);
	}

}
