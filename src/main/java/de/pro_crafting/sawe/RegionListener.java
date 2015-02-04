package de.pro_crafting.sawe;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionListener implements Listener {
	private SaWe plugin;
	
	public RegionListener(SaWe plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	private void playerMoveHandler(PlayerMoveEvent event) {
		if (event.getTo().getBlockX() == event.getFrom().getBlockX() &&
				event.getTo().getBlockY() == event.getFrom().getBlockY() &&
				event.getTo().getBlockZ() == event.getFrom().getBlockZ()) {
			return;
		}
		
		Player p = event.getPlayer();
		changeMask(p, event.getTo());
	}
			
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerJoinHandler(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		changeMask(p, p.getLocation());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerTeleportHandler(PlayerTeleportEvent event) {
		Player p = event.getPlayer();
		changeMask(p, event.getTo());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerRespawnHandler(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		changeMask(p, event.getRespawnLocation());
	}
	
	
	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void playerCommandHandler(PlayerCommandPreprocessEvent event) {
		WorldEditPlugin we = this.plugin.getWorldEdit();
		String command = event.getMessage().split(" ")[0];
		if (!isWorldEditCommand(command)) {
			return;
		}
		Player p = event.getPlayer();
		if (p.hasPermission("sawe.bypass."+p.getWorld().getName().toLowerCase())) {
			return;
		}
		if (we.getSession(p).getMask() == null) {
			this.plugin.sendMessage(p, "Du darfst hier kein Worldedit benutzen.");
			event.setCancelled(true);
		}
	}
	
	private void changeMask(Player player, Location to) {
		WorldEditPlugin we = this.plugin.getWorldEdit();
		String worldName = player.getWorld().getName().toLowerCase();
		if (player.hasPermission("sawe.bypass."+worldName)) {
			we.getSession(player).setMask((Mask)null);
			return;
		}
		ProtectedRegion rg = getRegionAt(to);
		if (rg == null) {
			return;
		}
		we.getSession(player).setMask((Mask)null);
		boolean allowWorldEdit = false;
		if (rg.getMembers().contains(player.getUniqueId()) && player.hasPermission("sawe.use.member."+worldName)) {
			allowWorldEdit = true;
		}
		else if (rg.getOwners().contains(player.getUniqueId()) && player.hasPermission("sawe.use.owner."+worldName)) {
			allowWorldEdit = true;
		}
		if (allowWorldEdit) {
			RegionMask rm = this.getRegionMask(rg);	
			we.getSession(player).setMask(rm);
		}
	}
	
	private boolean isWorldEditCommand(String command) {
		if (command.startsWith("/")) {
			command = command.replaceFirst("/", "");
		}
		command = command.toLowerCase();
		return this.plugin.getWorldEdit().getWorldEdit().getPlatformManager().getCommandManager().getDispatcher().get(command) != null;
	}
	
	private ProtectedRegion getRegionAt(Location loc) {
		RegionContainer rm = this.plugin.getWorldGuard().getRegionContainer();
		ApplicableRegionSet set = rm.get(loc.getWorld()).getApplicableRegions(BukkitUtil.toVector(loc));
		if (set.size() > 0) {
			return set.iterator().next();
		}
		return null;
	}
	
	private RegionMask getRegionMask(ProtectedRegion rg) {
		CuboidRegion cr = new CuboidRegion(rg.getMinimumPoint(), rg.getMaximumPoint());
		return new RegionMask(cr);
	}
}
