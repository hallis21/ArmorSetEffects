package hkr;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PermanentEffect {
    PotionEffectType effectType;
    int amplifier;
    PotionEffect potionEffect;

    public PermanentEffect(){}
    public PermanentEffect(String effectType, int amplifier){
        this.effectType = PotionEffectType.getByName(effectType.trim());
        if (this.effectType == null) {
            this.effectType = PotionEffectType.getByName("CONFUSION");
        }
        this.amplifier = amplifier;
        this.potionEffect = new PotionEffect(this.effectType, Integer.MAX_VALUE, amplifier);
    }
    public void fix() {
        if (this.effectType == null) {
            this.effectType = PotionEffectType.getByName("CONFUSION");
        }
        this.potionEffect = new PotionEffect(this.effectType, Integer.MAX_VALUE, amplifier);
    }
    /**
     * @param amplifier the amplifier to set
     */
    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }
    /**
     * @param effectType the effectType to set
     */
    public void setEffectType(String effectType) {
        this.effectType = PotionEffectType.getByName(effectType);
        if (this.effectType == null) {
            this.effectType = PotionEffectType.getByName("CONFUSION");
        }
    }
    /**
     * @return the amplifier
     */
    public int getAmplifier() {
        return amplifier;
    }
    /**
     * @return the effectType
     */
    public PotionEffectType getEffectType() {
        return effectType;
    }
    /**
     * @return the potionEffect
     */
    public PotionEffect getPotionEffect() {
        return potionEffect;
    }
    
}