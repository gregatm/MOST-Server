package de.muenchen.mostserver;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

//@SpringBootApplication
//@EnableR2dbcRepositories
public class MostServerApplication2 {

    public static void main(String[] args) {
        SpringApplication.run(MostServerApplication2.class, args);
    }
}
