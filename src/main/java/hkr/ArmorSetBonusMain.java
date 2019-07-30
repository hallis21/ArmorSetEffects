package hkr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.yaml.snakeyaml.Yaml;

import hkr.ArmorEquipEventFiles.ArmorListener;
import hkr.ArmorEquipEventFiles.DispenserArmorListener;

public class ArmorSetBonusMain extends JavaPlugin
{
    private ArrayList<ArmorSetNew> armorSets = new ArrayList<>();
    private HashMap<Player,ArmorSetNew> activeBonus = new HashMap<>();
    // Protected cause used in Listener
    protected HashMap<Player, HashMap<ItemEffect, Long>> cooldowns = new HashMap<Player, HashMap<ItemEffect, Long>>();
    protected HashMap<Player, Long> lastused = new HashMap<>();


public static void main(String[] args) {
        
}
    @Override
    public void onEnable(){
        extracted();
    }
    public ArrayList<ArmorSetNew> getArmorSets() {
        return armorSets;
    }

    private void loadConfigNew(){
        File dir = new File(this.getDataFolder()+"\\ArmorSets");
        File[] directoryListing = dir.listFiles();
        getLogger().info("Found folder, reading sets.");
        if (directoryListing != null) {
            for (File child : directoryListing) {
                try {
                    Yaml yaml = new Yaml();
                    InputStream file;
                    file = new FileInputStream(child);
                    Map<String, Object> newSetObj = yaml.load(file);
                    ArmorSetNew newSet = new ArmorSetNew();
                    newSet.setName((String)newSetObj.get("name"));
                    newSet.setHidden((boolean)newSetObj.get("hidden"));
                    ArmorPiece[] pieces = new ArmorPiece[4];
                    try {
                        List<Map<String, Object>> armorPieces = (List<Map<String, Object>>) newSetObj.get("armorPieces");
                        boolean empty = true;
                        for (int i = 0; i < 4; i++) {
                            try {
                                Map<String, Object> peiceData = armorPieces.get(i);
                                ArmorPiece newPiece = new ArmorPiece();
                                newPiece.setItem(((String)peiceData.get("item")).trim());
                                List<Map<String, Object>> tempT = (List<Map<String, Object>>) peiceData.get("metadata");
                                Map<String, Object> temp = (Map<String, Object>)tempT.get(0);
                                String[] tempMeta = new String[2];
                                tempMeta[0] = (String)temp.get("displayName");
                                tempMeta[1] = (String)temp.get("lore");
                                newPiece.setMetadata(tempMeta);
                                pieces[i] = newPiece;
                                empty = false;
                            } catch (IndexOutOfBoundsException | NullPointerException e) {
                                pieces[i] = new ArmorPiece();
                            }
                        }
                        if (empty) {
                            getServer().getConsoleSender().sendMessage(ChatColor.RED + newSet.getName()+ ": Armor set loaded with no chosen armor slots, this will interfere with all other armor sets");
                        }
                        
                    } catch (NullPointerException e) {
                        // Empty set if no items are assigned
                        for (int i = 0; i < 4; i++) {
                            pieces[i] = new ArmorPiece();
                        }
                        getServer().getConsoleSender().sendMessage(ChatColor.RED + newSet.getName()+ ": Armor set loaded with no chosen armor slots, this will interfere with all other armor sets");
                    }

                    newSet.setArmorPieces(pieces);
                    // If there are no effects the list will be empty and not castable to a list
                    try {
                        List<Map<String, Object>> permEffects = (List<Map<String, Object>>) newSetObj.get("permanentEffects");
                        PermanentEffect[] permanentEffects = new PermanentEffect[permEffects.size()];
                        int i = 0;
                        for (Map<String, Object> effect : permEffects) {
                            // If parsing permEffects fails it will be null
                            try {
                                PermanentEffect newEffect = new PermanentEffect();
                                newEffect.setEffectType((String)effect.get("effectType"));
                                newEffect.setAmplifier((Integer)effect.get("amplifier"));
                                permanentEffects[i] = newEffect;
                                newEffect.fix();
                                i++;
                            } catch (NullPointerException e) {
                                PermanentEffect newEffect = null;
                                i++;
                            }
    
                            
                        }
                        // If all elements are null it does not add the array, null
                        for (PermanentEffect permE : permanentEffects) {
                            if (permE != null) {
                                newSet.setPermanentEffects(permanentEffects);
                                break;
                            }
                        }
                        
                    } catch (ClassCastException | NullPointerException e) {
                       newSet.setPermanentEffects(null);
                    }

                    try {
                        
                        List<Map<String, Object>> itemE = (List<Map<String, Object>>) newSetObj.get("itemEffects");
                        ItemEffect[] itemEffects = new ItemEffect[itemE.size()];
                        int i = 0;
                        for (Map<String, Object> itemEffect : itemE) {
                            // If the item cannot be initialized it will simply be null
                            try {
                                ItemEffect newItemEffect = new ItemEffect();
                                String itemName =(String)itemEffect.get("item");
                                if (itemName == "null" || itemName == null) {
                                    newItemEffect.setItem(null);
                                } else{
                                    newItemEffect.setItem(itemName);
                                }
                                newItemEffect.setMetadata((String)itemEffect.get("metadata"));
                                List<Map<String, Object>> effects = (List<Map<String, Object>>) itemEffect.get("effects");
                                try {
                                    newItemEffect.setCooldown((int)itemEffect.get("cooldown"));
                                    
                                } catch (Exception e) {
                                    int l = 15;
                                    newItemEffect.setCooldown(l);
                                }
                                PotionEffect[] effectsList = new PotionEffect[itemEffect.size()];
                                int y = 0;
                                for (Map<String, Object> effect : effects) {
                                    // If there are no effects to add it throws a nullpointer and adds confusion for 0 seconds
                                    try {
                                        String type = (String) effect.get("effectType");
                                        int amp = (int)effect.get("amplifier");
                                        int duration = (int)effect.get("duration")*20;
                                        effectsList[y] = new PotionEffect(PotionEffectType.getByName(type), duration, amp);
                                        if (effectsList[y] == null) {
                                            effectsList[y] = new PotionEffect(PotionEffectType.getByName("CONFUSION"), 0, 0);
                                            getLogger().info("Error parsing itemeffects");
                                        }
                                        y++;
                                        
                                    } catch (NullPointerException e) {
                                        effectsList[y] = new PotionEffect(PotionEffectType.getByName("CONFUSION"), 0, 0);
                                    }
                                }
                                newItemEffect.setEffects(effectsList);
                                itemEffects[i] = newItemEffect;
                                i++;
                                
                            } catch (NullPointerException e) {
                                itemEffects[i] = null;
                                i++;
                            }
                        }
                        for (ItemEffect var : itemEffects) {
                            if (var != null) {
                                newSet.setItemEffects(itemEffects);
                                break;
                            }
                        }
                    } catch (NullPointerException e) {
                        newSet.setItemEffects(null);
                    }
                    armorSets.add(newSet);
                           
                    
                } catch (Exception e) {
                    getServer().getConsoleSender().sendMessage(ChatColor.RED + child.getName()+": Incorrect format.");
                    e.printStackTrace();
                }
                
            }
            getLogger().info("Found sets: "+armorSets.size());
        }
    }
    private void extracted() {
        try {
            checkFiles();
        } catch (IOException e) {
            getLogger().info("Error checking files");
            e.printStackTrace();
        }
            loadConfigNew();

    

        new CommandInit(this).InitCommands();

        
        getServer().getPluginManager().registerEvents(new ArmorListener(getConfig().getStringList("blocked")), this);
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);
        try {
            // Better way to check for this? Only in 1.13.1+?
            Class.forName("org.bukkit.event.block.BlockDispenseArmorEvent");
            getServer().getPluginManager().registerEvents(new DispenserArmorListener(), this);
        } catch (Exception ignored) {}
    }
    public boolean flush(){
        armorSets.clear();
        lastused.clear();
        try {
            checkFiles();
            loadConfigNew();
        } catch (Exception e) {
            getLogger().warning("Invalid config file(s).");
            return false;
        }
        updateAll();
        return true;
    }

    private void updateAll() {
        for (Player player : getServer().getOnlinePlayers()) {
            checkForBonus(player);
        }
    }
    @Override
    public void onDisable() {
        for (Player player : activeBonus.keySet()) {
            removeBonus(player);
        }
    }

    private void checkFiles() throws IOException {
        saveDefaultConfig();
        File f = new File(this.getDataFolder() + "/");
        if (!f.exists()) {
            f.mkdir();
        }
        File fc = new File(this.getDataFolder() + "/armorSets/");
        if (!fc.exists()){
            saveResource("armorSets", false);
        }

        saveResource("example_set.yml", true);
        saveResource("example_set2.yml", true);
        saveResource("example_set3.yml", true);
        saveResource("README.txt", true);

    }
	public void checkForBonus(Player ply) {
        final Player player = ply;
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
        
            @Override
            public void run() {
                ArmorSetNew added = new ArmorSetNew();
                for (ArmorSetNew set : armorSets) {
                    if (playerHas(player, set)) {
                        removeBonus(player);
                        addBonus(player, set);
                        added = set;
                        break;
                    } 
                }
                if (activeBonus.containsKey(player)) {
                    if (activeBonus.get(player) != added) {
                        removeBonus(player);
                        
                    }
                }
            }
        }, 1);
	}

    private boolean playerHas(Player player, ArmorSetNew Aset) {
        int i = 0;
        for (ArmorPiece a : Aset.getArmorPieces()) {
            ItemStack pItem = player.getInventory().getArmorContents()[3-i];
            if (a.getItem() != null) {
                if (a.getItem() == Material.AIR) {
                    if (pItem != null) {
                        return false;
                    }
                } else {
                    // Check same
                    if (pItem == null) {
                        return false;
                    } else {
                        if (pItem.getType() == a.getItem()) {
                            if (a.getMetadata()[0] != null && !pItem.getItemMeta().getDisplayName().contains(a.getMetadata()[0])) {
                                return false;
                            }
                            if (a.getMetadata()[1] != null && !pItem.getItemMeta().getLore().contains((String) a.getMetadata()[1])) {
                                return false;
                            }
                            
                        } else {
                            return false;
                        }
                    }
                }
            }
            i++;
        }



       return true;
    }

    public void removeBonus(Player player) {
        if (activeBonus.containsKey(player)) {
            ArmorSetNew activeSet = activeBonus.get(player);
            PermanentEffect[] effects = activeSet.getPermanentEffects();
            player.sendMessage("You lost armor set bonus: "+activeSet.getName());
            if (effects != null) {
                for (PermanentEffect pE : effects) {
                    player.removePotionEffect(pE.getEffectType());
                }
                
            }
            ItemEffect[] itemEffects = activeSet.getItemEffects();
            if (itemEffects != null) {
                for (ItemEffect iE : itemEffects) {
                    for (PotionEffect var : iE.getEffects()) {
                        if (var != null) {
                            player.removePotionEffect(var.getType());
                        }
                    }
                }
                
            }
            activeBonus.remove(player);
            }
    }

    public void addItemEffect(Player player, ItemEffect effect) {
        player.sendMessage("You activated an ability from: "+activeBonus.get(player).getName());
        for (PotionEffect peH : effect.getEffects()) {
            if (peH != null) {
                player.addPotionEffect(peH);
            }
        }
    }

    private void addBonus(Player player, ArmorSetNew set) {
        PermanentEffect[] effects = set.getPermanentEffects();

        player.sendMessage("You got an armor set bonus: "+set.getName());
        if (effects != null) {
            for (PermanentEffect pE : effects) {
                player.addPotionEffect(pE.getPotionEffect());
            }
            
        }
        activeBonus.put(player, set);
    }
    /**
     * @return the activeBonus
     */
    public HashMap<Player, ArmorSetNew> getActiveBonus() {
        return activeBonus;
    }
    

}