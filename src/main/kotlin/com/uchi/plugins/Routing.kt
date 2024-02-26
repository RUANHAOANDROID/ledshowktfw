package com.uchi.plugins

import com.uchi.db.DataBase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun Application.configureRouting() {
    routing {
        staticResources("/", "web")
        get("/restart") {
            DataBase.toString()
            call.respond(HttpStatusCode.OK, "Success")
        }
        get("/events"){
            call.respondSse(eventsFlow())
        }
    }
}
suspend fun ApplicationCall.respondSse(events: Flow<SseEvent>) {
    respondTextWriter(contentType = ContentType.Text.EventStream) {
        events.collect { event ->
            write(event.toString())
            flush()
        }
    }
}
fun eventsFlow(): Flow<SseEvent> = flow {
    repeat(100000) {
        emit(SseEvent("message", "Event $it"))
        delay(1000)
    }
}

class SseEvent(s: String, s1: String) {

}
