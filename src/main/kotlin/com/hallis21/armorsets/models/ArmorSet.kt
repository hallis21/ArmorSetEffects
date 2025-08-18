package com.hallis21.armorsets.models

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

@Serializable
data class ArmorSet(
    val name: String,
    val displayName: String? = null,
    val hidden: Boolean = false,
    val priority: Int = 0,
    val permission: String? = null,
    val equipMessage: String? = null,
    val unequipMessage: String? = null,
    val armorPieces: List<ArmorPiece> = emptyList(),
    val permanentEffects: List<PermanentEffect> = emptyList(),
    val itemEffects: List<ItemEffect> = emptyList(),
) {
    fun getDisplayNameOrFallback(): String = displayName ?: name

    fun hasPermission(player: Player): Boolean {
        return permission?.let { player.hasPermission("armorseteffects.sets.$it") } ?: true
    }

    fun getEquipMessage(): Component? {
        return equipMessage?.let { MiniMessage.miniMessage().deserialize(it) }
    }

    fun getUnequipMessage(): Component? {
        return unequipMessage?.let { MiniMessage.miniMessage().deserialize(it) }
    }

    fun isValidFor(player: Player): Boolean {
        return hasPermission(player) && player.hasPermission("armorseteffects.receive")
    }

    fun getRequiredSlots(): Set<ArmorSlot> {
        return armorPieces.map { it.slot }.toSet()
    }

    fun isComplete(equippedArmor: Map<ArmorSlot, ArmorPiece?>): Boolean {
        return armorPieces.all { requiredPiece ->
            equippedArmor[requiredPiece.slot]?.let { equippedPiece ->
                requiredPiece.matches(equippedPiece)
            } ?: false
        }
    }

    override fun toString(): String {
        return "ArmorSet(name='$name', priority=$priority, pieces=${armorPieces.size})"
    }
}
