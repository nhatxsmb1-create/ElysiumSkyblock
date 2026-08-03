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

public class AncientTreeEvent extends IslandWorldEvent {

    public AncientTreeEvent(Island island, Location center) { super(island, center); }

    @Override
    public void start(Runnable onFinish) {
        broadcast("§2🌳 An §2Ancient Tree §fhas grown! A §aDryad §fguards it!");
        World world = center.getWorld();

        BukkitRunnable aura = new BukkitRunnable() {
            double a = 0; int e = 0;
            @Override public void run() {
                e += 3; if (e > 20*60*5) { cancel(); return; }
                a += 8;
                for (int l = 0; l < 6; l++) {
                    double r = Math.max(0.5, 3.0 - l*0.4);
                    double ang = Math.toRadians(a + l*30);
                    Location p = center.clone().add(Math.cos(ang)*r, l*1.5, Math.sin(ang)*r);
                    fx(p, Particle.HAPPY_VILLAGER, 1, 0.0, 0.0, 0.0, 0.0);
                    fx(p, Particle.ANGRY_VILLAGER, 1);
                }
            }
        };
        aura.runTaskTimer(IridiumSkyblock.getInstance(), 0L, 3L);

        Witch dryad = (Witch) world.spawnEntity(center.clone().add(0,1,0), EntityType.WITCH);
        dryad.setCustomName("§a🌿 Ancient Dryad"); dryad.setCustomNameVisible(true);
        dryad.setMaxHealth(100.0); dryad.setHealth(100.0);
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        sound(center, Sound.BLOCK_GRASS_PLACE, 1f, 0.6f);

        new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 20;
                if (!dryad.isValid()) {
                    cancel(); aura.cancel();
                    Location d = dryad.getLocation();
                    world.dropItemNaturally(d, named(Material.VINE,        "§a§lNature Essence"));
                    world.dropItemNaturally(d, named(Material.OAK_SAPLING, "§2§lWoodland Seed"));
                    world.dropItemNaturally(d, named(Material.GREEN_DYE,   "§a§lForest Dust"));
                    broadcast("§a🌳 Ancient Dryad defeated! Nature Essence dropped!");
                    sound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.6f);
                    onFinish.run(); return;
                }
                if (e >= 20*60*5) { cancel(); aura.cancel(); dryad.remove(); broadcast("§c🌳 The Ancient Tree withered..."); onFinish.run(); }
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(), 20L, 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); i.setItemMeta(m); } return i;
    }
}
