package com.hallis21.armorsets.services

import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.models.ArmorSet
import com.hallis21.armorsets.models.ItemEffect
import com.hallis21.armorsets.models.PermanentEffect
import com.hallis21.armorsets.utils.Logger
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.SupervisorJob
// import kotlinx.coroutines.cancel
// import kotlinx.coroutines.delay
// import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class EffectManager(private val plugin: ArmorSetsPlugin) {
    // private val effectScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val activePlayerSets = ConcurrentHashMap<UUID, ArmorSet>()

    private val playerEffects = ConcurrentHashMap<UUID, MutableList<PotionEffect>>()

    private val itemCooldowns = ConcurrentHashMap<Pair<UUID, String>, Long>()

    private val miniMessage = MiniMessage.miniMessage()

    init {
        // startEffectRefreshTask()
    }

    fun applyArmorSetEffects(
        player: Player,
        armorSet: ArmorSet,
    ) {
        try {
            Logger.debug("Applying armor set effects for ${player.name}: ${armorSet.name}")

            removePlayerEffects(player)

            activePlayerSets[player.uniqueId] = armorSet

            applyPermanentEffects(player, armorSet.permanentEffects)

            armorSet.getEquipMessage()?.let { message ->
                player.sendMessage(message)
            }

            Logger.debug("Applied ${armorSet.permanentEffects.size} permanent effects to ${player.name}")
        } catch (e: Exception) {
            Logger.error("Failed to apply armor set effects for ${player.name}", e)
        }
    }

    fun removeArmorSetEffects(player: Player) {
        try {
            val armorSet = activePlayerSets.remove(player.uniqueId)
            if (armorSet != null) {
                Logger.debug("Removing armor set effects for ${player.name}: ${armorSet.name}")

                removePlayerEffects(player)

                armorSet.getUnequipMessage()?.let { message ->
                    player.sendMessage(message)
                }

                Logger.debug("Removed armor set effects for ${player.name}")
            }
        } catch (e: Exception) {
            Logger.error("Failed to remove armor set effects for ${player.name}", e)
        }
    }

    private fun applyPermanentEffects(
        player: Player,
        effects: List<PermanentEffect>,
    ) {
        val playerEffectList = mutableListOf<PotionEffect>()

        effects.forEach { permanentEffect ->
            permanentEffect.createPotionEffect()?.let { potionEffect ->
                try {
                    player.addPotionEffect(potionEffect, true)
                    playerEffectList.add(potionEffect)

                    Logger.debug("Applied effect ${permanentEffect.effectType} to ${player.name}")
                } catch (e: Exception) {
                    Logger.error("Failed to apply effect ${permanentEffect.effectType} to ${player.name}", e)
                }
            }
        }

        if (playerEffectList.isNotEmpty()) {
            playerEffects[player.uniqueId] = playerEffectList
        }
    }

    private fun removePlayerEffects(player: Player) {
        playerEffects.remove(player.uniqueId)?.forEach { effect ->
            try {
                player.removePotionEffect(effect.type)
            } catch (e: Exception) {
                Logger.error("Failed to remove effect ${effect.type} from ${player.name}", e)
            }
        }
    }

    fun handleItemEffect(
        player: Player,
        itemEffect: ItemEffect,
    ): Boolean {
        try {
            val currentTime = System.currentTimeMillis()
            val cooldownKey = Pair(player.uniqueId, itemEffect.item)
            val lastUsed = itemCooldowns[cooldownKey] ?: 0
            val cooldownMs = itemEffect.cooldown * 1000L

            if (currentTime - lastUsed < cooldownMs) {
                val remainingSeconds = ((cooldownMs - (currentTime - lastUsed)) / 1000).toInt()
                val cooldownMessage =
                    miniMessage.deserialize(
                        "<red>You must wait ${remainingSeconds}s before using this item again!</red>",
                    )
                player.sendMessage(cooldownMessage)
                return false
            }

            itemCooldowns[cooldownKey] = currentTime

            itemEffect.effects.forEach { temporaryEffect ->
                temporaryEffect.createPotionEffect()?.let { potionEffect ->
                    player.addPotionEffect(potionEffect, true)
                }
            }

            itemEffect.message?.let { message ->
                val component = miniMessage.deserialize(message)
                player.sendMessage(component)
            }

            Logger.debug("Applied item effect ${itemEffect.item} to ${player.name}")
            return true
        } catch (e: Exception) {
            Logger.error("Failed to handle item effect for ${player.name}", e)
            return false
        }
    }

    fun getActiveArmorSet(player: Player): ArmorSet? {
        return activePlayerSets[player.uniqueId]
    }

    fun hasActiveArmorSet(player: Player): Boolean {
        return activePlayerSets.containsKey(player.uniqueId)
    }

    fun removeAllEffects() {
        try {
            Logger.debug("Removing all active armor set effects...")

            val onlinePlayers =
                activePlayerSets.keys.mapNotNull { uuid ->
                    Bukkit.getPlayer(uuid)
                }

            onlinePlayers.forEach { player ->
                removePlayerEffects(player)
            }

            activePlayerSets.clear()
            playerEffects.clear()
            itemCooldowns.clear()

            Logger.debug("Removed all active armor set effects")
        } catch (e: Exception) {
            Logger.error("Failed to remove all effects", e)
        }
    }

    private fun startEffectRefreshTask() {
        // TODO: Implement with Bukkit scheduler instead of coroutines
        /*
        val config = plugin.configManager.getConfig()
        val refreshInterval = config.effects.refreshInterval.toLong()

        effectScope.launch {
            while (true) {
                try {
                    delay(refreshInterval * 50L)
                    refreshEffects()
                } catch (e: Exception) {
                    Logger.error("Error in effect refresh task", e)
                }
            }
        }
        */
    }

    private fun refreshEffects() {
        activePlayerSets.forEach { (playerId, armorSet) ->
            val player = Bukkit.getPlayer(playerId)
            if (player != null && player.isOnline) {
                val currentEffects = playerEffects[playerId] ?: return@forEach

                currentEffects.forEach { effect ->
                    if (!player.hasPotionEffect(effect.type)) {
                        player.addPotionEffect(effect, true)
                    }
                }
            } else {
                activePlayerSets.remove(playerId)
                playerEffects.remove(playerId)
            }
        }
    }

    fun cleanup() {
        try {
            removeAllEffects()
            // effectScope.cancel()
        } catch (e: Exception) {
            Logger.error("Error during effect manager cleanup", e)
        }
    }

    fun getActivePlayerCount(): Int {
        return activePlayerSets.size
    }

    fun getActiveSets(): Map<UUID, ArmorSet> {
        return activePlayerSets.toMap()
    }

    override fun toString(): String {
        return "EffectManager(activePlayers=${activePlayerSets.size}, activeEffects=${playerEffects.size})"
    }
}
