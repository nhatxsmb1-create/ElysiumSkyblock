package com.iridium.iridiumskyblock.worldevents.event;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.boss.*; import org.bukkit.entity.*;
import org.bukkit.potion.*; import org.bukkit.scheduler.BukkitRunnable;

public class AncientTreeEvent extends IslandWorldEvent {
    private static final double BASE_HP=100.0;

    public AncientTreeEvent(Island island, Location center) { super(island, center, WorldEventType.ANCIENT_TREE); }

    @Override public void start(Runnable onFinish) {
        broadcast("§2🌳 An §2Ancient Tree §fhas grown! A §aDryad §fguards it!");
        countdown("§2Tree awakening...", ()->spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world=center.getWorld(); double hp=scaledHP(BASE_HP);

        BukkitRunnable aura=new BukkitRunnable(){
            double a=0; int e=0; @Override public void run(){
                e+=3; if(e>20*60*5){cancel();return;}
                a+=8;
                for(int l=0;l<6;l++){double r=Math.max(0.5,3.0-l*0.4),ang=Math.toRadians(a+l*30);
                    Location p=center.clone().add(Math.cos(ang)*r,l*1.5,Math.sin(ang)*r);
                    fx(p,Particle.HAPPY_VILLAGER,1,0,0,0,0); fx(p,Particle.COMPOSTER,1,0.1,0.1,0.1,0);}
            }
        };
        aura.runTaskTimer(IridiumSkyblock.getInstance(),0L,3L);

        Witch dryad=(Witch)world.spawnEntity(center.clone().add(0,1,0),EntityType.WITCH);
        dryad.setCustomName("§a🌿 Ancient Dryad §7[Lv."+island.getLevel()+"]"); dryad.setCustomNameVisible(true);
        dryad.setMaxHealth(hp); dryad.setHealth(hp);
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,0,false,false));
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,0,false,false));
        sound(center,Sound.BLOCK_GRASS_PLACE,1f,0.6f);

        BossBar bar=createBossBar("§a🌿 Ancient Dryad",BarColor.GREEN); trackBossBar(bar,dryad);

        new BukkitRunnable(){
            int e=0; @Override public void run(){
                e+=20;
                if(!dryad.isValid()){
                    cancel(); aura.cancel(); bar.removeAll();
                    Location d=dryad.getLocation();
                    world.dropItemNaturally(d,named(Material.VINE,"§a§lNature Essence"));
                    world.dropItemNaturally(d,named(Material.OAK_SAPLING,"§2§lWoodland Seed"));
                    world.dropItemNaturally(d,named(Material.GREEN_DYE,"§a§lForest Dust"));
                    if(hasLootBonus()){world.dropItemNaturally(d,named(Material.TOTEM_OF_UNDYING,"§2§lDryad's Blessing"));
                        broadcast("§a🌳 §lBonus loot! §r§aDryad's Blessing dropped!");}
                    broadcast("§a🌳 Ancient Dryad defeated! Nature Essence dropped!");
                    sound(center,Sound.ENTITY_ENDER_DRAGON_DEATH,1f,1.6f);
                    logResult("CLEARED"); onFinish.run(); return;
                }
                if(e>=20*60*5){cancel();aura.cancel();bar.removeAll();dryad.remove();
                    broadcast("§c🌳 The Ancient Tree withered..."); logResult("TIMEOUT"); onFinish.run();}
            }
        }.runTaskTimer(IridiumSkyblock.getInstance(),20L,20L);
    }
}
