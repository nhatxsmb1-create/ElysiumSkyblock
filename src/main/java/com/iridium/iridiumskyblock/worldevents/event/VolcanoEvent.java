package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.boss.*; import org.bukkit.entity.*;
import org.bukkit.potion.*; import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.Random;

public class VolcanoEvent extends IslandWorldEvent {
    private static final int DURATION = 20*60*4, RADIUS = 60;
    private static final double BASE_HP = 180.0;

    public VolcanoEvent(Island island, Location center) { super(island, center, WorldEventType.VOLCANO); }

    @Override public void start(Runnable onFinish) {
        broadcast("§c🌋 A §cVolcano §ferupts nearby! Survive the inferno!");
        countdown("§cVolcano erupting...", () -> spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world = center.getWorld(); Random rng = new Random();
        double hp = scaledHP(BASE_HP);

        BukkitRunnable ash = new BukkitRunnable() {
            int e=0; @Override public void run() {
                e+=4; if(e>=DURATION){cancel();return;}
                for(int i=0;i<6;i++){
                    Location p=center.clone().add((rng.nextDouble()-.5)*RADIUS,18+rng.nextInt(8),(rng.nextDouble()-.5)*RADIUS);
                    fx(p,Particle.LARGE_SMOKE,1,0.1,0,0.1,0.02); fx(p,Particle.FLAME,1,0.1,0,0.1,0.04);
                }
            }
        };
        ash.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 4L);

        BukkitRunnable meteors = new BukkitRunnable() {
            int e=0; @Override public void run() {
                e+=100; if(e>=DURATION){cancel();return;}
                Location from=center.clone().add((rng.nextDouble()-.5)*RADIUS,40,(rng.nextDouble()-.5)*RADIUS);
                Fireball fb=(Fireball)world.spawnEntity(from,EntityType.FIREBALL);
                fb.setDirection(new Vector(0,-1,0)); fb.setYield(1.5f); fb.setIsIncendiary(false);
                sound(from,Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,0.5f,0.6f);
            }
        };
        meteors.runTaskTimer(IridiumSkyblock.getInstance(), 40L, 100L);

        Blaze boss=(Blaze)world.spawnEntity(center.clone().add(0,2,0),EntityType.BLAZE);
        boss.setCustomName("§c🌋 Fire Golem §7[Lv."+island.getLevel()+"]"); boss.setCustomNameVisible(true);
        boss.setMaxHealth(hp); boss.setHealth(hp);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,Integer.MAX_VALUE,1,false,false));

        BossBar bar=createBossBar("§c🌋 Fire Golem",BarColor.RED); trackBossBar(bar,boss);

        new BukkitRunnable() {
            int e=0; @Override public void run() {
                e+=20;
                if(!boss.isValid()){
                    cancel(); ash.cancel(); meteors.cancel(); bar.removeAll();
                    Location d=boss.getLocation();
                    world.dropItemNaturally(d,named(Material.MAGMA_CREAM,"§c§lMagma Crystal"));
                    world.dropItemNaturally(d,named(Material.BLAZE_ROD,"§6§lLava Core"));
                    world.dropItemNaturally(d,named(Material.NETHERRACK,"§4§lVolcanic Ore"));
                    if(hasLootBonus()){
                        world.dropItemNaturally(d,named(Material.NETHER_STAR,"§c§lInfernal Gem"));
                        broadcast("§6🌋 §lBonus loot! §r§6Infernal Gem dropped!");
                    }
                    broadcast("§a🌋 Fire Golem defeated! Volcanic loot dropped!");
                    sound(center,Sound.ENTITY_ENDER_DRAGON_DEATH,1f,0.8f);
                    logResult("CLEARED"); onFinish.run(); return;
                }
                if(e>=DURATION){cancel();ash.cancel();meteors.cancel();bar.removeAll();boss.remove();
                    broadcast("§c🌋 The volcano calmed..."); logResult("TIMEOUT"); onFinish.run();}
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }
}
