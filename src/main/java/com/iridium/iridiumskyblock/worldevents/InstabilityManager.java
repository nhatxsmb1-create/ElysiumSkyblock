package com.iridium.iridiumskyblock.worldevents;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InstabilityManager {

    private final File dataFile;
    private final Map<Integer, Integer> instabilityMap = new HashMap<>();

    public InstabilityManager() {
        this.dataFile = new File(IridiumSkyblock.getInstance().getDataFolder(), "instability.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : cfg.getKeys(false)) {
            try { instabilityMap.put(Integer.parseInt(key), cfg.getInt(key)); }
            catch (NumberFormatException ignored) {}
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        instabilityMap.forEach((id, val) -> cfg.set(String.valueOf(id), val));
        try { cfg.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public int get(int islandId) {
        return instabilityMap.getOrDefault(islandId, 0);
    }

    public int add(int islandId, int delta) {
        int updated = Math.max(0, Math.min(100, get(islandId) + delta));
        instabilityMap.put(islandId, updated);
        return updated;
    }

    public int set(int islandId, int value) {
        int clamped = Math.max(0, Math.min(100, value));
        instabilityMap.put(islandId, clamped);
        return clamped;
    }
}
