package hkr;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import hkr.ArmorEquipEventFiles.ArmorEquipEvent;

class ArmorEquipListener implements Listener {
    ArmorSetBonusMain pl;
    
    public ArmorEquipListener(ArmorSetBonusMain pl){
        this.pl = pl;
    }


    @EventHandler
    public void equipListen(ArmorEquipEvent event) {
        if (event.getPlayer().hasPermission("armorsetbonus.receive")) {
            pl.checkForBonus(event.getPlayer());
        }
    }
    @EventHandler
    public void onLogin(PlayerJoinEvent event){
        if (event.getPlayer().hasPermission("armorsetbonus.receive")) {
            pl.checkForBonus(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (pl.getActiveBonus().containsKey(player)) {
                ArmorSetNew activeSet = pl.getActiveBonus().get(player);
                // If there are no itemeffects it returns without checking further
                if (!activeSet.hasItemEffect()) {
                    return;
                }
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType() != Material.AIR) {
                    for (ItemEffect item : activeSet.getItemEffects()) {
                        if (item.getItem() == heldItem.getType()) {
                            if (!onCooldown(player, item)) {
                                pl.addItemEffect(player, item);
                                addCoolDown(player, item);
                                if (heldItem.getAmount()>1) {
                                    heldItem.setAmount(heldItem.getAmount()-1);
                                    
                                } else {
                                    player.getInventory().setItemInMainHand(null);
                                }
                                return;
                            }
                        }
                    }
                } else if (activeSet.hasNullItem()) {
                    if (lastChecked(player) && !onCooldown(player, activeSet.getNullItemEffect())) {
                        pl.addItemEffect(player, activeSet.getNullItemEffect());
                        addCoolDown(player, activeSet.getNullItemEffect());
                    }
                }
                
            }
        }

    }
    
    // Used for item usage
    private boolean lastChecked(Player player) {
        if (!pl.lastused.containsKey(player)) {
            pl.lastused.put(player, System.currentTimeMillis());
            return true;
        }
        long timeSince = System.currentTimeMillis() - pl.lastused.get(player);
        if (timeSince < 500) {
            return false;
        }
        pl.lastused.put(player, System.currentTimeMillis());
        return true;
    }

    private void addCoolDown(Player player, ItemEffect item) {
        HashMap<ItemEffect, Long> newMap = new HashMap<ItemEffect, Long>();
        newMap.put(item, System.currentTimeMillis());
        pl.cooldowns.put(player, newMap);
    }

    private boolean onCooldown(Player player, ItemEffect item) {
        try {
            if (pl.cooldowns.containsKey(player)) {
                long cooldown = item.getCooldown()*1000;
                long time = System.currentTimeMillis();
                long usedAt = pl.cooldowns.get(player).get(item);
                long timeSince = time-usedAt;
                if (timeSince >= cooldown) {
                    return false;
                } else {
                    double left = cooldown-timeSince;
                    player.sendMessage("This ability is on cooldown. ("+Math.round(left/1000)+" seconds)");
                    return true;
                }
                
                
            } else {
                return false;
            }
            
        } catch (Exception e) {
            return false;
        }
    }

    // Removes bonuses when a player leaves
    // Since effects have infinite duration it must be removed incase the plugin is suddenly broken / removed
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pl.removeBonus(event.getPlayer());
    }


    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        pl.removeBonus(event.getEntity().getPlayer());
    }

    @EventHandler
    public void onSpawn(PlayerRespawnEvent event) {
        pl.checkForBonus(event.getPlayer());
    }
    

}