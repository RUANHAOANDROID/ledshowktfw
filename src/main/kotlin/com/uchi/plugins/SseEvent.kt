package com.uchi.plugins

import com.uchi.uchiserver.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class SseEvent(val data: String, val event: String? = null, val id: String? = null) {
    fun toJson(): String {
        return json.encodeToString(this)
    }
}
