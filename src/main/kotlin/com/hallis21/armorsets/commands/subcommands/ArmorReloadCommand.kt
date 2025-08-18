package com.hallis21.armorsets.commands.subcommands

import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.commands.ArmorSubCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ArmorReloadCommand(private val plugin: ArmorSetsPlugin) : ArmorSubCommand, CommandExecutor {
    override fun execute(
        sender: CommandSender,
        args: Array<String>,
    ): Boolean {
        sender.sendMessage(
            Component.text("Reloading ArmorSetEffects configuration...", NamedTextColor.YELLOW),
        )

        val startTime = System.currentTimeMillis()
        val success = plugin.reload()
        val duration = System.currentTimeMillis() - startTime

        if (success) {
            val armorSetCount = plugin.armorSetManager.getArmorSetCount()
            val activePlayerCount = plugin.effectManager.getActivePlayerCount()

            sender.sendMessage(
                Component.text()
                    .append(Component.text("✓ ", NamedTextColor.GREEN))
                    .append(Component.text("ArmorSetEffects reloaded successfully!", NamedTextColor.GREEN))
                    .append(Component.newline())
                    .append(Component.text("  ● ", NamedTextColor.GRAY))
                    .append(Component.text("Loaded $armorSetCount armor sets", NamedTextColor.GRAY))
                    .append(Component.newline())
                    .append(Component.text("  ● ", NamedTextColor.GRAY))
                    .append(Component.text("$activePlayerCount players with active sets", NamedTextColor.GRAY))
                    .append(Component.newline())
                    .append(Component.text("  ● ", NamedTextColor.GRAY))
                    .append(Component.text("Completed in ${duration}ms", NamedTextColor.GRAY))
                    .build(),
            )
        } else {
            sender.sendMessage(
                Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text("Failed to reload ArmorSetEffects!", NamedTextColor.RED))
                    .append(Component.newline())
                    .append(Component.text("Check console for error details.", NamedTextColor.GRAY))
                    .build(),
            )
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

    override fun tabComplete(
        sender: CommandSender,
        args: Array<String>,
    ): List<String> {
        return emptyList()
    }

    override fun hasPermission(sender: CommandSender): Boolean {
        return sender.hasPermission(getPermission() ?: return true)
    }

    override fun getUsage(): String {
        return "armor-reload"
    }

    override fun getDescription(): String {
        return "Reloads the armor set configuration"
    }

    override fun getPermission(): String {
        return "armorseteffects.reload"
    }
}
