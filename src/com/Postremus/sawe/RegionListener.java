package com.Postremus.sawe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionListener implements Listener {
	private SaWe plugin;
	
	public RegionListener(SaWe plugin)
	{
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	private void playerMoveHandler(PlayerMoveEvent event)
	{
		if (event.getTo().getBlockX() == event.getFrom().getBlockX() &&
				event.getTo().getBlockY() == event.getFrom().getBlockY() &&
				event.getTo().getBlockZ() == event.getFrom().getBlockZ())
		{
			return;
		}
		
		Player p = event.getPlayer();
		changeMask(p, event.getTo());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerJoinHandler(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		changeMask(p, p.getLocation());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerTeleportHandler(PlayerTeleportEvent event)
	{
		Player p = event.getPlayer();
		changeMask(p, event.getTo());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerRespawnHandler(PlayerRespawnEvent event)
	{
		Player p = event.getPlayer();
		changeMask(p, event.getRespawnLocation());
	}
	
	
	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void playerCommandHandler(PlayerCommandPreprocessEvent event)
	{
		WorldEditPlugin we = this.plugin.getWorldEdit();
		String command = event.getMessage().split(" ")[0];
		if (!isWorldEditCommand(command))
		{
			return;
		}
		Player p = event.getPlayer();
		if (p.hasPermission("sawe.bypass"))
		{
			return;
		}
		if (we.getSession(p).getMask() == null)
		{
			this.plugin.sendMessage(p, "Du darfst hier kein Worldedit benutzen.");
			event.setCancelled(true);
		}
	}
	
	private void changeMask(Player player, Location to)
	{
		WorldEditPlugin we = this.plugin.getWorldEdit();
		if (player.hasPermission("sawe.bypass"))
		{
			we.getSession(player).setMask(null);
			return;
		}
		ProtectedRegion rg = getRegionAt(to);
		if (rg == null)
		{
			return;
		}
		we.getSession(player).setMask(null);
		boolean allowWorldEdit = false;
		player.sendMessage("id: "+rg.getId());
		if (rg.getMembers().contains(player.getName()) && player.hasPermission("sawe.use.member"))
		{
			allowWorldEdit = true;
		}
		else if (rg.getOwners().contains(player.getName()) && player.hasPermission("sawe.use.owner"))
		{
			allowWorldEdit = true;
		}
		
		if (allowWorldEdit)
		{
			RegionMask rm = this.getRegionMask(rg);	
			we.getSession(player).setMask(rm);
		}
	}
	
	private boolean isWorldEditCommand(String command)
	{
		if (command.startsWith("/"))
		{
			command = command.replaceFirst("/", "");
		}
		command = command.toLowerCase();
		Map<String, String> weCommands = this.plugin.getWorldEdit().getWorldEdit().getCommandsManager().getCommands();
		return weCommands.containsKey(command);
	}
	
	private ProtectedRegion getRegionAt(Location loc)
	{
		RegionManager rm = this.plugin.getWorldGuard().getRegionManager(loc.getWorld());
		ApplicableRegionSet set = rm.getApplicableRegions(loc);
		if (set.size() > 0)
		{
			return set.iterator().next();
		}
		return null;
	}
	
	private RegionMask getRegionMask(ProtectedRegion rg)
	{
		CuboidRegion cr = new CuboidRegion(rg.getMinimumPoint(), rg.getMaximumPoint());
		return new RegionMask(cr);
	}
}
