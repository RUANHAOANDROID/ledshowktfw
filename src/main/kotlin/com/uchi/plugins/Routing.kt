package com.uchi.plugins

import com.uchi.Constants
import com.uchi.uchiserver.Fail
import com.uchi.uchiserver.Success
import com.uchi.uchiserver.UchiServer
import com.uchi.uchiserver.throwUChiException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Application.configureRouting() {
    val url = "http://limit.api.yyxcloud.com"
    val client = HttpClient(CIO)
    val connections = mutableSetOf<WebSocketSession>() // 用于存储连接的集合
    routing {
//        staticResources("/", "web")
        webSocket("/ws") {
            println("onConnect!")
            connections.add(this)  // 将连接添加到集合中
            try {
                // 如果定时任务还未启动，则启动定时任务
                eventsFlow().collect { event ->
                    println("event send !")
                    connections.forEach { session ->
                        println("event send to $session")
                        session.send(
                            "{\n" +
                                    "  \"id\": \"${event.id}\",\n" +
                                    "  \"event\": \"${event.event}\",\n" +
                                    "  \"data\": \"${event.data}\"\n" +
                                    "}\n"
                        )
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                println("WebSocket connection closed.")
            } finally {
                connections.remove(this) // 在连接关闭时移除连接
            }
        }
        get("/sse") {
            call.respondSse(eventsFlow())
        }
        get("/leds/{authCode}") {
            val authCode = call.parameters["authCode"]
            call.respond(HttpStatusCode.OK, Constants.LED_DEVICES)
        }
        get("/auth/{authCode}") {
            val authCode = call.parameters["authCode"]
            runCatching<String> {
                val response = UchiServer.getInfo("$authCode")
                if (response.code.Fail()) {
                    "景区不存在或其他问题".throwUChiException()
                }
                response.udata.toString()
            }.onSuccess {
                call.respond(HttpStatusCode.OK, it)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError, it.message.toString())
            }
        }
        get("/updateMaxCount/{authCode}") {
            call.respond(HttpStatusCode.OK, "")
        }
        get("/reconnect") {
            Constants.LED_DEVICES.forEach {
                it.reconnect()
            }
            call.respond(HttpStatusCode.OK, "Success")
        }
    }
}

suspend fun ApplicationCall.respondSse(events: Flow<SseEvent>) {
    respondTextWriter(contentType = ContentType.Text.EventStream) {
        events.collect { event ->
            if (event.id != null) {
                write("id: ${event.id}\n")
            }
            if (event.event != null) {
                write("event: ${event.event}\n")
            }
            for (dataLine in event.data.lines()) {
                write("data: $dataLine\n")
            }
            write("\n")
            flush()
            println("send sse :${event}")
        }
    }
}

fun eventsFlow(): Flow<SseEvent> = flow {
    while (true) {
        Constants.LED_DEVICES.forEach { led ->
            emit(SseEvent(id = led.ip, event = "LED", data = led.status))
        }
        delay(2000)
    }
}

