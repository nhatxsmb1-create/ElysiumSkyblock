package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CelestialEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 4;

    public CelestialEvent(Island island, Location center) { super(island, center); }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§d☄ The §dCelestial Event §fhas begun! The sky shimmers!");
        World world = center.getWorld();
        Random rng = new Random();

        List<Entity> crystals = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            double a = i * (2 * Math.PI / 5);
            Location loc = center.clone().add(Math.cos(a)*15, 10+rng.nextInt(6), Math.sin(a)*15);
            EnderCrystal c = (EnderCrystal) world.spawnEntity(loc, EntityType.END_CRYSTAL);
            c.setShowingBottom(false);
            crystals.add(c);
        }

        BukkitRunnable stars = new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 4; if (e > DURATION_TICKS) { cancel(); return; }
                for (int i = 0; i < 8; i++) {
                    Location p = center.clone().add((rng.nextDouble()-.5)*60, 5+rng.nextDouble()*18, (rng.nextDouble()-.5)*60);
                    fx(p, Particle.END_ROD, 1, 0.05, 0.05, 0.05, 0.02);
                    fx(p, Particle.DRAGON_BREATH, 1, 0.0, 0.0, 0.0, 0.01);
                }
            }
        };
        stars.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 4L);

        Phantom beast = (Phantom) world.spawnEntity(center.clone().add(0,20,0), EntityType.PHANTOM);
        beast.setCustomName("§d✦ Star Beast"); beast.setCustomNameVisible(true);
        beast.setMaxHealth(150.0); beast.setHealth(150.0);
        sound(center, Sound.ENTITY_PHANTOM_AMBIENT, 1f, 0.5f);

        new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 20;
                if (!beast.isValid()) {
                    cancel(); stars.cancel(); crystals.forEach(c -> { if (c.isValid()) c.remove(); });
                    Location d = beast.getLocation();
                    world.dropItemNaturally(d, named(Material.GHAST_TEAR,     "§d§lStar Fragment"));
                    world.dropItemNaturally(d, named(Material.GLOWSTONE_DUST, "§e§lCelestial Dust"));
                    if (rng.nextInt(100) < 20) world.dropItemNaturally(d, named(Material.NETHER_STAR, "§b§lStellar Core"));
                    broadcast("§a☄ Star Beast defeated! Celestial rewards dropped!");
                    sound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.4f);
                    onFinish.run(); return;
                }
                if (e >= DURATION_TICKS) { cancel(); stars.cancel(); beast.remove(); crystals.forEach(c -> { if (c.isValid()) c.remove(); }); broadcast("§c☄ The Celestial Event faded..."); onFinish.run(); }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); i.setItemMeta(m); } return i;
    }
}
