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
        val containingSegments: MutableList<Segment> = mutableListOf()
        Iceway.lines.map { it.toSegments() }.forEach { segmentList ->
            segmentList.forEach { segment ->
                if (segment.containsX(this.x)) {
                    containingSegments.add(segment)
                }
                if (segment.containsZ(this.z)) {
                    containingSegments.add(segment)
                }
            }
            val snapSegment = containingSegments.map { segment ->
                if (segment.isHorizontal) {
                    segment to abs(segment.startZ - this.z)
                } else { // segment.isVertical == true
                    segment to abs(segment.startX - this.x)
            }
            }.minByOrNull { it.second }?.first ?: run {
                println("CRITICAL ERROR: Station $name failed to snap to any segment.")
                return 0 to 0
            }
            return if (snapSegment.isHorizontal) {
                this.x to snapSegment.startZ
            } else { // snapSegment.isVertical == true
                snapSegment.startX to this.z
            }
        }
        return 0 to 0
    }
}
