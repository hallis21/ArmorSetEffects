package com.hallis21.armorsets.services

// import com.charleskorn.kaml.Kaml
import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.models.ArmorSet
import com.hallis21.armorsets.utils.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class ArmorSetManager(private val plugin: ArmorSetsPlugin) {
    private val armorSets = mutableMapOf<String, ArmorSet>()

    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    // private val yaml = Kaml { encodeDefaults = true }

    fun loadArmorSets() {
        try {
            Logger.debug("Loading armor sets...")

            armorSets.clear()

            val config = plugin.configManager.getConfig()
            if (!config.armorSets.enabled) {
                Logger.info("Armor sets are disabled in configuration")
                return
            }

            if (config.armorSets.loadFromDirectory) {
                loadFromDirectory()
            }

            Logger.info("Loaded ${armorSets.size} armor sets successfully")
        } catch (e: Exception) {
            Logger.error("Failed to load armor sets", e)
        }
    }

    private fun loadFromDirectory() {
        val config = plugin.configManager.getConfig()
        val setsDir = File(plugin.dataFolder, config.armorSets.setsDirectory)

        if (!setsDir.exists()) {
            Logger.debug("Armor sets directory not found: ${setsDir.path}")
            return
        }

        Logger.debug("Loading armor sets from: ${setsDir.path}")

        loadFromDirectoryRecursive(setsDir)
    }

    private fun loadFromDirectoryRecursive(directory: File) {
        directory.listFiles()?.forEach { file ->
            when {
                file.isDirectory -> {
                    loadFromDirectoryRecursive(file)
                }
                file.isFile && (file.extension.equals("yml", true) || file.extension.equals("yaml", true)) -> {
                    loadYamlFile(file)
                }
                file.isFile && file.extension.equals("json", true) -> {
                    loadJsonFile(file)
                }
            }
        }
    }

    private fun loadYamlFile(file: File) {
        try {
            Logger.debug("Loading YAML armor set file: ${file.name}")

            val content = file.readText()
            val armorSetMap = json.decodeFromString<Map<String, ArmorSet>>(content)

            for ((key, armorSet) in armorSetMap) {
                val finalArmorSet = armorSet.copy(name = armorSet.name.ifEmpty { key })
                armorSets[key] = finalArmorSet
                Logger.debug("Loaded armor set: $key (priority: ${finalArmorSet.priority})")
            }
        } catch (e: Exception) {
            Logger.error("Failed to load YAML armor set file: ${file.name}", e)
        }
    }

    private fun loadJsonFile(file: File) {
        try {
            Logger.debug("Loading JSON armor set file: ${file.name}")

            val content = file.readText()

            val armorSetMap =
                try {
                    json.decodeFromString<Map<String, ArmorSet>>(content)
                } catch (e: Exception) {
                    val singleArmorSet = json.decodeFromString<ArmorSet>(content)
                    mapOf(file.nameWithoutExtension to singleArmorSet)
                }

            for ((key, armorSet) in armorSetMap) {
                val finalArmorSet = armorSet.copy(name = armorSet.name.ifEmpty { key })
                armorSets[key] = finalArmorSet
                Logger.debug("Loaded armor set: $key (priority: ${finalArmorSet.priority})")
            }
        } catch (e: Exception) {
            Logger.error("Failed to load JSON armor set file: ${file.name}", e)
        }
    }

    fun getArmorSet(name: String): ArmorSet? {
        return armorSets[name]
    }

    fun getAllArmorSets(): Collection<ArmorSet> {
        return armorSets.values
    }

    fun getVisibleArmorSets(): Collection<ArmorSet> {
        return armorSets.values.filter { !it.hidden }
    }

    fun getArmorSetsByPriority(): List<ArmorSet> {
        return armorSets.values.sortedByDescending { it.priority }
    }

    fun hasArmorSet(name: String): Boolean {
        return armorSets.containsKey(name)
    }

    fun getArmorSetCount(): Int {
        return armorSets.size
    }

    fun getArmorSetNames(): Set<String> {
        return armorSets.keys
    }

    fun addArmorSet(
        name: String,
        armorSet: ArmorSet,
    ) {
        armorSets[name] = armorSet
        Logger.debug("Added armor set: $name")
    }

    fun removeArmorSet(name: String): ArmorSet? {
        val removed = armorSets.remove(name)
        if (removed != null) {
            Logger.debug("Removed armor set: $name")
        }
        return removed
    }

    override fun toString(): String {
        return "ArmorSetManager(loaded=${armorSets.size} sets)"
    }
}
