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
import java.util.Random;

public class InvasionEvent extends IslandWorldEvent {

    private static final int[][] WAVES = {{4,0},{4,1},{2,2},{1,3}};

    public InvasionEvent(Island island, Location center) { super(island, center); }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§c👹 Your island is under §c§lINVASION§f! Defend it!");
        sound(center, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1f, 0.5f);
        spawnWave(0, onFinish);
    }

    private void spawnWave(int idx, Runnable onFinish) {
        World world = center.getWorld();
        Random rng = new Random();

        if (idx >= WAVES.length) {
            world.dropItemNaturally(center.clone().add(0,1,0), named(Material.SHIELD,     "§c§lRaid Trophy"));
            world.dropItemNaturally(center.clone().add(0,1,0), named(Material.GOLD_NUGGET,"§6§lInvasion Coin", 5));
            broadcast("§a👹 Invasion repelled! §cRaid Trophy §ahas dropped!");
            sound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            onFinish.run(); return;
        }

        broadcast("§c👹 Wave §e" + (idx+1) + "§c/§e" + WAVES.length + " §carrives!");
        sound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 0.8f);

        List<LivingEntity> waveMobs = new ArrayList<>();
        for (int i = 0; i < WAVES[idx][0]; i++) {
            double a = rng.nextDouble() * Math.PI * 2;
            Location loc = center.clone().add(Math.cos(a)*10, 1, Math.sin(a)*10);
            loc.setY(world.getHighestBlockYAt(loc) + 1);
            LivingEntity mob = spawnMob(world, loc, WAVES[idx][1], idx);
            if (mob != null) waveMobs.add(mob);
        }

        new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 20;
                if (waveMobs.stream().noneMatch(Entity::isValid)) {
                    cancel(); broadcast("§a👹 Wave " + (idx+1) + " cleared!");
                    IridiumSkyblock.getInstance().getServer().getScheduler()
                            .runTaskLater(IridiumSkyblock.getInstance(), () -> spawnWave(idx+1, onFinish), 60L);
                    return;
                }
                if (e >= 20*60*3) { cancel(); waveMobs.forEach(m -> { if (m.isValid()) m.remove(); }); broadcast("§c👹 Invasion overwhelmed your island..."); onFinish.run(); }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }

    private LivingEntity spawnMob(World world, Location loc, int type, int wave) {
        switch (type) {
            case 0: { Zombie z = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
                z.setCustomName("§cInvader §7[W"+(wave+1)+"]"); z.setCustomNameVisible(true);
                z.setMaxHealth(30.0+wave*5); z.setHealth(z.getMaxHealth()); return z; }
            case 1: { Skeleton s = (Skeleton) world.spawnEntity(loc, EntityType.SKELETON);
                s.setCustomName("§cArcher §7[W"+(wave+1)+"]"); s.setCustomNameVisible(true);
                s.setMaxHealth(25.0+wave*5); s.setHealth(s.getMaxHealth()); return s; }
            case 2: { Vindicator v = (Vindicator) world.spawnEntity(loc, EntityType.VINDICATOR);
                v.setCustomName("§4§lElite Raider"); v.setCustomNameVisible(true);
                v.setMaxHealth(80.0); v.setHealth(80.0);
                v.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false)); return v; }
            case 3: { Ravager r = (Ravager) world.spawnEntity(loc, EntityType.RAVAGER);
                r.setCustomName("§4§l⚔ Siege Commander"); r.setCustomNameVisible(true);
                r.setMaxHealth(200.0); r.setHealth(200.0); return r; }
            default: return null;
        }
    }

    private ItemStack named(Material mat, String name) { return named(mat, name, 1); }
    private ItemStack named(Material mat, String name, int amount) {
        ItemStack i = new ItemStack(mat, amount); ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); i.setItemMeta(m); } return i;
    }
}
