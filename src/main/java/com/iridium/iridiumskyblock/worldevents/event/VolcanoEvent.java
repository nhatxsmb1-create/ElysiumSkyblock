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
import org.bukkit.util.Vector;

import java.util.Random;

public class VolcanoEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 4;
    private static final int RADIUS = 60;

    public VolcanoEvent(Island island, Location center) { super(island, center); }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§c🌋 A §cVolcano §ferupts! Survive the fire!");
        World world = center.getWorld();
        Random rng = new Random();

        BukkitRunnable ash = new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 4; if (e >= DURATION_TICKS) { cancel(); return; }
                for (int i = 0; i < 6; i++) {
                    Location p = center.clone().add((rng.nextDouble()-.5)*RADIUS, 18+rng.nextInt(8), (rng.nextDouble()-.5)*RADIUS);
                    fx(p, Particle.LARGE_SMOKE, 1, 0.1, 0.0, 0.1, 0.02);
                    fx(p, Particle.FLAME,       1, 0.1, 0.0, 0.1, 0.04);
                }
            }
        };
        ash.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 4L);

        BukkitRunnable meteors = new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 100; if (e >= DURATION_TICKS) { cancel(); return; }
                Location from = center.clone().add((rng.nextDouble()-.5)*RADIUS, 40, (rng.nextDouble()-.5)*RADIUS);
                Fireball fb = (Fireball) world.spawnEntity(from, EntityType.FIREBALL);
                fb.setDirection(new Vector(0, -1, 0)); fb.setYield(1.5f); fb.setIsIncendiary(false);
                sound(from, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 0.6f);
            }
        };
        meteors.runTaskTimer(IridiumSkyblock.getInstance(), 40L, 100L);

        Blaze boss = (Blaze) world.spawnEntity(center.clone().add(0,2,0), EntityType.BLAZE);
        boss.setCustomName("§c🌋 Fire Golem"); boss.setCustomNameVisible(true);
        boss.setMaxHealth(180.0); boss.setHealth(180.0);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));

        new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 20;
                if (!boss.isValid()) {
                    cancel(); ash.cancel(); meteors.cancel();
                    Location d = boss.getLocation();
                    world.dropItemNaturally(d, named(Material.MAGMA_CREAM, "§c§lMagma Crystal"));
                    world.dropItemNaturally(d, named(Material.BLAZE_ROD,   "§6§lLava Core"));
                    world.dropItemNaturally(d, named(Material.NETHERRACK,  "§4§lVolcanic Ore"));
                    if (rng.nextInt(100) < 30) world.dropItemNaturally(d, named(Material.NETHER_STAR, "§c§lInfernal Gem"));
                    broadcast("§a🌋 Fire Golem defeated! Volcanic loot dropped!");
                    sound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 0.8f);
                    onFinish.run(); return;
                }
                if (e >= DURATION_TICKS) { cancel(); ash.cancel(); meteors.cancel(); boss.remove(); broadcast("§c🌋 The volcano calmed..."); onFinish.run(); }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); i.setItemMeta(m); } return i;
    }
}
