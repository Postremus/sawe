package com.pro_crafting.mc.sawe;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SaWe extends JavaPlugin{
    private final WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();
    private final WorldGuard worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
    private RegionListener regioListener;
    private WorldEditPlugin worldEditPlugin;

    public void onEnable() {
        regioListener = new RegionListener(this);
        this.getLogger().info("says Hello");

        worldEditPlugin = JavaPlugin.getPlugin(WorldEditPlugin.class);
    }

    public void onDisable() {
		HandlerList.unregisterAll(regioListener);
        this.getLogger().info("says Bye Bye");
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return this.worldGuardPlugin;
    }

    public WorldGuard getWorldGuard() {
        return this.worldGuard;
    }

    public WorldEditPlugin getWorldEdit() {
        return this.worldEditPlugin;
    }

    public void sendMessage(Player player, String message) {
		player.sendMessage("§a[SaWe]§7"+message);
	}
}
