package io.github.mcredwallhp.combatrebalancer;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/*
 * TODO
 * - Possibly prevent splash potions from influencing other players? So says the rev outline.
 * 	 (Hook into the potion splash event, check if it was thrown by the player it's being applied to, cancel if not.)
 */

public final class CombatRebalancer extends JavaPlugin implements Listener {
	
	
	Double scalingFactor;
	
	
	@Override
    public void onEnable() {
		this.saveDefaultConfig();
		this.getServer().getPluginManager().registerEvents(this, this);
		this.scalingFactor = this.getConfig().getDouble("scalingFactor", 2);
    }
    
    
    
    /*
     * Nerf weapon damage
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
    	
    	//Melee weapons to be nerfed
    	Material[] weaponsToNerf = {
    			Material.WOOD_SWORD,
    			Material.WOOD_AXE,
    			Material.WOOD_PICKAXE,
    			Material.WOOD_SPADE,
    			Material.STONE_SWORD,
    			Material.STONE_AXE,
    			Material.STONE_PICKAXE,
    			Material.STONE_SPADE,
    			Material.GOLD_SWORD,
    			Material.GOLD_AXE,
    			Material.GOLD_PICKAXE,
    			Material.GOLD_SPADE,
    			Material.IRON_SWORD,
    			Material.IRON_AXE,
    			Material.IRON_PICKAXE,
    			Material.IRON_SPADE,
    			Material.DIAMOND_SWORD,
    			Material.DIAMOND_AXE,
    			Material.DIAMOND_PICKAXE,
    			Material.DIAMOND_SPADE
    	};
    	
    	//Nerf melee weapons
    	if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
    		if (ArrayUtils.contains( weaponsToNerf, ((Player) e.getDamager()).getItemInHand().getType() )) {
    			e.setDamage(e.getDamage()/this.scalingFactor);
    		}
    	}
    	
    	//Nerf projectiles
    	if (e.getCause() == EntityDamageByEntityEvent.DamageCause.PROJECTILE) {
    		Arrow a = (Arrow) e.getDamager();
    		if (a.getShooter() instanceof Player) {
    			e.setDamage(e.getDamage()/this.scalingFactor);
    		}
    	}
    	
    }

    
    
}
