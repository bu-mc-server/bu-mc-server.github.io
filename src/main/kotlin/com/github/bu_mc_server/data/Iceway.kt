package com.github.bu_mc_server.data

import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Color

/**
 * Contains all of the stations and important facts thereof. Call `Iceway.init()` to read from the
 * config files and update all fields.
 */
object Iceway {

    /**
     * The list of stations. Must be populated by calling [init].
     */
    val stations: MutableList<Station> = mutableListOf()

    /**
     * The list of stations in the western divison. Must be populated by calling [init].
     */
    val westDivision: MutableList<Station> = mutableListOf()

    /**
     * The list of stations in the eastern divison. Must be populated by calling [init].
     */
    val eastDivision: MutableList<Station> = mutableListOf()

    /**
     * The list of lines. Useful for drawing the physical lines on the image.
     */
    val lines: MutableList<Line> = mutableListOf()


    /**
     * Initializes the Iceway config
     */
    fun init() {
        val config = ConfigManager.load()

        // reset any possibly saved values
        stations.clear()
        westDivision.clear()
        eastDivision.clear()

        // this loop populates stations and lines
        config.forEach { (regionName, regionElement) ->
            val region = regionName.toRegion()

            regionElement.jsonObject.forEach { (lineName, lineElement) ->
                val lineColor = lineName.toColor()

                if (lines.find { it.name == lineName } == null) {
                    lines.add(Line(lineName, lineColor))
                }

                val lineObj: Line? = lines.find { it.name == lineName }



                lineElement.jsonArray.forEach { stationElement ->
                    val stationObj = stationElement.jsonObject

                    val name = stationObj["name"]?.jsonPrimitive?.content ?: run {
                        println("No name passed for a station on line $lineName$")
                        "UNKNOWN"
                    }
                    val x = stationObj["x"]?.jsonPrimitive?.int ?: run {
                        println("No x coordinate passed for $name")
                        0
                    }
                    val z = stationObj["z"]?.jsonPrimitive?.int ?: run {
                        println("No z coordinate passed for $name")
                        0
                    }

                    // manage flags
                    // allows passing flags as station names
                    // or as a separate %FLAG% parameter
                    // flags should be passed as a name if no station exists at that spot
                    // flags should be passed as another parameter if a station exists at that spot
                    when (name) {
                        "%FLAG_TURN%" -> lineObj?.turn(x, z)
                        "%FLAG_TERMINUS_START%" -> lineObj?.start(x, z)
                        "%FLAG_TERMINUS_END%" -> lineObj?.end(x, z)
                        else -> {
                            lineObj?.station(name, x, z, lineName, lineColor, region)
                            put(name, x, z, lineName, lineColor, region)
                        }
                    }
                }
            }
        }

        westDivision.addAll(stations.filter { it.region == Region.WEST })
        eastDivision.addAll(stations.filter { it.region == Region.EAST })
    }

    /**
     * Wrapper for adding stations to [Iceway.stations]
     */
    fun put(name: String, x: Int, z: Int, lineName: String, lineColor: Color, region: Region) {
        stations.add(Station(name, x, z, lineName, lineColor, region))
    }

    /**
     * Iterator for [Iceway.stations]
     */
    fun forEach(action: (Station) -> Unit) {
        stations.forEach(action)
    }

    /**
     * Iterator for WEST stations in [Iceway.stations]
     */
    fun forEachWest(action: (Station) -> Unit) {
        stations.filter { it.region == Region.WEST }.forEach(action)
    }

    /**
     * Iterator for EAST stations in [Iceway.stations]
     */
    fun forEachEast(action: (Station) -> Unit) {
        stations.filter { it.region == Region.EAST }.forEach(action)
    }

    /**
     * Get a station using its name
     */
    operator fun get(name: String): Station? = stations.find { it.name == name }

    /**
     * Maps strings to colors. Only call on the parsed config file.
     * The colors are taken from https://minecraft.wiki/w/Map_item_format.
     */
    private fun String.toColor(): Color = when (this) {
        "RED" -> Color.RED
        "ORANGE" -> Color.ORANGE
        "YELLOW" -> Color.YELLOW
        "GREEN" -> Color.GREEN
        // "GREEN" -> Color(102, 127, 51)
        "BLUE" -> Color.BLUE
        "LIGHT_BLUE" -> Color(102, 153, 216)
        "PURPLE" -> Color.MAGENTA
        // "PURPLE" -> Color(127, 63, 178)
        "GRAY" -> Color.GRAY
        "LIGHT_GRAY" -> Color.LIGHT_GRAY
        "BLACK" -> Color.BLACK
        "PINK" -> Color.PINK
        "CYAN" -> Color.CYAN
        "LIME" -> Color(127, 204, 25)
        else -> Color.WHITE
    }

    private fun String.toRegion(): Region = when (this) {
        "West" -> Region.WEST
        "East" -> Region.EAST
        else -> Region.WEST
    }

    fun line(name: String): Line? = lines.find { it.name == name }
}