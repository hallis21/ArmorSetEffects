package hkr;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import hkr.ArmorSetBonusMain;

class ReloadSets implements CommandExecutor {

    ArmorSetBonusMain pl;
    public ReloadSets(ArmorSetBonusMain pl) {
        this.pl = pl;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Reloading config");
        if (pl.flush()) {
            sender.sendMessage("Reloaded");
        } else {
            sender.sendMessage("Something went wrong. This plugin might not behave correctly anymore.");
        }
        return true;
	}
    
}