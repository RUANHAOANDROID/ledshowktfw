package com.uchi.plugins

import com.uchi.Constants
import com.uchi.uchiserver.*
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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

fun Application.configureRouting() {
    var isRun = AtomicBoolean(false)
    routing {
        staticResources("/", "web")
        webSocket("/ws") {
            println("onConnect!")
            val uid = "${call.parameters["uid"]}"
            Constants.WsSessions[uid] = this  // 将连接添加到集合中
            try {
                launch {
                    while (true){
                        delay(5000)
                    }
                }.join()
//                eventsFlow().collect { event ->
//                    println("event send !")
//                    Constants.WsSessions.forEach { session ->
//                        println("event send to $session")
//                        session.value.send(
//                            "{\n" +
//                                    "  \"id\": \"${event.id}\",\n" +
//                                    "  \"event\": \"${event.event}\",\n" +
//                                    "  \"data\": \"${event.data}\"\n" +
//                                    "}\n"
//                        )
//                    }
//                }
            } catch (e: ClosedReceiveChannelException) {
                println("WebSocket connection closed.")
                Constants.WsSessions.remove(uid,this)
            } finally {
                Constants.WsSessions.remove(uid,this) // 在连接关闭时移除连接
            }
        }
        get("/leds/{authCode}") {
            val authCode = call.parameters["authCode"]
            runCatching<UChiResp<MutableList<LedListData>?>> {
                val resp = UchiServer.ledList("$authCode")
                if (resp.code.Fail()) {
                    "云限流服务返回错误".throwUChiException()
                }
                if (resp.udata == null) {
                    "没有添加LED".throwUChiException()
                }
                saveLedJson(resp.udata)
                resp
            }.onFailure {
                call.respond(HttpStatusCode.OK, respErr(it.message.toString()))
            }.onSuccess {
                call.respond(HttpStatusCode.OK, it)
            }
        }
        get("/auth/{authCode}") {
            val authCode = call.parameters["authCode"]
            runCatching<UChiResp<LimitsInfo?>> {
                val pair = UchiServer.getInfo("$authCode")
                if (pair.second.code.Fail()) {
                    "景区不存在或其他问题".throwUChiException()
                }
                if (pair.second.udata == null) {
                    "Data 返回为空，请联系限流云服务".throwUChiException()
                }
                saveAuthJson(AuthData("$authCode"))
                pair.second
            }.onFailure {
                call.respond(HttpStatusCode.OK, respErr(it.message.toString()))
            }.onSuccess {
                Constants.AuthCode = "$authCode"
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
                call.respond(HttpStatusCode.OK, respSuccess())
            }
        }
        get("/recon/{ip}") {
            val ip = call.parameters["ip"]
            runCatching {
                if (Constants.LED_DEVICES.isNotEmpty()) {
                    Constants.LED_DEVICES.forEach {
                        if (it.ip == ip) {
                            it.reconnect()
                        }
                    }
                }
            }

            call.respond(HttpStatusCode.OK, respSuccess())
        }
        get("/writeJsonToFile") {
            val jsonString = """{"msg":"操作成功","code":200,"data":{"name":"测试景点","limitsCount":555}}"""
            val jsonFile = File("output.json")
            jsonFile.writeText(jsonString)

            call.respondText("JSON written to file.")
        }
    }
}

@Serializable
data class AuthData(val auth: String)

fun saveAuthJson(auth: AuthData) {
    val jsonString = json.encodeToString(auth)
    val jsonFile = File("auth.json")
    jsonFile.writeText(jsonString)
}

fun getAuthJson(): AuthData? {
    val jsonFile = File("auth.json")
    var data: AuthData?
    val str = jsonFile.readText()
    data = json.decodeFromString<AuthData?>(str)
    return data
}

fun saveLedJson(leds: MutableList<LedListData>?) {
    val jsonString = json.encodeToString(leds)
    val jsonFile = File("leds.json")
    jsonFile.writeText(jsonString)
}

fun getLedJson(): MutableList<LedListData>? {
    val jsonFile = File("leds.json")
    var list: MutableList<LedListData>? = mutableListOf<LedListData>()
    val str = jsonFile.readText()
    list = json.decodeFromString<MutableList<LedListData>?>(str)
    return list
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
        if (Constants.AuthCode == "") continue
        if (Constants.LED_DEVICES.isNullOrEmpty()) continue
        emit(SseEvent(id = "a", event = "IN", data = "${Constants.IN_COUNT.get()}"))
        emit(SseEvent(id = "b", event = "EXIST", data = "${Constants.OUT_COUNT.get()}"))
        Constants.LED_DEVICES.forEach { led ->
            emit(SseEvent(id = led.ip, event = "LED", data = led.status))
            delay(200)
        }
        delay(3000)
    }
}

