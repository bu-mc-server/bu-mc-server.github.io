package com.github.bu_mc_server

import com.github.bu_mc_server.data.Iceway
import kotlinx.serialization.json.*
import java.io.File
import kotlin.math.max
import kotlin.math.min

fun main() {
    Iceway.init()

    val outputDir = File("public")
    if (!outputDir.exists()) outputDir.mkdirs()
    val outputFile = File(outputDir, "map_data.json")

    var minX = Int.MAX_VALUE
    var maxX = Int.MIN_VALUE
    var minZ = Int.MAX_VALUE
    var maxZ = Int.MIN_VALUE

    Iceway.lines.forEach { line ->
        val nodes = listOf(line.start, line.end) + line.turns
        nodes.forEach { (x, z) ->
            if (x != 0 || z != 0) {
                minX = min(minX, x)
                maxX = max(maxX, x)
                minZ = min(minZ, z)
                maxZ = max(maxZ, z)
            }
        }
    }

    val linesArray = buildJsonArray {
        Iceway.lines.forEach { line ->
            add(buildJsonObject {
                put("name", line.name)
                put("color", String.format("#%02x%02x%02x", line.color.red, line.color.green, line.color.blue))
                put("segments", buildJsonArray {
                    line.toSegments().forEach { segment ->
                        // Detect if the segment spans the gap between divisions
                        val crosses = (segment.startX < -5000 && segment.endX > -5000) || (segment.startX > -5000 && segment.endX < -5000)

                        if (crosses && segment.isHorizontal) {
                            val westStations = line.stations.filter { it.region.name == "WEST" }
                            val eastStations = line.stations.filter { it.region.name == "EAST" }

                            if (westStations.isNotEmpty() && eastStations.isNotEmpty()) {
                                // Find the bounds: the right-most West station and left-most East station
                                val westMaxX = westStations.maxOf { it.snap().first }
                                val eastMinX = eastStations.minOf { it.snap().first }
                                val z = segment.startZ

                                val leftX = min(segment.startX, segment.endX)
                                val rightX = max(segment.startX, segment.endX)

                                // Slice the giant segment into 3 fitted pieces
                                add(buildJsonObject { put("x1", leftX); put("z1", z); put("x2", westMaxX); put("z2", z); put("division", "WEST") })
                                add(buildJsonObject { put("x1", westMaxX); put("z1", z); put("x2", eastMinX); put("z2", z); put("division", "CROSS") })
                                add(buildJsonObject { put("x1", eastMinX); put("z1", z); put("x2", rightX); put("z2", z); put("division", "EAST") })
                            } else {
                                add(buildJsonObject { put("x1", segment.startX); put("z1", segment.startZ); put("x2", segment.endX); put("z2", segment.endZ); put("division", "CROSS") })
                            }
                        } else {
                            val div = if (segment.startX < -5000 && segment.endX < -5000) "WEST"
                            else if (segment.startX > -5000 && segment.endX > -5000) "EAST"
                            else "CROSS"
                            add(buildJsonObject { put("x1", segment.startX); put("z1", segment.startZ); put("x2", segment.endX); put("z2", segment.endZ); put("division", div) })
                        }
                    }
                })
            })
        }
    }

    val stationsArray = buildJsonArray {
        Iceway.stations.forEach { station ->
            val snapped = station.snap()
            add(buildJsonObject {
                put("name", station.name)
                put("x", snapped.first)
                put("z", snapped.second)
                put("lineName", station.presentName())
                put("color", String.format("#%02x%02x%02x", station.lineColor.red, station.lineColor.green, station.lineColor.blue))
                put("division", station.region.name)
            })
        }
    }

    val finalJson = buildJsonObject {
        put("minX", minX)
        put("maxX", maxX)
        put("minZ", minZ)
        put("maxZ", maxZ)
        put("lines", linesArray)
        put("stations", stationsArray)
    }

    outputFile.writeText(finalJson.toString())
    println("SUCCESS: Map data generated at ${outputFile.absolutePath}")
}