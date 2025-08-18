package com.hallis21.armorsets.models

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

@Serializable
data class ArmorPiece(
    val item: String,
    val slot: ArmorSlot,
    val metadata: ItemMetadata? = null,
) {
    fun getMaterial(): Material? {
        return try {
            Material.valueOf(item.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun matches(other: ArmorPiece): Boolean {
        return item.equals(other.item, ignoreCase = true) &&
            slot == other.slot &&
            (metadata?.matches(other.metadata) ?: true)
    }

    fun matches(itemStack: ItemStack?): Boolean {
        if (itemStack == null || itemStack.type == Material.AIR) return false

        val material = getMaterial() ?: return false
        if (itemStack.type != material) return false

        return metadata?.matches(itemStack.itemMeta) ?: true
    }

    fun createItemStack(): ItemStack? {
        val material = getMaterial() ?: return null
        val itemStack = ItemStack(material)

        metadata?.apply(itemStack.itemMeta)?.let { meta ->
            itemStack.itemMeta = meta
        }

        return itemStack
    }

    override fun toString(): String {
        return "ArmorPiece(item='$item', slot=$slot, hasMetadata=${metadata != null})"
    }
}

@Serializable
enum class ArmorSlot {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    ;

    companion object {
        fun fromMaterial(material: Material): ArmorSlot? {
            return when {
                material.name.endsWith("_HELMET") -> HELMET
                material.name.endsWith("_CHESTPLATE") -> CHESTPLATE
                material.name.endsWith("_LEGGINGS") -> LEGGINGS
                material.name.endsWith("_BOOTS") -> BOOTS
                else -> null
            }
        }
    }
}

@Serializable
data class ItemMetadata(
    val displayName: String? = null,
    val lore: List<String>? = null,
    val customModelData: Int? = null,
) {
    fun matches(other: ItemMetadata?): Boolean {
        if (other == null) return true

        return (displayName == null || displayName == other.displayName) &&
            (lore == null || lore == other.lore) &&
            (customModelData == null || customModelData == other.customModelData)
    }

    fun matches(itemMeta: ItemMeta?): Boolean {
        if (itemMeta == null) return true

        val nameMatches =
            displayName?.let { expected ->
                itemMeta.displayName()?.let { actual ->
                    expected == actual.toString()
                } ?: false
            } ?: true

        val loreMatches =
            lore?.let { expectedLore ->
                itemMeta.lore()?.let { actualLore ->
                    expectedLore == actualLore.map { it.toString() }
                } ?: false
            } ?: true

        val modelDataMatches =
            customModelData?.let { expected ->
                itemMeta.hasCustomModelData() && itemMeta.customModelData == expected
            } ?: true

        return nameMatches && loreMatches && modelDataMatches
    }

    fun apply(itemMeta: ItemMeta?): ItemMeta? {
        if (itemMeta == null) return null

        displayName?.let { name ->
            itemMeta.displayName(net.kyori.adventure.text.Component.text(name))
        }

        lore?.let { loreList ->
            itemMeta.lore(loreList.map { net.kyori.adventure.text.Component.text(it) })
        }

        customModelData?.let { modelData ->
            itemMeta.setCustomModelData(modelData)
        }

        return itemMeta
    }
}
