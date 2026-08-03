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

import java.util.ArrayList;
import java.util.List;

public class SpaceRiftEvent extends IslandWorldEvent {

    private static final int WARN_TICKS = 20 * 30;
    private static final int WAVES = 3, MOBS_PER_WAVE = 3, WAVE_INTERVAL = 45;

    public SpaceRiftEvent(Island island, Location center) { super(island, center); }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§5🌀 A §5Space Rift §fappeared above your island!");
        World world = center.getWorld();
        Location rift = center.clone().add(0, 25, 0);

        BukkitRunnable swirl = new BukkitRunnable() {
            double a = 0; int e = 0;
            @Override public void run() {
                e += 2; if (e > WARN_TICKS) { cancel(); return; }
                a += 18;
                for (int i = 0; i < 3; i++) {
                    double ang = Math.toRadians(a + i * 120);
                    Location p = rift.clone().add(Math.cos(ang)*2, 0, Math.sin(ang)*2);
                    fx(p, Particle.PORTAL, 5, 0.1, 0.3, 0.1, 0.3);
                    fx(p, Particle.DRAGON_BREATH, 2, 0.1, 0.1, 0.1, 0.02);
                }
            }
        };
        swirl.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 2L);
        sound(rift, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.4f);

        IridiumSkyblock.getInstance().getServer().getScheduler().runTaskLater(IridiumSkyblock.getInstance(), () -> {
            swirl.cancel();
            broadcast("§5🌀 §cThe Rift tears open! Void Entities pour through!");
            sound(rift, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1f, 0.6f);
            runWaves(world, rift, onFinish);
        }, WARN_TICKS);
    }

    private void runWaves(World world, Location rift, Runnable onFinish) {
        List<LivingEntity> mobs = new ArrayList<>();
        int[] wavesDone = {0};

        Runnable spawnWave = () -> {
            wavesDone[0]++;
            broadcast("§5🌀 Wave §e" + wavesDone[0] + "§5/" + WAVES + " §farrived!");
            sound(rift, Sound.ENTITY_ENDERMAN_SCREAM, 1f, 0.7f);
            for (int i = 0; i < MOBS_PER_WAVE; i++) {
                double a = Math.random() * Math.PI * 2;
                Location sp = rift.clone().add(Math.cos(a)*3, -5, Math.sin(a)*3);
                Enderman e = (Enderman) world.spawnEntity(sp, EntityType.ENDERMAN);
                e.setCustomName("§5Void Entity §7[W" + wavesDone[0] + "]");
                e.setCustomNameVisible(true);
                e.setMaxHealth(60.0 + wavesDone[0] * 20); e.setHealth(e.getMaxHealth());
                e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 0, false, false));
                mobs.add(e);
            }
        };

        for (int w = 0; w < WAVES; w++) {
            IridiumSkyblock.getInstance().getServer().getScheduler()
                    .runTaskLater(IridiumSkyblock.getInstance(), spawnWave::run, (long) w * WAVE_INTERVAL * 20L);
        }

        long timeout = (long)(WAVES+1) * WAVE_INTERVAL * 20L + 20*60L;
        new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 20;
                if (mobs.stream().noneMatch(Entity::isValid) && wavesDone[0] >= WAVES) {
                    cancel();
                    Location drop = rift.clone().add(0, -5, 0);
                    world.dropItemNaturally(drop, named(Material.CHORUS_FRUIT, "§5§lVoid Relic"));
                    world.dropItemNaturally(drop, named(Material.END_CRYSTAL,  "§d§lRift Fragment"));
                    broadcast("§a🌀 Space Rift closed! §5Void Relic §ahas dropped!");
                    sound(rift, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.2f);
                    onFinish.run(); return;
                }
                if (e >= timeout) {
                    cancel(); mobs.forEach(m -> { if (m.isValid()) m.remove(); });
                    broadcast("§c🌀 The Rift sealed itself..."); onFinish.run();
                }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); i.setItemMeta(m); } return i;
    }
}
