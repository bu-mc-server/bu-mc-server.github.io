package com.github.bu_mc_server.drawing

data class Segment(
    val startX: Int,
    val startZ: Int,
    val endX: Int,
    val endZ: Int
) {
    val isVertical: Boolean = startX == endX
    val isHorizontal: Boolean = startZ == endZ

    fun containsX(x: Int): Boolean = x in minOf(startX, endX)..maxOf(startX, endX)
    fun containsZ(z: Int): Boolean = z in minOf(startZ, endZ)..maxOf(startZ, endZ)
}
