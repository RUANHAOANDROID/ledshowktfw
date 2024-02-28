package com.uchi.uchiserver

import kotlinx.serialization.Serializable

@Serializable
data class LedList(
    val code: Int,
    val `data`: List<LedListData>,
    val msg: String,
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