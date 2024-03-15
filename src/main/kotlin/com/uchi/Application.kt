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
import org.jetbrains.exposed.sql.exposedLogger

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
                val limitInfo = UchiServer.getInfo(Constants.AuthCode)
                limitInfo.second.udata?.let {
                    Constants.OUT_COUNT.set(it.outCount)
                    Constants.EXITS_COUNT.set(it.existCount)
                    Constants.IN_COUNT.set(it.inCount)
                    Constants.MAX_COUNT.set(it.limitsCount)
                }
                Constants.LED_DEVICES.forEach {
                    if (!it.connected) {
                        it.connect()
                    }
                }
                Constants.LED_DEVICES.forEach {
                    if (it.connected) {
                        it.setLedContent(Constants.EXITS_COUNT.get(),  Constants.IN_COUNT.get())
                    }
                }
                // send msg
                val inSSE = SseEvent(id = "a", event = "IN", data = "${Constants.IN_COUNT.get()}")
                val existSSE = (SseEvent(id = "b", event = "EXIST", data = "${Constants.EXITS_COUNT.get()}"))
                val maxCount = (SseEvent(id = "b", event = "LIMIT", data = "${Constants.MAX_COUNT.get()}"))

                Constants.WsSessions.values.forEach { session ->
                    println("---send $existSSE")
                    session.send(existSSE.toJson())
                    println("---send $inSSE")
                    session.send(inSSE.toJson())
                    println("---send$maxCount")
                    session.send(maxCount.toJson())
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
                                this.displayMode = it.displayMode.removePrefix("0x").toInt(16).toByte()
                            }
                            Constants.LED_DEVICES.add(LedShow(ledParams))
                        }
                    }
                    return@runCatching
                }

                Constants.LED_DEVICES.forEach { led ->
                    val ledSSE = SseEvent(id = led.ip, event = "LED", data = led.status)
                    Constants.WsSessions.values.forEach { session ->
                        println("---send $ledSSE")
                        session.send(ledSSE.toJson())
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }.onSuccess {
                println("loop job success")
                exposedLogger.debug("aAAAAAA")
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
