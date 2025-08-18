package com.hallis21.armorsets.config

// import com.charleskorn.kaml.Kaml
import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.utils.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Material
import java.io.File

class ConfigManager(private val plugin: ArmorSetsPlugin) {
    private val configFile = File(plugin.dataFolder, "config.json")
    private val v1ConfigFile = File(plugin.dataFolder.parentFile, "ArmorSetEffects/config.yml")
    private val v1ArmorSetsDir = File(plugin.dataFolder.parentFile, "ArmorSetEffects/armorsets")

    private var config: PluginConfig = PluginConfig()

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    // private val yaml = Kaml { encodeDefaults = true }

    fun loadConfig() {
        try {
            Logger.debug("Loading configuration...")

            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }

            if (!configFile.exists()) {
                if (v1ConfigFile.exists()) {
                    Logger.info("Migrating V1 configuration...")
                    migrateV1Config()
                } else {
                    Logger.info("Creating default configuration...")
                    createDefaultConfig()
                }
            }

            if (configFile.exists()) {
                val configText = configFile.readText()
                config = json.decodeFromString<PluginConfig>(configText)
                Logger.debug("Configuration loaded successfully")
            }

            Logger.initialize(plugin.logger, config.debug)
        } catch (e: Exception) {
            Logger.error("Failed to load configuration", e)
            config = PluginConfig()
        }
    }

    fun saveConfig() {
        try {
            val configText = json.encodeToString(config)
            configFile.writeText(configText)
            Logger.debug("Configuration saved successfully")
        } catch (e: Exception) {
            Logger.error("Failed to save configuration", e)
        }
    }

    private fun createDefaultConfig() {
        config = PluginConfig()
        saveConfig()

        createExampleArmorSets()
    }

    private fun createExampleArmorSets() {
        val examplesDir = File(plugin.dataFolder, "sets/examples")
        if (!examplesDir.exists()) {
            examplesDir.mkdirs()
        }

        val exampleFile = File(examplesDir, "diamond_set.yml")
        if (!exampleFile.exists()) {
            val exampleContent =
                """
                # Example Diamond Armor Set
                diamond_set:
                  name: "Diamond Warrior"
                  displayName: "Diamond Warrior Set"
                  hidden: false
                  priority: 10
                  permission: "diamond"
                  equipMessage: "<green>Diamond Warrior set equipped! You feel invincible!</green>"
                  unequipMessage: "<red>Diamond Warrior set removed.</red>"

                  armorPieces:
                    - item: "DIAMOND_HELMET"
                      slot: "HELMET"
                    - item: "DIAMOND_CHESTPLATE"
                      slot: "CHESTPLATE"
                    - item: "DIAMOND_LEGGINGS"
                      slot: "LEGGINGS"
                    - item: "DIAMOND_BOOTS"
                      slot: "BOOTS"

                  permanentEffects:
                    - effectType: "FIRE_RESISTANCE"
                      amplifier: 0
                      ambient: true
                      particles: false
                    - effectType: "DAMAGE_RESISTANCE"
                      amplifier: 0
                      ambient: true
                      particles: false

                  itemEffects:
                    - item: "GOLDEN_APPLE"
                      cooldown: 30
                      consumeItem: true
                      message: "<gold>Diamond Warrior power activated!</gold>"
                      effects:
                        - effectType: "REGENERATION"
                          amplifier: 2
                          duration: 10
                        - effectType: "ABSORPTION"
                          amplifier: 1
                          duration: 15
                """.trimIndent()

            exampleFile.writeText(exampleContent)
        }
    }

    private fun migrateV1Config() {
        Logger.info("Migrating from ArmorSetEffects V1...")

        try {
            config = PluginConfig()

            if (v1ConfigFile.exists()) {
                val v1ConfigText = v1ConfigFile.readText()
                Logger.debug("V1 config found, migrating basic settings...")
            }

            if (v1ArmorSetsDir.exists() && v1ArmorSetsDir.isDirectory) {
                val v1ArmorSets =
                    v1ArmorSetsDir.listFiles { file ->
                        file.isFile && file.extension.equals("json", ignoreCase = true)
                    }

                if (v1ArmorSets != null) {
                    Logger.info("Found ${v1ArmorSets.size} V1 armor sets to migrate")

                    val setsDir = File(plugin.dataFolder, "sets")
                    if (!setsDir.exists()) {
                        setsDir.mkdirs()
                    }

                    v1ArmorSets.forEach { v1File ->
                        try {
                            migrateV1ArmorSet(v1File, setsDir)
                        } catch (e: Exception) {
                            Logger.error("Failed to migrate armor set: ${v1File.name}", e)
                        }
                    }
                }
            }

            saveConfig()
            Logger.info("V1 migration completed successfully!")
        } catch (e: Exception) {
            Logger.error("Failed to migrate V1 configuration", e)
            createDefaultConfig()
        }
    }

    private fun migrateV1ArmorSet(
        v1File: File,
        outputDir: File,
    ) {
        val v1Content = v1File.readText()
        val v1Data = json.decodeFromString<Map<String, Any>>(v1Content)

        Logger.debug("Migrating armor set: ${v1File.nameWithoutExtension}")

        val outputFile = File(outputDir, "${v1File.nameWithoutExtension}.yml")
        outputFile.writeText("# Migrated from V1: ${v1File.name}\n# TODO: Review and adjust as needed\n\n$v1Content")

        Logger.debug("Migrated ${v1File.name} -> ${outputFile.name}")
    }

    fun getConfig(): PluginConfig = config

    fun getBlockedMaterials(): Set<Material> {
        return config.blockedMaterials.mapNotNull { materialName ->
            try {
                Material.valueOf(materialName.uppercase())
            } catch (e: IllegalArgumentException) {
                Logger.warn("Invalid blocked material: $materialName")
                null
            }
        }.toSet()
    }
}

@Serializable
data class PluginConfig(
    val debug: Boolean = false,
    val armorSets: ArmorSetsConfig = ArmorSetsConfig(),
    val gui: GuiConfig = GuiConfig(),
    val effects: EffectsConfig = EffectsConfig(),
    val blockedMaterials: List<String> = emptyList(),
)

@Serializable
data class ArmorSetsConfig(
    val enabled: Boolean = true,
    val loadFromDirectory: Boolean = true,
    val setsDirectory: String = "sets",
)

@Serializable
data class GuiConfig(
    val enabled: Boolean = true,
    val title: String = "Armor Sets",
    val size: Int = 54,
    val showHidden: Boolean = false,
)

@Serializable
data class EffectsConfig(
    val stackSimilar: Boolean = false,
    val showParticles: Boolean = true,
    val refreshInterval: Int = 20,
    val priorityOverride: Boolean = true,
)
