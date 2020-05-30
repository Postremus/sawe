package com.pro_crafting.mc.sawe;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
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

public class RegionListener implements Listener {
    private SaWe plugin;

    public RegionListener(SaWe plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void playerMoveHandler(PlayerMoveEvent event) {
        if (event.getTo().getBlockX() == event.getFrom().getBlockX() &&
                event.getTo().getBlockY() == event.getFrom().getBlockY() &&
                event.getTo().getBlockZ() == event.getFrom().getBlockZ()) {
            return;
        }

        Player p = event.getPlayer();
        changeMask(p, event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinHandler(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        changeMask(p, p.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerTeleportHandler(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        changeMask(p, event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerRespawnHandler(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        changeMask(p, event.getRespawnLocation());
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerCommandHandler(PlayerCommandPreprocessEvent event) {
        WorldEditPlugin we = this.plugin.getWorldEdit();
        String command = event.getMessage().split(" ")[0];
        if (!isWorldEditCommand(command)) {
            return;
        }
        Player p = event.getPlayer();
        if (p.hasPermission("sawe.bypass." + p.getWorld().getName().toLowerCase())) {
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
        LocalSession session = we.getSession(player);
        if (player.hasPermission("sawe.bypass." + worldName)) {
            session.setMask(null);
            return;
        }
        ProtectedRegion rg = getRegionAt(to);
        session.setMask(null);

        if (rg == null) {
            return;
        }
        boolean allowWorldEdit = false;
        LocalPlayer wePlayer = this.plugin.getWorldGuardPlugin().wrapPlayer(player);
        if (rg.getMembers().contains(wePlayer) && player.hasPermission("sawe.use.member." + worldName)) {
            allowWorldEdit = true;
        } else if (rg.getOwners().contains(wePlayer) && player.hasPermission("sawe.use.owner." + worldName)) {
            allowWorldEdit = true;
        }
        if (allowWorldEdit) {
            RegionMask rm = this.getRegionMask(rg);
            session.setMask(rm);
        }
    }

    private boolean isWorldEditCommand(String command) {
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }
        command = command.toLowerCase();
        return this.plugin.getWorldEdit().getWorldEdit().getPlatformManager().getPlatformCommandManager().getCommandManager().getCommand(command).isPresent();
    }

    private ProtectedRegion getRegionAt(Location loc) {
        RegionContainer container = this.plugin.getWorldGuard().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(loc.getWorld()));

        ApplicableRegionSet set = regionManager.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

        for (ProtectedRegion protectedRegion : set) {
            if (protectedRegion.getOwners().size() > 0 || protectedRegion.getMembers().size() > 0) {
                return protectedRegion;
            }
        }
        return null;
    }

    private RegionMask getRegionMask(ProtectedRegion rg) {
        CuboidRegion cr = new CuboidRegion(rg.getMinimumPoint(), rg.getMaximumPoint());
        return new RegionMask(cr);
    }
}
