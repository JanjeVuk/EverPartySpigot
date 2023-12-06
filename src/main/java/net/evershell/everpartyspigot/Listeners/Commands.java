package net.evershell.everpartyspigot.Listeners;

import net.evershell.everpartyspigot.Commands.CommandParty;
import net.evershell.everpartyspigot.EverPartySpigot;
import net.evershell.everpartyspigot.Manager.PartyManager;


import java.util.Objects;

/**
 * Handles registration of the /party command executor.
 * Creates an instance of CommandParty and sets it as the executor for the /party command.
 */
public class Commands {

    /**
     * Constructor for Commands class.
     * Registers the CommandParty executor for the /party command.
     *
     * @param main The main plugin instance (EverPartySpigot).
     */
    public Commands(EverPartySpigot main) {
        PartyManager partyManager = new PartyManager();

        // Create an instance of CommandParty with PartyManager and ConfigurationManager
        CommandParty commandParty = new CommandParty(partyManager);

        // Set the command executor for /party to CommandParty
        Objects.requireNonNull(main.getCommand("party")).setExecutor(commandParty);
    }
}
