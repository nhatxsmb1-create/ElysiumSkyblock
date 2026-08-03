package com.iridium.iridiumskyblock.worldevents;

public enum WorldEventType {
    METEOR_SHOWER("☄ Meteor Shower",    0,  30),
    ANCIENT_TREE ("🌳 Ancient Tree",    0,  25),
    CELESTIAL    ("☄ Celestial Event", 10,  20),
    TORNADO      ("🌪 Tornado",         25,  20),
    INVASION     ("👹 Invasion",         25,  15),
    VOLCANO      ("🌋 Volcano",          50,  12),
    SPACE_RIFT   ("🌀 Space Rift",       60,  10);

    private final String displayName;
    private final int minInstability;
    private final int weight;

    WorldEventType(String displayName, int minInstability, int weight) {
        this.displayName = displayName;
        this.minInstability = minInstability;
        this.weight = weight;
    }

    public String getDisplayName()  { return displayName; }
    public int getMinInstability()  { return minInstability; }
    public int getWeight()          { return weight; }
}
