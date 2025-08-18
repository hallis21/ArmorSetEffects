package com.hallis21.armorsets.commands.subcommands

import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.commands.ArmorSubCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ArmorGuiCommand(private val plugin: ArmorSetsPlugin) : ArmorSubCommand, CommandExecutor {
    override fun execute(
        sender: CommandSender,
        args: Array<String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(
                Component.text("This command can only be used by players!", NamedTextColor.RED),
            )
            return true
        }

        val config = plugin.configManager.getConfig()
        if (!config.gui.enabled) {
            sender.sendMessage(
                Component.text("The armor sets GUI is currently disabled.", NamedTextColor.RED),
            )
            return true
        }

        // TODO: Open GUI when GUI system is implemented
        sender.sendMessage(
            Component.text("The armor sets GUI is not yet implemented.", NamedTextColor.YELLOW),
        )

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
        return "armor-gui"
    }

    override fun getDescription(): String {
        return "Opens the armor sets GUI"
    }

    override fun getPermission(): String {
        return "armorseteffects.viewsets"
    }
}
