package hkr;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

class SetsCommand implements CommandExecutor {

    ArmorSetBonusMain pl;

    public SetsCommand(ArmorSetBonusMain pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("armorsetbonus.viewsets")) {
            if (args.length == 0) {
                String toPrint = "Loaded armor sets: ";
                if (pl.getArmorSets().size() == 0) {
                    sender.sendMessage("No sets are currently loaded.");
                    return true;
                }
                boolean first = true;
                for (ArmorSetNew set : pl.getArmorSets()) {
                    if (!set.getHidden() || sender.hasPermission("armorsetbonus.viewsets.all")) {
                        if (!first) {
                            toPrint += ", "+set.getName();
                        } else { 
                            toPrint += set.getName();
                            first = false;
                        }
                    }
                }
                sender.sendMessage(toPrint);
            } else {
                if (sender.hasPermission("armorsetbonus.viewsets.inspect")) {
                    boolean found = false;
                    for (ArmorSetNew set : pl.getArmorSets()) {
                        String name = set.getName();
                        for (String str : args) {
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                yup = true;
                            }
                        }                   
                        
                        if (yup && (!set.getHidden() || sender.hasPermission("armorsetbonus.viewsets.inspect.all"))) {
                            sender.sendMessage(set.toString());
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        sender.sendMessage("This set does not exist or is hidden!");
                    }

                } else {
                    return false;
                }

            }
            
            return true;
        } else {
            return false;
        }
    }

}