package net.evershell.everpartyspigot.Manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void setupConfig() {
        plugin.saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Méthodes pour récupérer les valeurs spécifiques de la config.yml

    public String getRedisHost() {
        return config.getString("redis.host");
    }

    public int getRedisPort() {
        return config.getInt("redis.port");
    }

    public String getRedisPassword() {
        return config.getString("redis.password");
    }

    public int getMaxMembers() {
        return config.getInt("party.max_members");
    }

    public int getMaxPartiesPerPlayer() {
        return config.getInt("party.max_parties_per_player");
    }

    public boolean getDisbandEmptyParty() {
        return config.getBoolean("party.disband_empty_party");
    }

    public int getDisbandDelay() {
        return config.getInt("party.disband_delay");
    }
}