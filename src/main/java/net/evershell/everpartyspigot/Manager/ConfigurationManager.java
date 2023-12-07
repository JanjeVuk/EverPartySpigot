package net.evershell.everpartyspigot.Manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The ConfigurationManager class is responsible for managing the configuration of a JavaPlugin.
 */
public class ConfigurationManager {

    /**
     * The configuration manager for the plugin.
     */
    private final JavaPlugin plugin;
    /**
     * Private variable used to store the configuration of the plugin.
     * This variable is of type `FileConfiguration` which is a representation of a YAML configuration file.
     */
    private FileConfiguration config;

    /**
     * Class to manage the configuration of a JavaPlugin.
     */
    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /**
     * Sets up the configuration for the plugin.
     * Saves the default configuration file and reloads the configuration.
     */
    public void setupConfig() {
        plugin.saveDefaultConfig();
        reloadConfig();
    }

    /**
     * Reloads the configuration file for the plugin.
     * This includes reloading and updating all the values in the configuration file.
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * Retrieves the current configuration of the plugin.
     *
     * @return The configuration file for the plugin.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    // Méthodes pour récupérer les valeurs spécifiques de la config.yml

    /**
     * Retrieves the Redis host from the plugin configuration.
     *
     * @return The Redis host.
     */
    public String getRedisHost() {
        return config.getString("redis.host");
    }

    /**
     * Gets the Redis port specified in the configuration.
     *
     * @return The Redis port.
     */
    public int getRedisPort() {
        return config.getInt("redis.port");
    }

    /**
     * Retrieves the Redis password from the configuration file.
     *
     * @return The Redis password.
     */
    public String getRedisPassword() {
        return config.getString("redis.password");
    }

    /**
     * Retrieves the maximum number of members allowed in a party.
     *
     * @return The maximum number of members allowed in a party as an integer value.
     */
    public int getMaxMembers() {
        return config.getInt("party.max_members");
    }

    /**
     * Retrieves the maximum number of parties a player can have.
     *
     * @return The maximum number of parties per player as defined in the configuration file.
     */
    public int getMaxPartiesPerPlayer() {
        return config.getInt("party.max_parties_per_player");
    }

    /**
     * Retrieves the value of the "party.disband_empty_party" configuration property.
     *
     * @return the value of the "party.disband_empty_party" configuration property
     */
    public boolean getDisbandEmptyParty() {
        return config.getBoolean("party.disband_empty_party");
    }

    /**
     * Retrieves the disband delay value from the configuration file.
     *
     * @return The disband delay value.
     */
    public int getDisbandDelay() {
        return config.getInt("party.disband_delay");
    }
}