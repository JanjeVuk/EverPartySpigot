package net.evershell.everpartyspigot;

import net.evershell.everpartyspigot.Listeners.Commands;
import net.evershell.everpartyspigot.Manager.ConfigurationManager;
import net.evershell.everpartyspigot.Manager.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class for EverPartySpigot.
 * Handles plugin enable and disable events.
 */
public final class EverPartySpigot extends JavaPlugin {

    private static EverPartySpigot instance;
    private static RedisManager redisManager;

    /**
     * Called when the plugin is enabled.
     * Initializes the configuration, Redis manager, and registers events/commands.
     */
    @Override
    public void onEnable() {
        // Initialization of configuration
        ConfigurationManager configurationManager = new ConfigurationManager(this);
        configurationManager.setupConfig();

        instance = this;

        redisManager = new RedisManager(configurationManager.getRedisHost(), configurationManager.getRedisPort(), configurationManager.getRedisPassword());

        // Register events and commands
        new Commands(this); // Pass the ConfigurationManager instance to Commands
    }

    /**
     * Called when the plugin is disabled.
     * Handles plugin shutdown logic, closing the Redis connection pool.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        redisManager.closeJedisPool();
    }

    /**
     * Gets the instance of the EverPartySpigot plugin.
     *
     * @return The instance of the EverPartySpigot plugin.
     */
    public static EverPartySpigot getInstance() {
        return instance;
    }

    /**
     * Gets the RedisManager instance used by the plugin.
     *
     * @return The RedisManager instance.
     */
    public RedisManager getRedisManager() {
        return redisManager;
    }
}
