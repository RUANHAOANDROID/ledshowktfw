package com.uchi

import com.uchi.led.LedShow
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

object Constants {
    val Connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    val CoroutineScope = CoroutineScope(Dispatchers.IO)
    var LED_DEVICES = Collections.synchronizedList(mutableListOf<LedShow>())
    var IN_COUNT = 0
    var OUT_COUNT = 0
    var EXITS_COUNT = 0
    var authCode = "1a2d3"
}