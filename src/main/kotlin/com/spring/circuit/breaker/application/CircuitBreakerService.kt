package com.spring.circuit.breaker.application

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class CircuitBreakerService(
    private val webClient: WebClient = WebClient.create("http://localhost:10001"),
) {
    companion object {
        var successCount: AtomicInteger = AtomicInteger(0)
        var error400Count: AtomicInteger = AtomicInteger(0)
        var error500Count: AtomicInteger = AtomicInteger(0)
        var blockedRequestCount: AtomicInteger = AtomicInteger(0)
    }

    @CircuitBreaker(name = "circuitBreaker", fallbackMethod = "fallback")
    fun request() {
        webClient.get()
            .uri("/api/random-error")
            .retrieve()
            .bodyToMono(String::class.java)
            .block()

        successCount.incrementAndGet()
        println("success")
    }

    fun printStatus() {
        println("===== CURRENT STATUS =====")
        println("Success Count: $successCount")
        println("400 Error Count: $error400Count")
        println("500 Error Count: $error500Count")
        println("Blocked Request Count: $blockedRequestCount")
    }

    private fun fallback(exception: WebClientResponseException) {
        when (exception.statusCode.value()) {
            400 -> {
                error400Count.incrementAndGet()
                println("400 error")
            }

            500 -> {
                error500Count.incrementAndGet()
                println("500 error")
            }
        }
    }

    private fun fallback(exception: CallNotPermittedException) {
        blockedRequestCount.incrementAndGet()
        println("blocked")
    }

    private fun fallback(throwable: Throwable) {
        println("default fallback method")
    }
}
