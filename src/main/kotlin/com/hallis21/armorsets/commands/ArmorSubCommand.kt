package com.hallis21.armorsets.commands

import org.bukkit.command.CommandSender

interface ArmorSubCommand {
    fun execute(
        sender: CommandSender,
        args: Array<String>,
    ): Boolean

    fun tabComplete(
        sender: CommandSender,
        args: Array<String>,
    ): List<String>

    fun hasPermission(sender: CommandSender): Boolean

    fun getUsage(): String

    fun getDescription(): String

    fun getPermission(): String?
}
