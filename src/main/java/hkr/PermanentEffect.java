package hkr;

import javax.swing.text.html.HTMLDocument.HTMLReader.HiddenAction;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PermanentEffect {
    PotionEffectType effectType;
    int amplifier;
    PotionEffect potionEffect;
    boolean hasParticle = false;
    boolean isAmbient = false;

    public PermanentEffect(){}
    public PermanentEffect(String effectType, int amplifier, boolean isAmbient, boolean hasParticles){
        this.effectType = PotionEffectType.getByName(effectType.trim());
        if (this.effectType == null) {
            this.effectType = PotionEffectType.getByName("BAD_OMEN");
        }
        this.amplifier = amplifier;
        this.hasParticle = hasParticles;
        this.isAmbient = isAmbient;
        this.potionEffect = new PotionEffect(this.effectType, Integer.MAX_VALUE, amplifier, isAmbient, hasParticles);
    }
    public void fix() {
        if (this.effectType == null) {
            this.effectType = PotionEffectType.getByName("BAD_OMEN");
        }
        this.potionEffect = new PotionEffect(this.effectType, Integer.MAX_VALUE, amplifier, isAmbient, hasParticle);
        
    }
    /**
     * @param amplifier the amplifier to set
     */
    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public boolean hasParticle() {
        return hasParticle;
    }

    /**
     * @param effectType the effectType to set
     */
    public void setEffectType(String effectType) {
        this.effectType = PotionEffectType.getByName(effectType);
        if (this.effectType == null) {
            this.effectType = PotionEffectType.getByName("BAD_OMEN");
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
    public void setHasParticles(boolean hasParticle) {
        this.hasParticle = hasParticle;
    }
    
    public void setAmbient(boolean isAmbient) {
        this.isAmbient = isAmbient;
    }
    
}