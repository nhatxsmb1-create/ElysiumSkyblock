package com.iridium.iridiumskyblock.worldevents;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WorldEventLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final File logFile;

    public WorldEventLogger() {
        logFile = new File(IridiumSkyblock.getInstance().getDataFolder(), "worldevents.log");
    }

    public void log(Island island, WorldEventType type, String result) {
        String line = "[" + LocalDateTime.now().format(FMT) + "] "
                + type.getDisplayName()
                + " | Island: " + island.getName()
                + " (ID:" + island.getId() + ")"
                + " | Level:" + island.getLevel()
                + " | Instability:" + WorldEventManager.getInstance().getInstabilityManager().get(island.getId()) + "%"
                + " | Result: " + result;
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
