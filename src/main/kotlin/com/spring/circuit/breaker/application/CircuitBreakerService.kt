package com.spring.circuit.breaker.application

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class CircuitBreakerService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {
    private val circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuitBreaker")

    companion object {
        var successCount: AtomicInteger = AtomicInteger(0)
        var error400Count: AtomicInteger = AtomicInteger(0)
        var error500Count: AtomicInteger = AtomicInteger(0)
        var blockedRequestCount: AtomicInteger = AtomicInteger(0)

        private val webClient: WebClient = WebClient.create("http://localhost:10001")
    }

    fun request() {
        val decoratedSupplier: Supplier<Unit> = circuitBreaker.decorateSupplier {
            webClient.get()
                .uri("/api/random-error")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        }

        val result = runCatching { decoratedSupplier.get() }
            .recover { exception ->
                fallback(exception)
                return
            }

        if (result.isSuccess) {
            successCount.incrementAndGet()
            println("circuit breaker state: ${circuitBreaker.state} - [success]")
        }
    }

    fun printStatus() {
        println("===== CURRENT STATUS =====")
        println("Success Count: $successCount")
        println("400 Error Count: $error400Count")
        println("500 Error Count: $error500Count")
        println("Blocked Request Count: $blockedRequestCount")
    }

    private fun fallback(exception: Throwable) {
        when (exception) {
            is WebClientResponseException -> {
                handleStatusCode(exception)
            }

            is CallNotPermittedException -> {
                handleBlockRequest()
            }

            else -> {
                handleDefault()
            }
        }
    }

    private fun handleStatusCode(exception: WebClientResponseException) {
        when (exception.statusCode.value()) {
            400 -> {
                error400Count.incrementAndGet()
                println("circuit breaker state: ${circuitBreaker.state} - [400 error]")
            }

            500 -> {
                error500Count.incrementAndGet()
                println("circuit breaker state: ${circuitBreaker.state} - [500 error]")
            }
        }
    }

    private fun handleBlockRequest() {
        blockedRequestCount.incrementAndGet()
        println("circuit breaker state: ${circuitBreaker.state} - [blocked]")
    }

    private fun handleDefault() {
        println("circuit breaker state: ${circuitBreaker.state} - [default fallback method]")
    }
}
