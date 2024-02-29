package com.uchi.uchiserver


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UChiResp<T>(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("data")
    var udata: T? = null,
    @SerialName("msg")
    var msg: String = "null",
)

@Serializable
data class LimitsInfo(
    @SerialName("name")
    val name: String,
    @SerialName("limitsCount")
    val limitsCount: Int
)

@Serializable
data class LedListData(
    val fontSize: Int,
    val h: Int,
    val ip: String,
    val name: String,
    val port: Int,
    val w: Int,
    val x: Int,
    val y: Int
)