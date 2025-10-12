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
        // If we require metadata but item has none, it doesn't match
        if (itemMeta == null) {
            val result = displayName == null && lore == null && customModelData == null
            com.hallis21.armorsets.utils.Logger.debug(
                "ItemMeta null. Req: name=$displayName, lore=$lore, model=$customModelData. Match: $result",
            )
            return result
        }

        val actualDisplayName =
            itemMeta.displayName()?.let { component ->
                net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component)
            }
        val actualLore =
            itemMeta.lore()?.map { component ->
                net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component)
            }

        val nameMatches =
            displayName?.let { expected ->
                val matches = actualDisplayName == expected
                com.hallis21.armorsets.utils.Logger.debug(
                    "Name check - Expected: '$expected', Actual: '$actualDisplayName', Match: $matches",
                )
                matches
            } ?: true

        val loreMatches =
            lore?.let { expectedLore ->
                val matches = actualLore == expectedLore
                com.hallis21.armorsets.utils.Logger.debug(
                    "Lore check - Expected: $expectedLore, Actual: $actualLore, Match: $matches",
                )
                matches
            } ?: true

        val modelDataMatches =
            customModelData?.let { expected ->
                itemMeta.hasCustomModelData() && itemMeta.customModelData == expected
            } ?: true

        val result = nameMatches && loreMatches && modelDataMatches
        com.hallis21.armorsets.utils.Logger.debug(
            "Overall match: $result (name: $nameMatches, lore: $loreMatches, model: $modelDataMatches)",
        )
        return result
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
