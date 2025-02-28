package com.spring.circuit.breaker.application

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CircuitBreakerServiceTest {
    @Autowired
    lateinit var circuitBreakerService: CircuitBreakerService

    @Test
    fun `반복 요청 테스트`() {
        for (i in 1..1000) {
            circuitBreakerService.request()
        }
        circuitBreakerService.printStatus()
    }
}
