package com.uchi

import com.uchi.led.LedParameters
import com.uchi.led.LedShow
import com.uchi.plugins.*
import com.uchi.uchiserver.UchiServer
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
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
                                this.displayMode=it.displayMode.removePrefix("0x").toInt(16).toByte()
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
                // send msg
                val inSSE = SseEvent(id = "a", event = "IN", data = "${Constants.IN_COUNT.get()}")
                val existSSE = (SseEvent(id = "b", event = "EXIST", data = "${Constants.OUT_COUNT.get()}"))
                Constants.WsSessions.values.forEach { session ->
                    println("---send $existSSE")
                    session.send(existSSE.toJson())
                    println("---send $inSSE")
                    session.send(inSSE.toJson())
                }
                Constants.LED_DEVICES.forEach { led ->
                    val ledSSE = SseEvent(id = led.ip, event = "LED", data = led.status)
                    Constants.WsSessions.values.forEach { session ->
                        println("---send $ledSSE")
                        session.send(ledSSE.toJson())
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
