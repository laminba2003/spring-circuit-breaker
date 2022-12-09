# Spring Cloud Circuit Breaker

The concept of Circuit Breaker comes from Electrical Engineering. In most electricity networks, circuit breakers are switches that protect the network from damage caused by an overload of current or short circuits.

Similarly, in software, a circuit breaker stops the call to a remote service if we know the call to that remote service is either going to fail or time out. The advantage of this is to save resources and be proactive in our troubleshooting of the remote procedure calls.

The circuit breaker makes the decision of stopping the call based on the previous history of the calls. But there are alternative ways how it can handle the calls. Usually, it will keep track of previous calls. Suppose 4 out of 5 calls have failed or timed out, then the next call will fail. This helps to be more proactive in handling the errors with the calling service and the caller service can handle the response in a different way, allowing users to experience the application differently than an error page.

Another way a circuit breaker can act is if calls to remote service are failing in particular time duration.  A circuit breaker will open and will not allow the next call till remote service improves on error.

Until recently, Spring Cloud only provided us one way to add circuit breakers in our applications. This was through the use of Netflix Hystrix as part of the Spring Cloud Netflix project.

The Spring Cloud Netflix project is really just an annotation-based wrapper library around Hystrix. Therefore, these two libraries are tightly-coupled. This means we can't switch to another circuit breaker implementation without changing the application.

The Spring Cloud Circuit Breaker project solves this. It provides an abstraction layer across different circuit breaker implementations. It's a pluggable architecture. So, we can code against the provided abstraction/interface and switch to another implementation based on our needs.

For this project, we'll focus only on the **Resilience4J** implementation.

## Setup

There are two starters for the Resilience4J implementations, one for reactive applications and one for non-reactive applications.

- org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j - non-reactive applications

- org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j - reactive applications

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```


## Configuration

To provide a default configuration for all of your circuit breakers, you need to create a Customizer bean that is passed a Resilience4JCircuitBreakerFactory or ReactiveResilience4JCircuitBreakerFactory. The configureDefault method can be used to provide a default configuration.

```java
@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build()).build());
}
```

## Sending Request

The ReactiveCircuitBreakerFactory.create method will create an instance of a class called ReactiveCircuitBreaker. The run method takes with a Mono or Flux and wraps it in a circuit breaker. 
You can optionally profile a fallback function which will be called if the circuit breaker is tripped and will be passed the Throwable that caused the failure.

```java
ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("getCountries");
return circuitBreaker.run(client.get().uri("/countries")
                .retrieve()
                .bodyToFlux(Country.class),
        throwable -> Flux.just(new Country()));
```

### REST endpoints

| HTTP verb | Resource  | Description
|----|---|---|
|  GET  | /persons  | retrieve list and information of persons  
|  GET |  /persons/{id} | retrieve information of a person specified by {id}
|  POST | /persons  | create a new person with payload  
|  PUT   |  /persons/{id} | update a person with payload   
|  DELETE   | /persons/{id}  |  delete a person specified by {id} 
|  GET  | /countries  | retrieve list and information of countries  
|  GET |  /countries/{name} | retrieve information of a country specified by {name} 
|  POST | /countries  | create a new country with payload  
|  PUT   |  /countries/{name} | update a country with payload   
|  DELETE   | /countries/{name}  |  delete a country specified by {name} 
