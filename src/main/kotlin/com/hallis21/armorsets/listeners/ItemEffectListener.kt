package com.hallis21.armorsets.listeners

import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.utils.Logger
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class ItemEffectListener(private val plugin: ArmorSetsPlugin) : Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!event.hasItem()) return

        val player = event.player
        val item = event.item ?: return

        if (!player.hasPermission("armorseteffects.receive")) return

        try {
            val activeArmorSet = plugin.effectManager.getActiveArmorSet(player)
            if (activeArmorSet == null) {
                Logger.debug("Player ${player.name} has no active armor set")
                return
            }

            val matchingItemEffect =
                activeArmorSet.itemEffects.find { itemEffect ->
                    itemEffect.isValidItem(item.type)
                }

            if (matchingItemEffect != null) {
                Logger.debug("Found matching item effect for ${player.name}: ${matchingItemEffect.item}")

                val success = plugin.effectManager.handleItemEffect(player, matchingItemEffect)

                if (success && matchingItemEffect.consumeItem) {
                    consumeItem(player, item)
                }
            }
        } catch (e: Exception) {
            Logger.error("Error handling item effect for ${player.name}", e)
        }
    }

    private fun consumeItem(
        player: Player,
        item: org.bukkit.inventory.ItemStack,
    ) {
        try {
            if (item.amount > 1) {
                item.amount = item.amount - 1
            } else {
                player.inventory.setItemInMainHand(org.bukkit.inventory.ItemStack(Material.AIR))
            }
            Logger.debug("Consumed item ${item.type} for ${player.name}")
        } catch (e: Exception) {
            Logger.error("Failed to consume item for ${player.name}", e)
        }
    }
}
