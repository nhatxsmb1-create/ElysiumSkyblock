package com.iridium.iridiumskyblock.worldevents;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
            User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
            Optional<Island> userIsland = IridiumSkyblock.getInstance().getIslandManager()
                    .getTeamViaID(user.getTeamID());
            if (!userIsland.isPresent() || !userIsland.get().equals(island)) return;
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
            User user = IridiumSkyblock.getInstance().getUserManager().getUser(killer);
            Optional<Island> userIsland = IridiumSkyblock.getInstance().getIslandManager()
                    .getTeamViaID(user.getTeamID());
            if (!userIsland.isPresent() || !userIsland.get().equals(island)) return;
            int val = WorldEventManager.getInstance().getInstabilityManager()
                    .add(island.getId(), WorldEventManager.getInstance().getInstabilityPerKill());
            notify(killer, val);
        });
    }

    private void notify(Player player, int instability) {
        String msg = null;
        if      (instability == 25) msg = "§e⚠ Island Instability: §625% §7— Mild events possible";
        else if (instability == 50) msg = "§c⚠ Island Instability: §c50% §7— Strong events approaching!";
        else if (instability == 75) msg = "§4⚠ Island Instability: §475% §7— Extreme events incoming!";
        else if (instability >= 90) msg = "§4§l⚠ CRITICAL §c— Space Rift / Volcano imminent!";
        if (msg != null)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
}
