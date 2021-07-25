package hkr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hkr.ArmorEquipEventFiles.ArmorListener;
import hkr.ArmorEquipEventFiles.DispenserArmorListener;

public class ArmorSetEffectsMain extends JavaPlugin
{
    private ArrayList<ArmorSet> armorSets = new ArrayList<>();
    private HashMap<Player,ArmorSet> activeBonus = new HashMap<>();
    // Protected cause used in Listener
    protected HashMap<Player, HashMap<ItemEffect, Long>> cooldowns = new HashMap<Player, HashMap<ItemEffect, Long>>();
    protected HashMap<Player, Long> lastused = new HashMap<>();



    @Override
    public void onEnable(){
        start();
    }
    public ArrayList<ArmorSet> getArmorSets() {
        return armorSets;
    }

    private void loadConfigNew(){
        ArrayList<File> directoryListing = new ArrayList<>();
        ;
        try {
            Path dir = Paths.get(this.getDataFolder() + "/armorsets");
            if(!Files.exists(dir)){
                try {
                    checkFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                    getServer().getConsoleSender()
                            .sendMessage(ChatColor.RED + "Error checking files. If this persists try manually creating these folders: plugins/ArmorSetEffects/ArmorSets");
                }
            }
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir);
            for (Path path : dirStream) {
                File newFile = new File(path.toString());
                directoryListing.add(newFile);
            }
        } catch (IOException e1) {
            getLogger().warning("IOexception, could not load files");
            e1.printStackTrace();
            return;
        }
        String toprint = "Files found: ";

        for (File file : directoryListing) {
            toprint += file.getName() + " | ";
        }

        getLogger().info("Found folder, reading sets.");
        String temps = "(If no files are listed here the plugin has not found any)";
        if(!toprint.equals("Files found: ")) {temps = "";}
        getLogger().info(toprint + temps);
        if (directoryListing != null && directoryListing.size() != 0) {
            getLogger().info("Found " + directoryListing.size() + " files to read.");
            for (File child : directoryListing) {
                try {



                    InputStream file;
                    file = new FileInputStream(child);
                    // JsonReader jsonReader = new JsonReader(new InputStreamReader(file));
                    JsonObject jp = new JsonParser().parse(new InputStreamReader(file)).getAsJsonObject();

                    
                    ArmorSet newSet = new ArmorSet();
                    newSet.setName(jp.get("name").getAsString());
                    newSet.setHidden(jp.get("hidden").getAsBoolean());
                    newSet.setPriority(jp.get("priority").getAsInt());
                    if (!jp.get("equipMessage").isJsonNull()){
                        newSet.setGetMessage(jp.get("equipMessage").getAsString());
                    }
                    if (!jp.get("unequipMessage").isJsonNull()) {
                        newSet.setLooseMessage(jp.get("unequipMessage").getAsString());
                    }
                    if (!jp.get("permission").isJsonNull()) {
                        newSet.setPermission(jp.get("permission").getAsString());
                    }


                    ArmorPiece[] pieces = new ArmorPiece[4];
                    try {
                        JsonArray armorPieces = jp.get("armorPieces").getAsJsonArray();
                        boolean empty = true;
                        for (int i = 0; i < 4; i++) {
                            try {
                                JsonObject pieceData = armorPieces.get(i).getAsJsonObject();
                                ArmorPiece newPiece = new ArmorPiece();
                                String item = pieceData.get("item").getAsString().trim();
                                newPiece.setItem(item);
                                if (newPiece.getItem() == null && !(item.equals("AIR") || item.equals(""))){
                                    getServer().getConsoleSender()
                                            .sendMessage(ChatColor.RED+"Invalid material name in "+newSet.getName()+"! Could not find: "+item);
                                }

                                String[] tempMeta = {null , null};
                                // Add metadata if present
                                if (!pieceData.get("metadata").isJsonNull()){
                                    JsonObject meta = pieceData.get("metadata").getAsJsonObject();

                                    if (!meta.get("displayName").isJsonNull()){
                                        tempMeta[0] = (String) meta.get("displayName").getAsString();
                                    }
                                    if (!meta.get("lore").isJsonNull()) {
                                        tempMeta[1] = (String) meta.get("lore").getAsString();
                                    }
                                }

                                newPiece.setMetadata(tempMeta);
                                pieces[i] = newPiece;
                                empty = false;
                            } catch (IndexOutOfBoundsException | NullPointerException e) {
                                pieces[i] = new ArmorPiece();
                            }
                        }
                        if (empty) {
                            getServer().getConsoleSender().sendMessage(ChatColor.RED + newSet.getName()
                                    + ": Armor set loaded with no chosen armor slots, this will interfere with all other armor sets");
                        }

                    } catch (NullPointerException e) {
                        // Empty set if no items are assigned
                        for (int i = 0; i < 4; i++) {
                            pieces[i] = new ArmorPiece();
                        }
                        getServer().getConsoleSender().sendMessage(ChatColor.RED + newSet.getName()
                                + ": Armor set loaded with no chosen armor slots, this will interfere with all other armor sets");
                    }

                    newSet.setArmorPieces(pieces);
                    // If there are no effects the list will be empty and not castable to a list
                    try {
                        JsonArray permEffects = new JsonArray();
                        if (jp.get("permanentEffects").isJsonArray()){
                            permEffects = jp.get("permanentEffects").getAsJsonArray();
                        }
                        PermanentEffect[] permanentEffects = new PermanentEffect[permEffects.size()];
                        int i = 0;
                        for (JsonElement eff : permEffects) {
                            // If parsing permEffects fails it will be null
                            JsonObject effect = eff.getAsJsonObject();

                            try {
                                PermanentEffect newEffect = new PermanentEffect();
                                newEffect.setEffectType(effect.get("effectType").getAsString());
                                newEffect.setAmplifier(effect.get("amplifier").getAsInt());
                                permanentEffects[i] = newEffect;
                                newEffect.fix();
                                i++;
                            } catch (NullPointerException e) {
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
                        JsonArray itemE = new JsonArray();
                        if (jp.get("itemEffects").isJsonArray()) {
                            itemE = jp.get("itemEffects").getAsJsonArray();
                        }
                        ItemEffect[] itemEffects = new ItemEffect[itemE.size()];
                        int i = 0;
                        for (JsonElement itemEff : itemE) {
                            // If the item cannot be initialized it will simply be null
                            JsonObject itemEffect = itemEff.getAsJsonObject();
                            try {
                                ItemEffect newItemEffect = new ItemEffect();
                                String itemName = itemEffect.get("item").getAsString();
                                if (itemName == "null" || itemName == null) {
                                    newItemEffect.setItem(null);
                                } else {
                                    newItemEffect.setItem(itemName);
                                }


                                if (!itemEffect.get("useMessage").isJsonNull()){
                                    newItemEffect.setMessage(itemEffect.get("useMessage").getAsString());
                                }

                                if (!itemEffect.get("cooldownMessage").isJsonNull()) {
                                    newItemEffect.setCooldownMessage(itemEffect.get("cooldownMessage").getAsString());
                                }


                                // Unused
                                if (!itemEffect.get("metadata").isJsonNull()){
                                    JsonObject meta = itemEffect.get("metadata").getAsJsonObject();
                                    if (!meta.get("displayName").isJsonNull()) {
                                        newItemEffect.setName(meta.get("displayName").getAsString());
                                    }
                                    if (!meta.get("lore").isJsonNull()) {
                                        newItemEffect.setLore(meta.get("lore").getAsString());
                                        System.out.println("OOOFye");
                                    }

                                }

                                JsonArray effects = new JsonArray();
                                if (itemEffect.get("effects").isJsonArray()){
                                    effects = itemEffect.get("effects").getAsJsonArray();
                                }
                                try {
                                    newItemEffect.setCooldown(itemEffect.get("cooldown").getAsInt());

                                } catch (Exception e) {
                                    int l = 15;
                                    newItemEffect.setCooldown(l);
                                }
                                PotionEffect[] effectsList = new PotionEffect[effects.size()];
                                int y = 0;
                                for (JsonElement eff : effects) {
                                    // If there are no effects to add it throws a nullpointer and adds confusion for
                                    // 0 seconds
                                    JsonObject effect = eff.getAsJsonObject();
                                    try {
                                        String type = effect.get("effectType").getAsString();
                                        int amp = effect.get("amplifier").getAsInt();
                                        int duration = effect.get("duration").getAsInt() * 20;
                                        effectsList[y] = new PotionEffect(PotionEffectType.getByName(type), duration,
                                                amp);
                                        if (effectsList[y] == null) {
                                            effectsList[y] = new PotionEffect(PotionEffectType.getByName("CONFUSION"),
                                                    0, 0);
                                            getLogger().info("Error parsing itemeffects");
                                        }
                                        y++;

                                    } catch (NullPointerException e) {
                                        effectsList[y] = new PotionEffect(PotionEffectType.getByName("CONFUSION"), 0,
                                                0);
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
                        e.printStackTrace();
                    }
                    armorSets.add(newSet);

                } catch (Exception e) {
                    getServer().getConsoleSender().sendMessage(ChatColor.RED + child.getName() + ": Incorrect format.");
                    e.printStackTrace();
                }

            }
            getLogger().info("Found sets: " + armorSets.size());
        } else {
            getServer().getConsoleSender()
                    .sendMessage(ChatColor.RED + "No Files found in folder ArmorSetEffects/ArmorSets");
        }
    }
    private void start() {
        try {
            checkFiles();
            loadConfigNew();
        } catch (IOException e) {
            getLogger().info("Error checking files");
            e.printStackTrace();
        }
            

    

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
            e.printStackTrace();
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
        Path f = Paths.get(this.getDataFolder().toURI());
        if (!Files.exists(f)) {
            Files.createDirectories(f);
        }
        Path fc = Paths.get(this.getDataFolder() + "/armorsets");
        if (!Files.exists(fc)) {
            Files.createDirectories(fc);
        }

        saveResource("example_set.json", true);
        saveResource("example_set2.json", true);
        saveResource("example_set3.json", true);
        saveResource("README.txt", true);

    }
	public void checkForBonus(Player ply) {
        final Player player = ply;
        // Waits one tick before checking for bonus
        // Simply allows the server to update on all fronts before checking
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
        
            @Override
            public void run() {
                ArmorSet added = null;
                // Checks all sets and adds the one with the highest priority, higher is better
                for (ArmorSet set : armorSets) {
                    if (playerHas(player, set)) {
                        if (added == null || set.getPriority() >= added.getPriority()) {
                            added = set;
                        }
                    } 
                }
                if (added != null) {
                    addBonus(player, added);
                } else {
                    removeBonus(player);
                }
            }
        }, 1);
	}

    private boolean playerHas(Player player, ArmorSet Aset) {
        int i = 0;
        if (Aset.getPermission() != null && !player.hasPermission("armorseteffects.sets."+Aset.getPermission())) {
            return false;
        }

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

                            if (a.getMetadata()[0] != null){
                                if (pItem.getItemMeta().hasDisplayName()){
                                    if (!pItem.getItemMeta().getDisplayName().equals(a.getMetadata()[0])){
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            }



                         
                            if (a.getMetadata()[1] != null){
                                if (pItem.getItemMeta().hasLore()) {
                                    boolean found = false;
                                    for (String lore : pItem.getItemMeta().getLore()) {
                                        if (lore.equals(a.getMetadata()[1])) {
                                            found = true;
                                        }
                                    }  

                                    if (!found){
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
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
            ArmorSet activeSet = activeBonus.get(player);
            PermanentEffect[] effects = activeSet.getPermanentEffects();
            
            if (activeSet.getLooseMessage() == null) {
                player.sendMessage("You lost an armor set bonus: " + activeSet.getName());
            } else if (!activeSet.getLooseMessage().equals("")) {
                player.sendMessage(activeSet.getLooseMessage());
            }

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
        if (effect.getMessage() == null){
            player.sendMessage("You activated an ability from: "+activeBonus.get(player).getName());
        } else if (!effect.getMessage().equals("")) {
            player.sendMessage(effect.getMessage());
        }
        for (PotionEffect peH : effect.getEffects()) {
            if (peH != null) {
                player.addPotionEffect(peH);
            }
        }
    }

    private void addBonus(Player player, ArmorSet set) {
        PermanentEffect[] effects = set.getPermanentEffects();
        if (activeBonus.containsKey(player)){
            if (activeBonus.get(player) == set){
                // Already active
                return;
            }
            removeBonus(player);
        }

        if (set.getGetMessage() == null){
            player.sendMessage("You got an armor set bonus: "+set.getName());
        } else if (!set.getGetMessage().equals("")){
            player.sendMessage(set.getGetMessage());
        }
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
    public HashMap<Player, ArmorSet> getActiveBonus() {
        return activeBonus;
    }
    


    static String parseColors(String msg){
        String ret = "";
        String[] parts = msg.split("\\{color:.+?\\}");
        Pattern p = Pattern.compile("\\{color:.+?\\}");
        Matcher m = p.matcher(msg);
        int i = 0;
        while (m.find()) {
            String colorStr = m.group();
            colorStr = colorStr.replace("{color:", "");
            colorStr = colorStr.replace("}", "");


            ChatColor c = ChatColor.valueOf(colorStr);
            
        
            if (parts.length > i) {
                ret += parts[i] + c;
            } else {
                // Trailing color
                break;
            }
            i++;
            
        }
        while (parts.length > i){
            ret += parts[i];
            i++;
        }
        if (parts.length == 1){
            return msg;
        }

        return ret;
    }

}
