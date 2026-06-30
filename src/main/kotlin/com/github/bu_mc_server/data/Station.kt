package com.github.bu_mc_server.data

import com.github.bu_mc_server.drawing.Segment
import java.awt.Color
import kotlin.math.abs

data class Station(
    val name: String,
    val x: Int,
    val z: Int,
    val line: Color,
    val region: Region
) {
    fun coords(): Pair<Int, Int> = x to z

    /**
     * Finds the closest segment to the given station, and projects to that segment.
     */
    fun snap(): Pair<Int, Int> {
        val lineObj = Iceway.line(this.line) ?: return this.x to this.z
        val segments = lineObj.toSegments()

        val containingSegments: MutableList<Segment> = mutableListOf()

        segments.forEach { segment ->
            if (segment.containsX(this.x)) containingSegments.add(segment)
            if (segment.containsZ(this.z)) containingSegments.add(segment)
        }

        val snapSegment = containingSegments.map { segment ->
            if (segment.isHorizontal) {
                segment to abs(segment.startZ - this.z)
            } else { // snapSegment.isVertical
                segment to abs(segment.startX - this.x)
            }
        }.minByOrNull { it.second }?.first ?: run {
            println("CRITICAL ERROR: Station $name failed to snap to any segment.")
            // fallback to actual coords
            return this.x to this.z
        }

        return if (snapSegment.isHorizontal) {
            this.x to snapSegment.startZ
        } else { // snapSegment.isVertical
            snapSegment.startX to this.z
        }
    }
}
