package com.github.bu_mc_server.data

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

    fun init() {
        ConfigManager.read()
    }

    /**
     * Wrapper for adding stations to [Iceway.stations]
     */
    fun put(name: String, x: Int, z: Int, line: Color, region: Region) {
        stations.add(Station(name, x, z, line, region))
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
}