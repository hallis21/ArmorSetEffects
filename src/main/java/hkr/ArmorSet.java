package hkr;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

class ArmorSet {
    public String name;
    public Material[] armor;
    PotionEffect[] effects;
    PotionEffect[] itemEffects;
    // 1 - Helm, 2 - Chest, 3 - pants, 4 - boots
    public ArmorSet(String name, Material[] armor, ArrayList<PotionEffect> effects){
        this.name = name;
        this.armor = armor;
        this.effects = effects.toArray(new PotionEffect[effects.size()]);

    }
    public ArmorSet(){
        // Dummy set
    }

    @Override
    public String toString() {
        return (name+": "+armor[0]+", "+armor[1] + ", "+armor[2] + ", "+armor[3]+" | " +effects);
    }

    @Override
    public boolean equals(Object o) {
        // Implemented for both armorset and itemstack for easier checking later
        // No need to convert armorslots into an armorSet
        if ((o instanceof ArmorSet)) {
            ArmorSet obj = (ArmorSet) o;
            for (int i = 0; i < 4; i++) {
                // Checks reversed and normal for matches
                if (armor[i] == obj.armor[i]) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;

        }
        return false;
    }
}