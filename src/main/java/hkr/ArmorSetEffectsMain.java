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
import java.util.List;
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
                            .sendMessage(ChatColor.RED + "Error checking files. If this persists try manually creating these folders: plugins/ArmorSetEffects/armorsets");
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
        toprint = toprint.subSequence(0, toprint.length()-3).toString();

        getLogger().info("Found folder, reading sets.");
        String temps = "(If no files are listed here the plugin has not found any)";
        if(directoryListing != null && directoryListing.size() != 0)  {temps = "";}
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
                    
                    // Tests to see that all parts are present

                    // Uses default values for non critical things
                    if (jp.get("name") == null) {
                        getLogger().warning("Could not find field \"name\" (required) in file "+child.getName());
                        continue;
                    } else {
                        try {
                            newSet.setName(jp.get("name").getAsString());
                        } catch (Exception e) {
                            getLogger().warning("Field \"name\" is in wrong format, expected (string) in file " + child.getName());
                            continue;
                        }
                    }
                    if (jp.get("hidden") == null) {
                        getLogger().warning("Could not find field \"hidden\" in file " + child.getName()+", using default (false)");
                        newSet.setHidden(false);
                    } else {
                        try {
                            newSet.setHidden(jp.get("hidden").getAsBoolean());
                        } catch (Exception e) {
                            newSet.setHidden(false);
                            getLogger().warning(
                                    "Field \"hidden\" is in wrong format, expected (boolean) in file " + child.getName()
                                            + ", using default (false)");
                        }
                    }
                    if (jp.get("priority") == null) {
                        newSet.setPriority(0);
                        getLogger().warning("Could not find field \"priority\" in file " + child.getName()+", using default (0)");
                    } else {
                        try {
                            newSet.setPriority(jp.get("priority").getAsInt());
                        } catch (Exception e) {
                            newSet.setPriority(0);
                            getLogger().warning(
                                    "Field \"priority\" is in wrong format, expected (int) in file " + child.getName()+", using default (0)");
                        }
                    }
                    if (jp.get("equipMessage") == null) {
                        getLogger().warning("Could not find field \"equipMessage\" in file " + child.getName()+", using default");
                    } else {
                        try {
                            if (!jp.get("equipMessage").isJsonNull()) {
                                newSet.setGetMessage(jp.get("equipMessage").getAsString());
                            }
                        } catch (Exception e) {
                            getLogger().warning(
                                    "Field \"equipMessage\" is in wrong format, expected (string) in file " + child.getName()
                                            + ", using default");
                        }
                    }
                    if (jp.get("unequipMessage") == null) {
                        getLogger().warning(
                                "Could not find field \"unequipMessage\" in file " + child.getName() + ", using default");
                    } else {
                        try {
                            if (!jp.get("unequipMessage").isJsonNull()) {
                                newSet.setLooseMessage(jp.get("unequipMessage").getAsString());
                            }
                        } catch (Exception e) {
                            getLogger().warning("Field \"unequipMessage\" is in wrong format, expected (string) in file "
                                    + child.getName() + ", using default");
                        }
                    }
                    if (jp.get("permission") == null) {
                        getLogger().warning("Could not find field \"permission\" in file " + child.getName()+ ", using default");
                    } else {
                        try {
                            if (!jp.get("permission").isJsonNull()) {
                                newSet.setPermission(jp.get("permission").getAsString());
                            }
                        } catch (Exception e) {
                            getLogger().warning(
                                    "Field \"permission\" is in wrong format, expected (string) in file " + child.getName()+ ", using default");
                        }
                    }
                    if (jp.get("armorPieces") == null) {
                        getLogger().warning("Could not find field \"armorPieces\" (required) in file " + child.getName());
                        continue;
                    } else {
                        try {
                            jp.get("armorPieces").getAsJsonArray();
                        } catch (Exception e) {
                            getLogger().warning(
                                    "Field \"armorPieces\" (required) is in wrong format, expected (JSON array) in file " + child.getName());
                            continue;
                        }
                    }


                    ArmorPiece[] pieces = new ArmorPiece[4];
                    JsonArray armorPieces = jp.get("armorPieces").getAsJsonArray();
                    if (armorPieces.size() != 4) {
                        getLogger().warning("Invalid amount of item pieces in file "+child.getName());
                        continue;
                    }

                    for (int i = 0; i < 4; i++) {
                        JsonObject pieceData = null;
                        ArmorPiece newPiece = new ArmorPiece();
                        // Check that it exists as json object
                        try {
                            pieceData = armorPieces.get(i).getAsJsonObject();
                        } catch (Exception e) {
                            getLogger().warning(
                                    "Field item peice " + i
                                            + "  (required) is in wrong format, in file "
                                            + child.getName());
                            break;
                        }
                        


                        if (pieceData.get("item") == null) {
                            getLogger().warning("Could not find field \"item\"(required) in item piece "+i+" in file"
                                    + child.getName());
                            break;
                        } else {
                            try {
                                String item = pieceData.get("item").getAsString().trim();
                                newPiece.setItem(item);
                                if (newPiece.getItem() == null && !(item.equals("AIR"))) {
                                    getLogger().warning(ChatColor.RED + "Invalid material name in "
                                                    + child.getName() + "! Could not find: " + item);
                                    break;
                                }
                            } catch (Exception e) {
                                getLogger().warning("Field \"item\"(required) in  item peice " + i
                                        + " is in wrong format, in file " + child.getName());
                                break;
                            }
                        }

                        // Metadata
                        String[] tempMeta = { null, null };
                        if (pieceData.get("metadata") == null) {
                            getLogger().warning("Could not find field \"metadata\"in item piece " + i
                                    + " in file" + child.getName()+", using default");
                            
                        } else {
                            try {
                                JsonObject meta = pieceData.get("metadata").getAsJsonObject();
                                
                                if (meta.get("displayName") == null){
                                    getLogger().warning("Could not find field \"metadata: displayName\"in item piece " + i
                                            + " in file" + child.getName() + ", using default");
                                } else {
                                    try {
                                        tempMeta[0] = (meta.get("displayName").isJsonNull()) ? null : meta
                                                .get("displayName").getAsString();
                                    } catch (Exception e) {
                                        getLogger().warning("Field \"metadata: displayName\" in  item peice " + i
                                                + " is in wrong format, in file " + child.getName()
                                                + ", using default");
                                    }
                                }
                                if (meta.get("lore") == null) {
                                    getLogger().warning(
                                            "Could not find field \"metadata: lore\"in item piece " + i
                                                    + " in file" + child.getName() + ", using default");
                                } else {
                                    try {
                                        tempMeta[1] = (meta.get("lore").isJsonNull()) ? null
                                                : meta.get("lore").getAsString();
                                    } catch (Exception e) {
                                        getLogger().warning("Field \"metadata: lore\" in  item peice " + i
                                                + " is in wrong format, in file " + child.getName()
                                                + ", using default");
                                    }
                                }
                                
                            } catch (Exception e) {
                                getLogger().warning("Field \"metadata\" in  item peice " + i
                                        + " is in wrong format, in file " + child.getName()
                                        + ", using default");

                            }

                        }
                        newPiece.setMetadata(tempMeta);
                        pieces[i] = newPiece;
                    }
                    // If null pieces are present an error happend
                    boolean breakit = false;
                    for (ArmorPiece set : pieces){
                        if (set == null) breakit = true;
                    }
                    if (breakit) break;
                    newSet.setArmorPieces(pieces);
                    JsonArray permEffectsJson = new JsonArray();
                    if (jp.get("permanentEffects") == null) {
                        getLogger().warning(
                                "Could not find field \"permanentEffects\" in file " + child.getName() + ", using default (none)");
                    } else {
                        try {
                            if (!jp.get("permanentEffects").isJsonNull()) {
                                permEffectsJson = jp.get("permanentEffects").getAsJsonArray();
                            }
                        } catch (Exception e) {
                            getLogger().warning("Field \"permanentEffects\" is in wrong format, expected (JSON array) in file "
                                    + child.getName() + ", using default (none)");
                        }
                    }

                    ArrayList<PermanentEffect> permEffects = new ArrayList<>();

                    // Default state of json array is 0 so the loop will only loop if items present
                    int f = 0;
                    for (JsonElement eff : permEffectsJson) {
                        if (!eff.isJsonObject()) {
                            getLogger().warning(
                                    "Field \"permanentEffects: ("+f+")\" is in wrong format, expected (JSON object) in file "
                                            + child.getName() + ", skipping");
                            continue;
                        }
                        f++;
                        JsonObject effectObject = eff.getAsJsonObject();
                        PermanentEffect newEffect = new PermanentEffect();

                        if (effectObject.get("effectType") == null) {
                            getLogger().warning("Could not find field \"effectType\" (required) in file " + child.getName()
                                    + ", skipping");
                            continue;
                        } else {
                            try {
                                newEffect.setEffectType(effectObject.get("effectType").getAsString());
                            } catch (Exception e) {
                                getLogger().warning("Field \"effectType\" (required) is in wrong format, expected (string) in file "
                                        + child.getName() + ", skipping");
                                continue;
                            }
                        }

                        if (effectObject.get("amplifier") == null) {
                            getLogger().warning("Could not find field \"amplifier\" (required) in file "
                                    + child.getName() + ", skipping");
                            continue;
                        } else {
                            try {
                                newEffect.setAmplifier(effectObject.get("amplifier").getAsInt());
                            } catch (Exception e) {
                                getLogger().warning(
                                        "Field \"amplifier\" (required) is in wrong format, expected (int) in file "
                                                + child.getName() + ", skipping");
                                continue;
                            }
                        }
                        if (effectObject.get("hasParticles") == null) {
                            getLogger().warning("Could not find field \"hasParticles\" in file "
                                    + child.getName() + ", using default");
                            newEffect.setHasParticles(true);

                        } else {
                            try {
                                newEffect.setHasParticles(effectObject.get("hasParticles").getAsBoolean());
                            } catch (Exception e) {
                                newEffect.setHasParticles(true);
                                getLogger().warning(
                                        "Field \"hasParticles\" is in wrong format, expected (boolean) in file "
                                                + child.getName() + ", using default");
                                
                            }
                        }
                        if (effectObject.get("isAmbient") == null) {
                            getLogger().warning("Could not find field \"isAmbient\" in file " + child.getName()
                                    + ", using default");
                            newEffect.setAmbient(false);

                        } else {
                            try {
                                newEffect.setAmbient(effectObject.get("isAmbient").getAsBoolean());
                            } catch (Exception e) {
                                newEffect.setAmbient(false);
                                getLogger().warning(
                                        "Field \"isAmbient\" is in wrong format, expected (boolean) in file "
                                                + child.getName() + ", using default");

                            }
                        }
                        newEffect.fix();
                        permEffects.add(newEffect);



                    }
                    newSet.setPermanentEffects(permEffects);

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
                                        boolean hasParticle = effect.get("hasParticles").getAsBoolean();
                                        effectsList[y] = new PotionEffect(PotionEffectType.getByName(type), duration,
                                                amp, true, hasParticle);
                                        if (effectsList[y] == null) {
                                            effectsList[y] = new PotionEffect(PotionEffectType.getByName("CONFUSION"),
                                                    0, 0);
                                            getLogger().info("Error parsing itemeffects");
                                        }
                                        y++;

                                    } catch (NullPointerException e) {
                                        effectsList[y] = new PotionEffect(PotionEffectType.getByName("CONFUSION"), 0,
                                                0);
                                        getLogger().info("Error parsing itemeffects");
                                    }
                                }
                                newItemEffect.setEffects(effectsList);
                                itemEffects[i] = newItemEffect;
                                i++;

                            } catch (NullPointerException e) {
                                itemEffects[i] = null;
                                getLogger().info("Error parsing itemeffects");
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
            List<PermanentEffect> effects = activeSet.getPermanentEffects();
            
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
        List<PermanentEffect> effects = set.getPermanentEffects();
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
