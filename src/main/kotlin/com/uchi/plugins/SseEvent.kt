package com.uchi.plugins
data class SseEvent(val data: String, val event: String? = null, val id: String? = null)
