package hkr;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

public class ItemEffect {
    // Nullable
    Material item;
    List<PotionEffect> effects = new ArrayList<>();
    String metadata[] = {null, null};
    Long cooldown;
    String message = null;
    String cooldownMessage = null;
    boolean consumeItem = true;

    public ItemEffect(){}
    public ItemEffect(String item, List<PotionEffect> effects, String[] metadata, Long cooldown) {
        this.item = Material.getMaterial(item, false);
        this.effects = effects;
        this.metadata = metadata;
        this.cooldown = cooldown;
    }


    public void setName(String name) {
        metadata[0] = name;
    }
    public String getName() {
        return metadata[0];
    }
    
    public void setLore(String lore) {
        metadata[1] = lore;
    }

    public String getLore() {
        return metadata[1];
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }
    public void setCooldownMessage(String cooldownMessage) {
        this.cooldownMessage = ArmorSetEffectsMain.parseColors(cooldownMessage);
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = ArmorSetEffectsMain.parseColors(message);

    }
    /**
     * @return the cooldown
     */
    public Long getCooldown() {
        return cooldown;
    }
    /**
     * @param cooldown the cooldown to set
     */
    public void setCooldown(int cooldown) {
        this.cooldown = (long) cooldown;
    }
    /**
     * @param effects the effects to set
     */
    public void setEffects(List<PotionEffect> effects) {
        this.effects = effects;
    }
    
    public void addEffects(PotionEffect effect) {
        effects.add(effect);
    }
    /**
     * @param item the item to set
     * Nullable
     */
    public void setItem(String item) {
        if (item == null) {
            this.item = null;
            return;
        }
        this.item = Material.getMaterial(item);
    }
   
    /**
     * @return the effects
     */
    public List<PotionEffect> getEffects() {
        return effects;
    }
    /**
     * @return the item
     */
    public Material getItem() {
        return item;
    }
    
    public void setConsumeItem(boolean consumeItem) {
        this.consumeItem = consumeItem;
    }
    
    public boolean getConsumeItem() {
        return consumeItem;
    }

    

}