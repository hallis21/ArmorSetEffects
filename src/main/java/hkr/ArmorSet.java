package hkr;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.potion.PotionEffect;


public class ArmorSet {
    private String name;
    private boolean hidden;
    private ArmorPiece[] armorPieces;
    private List<PermanentEffect> permanentEffects = new ArrayList<>();
    private List<ItemEffect> itemEffects = new ArrayList<>();
    // Only one null effect allowed / will be added
    private ItemEffect nullEffect;
    private int priority;
    private String getMessage = null;
    private String looseMessage = null;
    private String permission;

    public ArmorSet() {
    }

    public ArmorSet(String name, boolean hidden, ArmorPiece[] armorPieces, 
            List<PermanentEffect> permanentEffects, List<ItemEffect> itemEffects, int priority) {
        this.name = name;
        this.hidden = hidden;
        this.armorPieces = armorPieces;
        this.permanentEffects = permanentEffects;
        this.itemEffects = itemEffects;
        this.priority = priority;
    }
    public boolean hasItemEffect(){
        if (itemEffects != null) {
            return true;
        }
        return false;
    }


    public String getLooseMessage() {
        return looseMessage;
    }
    public void setLooseMessage(String looseMessage) {
        this.looseMessage = ArmorSetEffectsMain.parseColors(looseMessage);
    }
    public String getGetMessage() {
        return getMessage;
    }
    public void setGetMessage(String getMessage) {
        this.getMessage = ArmorSetEffectsMain.parseColors(getMessage);
    }
    /**
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }
    /**
     * @param permission the permission to set
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }
    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }
    public ItemEffect getNullItemEffect() {
        return nullEffect;
    }
    public boolean hasNullItem(){
        for (ItemEffect var : itemEffects) {
            if (var.getItem() == null) {
                nullEffect = var;
                return true;
            }
        }
        return false;
    }
    
    public boolean hasPermEffect() {
        if (permanentEffects != null) {
            return true;
        }
        return false;
    }
    public String toString(){
        String str = name+": \n";
        String[] items = new String[4];
        for (int i = 0; i < 4; i++) {
            try {
                items[i] = armorPieces[i].toString();
            } catch (NullPointerException e) {
                items[i] = "Any";
            }
        }
        str += "Helmet: "+items[0];
        str += "\nChestplate: "+ items[1];
        str += "\nLeggings: "+ items[2]; 
        str += "\nBoots: " + items[3];
        str += "\n";
        str += "\nEffects: ";
        if (permanentEffects != null) {
            for (PermanentEffect var : permanentEffects) {
                str += "\n    "+var.getEffectType().getName()+" Level: "+var.getAmplifier();
            }   
        } else {
            str += "none";
        }
        str += "\nItem Abilites: ";
        if (itemEffects != null) {
            for (ItemEffect var : itemEffects) {
                if (var.getItem() != null) {
                    str += "\n    "+ var.getItem().toString()+": ";
                } else {
                    str += "\n    Empty Hand (Must click block to register): ";
                }
                for (PotionEffect eff : var.getEffects()) {
                    try {
                        str += "\n    "+eff.getType().getName()+" Lvl: "+eff.getAmplifier() + " Duration: "+eff.getDuration()/20;
                    } catch (Exception e) {// Ignored
                    }
                }
                str+="\n    Cooldown: "+var.getCooldown()+"s";
            }

        } else {
            str += "none";
        }



        return str;
    }
    /**
     * @param armorPieces the armorPieces to set
     */
    public void setArmorPieces(ArmorPiece[] armorPieces) {
        this.armorPieces = armorPieces;
    }
    /**
     * @param hidden the hidden to set
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    /**
     * @param itemEffects the itemEffects to set
     */
    public void setItemEffects(List<ItemEffect> itemEffects) {
        this.itemEffects = itemEffects;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @param permanentEffects the permanentEffects to set
     */
    public void setPermanentEffects(List<PermanentEffect> permanentEffects) {
        this.permanentEffects = permanentEffects;
    }
    /**
     * @return the armorPieces
     */
    public ArmorPiece[] getArmorPieces() {
        return armorPieces;
    }
    /**
     * @return the itemEffects
     */
    public List<ItemEffect> getItemEffects() {
        return itemEffects;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @return the permanentEffects
     */
    public List<PermanentEffect> getPermanentEffects() {
        return permanentEffects;
    }
    public boolean getHidden(){
        return hidden;
    }
}