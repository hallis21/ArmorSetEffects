package hkr;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


class ReloadSets implements CommandExecutor {

    ArmorSetEffectsMain pl;
    public ReloadSets(ArmorSetEffectsMain pl) {
        this.pl = pl;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("armorseteffects.reload") || sender.isOp())){
            return false;
        }



        sender.sendMessage("Reloading config");
        if (pl.flush()) {
            sender.sendMessage("Reloaded");
        } else {
            sender.sendMessage("Something went wrong. This plugin might not behave correctly anymore.");
        }
        return true;
	}
    
}