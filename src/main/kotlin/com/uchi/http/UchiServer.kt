package com.uchi.http

import com.uchi.Constants
import com.uchi.led.LedParameters
import com.uchi.led.LedShow
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class UchiServer {
   suspend fun getExitsCount(): Int {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://192.168.8.1:8080"))
            .GET()
            .build()
        runCatching {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val statusCode = response.statusCode()
            val responseBody = response.body()
            println(statusCode)
            println(responseBody)
        }
        return 2
    }

   suspend fun getInCount(): Int {
        return 3
    }

    suspend fun getLEDList(): MutableList<LedShow> {
        Constants.LED_DEVICES.clear()
        Constants.LED_DEVICES.add(LedShow(LedParameters()))
        val item2 = LedParameters().apply {
            ip = "192.168.8.111"
        }
        Constants.LED_DEVICES.add(LedShow(item2))
        return Constants.LED_DEVICES
    }
}