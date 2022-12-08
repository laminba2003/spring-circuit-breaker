package com.spring.training.client;

import com.spring.training.model.Country;
import com.spring.training.model.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FallbackClient {

    public static Flux<Country> getCountries() {
        return Flux.just(
                new Country("Spain", "Madrid", 1234488809),
                new Country("Senegal", "Dakar", 123989899),
                new Country("France", "Paris", 1234486576)
        );
    }

    public static Mono<Country> getCountry() {
        return Mono.just(new Country("Spain", "Madrid", 1234488809));
    }

    public static Flux<Person> getPersons() {
        Country country = new Country("France", "Paris", 1234486576);
        return Flux.just(
                new Person(1L, "John", "Doe", country),
                new Person(2L, "Mark", "Lane", country),
                new Person(3L, "Isaac", "Newton", country)
        );
    }

    public static Mono<Person> getPerson() {
        Country country = new Country("France", "Paris", 1234486576);
        return Mono.just(new Person(1L, "John", "Doe", country));
    }

}
