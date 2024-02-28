package com.uchi.uchiserver

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*

fun Int.Success(): Boolean {
    return this == 1
}

fun Int.Fail(): Boolean {
    return this == 0
}


class UChiException(msg: String) : Exception(msg)

fun String.throwUChiException() {
    throw UChiException(this)
}

object UchiServer {
    val url = "http://limit.api.yyxcloud.com"
    val client = HttpClient(CIO) // 安装 JsonFeature

    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
    suspend fun test(authCode: String) {
        ledList(authCode)
    }

    suspend fun getInfo(authCode: String): UChiResp<String?> {
        val resp = client.get(url + "/gateMachine/queryLocation/${authCode}")
        val data = jsonStr(resp)
        return json.decodeFromString<UChiResp<String?>>(data)
    }

    suspend fun ledList(authCode: String = "1a2d3"): LedList {
        val resp = client.get(url + "/gateMachine/led/list/${authCode}")
        val data = resp.body<String>()
        val list = json.decodeFromString<LedList>(data)
        return list
    }

    suspend fun existCount(authCode: String): BasicInt {
        val resp = client.get(url + "/gateMachine/existCount/${authCode}")
        val data = resp.body<String>()
        return json.decodeFromString<BasicInt>(data)
    }

    suspend fun outCount(authCode: String): BasicInt {
        val resp = client.get(url + "/gateMachine/outCount/${authCode}")
        val data = jsonStr(resp)
        return json.decodeFromString<BasicInt>(data)
    }

    private suspend fun jsonStr(resp: HttpResponse): String {
        val data = resp.body<String>()
        println(data)
        return data
    }

    suspend fun inCount(authCode: String): BasicInt {
        val resp = client.get(url + "/gateMachine/inCount/${authCode}")
        val data = jsonStr(resp)
        return json.decodeFromString<BasicInt>(data)
    }

    suspend fun updateLimitCount(authCode: String, count: Int): String? {
        val resp = client.get(url + "/gateMachine/updateLimit/${authCode}/${count}")
        val data = jsonStr(resp)
        return data
    }

}