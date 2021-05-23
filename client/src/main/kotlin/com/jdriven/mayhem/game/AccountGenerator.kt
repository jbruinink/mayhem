package com.jdriven.mayhem.game

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class AccountGenerator {
    private val index = AtomicInteger()

    fun generate(): Account {
        val name = index.incrementAndGet().toString()
        return Account(name, "password", "$name@example.com")
    }
}
