package hkr;


import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

public class ItemEffect {
    // Nullable
    Material item;
    PotionEffect[] effects;
    String metadata;
    Long cooldown;
    String message = null;
    String cooldownMessage = null;

    public ItemEffect(){}
    public ItemEffect(String item, PotionEffect[] effects, String metadata, Long cooldown) {
        this.item = Material.getMaterial(item, false);
        this.effects = effects;
        this.metadata = metadata;
        this.cooldown = cooldown;
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
    public void setEffects(PotionEffect[] effects) {
        this.effects = effects;
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
     * @param metadata the metadata to set
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    /**
     * @return the effects
     */
    public PotionEffect[] getEffects() {
        return effects;
    }
    /**
     * @return the item
     */
    public Material getItem() {
        return item;
    }
    /**
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

}