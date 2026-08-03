package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MeteorShowerEvent extends IslandWorldEvent {

    private static final int COUNT = 5, RADIUS = 55, PICKUP_S = 15;

    public MeteorShowerEvent(Island island, Location center) { super(island, center); }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§e🌠 A §eMeteor Shower §fis incoming!");
        World world = center.getWorld();
        Random rng = new Random();

        new BukkitRunnable() {
            int n = 0;
            @Override public void run() {
                if (n >= COUNT) { cancel(); broadcast("§e🌠 The shower has passed."); onFinish.run(); return; }
                n++;
                Location impact = center.clone().add((rng.nextDouble()-.5)*RADIUS, 0, (rng.nextDouble()-.5)*RADIUS);
                impact.setY(world.getHighestBlockYAt(impact));
                dropMeteor(world, impact, rng);
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 40L, 20*20L);
    }

    private void dropMeteor(World world, Location ground, Random rng) {
        broadcast("§e🌠 A meteor incoming! Get ready!");
        sound(ground.clone().add(0,50,0), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.4f);

        new BukkitRunnable() {
            double y = 50;
            @Override public void run() {
                y -= 3;
                Location cur = ground.clone().add(0, y, 0);
                fx(cur, Particle.FLAME, 3, 0.3, 0.1, 0.3, 0.05);
                fx(cur, Particle.LAVA,  2, 0.2, 0.1, 0.2, 0.01);
                if (y <= 0) { cancel(); impact(world, ground, rng); }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 1L);
    }

    private void impact(World world, Location loc, Random rng) {
        sound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.7f);
        fx(loc, Particle.EXPLOSION_HUGE, 3, 0.5, 0.5, 0.5, 0.0);
        fx(loc, Particle.LAVA, 10, 0.5, 0.5, 0.5, 0.1);
        broadcast("§6☄ Meteor at §e(" + loc.getBlockX() + ", " + loc.getBlockZ() + ")§6! Grab in §c" + PICKUP_S + "s§6!");

        Material[] opts = {Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT};
        Material mat = opts[rng.nextInt(opts.length)];
        ItemStack loot = new ItemStack(mat, 1 + rng.nextInt(3));
        Item lootItem = world.dropItem(loc.clone().add(0,1,0), loot);
        lootItem.setPickupDelay(0);

        if (rng.nextInt(100) < 20) {
            Zombie mini = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
            mini.setCustomName("§6Meteor Golem"); mini.setCustomNameVisible(true);
            mini.setMaxHealth(80.0); mini.setHealth(80.0);
        }
        IridiumSkyblock.getInstance().getServer().getScheduler().runTaskLater(IridiumSkyblock.getInstance(), () -> {
            if (lootItem.isValid()) { lootItem.remove(); broadcast("§c☄ The meteor loot crumbled..."); }
        }, PICKUP_S * 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); i.setItemMeta(m); } return i;
    }
}
