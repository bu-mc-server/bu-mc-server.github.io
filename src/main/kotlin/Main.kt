package com.github.bu

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
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
    ImageIO.write(image, "png", outputFile)
}