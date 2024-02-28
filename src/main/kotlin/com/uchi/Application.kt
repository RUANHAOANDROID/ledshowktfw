package com.uchi

import com.uchi.uchiserver.UchiServer
import com.uchi.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*

fun main() {

    val embeddedServer = embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
    val ledJob = Constants.CoroutineScope.launch {
        while (true) {
            delay(1000)
            if (Constants.AuthCode == "") {
                break
            }
            val leds = UchiServer.test(Constants.AuthCode)
//            leds.forEach {
//                it.connect()
//            }
//            println("responseBody")
//            if (leds.isNotEmpty()) {
//                leds.forEach {
//                    runCatching {
//                        it.setLedContent(0, 1)
//                    }
//                }
//            }
        }

    }
    ledJob.start()
    embeddedServer.start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureRouting()
}
