package com.uchi.uchiserver


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasicString(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val udata: String,
    @SerialName("msg")
    val msg: String,
)