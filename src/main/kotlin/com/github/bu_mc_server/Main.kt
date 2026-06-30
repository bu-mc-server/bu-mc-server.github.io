package com.github.bu_mc_server

import com.github.bu_mc_server.data.Iceway
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
 */
fun main() {

    Iceway.init()

    val outputDir = File("public")
    if (!outputDir.exists()) outputDir.mkdirs()

    // setup the image
    val width = 500
    val height = 500
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()

    // color the image
    graphics.color = Color.RED
    graphics.fillRect(0, 0, width, height)
    graphics.dispose()

    // save image
    val outputFile = File(outputDir, "iceway_map.png")
    val success = ImageIO.write(image, "png", outputFile)

    if (!success) {
        throw RuntimeException("Critical Error: ImageIO failed to write the PNG file.")
    }

    println("SUCCESS: Image generated at ${outputFile.absolutePath}")
}