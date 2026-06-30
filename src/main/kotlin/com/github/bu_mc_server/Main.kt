package com.github.bu_mc_server

import com.github.bu_mc_server.data.Iceway
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

/**
 * TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
 */
fun main() {

    Iceway.init()

    val outputDir = File("public")
    if (!outputDir.exists()) outputDir.mkdirs()
    val outputFile = File(outputDir, "iceway_map.png")


    // gather bounds
    var minX = Iceway.stations.minByOrNull { it.x }?.x ?: run {
        println("Really weird error")
        0
    }
    var maxX = Iceway.stations.maxByOrNull { it.x }?.x ?: run {
        println("Really weird error")
        0
    }
    var minZ = Iceway.stations.minByOrNull { it.z }?.z ?: run {
        println("Really weird error")
        0
    }
    var maxZ = Iceway.stations.maxByOrNull { it.z }?.z ?: run {
        println("Really weird error")
        0
    }

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

    // image variables
    val padding = 200

    val worldWidth = (maxX - minX).toDouble()
    val worldHeight = (maxZ - minZ).toDouble()

    // scaling
    val maxDim = 4000.0
    val scale = min(
        (maxDim - 2 * padding) / worldWidth,
        (maxDim - 2 * padding) / worldHeight
    )

    val imgWidth = (worldWidth * scale).toInt() + 2 * padding
    val imgHeight = (worldHeight * scale).toInt() + 2 * padding

    fun mapX(worldX: Int): Int = padding + ((worldX - minX) * scale).toInt()
    fun mapZ(worldZ: Int): Int = padding + ((worldZ - minZ) * scale).toInt()

    // setup image
    val image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB)
    val g2d = image.createGraphics()

    // anti-aliasing for good text
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    // background
    g2d.color = Color(30, 30, 30)
    g2d.fillRect(0, 0, imgWidth, imgHeight)

    // draw lines
    g2d.stroke = BasicStroke(16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    Iceway.lines.forEach { line ->
        g2d.color = line.color
        val segments = line.toSegments()
        segments.forEach { segment ->
            val x1 = mapX(segment.startX)
            val z1 = mapZ(segment.startZ)
            val x2 = mapX(segment.endX)
            val z2 = mapZ(segment.endZ)
            g2d.drawLine(x1, z1, x2, z2)
        }
    }

    // draw stations
    val font = Font("SansSerif", Font.BOLD, 32)
    g2d.font = font
    val fontMetrics = g2d.fontMetrics

    Iceway.stations.forEach { station ->
        val px = mapX(station.snap().first)
        val pz = mapZ(station.snap().second)

        // draw the dot
        val dotRadius = 12
        g2d.color = Color.WHITE
        g2d.fillOval(px - dotRadius, pz - dotRadius, dotRadius * 2, dotRadius * 2)

        // draw dot border
        g2d.stroke = BasicStroke(4f)
        g2d.color = Color.BLACK
        g2d.drawOval(px - dotRadius, pz - dotRadius, dotRadius * 2, dotRadius * 2)
    }

    g2d.dispose()

    // present output
    val success = ImageIO.write(image, "png", outputFile)
    if (!success) throw RuntimeException("Failed to write to image.")
    println("SUCCESS: Image generated at ${outputFile.absolutePath}")
}