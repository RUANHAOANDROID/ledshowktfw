package com.uchi

import com.uchi.led.LedShow
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object Constants {
    val UChiUrl = "http://limit.api.yyxcloud.com"
    val Connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    val CoroutineScope = CoroutineScope(Dispatchers.IO)
    var LED_DEVICES = Collections.synchronizedList(mutableListOf<LedShow>())
    var IN_COUNT = AtomicInteger(0)
    var OUT_COUNT = AtomicInteger(0)
    var EXITS_COUNT = AtomicInteger(0)
    var AuthCode = ""
}