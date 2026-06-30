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

    // Calculate bounds from line structures to set the default camera view
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
                // Convert java.awt.Color to HTML Hex
                put("color", String.format("#%02x%02x%02x", line.color.red, line.color.green, line.color.blue))
                put("segments", buildJsonArray {
                    line.toSegments().forEach { segment ->
                        add(buildJsonObject {
                            put("x1", segment.startX)
                            put("z1", segment.startZ)
                            put("x2", segment.endX)
                            put("z2", segment.endZ)
                        })
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
                put("color", String.format("#%02x%02x%02x", station.line.red, station.line.green, station.line.blue))
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