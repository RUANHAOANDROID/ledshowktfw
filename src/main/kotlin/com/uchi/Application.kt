package com.uchi

import com.uchi.led.LedParameters
import com.uchi.led.LedShow
import com.uchi.plugins.*
import com.uchi.uchiserver.UchiServer
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*

fun main() {

    val embeddedServer = embeddedServer(Netty, port = 6688, host = "0.0.0.0", module = Application::module)
    val ledJob = Constants.CoroutineScope.launch {
        while (true) {
            delay(3000)
            runCatching {
                if (Constants.AuthCode == "") {
                    getAuthJson()?.let {
                        Constants.AuthCode = it.auth
                    }
                    return@runCatching
                }
                if (Constants.LED_DEVICES.isNullOrEmpty()) {
                    getLedJson()?.let {
                        it.forEach {
                            val ledParams = LedParameters().apply {
                                this.title = it.name
                                this.ip = it.ip
                                this.x = it.x
                                this.y = it.y
                                this.port = it.port
                                this.height = it.h
                                this.width = it.w
                                this.fontSize = it.fontSize
                            }
                            Constants.LED_DEVICES.add(LedShow(ledParams))
                        }
                    }
                    return@runCatching
                }
                val inCount = UchiServer.inCount(Constants.AuthCode)
                inCount.udata?.let { Constants.IN_COUNT.set(it) }
                delay(500)
                val existCount = UchiServer.existCount(Constants.AuthCode)
                existCount.udata?.let { Constants.OUT_COUNT.set(it) }
                Constants.LED_DEVICES.forEach {
                    if (!it.connected) {
                        it.connect()
                    }
                }
                Constants.LED_DEVICES.forEach {
                    if (it.connected) {
                        it.setLedContent(0, 1)
                    }
                }
            }.onFailure {
                println(it)
            }.onSuccess {
                println("led success")
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
