package com.iridium.iridiumskyblock.commands;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.worldevents.WorldEventManager;
import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.commands.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class EventCommand extends Command<Island, User> {

    public EventCommand() {
        super(Collections.singletonList("event"),
              "View your island's instability and active event info",
              "%prefix% &7/is event",
              "", 0);
    }

    @Override
    public boolean execute(User user, Island island, String[] args, IridiumTeams<Island, User> teams) {
        Player player = user.getPlayer();
        if (player == null) return false;

        WorldEventManager mgr = WorldEventManager.getInstance();
        int instability = mgr.getInstabilityManager().get(island.getId());
        boolean active  = mgr.getActiveEvents().contains(island.getId());
        boolean cooldown = mgr.isOnCooldown(island.getId());
        long cdRemain    = mgr.getCooldownRemaining(island.getId());

        // Progress bar
        int filled  = instability / 5;   // out of 20 segments
        String bar  = "§a" + "█".repeat(Math.min(filled, 20))
                    + "§8" + "█".repeat(Math.max(0, 20 - filled));
        String color = instability < 25 ? "§a" : instability < 50 ? "§e" : instability < 75 ? "§c" : "§4";

        player.sendMessage("§8§m                                        ");
        player.sendMessage("§d§l  ISLAND INSTABILITY");
        player.sendMessage("  " + bar + " " + color + instability + "%");
        player.sendMessage("");
        player.sendMessage("  §7Status: " + (active ? "§c⚡ Event in progress" : cooldown
                ? "§e⏱ Cooldown §7(" + cdRemain + "s)" : "§a✔ Ready"));

        if (instability < 25)
            player.sendMessage("  §7Risk level: §aLow §7— Mild events only");
        else if (instability < 50)
            player.sendMessage("  §7Risk level: §eMedium §7— Tornado, Invasion possible");
        else if (instability < 75)
            player.sendMessage("  §7Risk level: §cHigh §7— Volcano possible");
        else
            player.sendMessage("  §7Risk level: §4CRITICAL §7— Space Rift imminent!");

        player.sendMessage("  §7Instability decays §f" +
                mgr.getConfig().instabilityDecayPerCheck + "% §7every " +
                mgr.getConfig().checkIntervalSeconds / 60 + " min passively.");
        player.sendMessage("§8§m                                        ");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args, IridiumTeams<Island, User> teams) {
        return Collections.emptyList();
    }
}
