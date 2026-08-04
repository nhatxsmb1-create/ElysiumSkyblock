package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TornadoEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 3;
    private static final double BASE_HP = 120.0;

    public TornadoEvent(Island island, Location center) {
        super(island, center, WorldEventType.TORNADO);
    }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§b🌪 A §fTornado §bapproaches! Prepare to fight the §eStorm Spirit§b!");
        countdown("§eTornado forming...", () -> spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world = center.getWorld();
        double hp = scaledHP(BASE_HP);

        Zombie boss = (Zombie) world.spawnEntity(center.clone().add(0, 3, 0), EntityType.ZOMBIE);
        boss.setCustomName("§b⚡ Storm Spirit §7[Lv." + island.getLevel() + "]");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(hp); boss.setHealth(hp);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));

        BossBar bar = createBossBar("§b⚡ Storm Spirit", BarColor.BLUE);
        trackBossBar(bar, boss);

        BukkitRunnable particles = new BukkitRunnable() {
            double angle = 0; int elapsed = 0;
            @Override public void run() {
                elapsed += 2; if (elapsed >= DURATION_TICKS || !boss.isValid()) { cancel(); return; }
                angle += 15;
                for (int l = 0; l < 8; l++) {
                    double a = Math.toRadians(angle + l * 22), r = 1.5 + l * 0.3;
                    Location p = center.clone().add(Math.cos(a)*r, l*0.5, Math.sin(a)*r);
                    fx(p, Particle.CLOUD, 1, 0, 0, 0, 0.01);
                    fx(p, Particle.CRIT, 1);
                }
            }
        };
        particles.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 2L);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (!boss.isValid()) {
                    cancel(); particles.cancel(); bar.removeAll();
                    world.dropItemNaturally(boss.getLocation(), named(Material.NETHER_STAR, "§b§lStorm Core"));
                    if (hasLootBonus()) world.dropItemNaturally(boss.getLocation(), named(Material.LIGHTNING_ROD, "§b§lStorm Shard"));
                    broadcast("§a🌪 Storm Spirit defeated! §eStorm Core §ahas dropped!");
                    sound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.5f);
                    logResult("CLEARED");
                    onFinish.run(); return;
                }
                if (elapsed >= DURATION_TICKS) {
                    cancel(); particles.cancel(); bar.removeAll(); boss.remove();
                    broadcast("§c🌪 The Tornado dissipated... Storm Spirit escaped.");
                    logResult("TIMEOUT");
                    onFinish.run();
                }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }
}
