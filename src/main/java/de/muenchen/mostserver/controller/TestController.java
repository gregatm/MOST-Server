package de.muenchen.mostserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping
    public Mono<String> getHello() {
        return Mono.just("Hello");
    }


}
