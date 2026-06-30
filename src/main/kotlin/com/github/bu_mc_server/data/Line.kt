package com.github.bu_mc_server.data

import com.github.bu_mc_server.drawing.Segment
import java.awt.Color

data class Line(
    val name: String,
    val color: Color,
    val stations: MutableList<Station> = mutableListOf(),
    var start: Pair<Int, Int> = 0 to 0,
    var end: Pair<Int, Int> = 0 to 0,
    val turns: MutableList<Pair<Int, Int>> = mutableListOf()
) {
    /**
     * Adds a station to the list of stations
     */
    fun station(name: String, x: Int, z: Int, lineName: String, lineColor: Color, region: Region) {
        stations.add(Station(name, x, z, lineName, lineColor, region))
    }

    /**
     * Sets starting terminus
     */
    fun start(x: Int, z: Int) {
        start = x to z
    }

    /**
     * Sets the ending terminus
     */
    fun end(x: Int, z: Int) {
        end = x to z
    }

    /**
     * Adds a turn
     */
    fun turn(x: Int, z: Int) {
        turns.add(x to z)
    }

    /**
     * Breaks this [Line] into a list of [Segment]s to be drawn.
     */
    fun toSegments(): List<Segment> {

        // The implementation here is kinda messy. I didn't wanna assume people would always add new turns in order,
        // so it do the following:
        // it starts at this.start. If this.turns is nonempty, it searches for any turns that have a matching x or z
        // coordinate with this.start. It then draws an edge to that turn (imagine drawing edges between vertices on
        // a graph). It then cannot go in the same direction, so if this.turns is still nonempty, it looks for turns that
        // share the same relevant coordinate with it, and if it finds any, it draws an edge to that turn. This continues
        // recursively until this.turns is empty, at which time it check if this.end shares the relevant coordinate with
        // the last turn, and if so, draws an edge. If this process fails at any time, it goes back and tries again.
        // In general no such path need be unique on a finite 2d lattice, but we have some special rules due to our exact
        // situation which allows the path to be unique, assuming it exists.
        // in particular, you MUST turn at a turn, and cannot travel through turns without turning.
        // this has terrible runtime, but luckily our lines are not extraordinarily long and i dont think any of them will
        // run for long at all.

        val allNodes = listOf(start, end) + turns

        // helper function to check if any node physically blocks the path between points a, b
        fun isBetween(a: Pair<Int, Int>, b: Pair<Int, Int>, mid: Pair<Int, Int>): Boolean {
            if (mid == a || mid == b) return false

            if (a.first == b.first && mid.first == a.first) {
                return mid.second in minOf(a.second, b.second)..maxOf(a.second, b.second)
            }
            if (a.second == b.second && mid.second == a.second) {
                return mid.first in minOf(a.first, b.first)..maxOf(a.first, b.first)
            }
            return false
        }

        // constrained depth-first search
        fun findPath(current: Pair<Int, Int>, unvisited: List<Pair<Int, Int>>, lastAxis: Direction?): List<Pair<Int, Int>>? {

            // base case: all turns visited, try connecting to end terminus
            if (unvisited.isEmpty()) {
                val endAxis = if (current.first == end.first) {
                    Direction.Z
                } else if (current.second == end.second) {
                    Direction.X
                } else null

                // must be orthogonal and must turn
                if (endAxis != null && endAxis != lastAxis) {
                    // check line of sight to end terminus
                    val blocked = allNodes.any { isBetween(current, end, it) }
                    if (!blocked) return listOf(current, end)
                }
                return null
            }

            // recursive case: find next turn
            for (nextTurn in unvisited) {
                val nextAxis = if (current.first == nextTurn.first) {
                    Direction.Z
                } else if (current.second == nextTurn.second) {
                    Direction.X
                } else null

                // must be orthogonal and must turn
                // aka it must not share the same axis as the last move
                if (nextAxis == null || nextAxis == lastAxis) continue

                // must be line of sight
                // if any nodes exist between this and next, we cannot go there
                val blocked = allNodes.any { isBetween(current, nextTurn, it) }
                if (blocked) continue

                // the move is valid. remove from pool and recurse
                val remaining = unvisited.toMutableList().apply { remove(nextTurn) }
                val pathFromNext = findPath(nextTurn, remaining, nextAxis)

                // if the downstream path successfully found the end, bubble the result up
                if (pathFromNext != null) {
                    return listOf(current) + pathFromNext
                }
            }

            // dead end
            return null
        }

        // start from starting terminus
        val orderedPoints = findPath(start, turns.toList(), null)

        if (orderedPoints == null) {
            println("CRITICAL ERROR: No path found for $color. Check for malformed turns or terminuses.")
            return emptyList()
        }

        val segments = mutableListOf<Segment>()
        for (i in 0 until orderedPoints.size - 1) {
            val p1 = orderedPoints[i]
            val p2 = orderedPoints[i + 1]
            segments.add(Segment(p1.first, p1.second, p2.first, p2.second))
        }

        return segments
    }
}
