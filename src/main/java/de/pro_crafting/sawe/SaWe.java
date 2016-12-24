package de.pro_crafting.sawe;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SaWe extends JavaPlugin{
    private RegionListener regioListener;
    private WorldGuardPlugin worldGuardPlugin;
    private WorldEditPlugin worldEditPlugin;

    public void onEnable() {
        regioListener = new RegionListener(this);
        this.getLogger().info("says Hello");

        worldGuardPlugin = JavaPlugin.getPlugin(WorldGuardPlugin.class);
        worldEditPlugin = JavaPlugin.getPlugin(WorldEditPlugin.class);
    }

    public void onDisable() {
		HandlerList.unregisterAll(regioListener);
        this.getLogger().info("says Bye Bye");
    }

    public WorldGuardPlugin getWorldGuard() {
        return this.worldGuardPlugin;
    }

    public WorldEditPlugin getWorldEdit() {
        return this.worldEditPlugin;
    }

    public void sendMessage(Player player, String message) {
		player.sendMessage("§a[SaWe]§7"+message);
	}
}
