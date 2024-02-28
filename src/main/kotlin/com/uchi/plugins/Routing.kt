package com.uchi.plugins

import com.uchi.Constants
import com.uchi.uchiserver.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString

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
        get("/leds/{authCode}") {
            val authCode = call.parameters["authCode"]
            runCatching<List<LedListData>?> {
                val resp = UchiServer.ledList("$authCode")
                if (resp.code.Fail()) {
                    "云限流服务返回错误".throwUChiException()
                }
                if (resp.udata == null) {
                    "没有添加LED".throwUChiException()
                }
                resp.udata
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError, respErr(it.message.toString()))
            }.onSuccess {
                call.respond(HttpStatusCode.OK, respSuccess(json.encodeToString(it)))
            }
        }
        get("/auth/{authCode}") {
            val authCode = call.parameters["authCode"]
            runCatching< UChiResp<LimitsInfo?>> {
                val tow = UchiServer.getInfo("$authCode")
                if (tow.second.code.Fail()) {
                    "景区不存在或其他问题".throwUChiException()
                }
                if (tow.second.udata == null) {
                    "Data 返回为空，请联系限流云服务".throwUChiException()
                }
                tow.second
            }.onFailure {
                call.respond(HttpStatusCode.NotFound, respErr(it.message.toString()))
            }.onSuccess {
                call.respond(HttpStatusCode.OK, it)
            }
        }
        get("/updateMaxCount/{authCode}/{count}") {
            val authCode = call.parameters["authCode"]
            val count = call.parameters["count"]
            runCatching {
                UchiServer.updateLimitCount("$authCode", "$count")
            }.onFailure {
                call.respond(HttpStatusCode.OK, respErr(it.message.toString()))
            }.onSuccess {
                call.respond(HttpStatusCode.OK, "")
            }
        }
        get("/reconnect") {
            Constants.LED_DEVICES.forEach {
                it.reconnect()
            }
            call.respond(HttpStatusCode.OK, respSuccess(""))
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

