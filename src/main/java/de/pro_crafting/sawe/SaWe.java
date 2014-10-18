package de.pro_crafting.sawe;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class SaWe extends JavaPlugin{
	RegionListener regioListener;
	
	@Override
	public void onEnable() {
		regioListener = new RegionListener(this);
		this.getLogger().info("sagt Hallo");		
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(regioListener);
		this.getLogger().info("sagt Bis Bald");
	}
	
	public WorldGuardPlugin getWorldGuard() {
	    return (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
	}
	
	public WorldEditPlugin getWorldEdit() {
	    return (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
	}
	
    public void sendMessage(Player player, String message) {
		player.sendMessage("§a[SaWe]§7"+message);
	}
}
