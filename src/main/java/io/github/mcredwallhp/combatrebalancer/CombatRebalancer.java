package io.github.mcredwallhp.combatrebalancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/*
 * TODO
 * - Possibly prevent splash potions from influencing other players? So says the rev outline.
 * 	 (Hook into the potion splash event, check if it was thrown by the player it's being applied to, cancel if not.)
 */
public final class CombatRebalancer extends JavaPlugin implements Listener {

    
    
    Double scalingFactor;
    List<String> playersInDebugMode;
    Boolean tweakPlayerSpeed;

    
    
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.scalingFactor = this.getConfig().getDouble("scalingFactor", 2);
        this.playersInDebugMode = new ArrayList<String>();
        this.tweakPlayerSpeed = this.getConfig().getBoolean("tweakPlayerSpeed", false);
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
            if (ArrayUtils.contains(weaponsToNerf, ((Player) e.getDamager()).getItemInHand().getType())) {
                e.setDamage(e.getDamage() / this.scalingFactor);
                this.sendDebugMessage(e);
            }
        }

        // Nerf projectiles
        if ((e.getCause() == EntityDamageByEntityEvent.DamageCause.PROJECTILE) && (e.getEntity() instanceof Player)) {
            Projectile a = (Projectile) e.getDamager();
            if (a.getShooter() instanceof Player) {
                e.setDamage(e.getDamage() / this.scalingFactor);
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
            dmg = "O: " + e.getDamage() + " N: " + (e.getDamage() / this.scalingFactor);

            msg = damager + " hit for (" + dmg + ")";
            player.sendMessage(tag + " " + msg);

        }

    }
    
    
    
    /**
     * Dynamic player speed adjustment
     * Reimplementation of PwnPvpBalance's similar feature
     * https://github.com/Pwn9/PwnPvpBalance/
     */
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        
        if (this.tweakPlayerSpeed != true) return;
        
        World w = event.getPlayer().getWorld();
        Player player = event.getPlayer();
        
        double playerWeight = 0;
        HashMap<Material, Integer> armorWeights = new HashMap<Material, Integer>();
        armorWeights.put(Material.LEATHER_HELMET, 1);
        armorWeights.put(Material.GOLD_HELMET, 2);
        armorWeights.put(Material.CHAINMAIL_HELMET, 2);
        armorWeights.put(Material.IRON_HELMET, 3);
        armorWeights.put(Material.DIAMOND_HELMET, 4);
        armorWeights.put(Material.LEATHER_CHESTPLATE, 1);
        armorWeights.put(Material.GOLD_CHESTPLATE, 2);
        armorWeights.put(Material.CHAINMAIL_CHESTPLATE, 2);
        armorWeights.put(Material.IRON_CHESTPLATE, 3);
        armorWeights.put(Material.DIAMOND_CHESTPLATE, 4);
        armorWeights.put(Material.LEATHER_LEGGINGS, 1);
        armorWeights.put(Material.GOLD_LEGGINGS, 2);
        armorWeights.put(Material.CHAINMAIL_LEGGINGS, 2);
        armorWeights.put(Material.IRON_LEGGINGS, 3);
        armorWeights.put(Material.DIAMOND_LEGGINGS, 4);
        armorWeights.put(Material.LEATHER_BOOTS, 1);
        armorWeights.put(Material.GOLD_BOOTS, 2);
        armorWeights.put(Material.CHAINMAIL_BOOTS, 2);
        armorWeights.put(Material.IRON_BOOTS, 3);
        armorWeights.put(Material.DIAMOND_BOOTS, 4);
        
        ItemStack[] inventorySlots = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };
        
        for (ItemStack slot : inventorySlots) {
            if (slot == null) break;
            if (armorWeights.containsKey(slot.getType())) {
                playerWeight = playerWeight + armorWeights.get(slot.getType());
            }
        }
        
        double currentSpeed = player.getWalkSpeed();
        int currentSpeedRounded = (int) Math.round(currentSpeed*1000);
        double speedMod;
        int speedModRounded;
        if (playerWeight == 0) {
            speedMod = (0.26f);
            speedModRounded = (int) Math.round(speedMod*1000);
        } else {
            speedMod = (0.26 - ((playerWeight / 2) / 100));
            speedModRounded = (int) Math.round(speedMod*1000);
        }
        if (speedModRounded != currentSpeedRounded) {
            player.setWalkSpeed((float) speedMod);
        }
        
    }

    
    
}
