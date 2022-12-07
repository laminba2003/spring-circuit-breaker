package com.spring.training.client;

import com.spring.training.exception.EntityNotFoundException;
import com.spring.training.model.Country;
import lombok.AllArgsConstructor;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class CountryClient {

    final WebClient client;
    final ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

    public Flux<Country> getCountries() {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("getCountries");
        return circuitBreaker.run(client.get().uri("/countries")
                        .retrieve()
                        .bodyToFlux(Country.class),
                throwable -> Flux.just(new Country()));
    }

    public Mono<Country> getCountry(String name) {
        return client.get().uri("/countries/{name}", name)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("country not found with name : " + name)))
                .bodyToMono(Country.class);
    }

    public Mono<Country> createCountry(Country country) {
        return client.post().uri("/countries")
                .body(Mono.just(country), Country.class)
                .retrieve()
                .bodyToMono(Country.class);
    }

    public Mono<Country> updateCountry(String name, Country country) {
        return client.put().uri("/countries/{name}", name)
                .body(Mono.just(country), Country.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("country not found with name : " + name)))
                .bodyToMono(Country.class);
    }

    public Mono<Void> deleteCountry(String name) {
        return client.delete().uri("/countries/{name}", name)
                .retrieve()
                .bodyToMono(Void.class);
    }

}
