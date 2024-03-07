package com.uchi.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.logging.*

internal val LOGGER = KtorSimpleLogger("com.uchi.plugins.RequestTracePlugin")

val RequestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
    onCall { call ->
        LOGGER.trace("Processing call: ${call.request.uri}")
    }
}