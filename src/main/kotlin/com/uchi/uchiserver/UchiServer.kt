package com.uchi.uchiserver

import com.uchi.Constants
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

val json = Json {
//    isLenient = true
    ignoreUnknownKeys = true
}

object UchiServer {
    val url = Constants.UChiUrl
    val client = HttpClient(CIO) // 安装 JsonFeature
    suspend fun getInfo(authCode: String): Pair<String, UChiResp<LimitsInfo?>> {
        val urlString = url + "/gateMachine/queryLocation/${authCode}"
        println(urlString)
        val resp = client.get(urlString)
        val data = jsonStr(resp)
        val second = json.decodeFromString<UChiResp<LimitsInfo?>>(data)
        return Pair(data, second)
    }

    suspend fun ledList(authCode: String): UChiResp<MutableList<LedListData>?> {
        val urlString = url + "/gateMachine/led/list/${authCode}"
        println(urlString)
        val resp = client.get(urlString)
        val data = resp.body<String>()
        val list = json.decodeFromString<UChiResp<MutableList<LedListData>?>>(data)
        return list
    }

    suspend fun updateLimitCount(authCode: String, count: String): UChiResp<String?> {
        val urlString = url + "/gateMachine/updateLimit/${authCode}/${count}"
        println(urlString)
        val resp = client.get(urlString)
        val data = jsonStr(resp)
        return json.decodeFromString<UChiResp<String?>>(data)
    }

    private suspend fun jsonStr(resp: HttpResponse): String {
        val data = resp.body<String>()
        println(data)
        return data
    }
}