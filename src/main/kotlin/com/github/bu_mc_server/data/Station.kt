package com.github.bu_mc_server.data

import java.awt.Color

data class Station(
    val name: String,
    val x: Int,
    val z: Int,
    val line: Color,
    val region: Region
) {
    fun coords(): Pair<Int, Int> = x to z
}
