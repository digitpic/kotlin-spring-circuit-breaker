package com.spring.circuit.breaker.global.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import java.time.Duration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClientResponseException

@Configuration
class ResilienceConfig {
    @Bean
    fun circuitBreakerConfig(): CircuitBreakerConfig {
        return CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .minimumNumberOfCalls(1)
            .slidingWindowSize(10)
            .failureRateThreshold(15F)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .permittedNumberOfCallsInHalfOpenState(1)
            .recordExceptions(WebClientResponseException.InternalServerError::class.java)
            .ignoreExceptions(WebClientResponseException.BadRequest::class.java)
            .build()
    }

    @Bean
    fun circuitBreakerRegistry(
        circuitBreakerConfig: CircuitBreakerConfig,
    ): CircuitBreakerRegistry {
        return CircuitBreakerRegistry.of(circuitBreakerConfig)
    }
}
