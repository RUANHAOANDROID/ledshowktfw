package com.uchi.led

import kotlinx.serialization.Serializable

/**
 * LED参数 ，注意默认是单块的
 */
@Serializable
data class LedParameters(
    var title: String = "led",
    var ip: String = "192.168.8.199",
    var port: Int = 5005,
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 32,
    var height: Int = 16,
    var fontSize: Int = 10,
    var displayMode:Byte=0x00,
)