package io.github.mcredwallhp.combatrebalancer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/*
 * TODO
 * - Possibly prevent splash potions from influencing other players? So says the rev outline.
 * 	 (Hook into the potion splash event, check if it was thrown by the player it's being applied to, cancel if not.)
 */

public final class CombatRebalancer extends JavaPlugin implements Listener {
	
	
	Double scalingFactor;
	List<String> playersInDebugMode;
	
	
	@Override
    public void onEnable() {
		this.saveDefaultConfig();
		this.getServer().getPluginManager().registerEvents(this, this);
		this.scalingFactor = this.getConfig().getDouble("scalingFactor", 2);
		this.playersInDebugMode = new ArrayList<String>();
    }
    
    
    
    /*
     * Nerf weapon damage
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
    	
    	// Melee weapons to be nerfed
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
    	
    	// Nerf melee weapons
    	if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
    		if (ArrayUtils.contains( weaponsToNerf, ((Player) e.getDamager()).getItemInHand().getType() )) {
    			e.setDamage(e.getDamage()/this.scalingFactor);
    			this.sendDebugMessage(e);
    		}
    	}
    	
    	// Nerf projectiles
    	if ( (e.getCause() == EntityDamageByEntityEvent.DamageCause.PROJECTILE) && (e.getEntity() instanceof Player) ) {
    		Arrow a = (Arrow) e.getDamager();
    		if (a.getShooter() instanceof Player) {
    			e.setDamage(e.getDamage()/this.scalingFactor);
    			this.sendDebugMessage(e);
    		}
    	}
    	
    }
    
    
    
    /*
     * Command handler
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	
    	Player player = (Player) sender;
    	
    	if (!player.hasPermission("combatrebalancer.admin")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
			return true;
		}
    	
    	// Prints damage info in chat - /crdebug
    	if (cmd.getName().equalsIgnoreCase("crdebug")) {
    		if (!(sender instanceof Player)) {
    			sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
    			return true;
    		} else {
    			this.toggleDebug(player);
    			return true;
    		}
    	}
    	
    	// Get or set scaling factor - /crscale [int]
    	if (cmd.getName().equalsIgnoreCase("crscale")) {
    		if (args.length < 1) {
    			player.sendMessage(ChatColor.GOLD + "CombatRebalancer scaling factor: " + this.scalingFactor);
    			return true;
    		} else {
    			double newFactor = Double.parseDouble(args[0]);
    			this.scalingFactor = newFactor;
    			this.getConfig().set("scalingFactor", newFactor);
    			this.saveConfig();
    			player.sendMessage(ChatColor.GOLD + "CombatRebalancer scaling factor changed: " + newFactor);
    			return true;
    		}
    	}
    	
    	return false;
    	
    }
    
    
    
    /*
     * Flag a user to receive debug messages
     */
    public void toggleDebug(Player player) {
    	
    	if (this.playersInDebugMode.contains(player.getName())) {
    		this.playersInDebugMode.remove(player.getName());
    		player.sendMessage(ChatColor.GOLD + "CombatRebalancer debug deactivated");
    	} else {
    		this.playersInDebugMode.add(player.getName());
    		player.sendMessage(ChatColor.GOLD + "CombatRebalancer debug activated");
    	}
    	
    }
    
    
    
    /*
     * Create and send debug messages to users flagged for debug
     */
    public void sendDebugMessage(EntityDamageByEntityEvent e) {
    	
    	Player player;
		String damager;
		String tag;
		String dmg;
		String msg;
		
    	for (String playerName : this.playersInDebugMode) {
    		
			player = Bukkit.getPlayer(playerName);
			
			if (e.getCause() == EntityDamageByEntityEvent.DamageCause.PROJECTILE) {
				damager = "Projectile";
			} else {
				damager = ((Player) e.getDamager()).getName();
			}
			
			tag = ChatColor.RED + "[CRCombat]" + ChatColor.WHITE;
			dmg = "O: " + e.getDamage() + " N: " + (e.getDamage()/this.scalingFactor);
			
			msg = damager + " hit for ("+dmg+")";
			player.sendMessage(tag + " " + msg);
			
		}
    	
    }

    
    
}
