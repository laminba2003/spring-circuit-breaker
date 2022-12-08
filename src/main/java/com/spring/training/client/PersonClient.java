package com.spring.training.client;

import com.spring.training.exception.EntityNotFoundException;
import com.spring.training.model.Person;
import lombok.AllArgsConstructor;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class PersonClient {

    final WebClient client;
    final ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

    public Flux<Person> getPersons() {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("getPersons");
        return circuitBreaker.run(client.get().uri("/persons")
                        .retrieve()
                        .bodyToFlux(Person.class),
                throwable -> FallbackClient.getPersons());
    }

    public Mono<Person> getPerson(Long id) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("getPerson");
        return circuitBreaker.run(client.get().uri("/persons/{id}", id)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("person not found with id : " + id)))
                        .bodyToMono(Person.class),
                throwable -> FallbackClient.getPerson());
    }

    public Mono<Person> createPerson(Person person) {
        return client.post().uri("/persons")
                .body(Mono.just(person), Person.class)
                .retrieve()
                .bodyToMono(Person.class);
    }

    public Mono<Person> updatePerson(Long id, Person person) {
        return client.put().uri("/persons/{id}", id)
                .body(Mono.just(person), Person.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("person not found with id : " + id)))
                .bodyToMono(Person.class);
    }

    public Mono<Void> deletePerson(Long id) {
        return client.delete().uri("/persons/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
