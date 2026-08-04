package com.iridium.iridiumskyblock.commands;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.worldevents.WorldEventManager;
import com.iridium.iridiumskyblock.worldevents.WorldEventType;
import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.commands.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class AdminEventCommand extends Command<Island, User> {

    public AdminEventCommand() {
        super(Collections.singletonList("event"),
              "Manage world events on islands",
              "%prefix% &7/is admin event <trigger|setinstability|reload> [player] [value]",
              "iridiumskyblock.admin", 0);
    }

    @Override
    public boolean execute(User user, Island island, String[] args, IridiumTeams<Island, User> teams) {
        org.bukkit.command.CommandSender sender = user.getPlayer() != null
                ? user.getPlayer() : IridiumSkyblock.getInstance().getServer().getConsoleSender();

        if (args.length < 2) { sendUsage(sender); return false; }

        String sub = args[1].toLowerCase();

        switch (sub) {
            case "trigger": {
                // /is admin event trigger <player> <type>
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /is admin event trigger <player> <"
                            + typeList() + ">");
                    return false;
                }
                Optional<Island> target = getIslandByPlayerName(args[2]);
                if (!target.isPresent()) { sender.sendMessage("§cIsland not found for: " + args[2]); return false; }
                WorldEventType type;
                try { type = WorldEventType.valueOf(args[3].toUpperCase()); }
                catch (IllegalArgumentException e) { sender.sendMessage("§cUnknown type. Options: " + typeList()); return false; }
                WorldEventManager.getInstance().triggerEvent(target.get(), type);
                sender.sendMessage("§aTriggered §e" + type.getDisplayName() + " §aon §e" + args[2] + "§a's island.");
                return true;
            }
            case "setinstability": {
                // /is admin event setinstability <player> <0-100>
                if (args.length < 4) { sender.sendMessage("§cUsage: /is admin event setinstability <player> <0-100>"); return false; }
                Optional<Island> target = getIslandByPlayerName(args[2]);
                if (!target.isPresent()) { sender.sendMessage("§cIsland not found for: " + args[2]); return false; }
                int value;
                try { value = Integer.parseInt(args[3]); }
                catch (NumberFormatException e) { sender.sendMessage("§cValue must be 0–100."); return false; }
                int set = WorldEventManager.getInstance().getInstabilityManager().set(target.get().getId(), value);
                sender.sendMessage("§aSet instability of §e" + args[2] + "§a's island to §e" + set + "%§a.");
                return true;
            }
            case "reload": {
                WorldEventManager.getInstance().reload();
                sender.sendMessage("§aWorld Events config reloaded.");
                return true;
            }
            default:
                sendUsage(sender);
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args, IridiumTeams<Island, User> teams) {
        if (args.length == 2) return Arrays.asList("trigger", "setinstability", "reload");
        if (args.length == 3 && args[1].equalsIgnoreCase("trigger") || args[1].equalsIgnoreCase("setinstability")) {
            return IridiumSkyblock.getInstance().getIslandManager().getTeams().stream()
                    .map(i -> i.getName()).filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("trigger")) {
            return Arrays.stream(WorldEventType.values()).map(Enum::name)
                    .filter(n -> n.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Optional<Island> getIslandByPlayerName(String name) {
        return IridiumSkyblock.getInstance().getIslandManager().getTeams().stream()
                .filter(i -> i.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> {
                    User u = IridiumSkyblock.getInstance().getUserManager().getUser(name);
                    if (u == null) return Optional.empty();
                    return IridiumSkyblock.getInstance().getIslandManager().getTeamViaID(u.getTeamID());
                });
    }

    private void sendUsage(org.bukkit.command.CommandSender s) {
        s.sendMessage("§cUsage: /is admin event <trigger|setinstability|reload> [args]");
    }

    private String typeList() {
        return Arrays.stream(WorldEventType.values()).map(Enum::name).collect(Collectors.joining("|"));
    }
}
