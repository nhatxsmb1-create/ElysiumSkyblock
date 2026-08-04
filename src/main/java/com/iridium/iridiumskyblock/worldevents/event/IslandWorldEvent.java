package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.worldevents.WorldEventManager;
import com.iridium.iridiumskyblock.worldevents.WorldEventType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class IslandWorldEvent {

    protected final Island island;
    protected final Location center;
    protected final WorldEventType eventType;
    protected final Random rng = new Random();

    protected IslandWorldEvent(Island island, Location center, WorldEventType eventType) {
        this.island    = island;
        this.center    = center.clone();
        this.eventType = eventType;
    }

    public abstract void start(Runnable onFinish);

    // ── Scaling ──────────────────────────────────────────────

    /** HP scaled by island level: level 10 = +150% base HP */
    protected double scaledHP(double baseHP) {
        double mult = 1.0 + island.getLevel() * WorldEventManager.getInstance().getConfig().bossHPPerLevel;
        return baseHP * mult;
    }

    /** Returns true if island has high instability and random roll passes */
    protected boolean hasLootBonus() {
        int instab = WorldEventManager.getInstance().getInstabilityManager().get(island.getId());
        return instab >= WorldEventManager.getInstance().getConfig().bonusLootThreshold
                && rng.nextDouble() < WorldEventManager.getInstance().getConfig().bonusLootChance;
    }

    // ── Countdown ─────────────────────────────────────────────

    /** Shows a countdown title, then calls onComplete. */
    protected void countdown(String subtitle, Runnable onComplete) {
        int secs = WorldEventManager.getInstance().getConfig().countdownSeconds;
        new BukkitRunnable() {
            int remaining = secs;
            @Override public void run() {
                if (remaining <= 0) { cancel(); onComplete.run(); return; }
                String title = remaining <= 3 ? "§c" + remaining : "§e" + remaining;
                broadcastTitle(title, subtitle);
                remaining--;
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 20L);
    }

    // ── Boss Bar ──────────────────────────────────────────────

    /** Creates a boss bar and adds all online island members. */
    protected BossBar createBossBar(String title, BarColor color) {
        BossBar bar = Bukkit.createBossBar(title, color, BarStyle.SEGMENTED_10);
        onlinePlayers().forEach(bar::addPlayer);
        return bar;
    }

    /** Keeps the boss bar HP synced and removes it when boss dies. */
    protected void trackBossBar(BossBar bar, LivingEntity boss) {
        new BukkitRunnable() {
            @Override public void run() {
                if (!boss.isValid()) { bar.removeAll(); cancel(); return; }
                bar.setProgress(Math.max(0, boss.getHealth() / boss.getMaxHealth()));
                // Add new online players who joined mid-event
                onlinePlayers().forEach(p -> { if (!bar.getPlayers().contains(p)) bar.addPlayer(p); });
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 10L);
    }

    // ── Helpers ───────────────────────────────────────────────

    protected List<Player> onlinePlayers() {
        return IridiumSkyblock.getInstance().getIslandManager()
                .getMembersOnIsland(island).stream()
                .map(User::getPlayer).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected void broadcast(String msg) {
        onlinePlayers().forEach(p -> p.sendMessage("§d[World Event] §r" + msg));
    }

    protected void broadcastTitle(String title, String sub) {
        onlinePlayers().forEach(p -> p.sendTitle(title, sub, 5, 30, 10));
    }

    protected void broadcastBar(String msg) {
        onlinePlayers().forEach(p ->
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg)));
    }

    protected void sound(Location loc, Sound sound, float vol, float pitch) {
        try { loc.getWorld().playSound(loc, sound, vol, pitch); } catch (Exception ignored) {}
    }

    protected void fx(Location loc, Particle particle, int count,
                      double ox, double oy, double oz, double extra) {
        try { loc.getWorld().spawnParticle(particle, loc, count, ox, oy, oz, extra); }
        catch (Exception ignored) {}
    }

    protected void fx(Location loc, Particle particle, int count) {
        fx(loc, particle, count, 0.2, 0.2, 0.2, 0.0);
    }

    protected World islandWorld() {
        return IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.NORMAL);
    }

    protected ItemStack named(Material mat, String name) {
        return named(mat, name, 1);
    }

    protected ItemStack named(Material mat, String name, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    protected void logResult(String result) {
        WorldEventManager.getInstance().getLogger().log(island, eventType, result);
    }
}
