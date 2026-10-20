package com.crowdsense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.crowdsense.config")
public class CrowdSenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrowdSenseApplication.class, args);
    }
}
