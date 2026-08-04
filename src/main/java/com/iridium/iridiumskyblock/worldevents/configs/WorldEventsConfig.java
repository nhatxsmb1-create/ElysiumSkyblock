package com.iridium.iridiumskyblock.worldevents.configs;

/**
 * Loaded from plugins/ElysiumSkyblock/worldevents.yml
 * All values are configurable without recompiling.
 */
public class WorldEventsConfig {

    // Scheduler
    public int  checkIntervalSeconds      = 300;   // How often the scheduler checks each island
    public int  baseEventChance           = 5;     // % chance at 0% instability
    public double chancePerInstability    = 0.5;   // Extra % per instability point

    // Instability
    public int  instabilityPerMine        = 1;
    public int  instabilityPerKill        = 2;
    public int  instabilityDecayPerCheck  = 3;

    // Cooldown (per island, prevents back-to-back events)
    public int  islandCooldownSeconds     = 600;   // 10 min default

    // Boss scaling
    public double bossHPPerLevel          = 0.15;  // +15% HP per island level

    // Loot bonus
    public int  bonusLootThreshold        = 70;    // instability % needed for bonus loot
    public double bonusLootChance         = 0.5;   // 50% to get bonus when threshold met

    // Countdown
    public int  countdownSeconds          = 5;

    // Server-wide announce for rare events (Space Rift, Volcano)
    public boolean announceRareEvents     = true;
}
