package com.iridium.iridiumskyblock.worldevents;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.worldevents.configs.WorldEventsConfig;
import com.iridium.iridiumskyblock.worldevents.event.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class WorldEventManager {

    private static WorldEventManager instance;

    private WorldEventsConfig config;
    private final InstabilityManager instabilityManager;
    private final WorldEventLogger logger;
    private final Set<Integer> activeEvents = new HashSet<>();
    private final Map<Integer, Long> cooldowns = new HashMap<>();
    private BukkitTask schedulerTask;

    private static final Set<WorldEventType> RARE_EVENTS =
            EnumSet.of(WorldEventType.SPACE_RIFT, WorldEventType.VOLCANO);

    public WorldEventManager() {
        instance = this;
        this.config = loadConfig();
        this.instabilityManager = new InstabilityManager();
        this.logger = new WorldEventLogger();
    }

    public static WorldEventManager getInstance() { return instance; }

    // ── Config ────────────────────────────────────────────────

    private WorldEventsConfig loadConfig() {
        File file = new File(IridiumSkyblock.getInstance().getDataFolder(), "worldevents.yml");
        WorldEventsConfig cfg = new WorldEventsConfig();
        if (!file.exists()) {
            saveConfig(cfg, file);
            return cfg;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        cfg.checkIntervalSeconds     = yaml.getInt("check-interval-seconds",     cfg.checkIntervalSeconds);
        cfg.baseEventChance          = yaml.getInt("base-event-chance",          cfg.baseEventChance);
        cfg.chancePerInstability     = yaml.getDouble("chance-per-instability",  cfg.chancePerInstability);
        cfg.instabilityPerMine       = yaml.getInt("instability-per-mine",       cfg.instabilityPerMine);
        cfg.instabilityPerKill       = yaml.getInt("instability-per-kill",       cfg.instabilityPerKill);
        cfg.instabilityDecayPerCheck = yaml.getInt("instability-decay-per-check",cfg.instabilityDecayPerCheck);
        cfg.islandCooldownSeconds    = yaml.getInt("island-cooldown-seconds",    cfg.islandCooldownSeconds);
        cfg.bossHPPerLevel           = yaml.getDouble("boss-hp-per-level",       cfg.bossHPPerLevel);
        cfg.bonusLootThreshold       = yaml.getInt("bonus-loot-threshold",       cfg.bonusLootThreshold);
        cfg.bonusLootChance          = yaml.getDouble("bonus-loot-chance",       cfg.bonusLootChance);
        cfg.countdownSeconds         = yaml.getInt("countdown-seconds",          cfg.countdownSeconds);
        cfg.announceRareEvents       = yaml.getBoolean("announce-rare-events",   cfg.announceRareEvents);
        return cfg;
    }

    private void saveConfig(WorldEventsConfig cfg, File file) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("check-interval-seconds",     cfg.checkIntervalSeconds);
        yaml.set("base-event-chance",          cfg.baseEventChance);
        yaml.set("chance-per-instability",     cfg.chancePerInstability);
        yaml.set("instability-per-mine",       cfg.instabilityPerMine);
        yaml.set("instability-per-kill",       cfg.instabilityPerKill);
        yaml.set("instability-decay-per-check",cfg.instabilityDecayPerCheck);
        yaml.set("island-cooldown-seconds",    cfg.islandCooldownSeconds);
        yaml.set("boss-hp-per-level",          cfg.bossHPPerLevel);
        yaml.set("bonus-loot-threshold",       cfg.bonusLootThreshold);
        yaml.set("bonus-loot-chance",          cfg.bonusLootChance);
        yaml.set("countdown-seconds",          cfg.countdownSeconds);
        yaml.set("announce-rare-events",       cfg.announceRareEvents);
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void reload() { this.config = loadConfig(); }

    // ── Lifecycle ─────────────────────────────────────────────

    public void start() {
        long ticks = config.checkIntervalSeconds * 20L;
        schedulerTask = IridiumSkyblock.getInstance().getServer().getScheduler()
                .runTaskTimer(IridiumSkyblock.getInstance(), this::tick, ticks, ticks);
    }

    public void stop() {
        if (schedulerTask != null) schedulerTask.cancel();
        instabilityManager.save();
    }

    // ── Scheduler tick ────────────────────────────────────────

    private void tick() {
        Random rng = new Random();
        World world = IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.NORMAL);
        if (world == null) return;

        for (Island island : IridiumSkyblock.getInstance().getIslandManager().getTeams()) {
            int id = island.getId();
            if (activeEvents.contains(id)) continue;
            if (isOnCooldown(id)) continue;

            int instability = instabilityManager.get(id);
            instabilityManager.add(id, -config.instabilityDecayPerCheck);

            double chance = config.baseEventChance + instability * config.chancePerInstability;
            if (rng.nextDouble() * 100 > chance) continue;

            WorldEventType type = pickType(instability, rng);
            if (type != null) triggerEvent(island, type);
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

    // ── Trigger ───────────────────────────────────────────────

    public void triggerEvent(Island island, WorldEventType type) {
        World world = IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.NORMAL);
        if (world == null) return;
        Location center = island.getCenter(world);

        // Notify island members
        IridiumSkyblock.getInstance().getIslandManager().getMembersOnIsland(island)
                .forEach(user -> {
                    Player p = user.getPlayer();
                    if (p == null) return;
                    p.sendTitle("§6" + type.getDisplayName(),
                            "§eA world event is approaching!", 10, 60, 20);
                    p.sendMessage("§d§l[World Event] §r§6" + type.getDisplayName() + " §fhas appeared on your island!");
                });

        // Server-wide announce for rare events
        if (config.announceRareEvents && RARE_EVENTS.contains(type)) {
            Bukkit.broadcastMessage("§d§l[ELYSIUM] §r§6" + type.getDisplayName()
                    + " §fhas appeared on island §e" + island.getName() + "§f!");
        }

        activeEvents.add(island.getId());

        IslandWorldEvent event = createEvent(type, island, center);
        if (event != null) {
            event.start(() -> {
                activeEvents.remove(island.getId());
                setCooldown(island.getId());
            });
        } else {
            activeEvents.remove(island.getId());
        }
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

    // ── Cooldown ──────────────────────────────────────────────

    public boolean isOnCooldown(int islandId) {
        Long last = cooldowns.get(islandId);
        return last != null && System.currentTimeMillis() - last < config.islandCooldownSeconds * 1000L;
    }

    public long getCooldownRemaining(int islandId) {
        Long last = cooldowns.get(islandId);
        if (last == null) return 0;
        long remaining = config.islandCooldownSeconds * 1000L - (System.currentTimeMillis() - last);
        return Math.max(0, remaining / 1000);
    }

    private void setCooldown(int islandId) {
        cooldowns.put(islandId, System.currentTimeMillis());
    }

    // Accessors for listener
    public int getInstabilityPerMine() { return config.instabilityPerMine; }
    public int getInstabilityPerKill() { return config.instabilityPerKill; }
}
