package com.uchi.plugins

import com.uchi.uchiserver.UChiResp
import com.uchi.uchiserver.json
import kotlinx.serialization.encodeToString

val SUCCESS = 1
val FAIL = 0

fun <T> respSuccess(data: T? = null): String {
    val serializer = UChiResp(code = SUCCESS, msg = "SUCCESS", udata = data)
    return json.encodeToString(serializer)
}

fun respErr(msg: String = "FAIL"): String {
    val serializer = UChiResp(code = FAIL, msg = msg, udata = "")
    return json.encodeToString(serializer)
}

