package com.hallis21.armorsets.listeners

import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.events.ArmorEquipEvent
import com.hallis21.armorsets.events.EquipMethod
import com.hallis21.armorsets.models.ArmorPiece
import com.hallis21.armorsets.models.ArmorSlot
import com.hallis21.armorsets.utils.Logger
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseArmorEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class ArmorEquipListener(private val plugin: ArmorSetsPlugin) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        object : BukkitRunnable() {
            override fun run() {
                checkPlayerArmor(player)
            }
        }.runTaskLater(plugin, 5L)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.effectManager.removeArmorSetEffects(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        for (slot in ArmorSlot.values()) {
            val armorItem = getArmorItem(player, slot)
            if (armorItem != null && armorItem.type != Material.AIR) {
                val armorEquipEvent =
                    ArmorEquipEvent(
                        player,
                        EquipMethod.DEATH,
                        slot,
                        armorItem,
                        null,
                    )
                Bukkit.getPluginManager().callEvent(armorEquipEvent)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerItemBreak(event: PlayerItemBreakEvent) {
        val player = event.player
        val brokenItem = event.getBrokenItem()

        ArmorSlot.fromMaterial(brokenItem.type)?.let { slot ->
            val armorEquipEvent =
                ArmorEquipEvent(
                    player,
                    EquipMethod.BROKE,
                    slot,
                    brokenItem,
                    null,
                )
            Bukkit.getPluginManager().callEvent(armorEquipEvent)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.hasItem()) return

        val player = event.player
        val item = event.item ?: return

        ArmorSlot.fromMaterial(item.type)?.let { slot ->
            val currentArmor = getArmorItem(player, slot)

            if (currentArmor == null || currentArmor.type == Material.AIR) {
                val armorEquipEvent =
                    ArmorEquipEvent(
                        player,
                        EquipMethod.PICK_DROP,
                        slot,
                        null,
                        item,
                    )
                Bukkit.getPluginManager().callEvent(armorEquipEvent)

                if (!armorEquipEvent.isCancelled) {
                    object : BukkitRunnable() {
                        override fun run() {
                            checkPlayerArmor(player)
                        }
                    }.runTaskLater(plugin, 3L)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (event.isCancelled) return

        when (event.slotType) {
            InventoryType.SlotType.ARMOR -> {
                handleArmorSlotClick(event, player)
            }
            InventoryType.SlotType.QUICKBAR, InventoryType.SlotType.CONTAINER -> {
                handleQuickbarOrContainerClick(event, player)
            }
            else -> return
        }
    }

    private fun handleArmorSlotClick(
        event: InventoryClickEvent,
        player: Player,
    ) {
        val slot = getArmorSlotFromRawSlot(event.rawSlot) ?: return
        val currentItem = event.currentItem
        val cursorItem = event.cursor

        val equipMethod =
            when (event.click) {
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> EquipMethod.SHIFT_CLICK
                else -> EquipMethod.PICK_DROP
            }

        val armorEquipEvent =
            ArmorEquipEvent(
                player,
                equipMethod,
                slot,
                currentItem,
                cursorItem,
            )
        Bukkit.getPluginManager().callEvent(armorEquipEvent)

        if (!armorEquipEvent.isCancelled) {
            object : BukkitRunnable() {
                override fun run() {
                    checkPlayerArmor(player)
                }
            }.runTaskLater(plugin, 1L)
        }
    }

    private fun handleQuickbarOrContainerClick(
        event: InventoryClickEvent,
        player: Player,
    ) {
        if (event.click != ClickType.SHIFT_LEFT && event.click != ClickType.SHIFT_RIGHT) return
        if (event.currentItem == null || event.currentItem!!.type == Material.AIR) return

        val item = event.currentItem!!
        val slot = ArmorSlot.fromMaterial(item.type) ?: return
        val currentArmor = getArmorItem(player, slot)

        if (currentArmor == null || currentArmor.type == Material.AIR) {
            val armorEquipEvent =
                ArmorEquipEvent(
                    player,
                    EquipMethod.SHIFT_CLICK,
                    slot,
                    null,
                    item,
                )
            Bukkit.getPluginManager().callEvent(armorEquipEvent)

            if (!armorEquipEvent.isCancelled) {
                object : BukkitRunnable() {
                    override fun run() {
                        checkPlayerArmor(player)
                    }
                }.runTaskLater(plugin, 1L)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return

        val armorSlots =
            event.rawSlots.mapNotNull { rawSlot ->
                getArmorSlotFromRawSlot(rawSlot)
            }

        if (armorSlots.isNotEmpty()) {
            armorSlots.forEach { slot ->
                val currentItem = getArmorItem(player, slot)
                val newItem =
                    event.newItems[
                        event.rawSlots.first {
                            getArmorSlotFromRawSlot(it) == slot
                        },
                    ]

                val armorEquipEvent =
                    ArmorEquipEvent(
                        player,
                        EquipMethod.DRAG,
                        slot,
                        currentItem,
                        newItem,
                    )
                Bukkit.getPluginManager().callEvent(armorEquipEvent)
            }

            object : BukkitRunnable() {
                override fun run() {
                    checkPlayerArmor(player)
                }
            }.runTaskLater(plugin, 1L)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockDispenseArmor(event: BlockDispenseArmorEvent) {
        val player = event.targetEntity as? Player ?: return
        val item = event.item
        val slot = ArmorSlot.fromMaterial(item.type) ?: return
        val currentItem = getArmorItem(player, slot)

        val armorEquipEvent =
            ArmorEquipEvent(
                player,
                EquipMethod.DISPENSER,
                slot,
                currentItem,
                item,
            )
        Bukkit.getPluginManager().callEvent(armorEquipEvent)

        if (!armorEquipEvent.isCancelled) {
            object : BukkitRunnable() {
                override fun run() {
                    checkPlayerArmor(player)
                }
            }.runTaskLater(plugin, 1L)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onArmorEquip(event: ArmorEquipEvent) {
        if (event.isCancelled) return

        val player = event.player
        if (!player.hasPermission("armorseteffects.receive")) return

        val blockedMaterials = plugin.configManager.getBlockedMaterials()
        if (event.newArmorPiece?.type in blockedMaterials) {
            event.isCancelled = true
            return
        }

        object : BukkitRunnable() {
            override fun run() {
                checkPlayerArmor(player)
            }
        }.runTaskLater(plugin, 2L)
    }

    private fun checkPlayerArmor(player: Player) {
        try {
            if (!player.isOnline || !player.hasPermission("armorseteffects.receive")) {
                return
            }

            val currentArmor =
                mapOf(
                    ArmorSlot.HELMET to
                        player.inventory.helmet?.let {
                            ArmorPiece(it.type.name, ArmorSlot.HELMET)
                        },
                    ArmorSlot.CHESTPLATE to
                        player.inventory.chestplate?.let {
                            ArmorPiece(it.type.name, ArmorSlot.CHESTPLATE)
                        },
                    ArmorSlot.LEGGINGS to
                        player.inventory.leggings?.let {
                            ArmorPiece(it.type.name, ArmorSlot.LEGGINGS)
                        },
                    ArmorSlot.BOOTS to
                        player.inventory.boots?.let {
                            ArmorPiece(it.type.name, ArmorSlot.BOOTS)
                        },
                )

            val currentActiveSet = plugin.effectManager.getActiveArmorSet(player)
            var bestMatch: com.hallis21.armorsets.models.ArmorSet? = null

            val armorSets = plugin.armorSetManager.getArmorSetsByPriority()
            for (armorSet in armorSets) {
                if (armorSet.isValidFor(player) && armorSet.isComplete(currentArmor)) {
                    bestMatch = armorSet
                    break
                }
            }

            if (bestMatch != currentActiveSet) {
                if (currentActiveSet != null) {
                    plugin.effectManager.removeArmorSetEffects(player)
                }

                if (bestMatch != null) {
                    plugin.effectManager.applyArmorSetEffects(player, bestMatch)
                }
            }
        } catch (e: Exception) {
            Logger.error("Error checking player armor for ${player.name}", e)
        }
    }

    private fun getArmorItem(
        player: Player,
        slot: ArmorSlot,
    ): ItemStack? {
        return when (slot) {
            ArmorSlot.HELMET -> player.inventory.helmet
            ArmorSlot.CHESTPLATE -> player.inventory.chestplate
            ArmorSlot.LEGGINGS -> player.inventory.leggings
            ArmorSlot.BOOTS -> player.inventory.boots
        }
    }

    private fun getArmorSlotFromRawSlot(rawSlot: Int): ArmorSlot? {
        return when (rawSlot) {
            5 -> ArmorSlot.HELMET
            6 -> ArmorSlot.CHESTPLATE
            7 -> ArmorSlot.LEGGINGS
            8 -> ArmorSlot.BOOTS
            else -> null
        }
    }
}
