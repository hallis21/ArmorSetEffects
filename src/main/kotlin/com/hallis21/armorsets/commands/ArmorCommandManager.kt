package com.hallis21.armorsets.commands

import com.hallis21.armorsets.ArmorSetsPlugin
import com.hallis21.armorsets.commands.subcommands.ArmorGuiCommand
import com.hallis21.armorsets.commands.subcommands.ArmorListCommand
import com.hallis21.armorsets.commands.subcommands.ArmorReloadCommand
import com.hallis21.armorsets.utils.Logger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ArmorCommandManager(private val plugin: ArmorSetsPlugin) : CommandExecutor, TabCompleter {
    private val subCommands = mutableMapOf<String, ArmorSubCommand>()

    init {
        registerSubCommands()
    }

    private fun registerSubCommands() {
        subCommands["reload"] = ArmorReloadCommand(plugin)
        subCommands["list"] = ArmorListCommand(plugin)
        subCommands["gui"] = ArmorGuiCommand(plugin)
    }

    fun registerCommands() {
        plugin.getCommand("armor-reload")?.setExecutor(ArmorReloadCommand(plugin))
        plugin.getCommand("armor-list")?.let { command ->
            val listCommand = ArmorListCommand(plugin)
            command.setExecutor(listCommand)
            command.tabCompleter = listCommand
        }
        plugin.getCommand("armor-gui")?.setExecutor(ArmorGuiCommand(plugin))

        Logger.debug("Commands registered successfully")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty()) {
            sendHelpMessage(sender)
            return true
        }

        val subCommandName = args[0].lowercase()
        val subCommand = subCommands[subCommandName]

        if (subCommand == null) {
            sender.sendMessage(
                Component.text("Unknown subcommand: $subCommandName", NamedTextColor.RED),
            )
            sendHelpMessage(sender)
            return true
        }

        if (!subCommand.hasPermission(sender)) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command!", NamedTextColor.RED),
            )
            return true
        }

        val subArgs = args.drop(1).toTypedArray()
        return subCommand.execute(sender, subArgs)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> {
        if (args.size == 1) {
            return subCommands.keys.filter { subCommandName ->
                subCommandName.startsWith(args[0].lowercase()) &&
                    subCommands[subCommandName]?.hasPermission(sender) == true
            }
        }

        if (args.size >= 2) {
            val subCommandName = args[0].lowercase()
            val subCommand = subCommands[subCommandName]

            if (subCommand != null && subCommand.hasPermission(sender)) {
                val subArgs = args.drop(1).toTypedArray()
                return subCommand.tabComplete(sender, subArgs)
            }
        }

        return emptyList()
    }

    private fun sendHelpMessage(sender: CommandSender) {
        val helpMessage =
            Component.text()
                .append(Component.text("ArmorSetEffects V2 Commands:", NamedTextColor.GOLD))
                .append(Component.newline())

        subCommands.forEach { (name, subCommand) ->
            if (subCommand.hasPermission(sender)) {
                helpMessage
                    .append(Component.text("  /${subCommand.getUsage()}", NamedTextColor.YELLOW))
                    .append(Component.text(" - ${subCommand.getDescription()}", NamedTextColor.GRAY))
                    .append(Component.newline())
            }
        }

        sender.sendMessage(helpMessage.build())
    }
}
