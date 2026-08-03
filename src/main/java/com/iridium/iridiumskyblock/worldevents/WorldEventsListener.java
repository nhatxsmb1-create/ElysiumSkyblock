package com.iridium.iridiumskyblock.worldevents;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Optional;

public class WorldEventsListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Optional<Island> islandOpt = IridiumSkyblock.getInstance().getIslandManager()
                .getTeamViaLocation(event.getBlock().getLocation());
        islandOpt.ifPresent(island -> {
            Optional<User> userOpt = IridiumSkyblock.getInstance().getUserManager().getUser(player);
            if (!userOpt.isPresent()) return;
            if (!island.equals(userOpt.get().getTeam().orElse(null))) return;
            int val = WorldEventManager.getInstance().getInstabilityManager()
                    .add(island.getId(), WorldEventManager.getInstance().getInstabilityPerMine());
            notify(player, val);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        Optional<Island> islandOpt = IridiumSkyblock.getInstance().getIslandManager()
                .getTeamViaLocation(event.getEntity().getLocation());
        islandOpt.ifPresent(island -> {
            Optional<User> userOpt = IridiumSkyblock.getInstance().getUserManager().getUser(killer);
            if (!userOpt.isPresent()) return;
            if (!island.equals(userOpt.get().getTeam().orElse(null))) return;
            int val = WorldEventManager.getInstance().getInstabilityManager()
                    .add(island.getId(), WorldEventManager.getInstance().getInstabilityPerKill());
            notify(killer, val);
        });
    }

    private void notify(Player player, int instability) {
        if      (instability == 25)  player.sendActionBar("§e⚠ Island Instability: §625% §7— Mild events possible");
        else if (instability == 50)  player.sendActionBar("§c⚠ Island Instability: §c50% §7— Strong events approaching!");
        else if (instability == 75)  player.sendActionBar("§4⚠ Island Instability: §475% §7— Extreme events incoming!");
        else if (instability >= 90)  player.sendActionBar("§4§l⚠ CRITICAL §c— Space Rift / Volcano imminent!");
    }
}
