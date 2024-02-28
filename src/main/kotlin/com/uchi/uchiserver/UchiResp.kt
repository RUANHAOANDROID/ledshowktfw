package com.uchi.uchiserver


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UChiResp<T>(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val udata: T? = null,
    @SerialName("msg")
    val msg: String = "null",
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
    val deleted: String,
    val fontSize: String,
    val h: String,
    val ip: String,
    val ledConfigId: String,
    val locationId: String,
    val name: String,
    val port: String,
    val status: String,
    val w: String,
    val x: String,
    val y: String
)