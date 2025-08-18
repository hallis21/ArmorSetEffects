package com.hallis21.armorsets.events

import com.hallis21.armorsets.models.ArmorSlot
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

class ArmorEquipEvent(
    player: Player,
    val equipMethod: EquipMethod,
    val armorSlot: ArmorSlot,
    val oldArmorPiece: ItemStack?,
    val newArmorPiece: ItemStack?,
) : PlayerEvent(player), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    override fun toString(): String {
        return "ArmorEquipEvent(player=${player.name}, method=$equipMethod, slot=$armorSlot, " +
            "old=${oldArmorPiece?.type}, new=${newArmorPiece?.type})"
    }
}

enum class EquipMethod {
    SHIFT_CLICK,
    DRAG,
    PICK_DROP,
    HOTBAR,
    HOTBAR_SWAP,
    DISPENSER,
    BROKE,
    DEATH,
    UNKNOWN,
}
