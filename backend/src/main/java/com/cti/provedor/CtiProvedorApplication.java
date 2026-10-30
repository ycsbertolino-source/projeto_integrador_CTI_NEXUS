package com.cti.provedor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication          // liga tudo sozinho
@ConfigurationPropertiesScan    // lê a seção "app" (AppProperties)
public class CtiProvedorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CtiProvedorApplication.class, args);
    }
}
