package com.hallis21.armorsets.models

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Serializable
data class PermanentEffect(
    val effectType: String,
    val amplifier: Int = 0,
    val ambient: Boolean = false,
    val particles: Boolean = true,
    val icon: Boolean = true,
) {
    fun getPotionEffectType(): PotionEffectType? {
        return try {
            PotionEffectType.getByName(effectType.uppercase())
        } catch (e: Exception) {
            null
        }
    }

    fun createPotionEffect(duration: Int = Int.MAX_VALUE): PotionEffect? {
        val type = getPotionEffectType() ?: return null
        return PotionEffect(type, duration, amplifier, ambient, particles, icon)
    }

    override fun toString(): String {
        return "PermanentEffect(type='$effectType', amplifier=$amplifier)"
    }
}

@Serializable
data class ItemEffect(
    val item: String,
    val cooldown: Int = 0,
    val consumeItem: Boolean = false,
    val message: String? = null,
    val effects: List<TemporaryEffect> = emptyList(),
) {
    fun getMaterial(): Material? {
        return try {
            Material.valueOf(item.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun isValidItem(material: Material): Boolean {
        return getMaterial() == material
    }

    override fun toString(): String {
        return "ItemEffect(item='$item', cooldown=${cooldown}s, effects=${effects.size})"
    }
}

@Serializable
data class TemporaryEffect(
    val effectType: String,
    val amplifier: Int = 0,
    val duration: Int = 20,
    val ambient: Boolean = false,
    val particles: Boolean = true,
    val icon: Boolean = true,
) {
    fun getPotionEffectType(): PotionEffectType? {
        return try {
            PotionEffectType.getByName(effectType.uppercase())
        } catch (e: Exception) {
            null
        }
    }

    fun createPotionEffect(): PotionEffect? {
        val type = getPotionEffectType() ?: return null
        return PotionEffect(type, duration * 20, amplifier, ambient, particles, icon)
    }

    override fun toString(): String {
        return "TemporaryEffect(type='$effectType', amplifier=$amplifier, duration=${duration}s)"
    }
}

@Serializable
enum class EffectTrigger {
    EQUIP,
    UNEQUIP,
    ITEM_USE,
    PERIODIC,
}
