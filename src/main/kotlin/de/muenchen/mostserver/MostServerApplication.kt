package de.muenchen.mostserver

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@OpenAPIDefinition(
    info = Info(
        title = "MOST SensorThings API",
        version = "1.0",
        description = "Munich Open SensorThings Server supporting the SensorThings API with OData"
    )
)
@SpringBootApplication
class MostServerApplication

fun main(args: Array<String>) {
    runApplication<MostServerApplication>(*args)
}

