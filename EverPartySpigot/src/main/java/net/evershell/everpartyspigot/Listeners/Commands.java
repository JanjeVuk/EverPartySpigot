package net.evershell.everpartyspigot.Listeners;


import net.evershell.everpartyspigot.EverPartySpigot;
import net.evershell.everpartyspigot.Manager.ConfigurationManager;
import net.evershell.everpartyspigot.Manager.PartyManager;
import net.evershell.everpartyspigot.Player.Commands.CommandParty;

import java.util.Objects;

public class Commands {

    public Commands(EverPartySpigot main, ConfigurationManager configManager) {
        PartyManager partyManager = new PartyManager(configManager.getRedisHost(), configManager.getRedisPort(), configManager.getRedisPassword());

        // Création d'une instance de CommandParty avec le PartyManager et ConfigurationManager
        CommandParty commandParty = new CommandParty(partyManager);

        // Configuration de l'exécuteur de la commande /party avec CommandParty
        Objects.requireNonNull(main.getCommand("party")).setExecutor(commandParty);
    }
}
