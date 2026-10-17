package fr.anas.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Test plugin;
    private final File dataFile;
    private final Gson gson;
    private Map<UUID, PlayerData> cache = new HashMap<>();

    public DataManager(Test plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "players.json");
        load();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public void load() {
        if (!dataFile.exists()) {
            cache = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>() {}.getType();
            Map<UUID, PlayerData> data = gson.fromJson(reader, type);
            if (data != null) {
                cache = data;
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de lire players.json : " + e.getMessage());
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde de players.json : " + e.getMessage());
        }
    }
}
