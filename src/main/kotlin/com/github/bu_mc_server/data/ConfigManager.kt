package com.github.bu_mc_server.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File

object ConfigManager {

    private val configFile = File("src/main/resources/config.json")

    fun load(): JsonObject {
        require(configFile.exists()) { "Config file does not exist." }
        return Json.parseToJsonElement(configFile.readText()).jsonObject
    }
}