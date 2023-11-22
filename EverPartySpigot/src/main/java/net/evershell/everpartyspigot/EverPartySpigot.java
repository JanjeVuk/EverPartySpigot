package net.evershell.everpartyspigot;

import net.evershell.everpartyspigot.Listeners.Commands;
import net.evershell.everpartyspigot.Manager.ConfigurationManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EverPartySpigot extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialisation de la configuration
        ConfigurationManager configurationManager = new ConfigurationManager(this);
        configurationManager.setupConfig();

        // Enregistrement des événements et commandes
        new Events(this);
        new Commands(this, configurationManager); // Passer l'instance de ConfigurationManager à Commands
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
