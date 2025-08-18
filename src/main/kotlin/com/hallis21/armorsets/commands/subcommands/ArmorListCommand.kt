package com.hallis21.armorsets.commands.subcommands

import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.commands.ArmorSubCommand
import com.hallis21.armorsets.models.ArmorSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ArmorListCommand(private val plugin: ArmorSetsPlugin) : ArmorSubCommand, CommandExecutor, TabCompleter {
    override fun execute(
        sender: CommandSender,
        args: Array<String>,
    ): Boolean {
        if (args.isEmpty()) {
            showArmorSetList(sender)
        } else {
            val setName = args[0]
            showArmorSetDetails(sender, setName)
        }
        return true
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        return execute(sender, Array(args.size) { i -> args[i] })
    }

    private fun showArmorSetList(sender: CommandSender) {
        val canViewHidden = sender.hasPermission("armorseteffects.viewsets.all")
        val armorSets =
            if (canViewHidden) {
                plugin.armorSetManager.getAllArmorSets()
            } else {
                plugin.armorSetManager.getVisibleArmorSets()
            }

        if (armorSets.isEmpty()) {
            sender.sendMessage(
                Component.text("No armor sets are currently loaded.", NamedTextColor.YELLOW),
            )
            return
        }

        val message =
            Component.text()
                .append(Component.text("Available Armor Sets:", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.newline())

        val sortedSets = armorSets.sortedByDescending { it.priority }

        sortedSets.forEach { armorSet ->
            val hasPermission =
                if (sender is Player) {
                    armorSet.hasPermission(sender)
                } else {
                    true
                }

            val color =
                when {
                    !hasPermission -> NamedTextColor.DARK_GRAY
                    armorSet.hidden -> NamedTextColor.GRAY
                    else -> NamedTextColor.GREEN
                }

            message
                .append(Component.text("  ● ", color))
                .append(Component.text(armorSet.getDisplayNameOrFallback(), color))
                .append(Component.text(" (Priority: ${armorSet.priority})", NamedTextColor.GRAY))

            if (armorSet.hidden) {
                message.append(Component.text(" [Hidden]", NamedTextColor.DARK_GRAY))
            }

            if (!hasPermission) {
                message.append(Component.text(" [No Permission]", NamedTextColor.RED))
            }

            message.append(Component.newline())
        }

        message
            .append(Component.newline())
            .append(Component.text("Use ", NamedTextColor.GRAY))
            .append(Component.text("/armor-list <set name>", NamedTextColor.YELLOW))
            .append(Component.text(" for details", NamedTextColor.GRAY))

        sender.sendMessage(message.build())
    }

    private fun showArmorSetDetails(
        sender: CommandSender,
        setName: String,
    ) {
        val armorSet = plugin.armorSetManager.getArmorSet(setName)

        if (armorSet == null) {
            sender.sendMessage(
                Component.text("Armor set '$setName' not found!", NamedTextColor.RED),
            )
            return
        }

        val canInspectAll = sender.hasPermission("armorseteffects.viewsets.inspect.all")
        val canInspect = sender.hasPermission("armorseteffects.viewsets.inspect")

        if (armorSet.hidden && !canInspectAll) {
            sender.sendMessage(
                Component.text("You don't have permission to inspect that armor set!", NamedTextColor.RED),
            )
            return
        }

        if (!armorSet.hidden && !canInspect && !canInspectAll) {
            sender.sendMessage(
                Component.text("You don't have permission to inspect armor sets!", NamedTextColor.RED),
            )
            return
        }

        showDetailedArmorSetInfo(sender, armorSet)
    }

    private fun showDetailedArmorSetInfo(
        sender: CommandSender,
        armorSet: ArmorSet,
    ) {
        val message =
            Component.text()
                .append(Component.text("Armor Set: ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(armorSet.getDisplayNameOrFallback(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.newline())

        message
            .append(Component.text("Priority: ", NamedTextColor.GRAY))
            .append(Component.text(armorSet.priority.toString(), NamedTextColor.WHITE))
            .append(Component.newline())

        if (armorSet.permission != null) {
            message
                .append(Component.text("Permission: ", NamedTextColor.GRAY))
                .append(Component.text("armorseteffects.sets.${armorSet.permission}", NamedTextColor.WHITE))
                .append(Component.newline())
        }

        if (armorSet.hidden) {
            message
                .append(Component.text("Status: ", NamedTextColor.GRAY))
                .append(Component.text("Hidden", NamedTextColor.YELLOW))
                .append(Component.newline())
        }

        message.append(Component.newline())

        if (armorSet.armorPieces.isNotEmpty()) {
            message
                .append(Component.text("Required Armor:", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                .append(Component.newline())

            armorSet.armorPieces.forEach { piece ->
                message
                    .append(Component.text("  ${piece.slot.name}: ", NamedTextColor.GRAY))
                    .append(Component.text(piece.item.replace("_", " "), NamedTextColor.WHITE))
                    .append(Component.newline())
            }
            message.append(Component.newline())
        }

        if (armorSet.permanentEffects.isNotEmpty()) {
            message
                .append(Component.text("Permanent Effects:", NamedTextColor.GREEN, TextDecoration.UNDERLINED))
                .append(Component.newline())

            armorSet.permanentEffects.forEach { effect ->
                val effectName =
                    effect.effectType.replace("_", " ").lowercase()
                        .split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

                message
                    .append(Component.text("  ● ", NamedTextColor.GREEN))
                    .append(Component.text(effectName, NamedTextColor.WHITE))

                if (effect.amplifier > 0) {
                    message.append(Component.text(" ${effect.amplifier + 1}", NamedTextColor.GRAY))
                }

                message.append(Component.newline())
            }
            message.append(Component.newline())
        }

        if (armorSet.itemEffects.isNotEmpty()) {
            message
                .append(Component.text("Item Effects:", NamedTextColor.LIGHT_PURPLE, TextDecoration.UNDERLINED))
                .append(Component.newline())

            armorSet.itemEffects.forEach { itemEffect ->
                val itemName =
                    itemEffect.item.replace("_", " ").lowercase()
                        .split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

                message
                    .append(Component.text("  ● ", NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text(itemName, NamedTextColor.WHITE))
                    .append(Component.text(" (${itemEffect.cooldown}s cooldown)", NamedTextColor.GRAY))
                    .append(Component.newline())

                itemEffect.effects.forEach { effect ->
                    val effectName =
                        effect.effectType.replace("_", " ").lowercase()
                            .split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

                    message
                        .append(Component.text("    - ", NamedTextColor.GRAY))
                        .append(Component.text(effectName, NamedTextColor.WHITE))

                    if (effect.amplifier > 0) {
                        message.append(Component.text(" ${effect.amplifier + 1}", NamedTextColor.GRAY))
                    }

                    message
                        .append(Component.text(" for ${effect.duration}s", NamedTextColor.GRAY))
                        .append(Component.newline())
                }
            }
        }

        sender.sendMessage(message.build())
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> {
        return tabComplete(sender, Array(args.size) { i -> args[i] })
    }

    override fun tabComplete(
        sender: CommandSender,
        args: Array<String>,
    ): List<String> {
        if (args.size == 1) {
            val canViewHidden = sender.hasPermission("armorseteffects.viewsets.all")
            val armorSets =
                if (canViewHidden) {
                    plugin.armorSetManager.getAllArmorSets()
                } else {
                    plugin.armorSetManager.getVisibleArmorSets()
                }

            return armorSets
                .map { it.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
                .sorted()
        }
        return emptyList()
    }

    override fun hasPermission(sender: CommandSender): Boolean {
        return sender.hasPermission(getPermission() ?: return true)
    }

    override fun getUsage(): String {
        return "armor-list [set name]"
    }

    override fun getDescription(): String {
        return "Lists available armor sets or shows details for a specific set"
    }

    override fun getPermission(): String {
        return "armorseteffects.viewsets"
    }
}
