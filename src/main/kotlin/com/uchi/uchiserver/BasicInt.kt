package com.uchi.uchiserver


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BasicInt(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val udata: Int,
    @SerialName("msg")
    val msg: String,
)

@Serializable
data class UChiResp<T>(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val udata: T? = null,
    @SerialName("msg")
    val msg: String,
) {

}