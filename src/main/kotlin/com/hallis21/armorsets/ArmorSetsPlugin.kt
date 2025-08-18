package com.hallis21.armorsets

import com.hallis21.armorsets.commands.ArmorCommandManager
import com.hallis21.armorsets.config.ConfigManager
import com.hallis21.armorsets.listeners.ArmorEquipListener
import com.hallis21.armorsets.services.ArmorSetManager
import com.hallis21.armorsets.services.EffectManager
import com.hallis21.armorsets.utils.Logger
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.SupervisorJob
// import kotlinx.coroutines.cancel
import org.bukkit.plugin.java.JavaPlugin

class ArmorSetsPlugin : JavaPlugin() {
    // private val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var configManager: ConfigManager
        private set

    lateinit var armorSetManager: ArmorSetManager
        private set

    lateinit var effectManager: EffectManager
        private set

    lateinit var commandManager: ArmorCommandManager
        private set

    override fun onEnable() {
        try {
            Logger.initialize(logger)
            Logger.info("Starting ArmorSetEffects V2...")

            initializeServices()
            registerListeners()
            registerCommands()

            Logger.info("ArmorSetEffects V2 enabled successfully!")
        } catch (e: Exception) {
            Logger.error("Failed to enable ArmorSetEffects V2", e)
            isEnabled = false
        }
    }

    override fun onDisable() {
        try {
            Logger.info("Disabling ArmorSetEffects V2...")

            cleanupServices()
            // pluginScope.cancel()

            Logger.info("ArmorSetEffects V2 disabled successfully!")
        } catch (e: Exception) {
            Logger.error("Error during plugin disable", e)
        }
    }

    private fun initializeServices() {
        Logger.debug("Initializing services...")

        configManager = ConfigManager(this)
        armorSetManager = ArmorSetManager(this)
        effectManager = EffectManager(this)
        commandManager = ArmorCommandManager(this)

        configManager.loadConfig()
        armorSetManager.loadArmorSets()

        Logger.debug("Services initialized successfully")
    }

    private fun registerListeners() {
        Logger.debug("Registering event listeners...")

        server.pluginManager.registerEvents(ArmorEquipListener(this), this)

        Logger.debug("Event listeners registered")
    }

    private fun registerCommands() {
        Logger.debug("Registering commands...")

        commandManager.registerCommands()

        Logger.debug("Commands registered")
    }

    private fun cleanupServices() {
        Logger.debug("Cleaning up services...")

        if (::effectManager.isInitialized) {
            effectManager.removeAllEffects()
        }

        Logger.debug("Services cleaned up")
    }

    fun reload(): Boolean {
        return try {
            Logger.info("Reloading ArmorSetEffects V2...")

            effectManager.removeAllEffects()
            configManager.loadConfig()
            armorSetManager.loadArmorSets()

            Logger.info("ArmorSetEffects V2 reloaded successfully!")
            true
        } catch (e: Exception) {
            Logger.error("Failed to reload ArmorSetEffects V2", e)
            false
        }
    }

    companion object {
        @JvmStatic
        lateinit var instance: ArmorSetsPlugin
            private set
    }

    init {
        instance = this
    }
}
