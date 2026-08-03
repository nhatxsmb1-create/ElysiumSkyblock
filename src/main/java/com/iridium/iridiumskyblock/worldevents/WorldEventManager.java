package com.iridium.iridiumskyblock.worldevents;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.managers.IslandManager;
import com.iridium.iridiumskyblock.worldevents.event.*;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Getter
public class WorldEventManager {

    private static WorldEventManager instance;

    private final InstabilityManager instabilityManager;
    private final Set<Integer> activeEvents = new HashSet<>();
    private BukkitTask schedulerTask;

    // Config
    private final int checkIntervalSeconds = 300;
    private final int baseChance           = 5;
    private final double chancePerPoint    = 0.5;
    private final int decayPerCheck        = 3;
    private final int instabilityPerMine   = 1;
    private final int instabilityPerKill   = 2;

    public WorldEventManager() {
        instance = this;
        this.instabilityManager = new InstabilityManager();
    }

    public static WorldEventManager getInstance() { return instance; }

    public void start() {
        long ticks = checkIntervalSeconds * 20L;
        schedulerTask = IridiumSkyblock.getInstance().getServer().getScheduler()
                .runTaskTimer(IridiumSkyblock.getInstance(), this::tick, ticks, ticks);
    }

    public void stop() {
        if (schedulerTask != null) schedulerTask.cancel();
        instabilityManager.save();
    }

    private void tick() {
        Random rng = new Random();
        World world = IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.NORMAL);
        if (world == null) return;

        for (Island island : IridiumSkyblock.getInstance().getIslandManager().getTeams()) {
            int id = island.getId();
            if (activeEvents.contains(id)) continue;

            int instability = instabilityManager.get(id);
            instabilityManager.add(id, -decayPerCheck);

            double chance = baseChance + instability * chancePerPoint;
            if (rng.nextDouble() * 100 > chance) continue;

            WorldEventType type = pickType(instability, rng);
            if (type == null) continue;
            triggerEvent(island, type);
        }
    }

    private WorldEventType pickType(int instability, Random rng) {
        List<WorldEventType> eligible = new ArrayList<>();
        int total = 0;
        for (WorldEventType t : WorldEventType.values()) {
            if (instability >= t.getMinInstability()) { eligible.add(t); total += t.getWeight(); }
        }
        if (eligible.isEmpty()) return null;
        int roll = rng.nextInt(total), cursor = 0;
        for (WorldEventType t : eligible) { cursor += t.getWeight(); if (roll < cursor) return t; }
        return eligible.get(eligible.size() - 1);
    }

    public void triggerEvent(Island island, WorldEventType type) {
        World world = IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.NORMAL);
        if (world == null) return;
        Location center = island.getCenter(world);

        IridiumSkyblock.getInstance().getIslandManager().getMembersOnIsland(island)
                .forEach(user -> {
                    Player p = user.getPlayer();
                    if (p == null) return;
                    p.sendTitle("§6" + type.getDisplayName(), "§eA world event on your island!", 10, 60, 20);
                    p.sendMessage("§d§l[World Event] §r§6" + type.getDisplayName() + " §fhas appeared!");
                });

        activeEvents.add(island.getId());
        IslandWorldEvent event = createEvent(type, island, center);
        if (event != null) event.start(() -> activeEvents.remove(island.getId()));
        else activeEvents.remove(island.getId());
    }

    private IslandWorldEvent createEvent(WorldEventType type, Island island, Location center) {
        switch (type) {
            case TORNADO:       return new TornadoEvent(island, center);
            case VOLCANO:       return new VolcanoEvent(island, center);
            case SPACE_RIFT:    return new SpaceRiftEvent(island, center);
            case METEOR_SHOWER: return new MeteorShowerEvent(island, center);
            case ANCIENT_TREE:  return new AncientTreeEvent(island, center);
            case INVASION:      return new InvasionEvent(island, center);
            case CELESTIAL:     return new CelestialEvent(island, center);
            default: return null;
        }
    }
}
