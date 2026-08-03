package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class IslandWorldEvent {

    protected final Island island;
    protected final Location center;

    protected IslandWorldEvent(Island island, Location center) {
        this.island = island;
        this.center = center.clone();
    }

    public abstract void start(Runnable onFinish);

    // ── helpers ───────────────────────────────────────────────

    protected List<Player> onlinePlayers() {
        return IridiumSkyblock.getInstance().getIslandManager()
                .getMembersOnIsland(island).stream()
                .map(User::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected void broadcast(String msg) {
        onlinePlayers().forEach(p -> p.sendMessage("§d[World Event] §r" + msg));
    }

    protected void broadcastTitle(String title, String sub) {
        onlinePlayers().forEach(p -> p.sendTitle(title, sub, 10, 60, 20));
    }

    protected void broadcastBar(String msg) {
        onlinePlayers().forEach(p -> p.sendActionBar(msg));
    }

    /** Spawn particle with safe fallback */
    protected void fx(Location loc, Particle particle, int count,
                      double ox, double oy, double oz, double extra) {
        try { loc.getWorld().spawnParticle(particle, loc, count, ox, oy, oz, extra); }
        catch (Exception ignored) {}
    }

    protected void fx(Location loc, Particle particle, int count) {
        fx(loc, particle, count, 0.2, 0.2, 0.2, 0.0);
    }

    /** Play sound with safe fallback */
    protected void sound(Location loc, Sound sound, float vol, float pitch) {
        try { loc.getWorld().playSound(loc, sound, vol, pitch); }
        catch (Exception ignored) {}
    }

    protected World islandWorld() {
        return IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.NORMAL);
    }
}
