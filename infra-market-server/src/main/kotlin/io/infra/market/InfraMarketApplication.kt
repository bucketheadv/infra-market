package io.infra.market

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InfraMarketApplication

fun main(args: Array<String>) {
    runApplication<InfraMarketApplication>(*args)
}
