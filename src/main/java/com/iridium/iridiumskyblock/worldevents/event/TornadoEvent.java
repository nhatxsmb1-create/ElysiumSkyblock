package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TornadoEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 3;

    public TornadoEvent(Island island, Location center) { super(island, center); }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§b🌪 A §fTornado §bhas formed! Defeat the §eStorm Spirit§b!");
        World world = center.getWorld();

        Zombie boss = (Zombie) world.spawnEntity(center.clone().add(0, 3, 0), EntityType.ZOMBIE);
        boss.setCustomName("§b⚡ Storm Spirit");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(120.0); boss.setHealth(120.0);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));

        BukkitRunnable particles = new BukkitRunnable() {
            double angle = 0; int elapsed = 0;
            @Override public void run() {
                elapsed += 2; if (elapsed >= DURATION_TICKS || !boss.isValid()) { cancel(); return; }
                angle += 15;
                for (int l = 0; l < 8; l++) {
                    double a = Math.toRadians(angle + l * 22);
                    double r = 1.5 + l * 0.3;
                    Location p = center.clone().add(Math.cos(a) * r, l * 0.5, Math.sin(a) * r);
                    fx(p, Particle.CLOUD, 1, 0.0, 0.0, 0.0, 0.01);
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
                    cancel(); particles.cancel();
                    world.dropItemNaturally(boss.getLocation(), named(Material.NETHER_STAR, "§b§lStorm Core"));
                    broadcast("§a🌪 Storm Spirit defeated! §eStorm Core §ahas dropped!");
                    sound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.5f);
                    onFinish.run(); return;
                }
                if (elapsed >= DURATION_TICKS) {
                    cancel(); particles.cancel(); boss.remove();
                    broadcast("§c🌪 The Tornado dissipated...");
                    onFinish.run();
                }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); i.setItemMeta(m); } return i;
    }
}
