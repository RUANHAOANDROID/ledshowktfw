package com.uchi

import com.uchi.http.UchiServer
import com.uchi.led.LedShow
import com.uchi.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*

fun main() {
    val coroutineScope = CoroutineScope(Dispatchers.Default)
    val embeddedServer = embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)

    val ledJob = coroutineScope.launch {

        val leds = UchiServer().getLEDList()
        leds.forEach {
            it.connect()
        }
        while (true) {
            delay(10000)
            println("responseBody")
            if (leds.isNotEmpty()) {
                leds.forEach {
                    runCatching {
                        it.setLedContent(0, 1)
                    }
                }
            }
        }

    }
    ledJob.start()
    embeddedServer.start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureRouting()
}
