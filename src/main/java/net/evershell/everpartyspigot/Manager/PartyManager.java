package net.evershell.everpartyspigot.Manager;

import net.evershell.everpartyspigot.EverPartySpigot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The PartyManager class is responsible for managing parties and invitations in a game.
 */
public class PartyManager {

    /**
     * Represents a RedisManager object in the PartyManager class.
     *
     * This class is responsible for managing the connection to Redis and performing operations on it.
     * The RedisManager instance is used by the PartyManager class to interact with the Redis database.
     *
     * The RedisManager class provides methods for storing and retrieving data from Redis,
     * as well as managing the connection to the Redis server.
     *
     * This class is used internally by the PartyManager class and should not be accessed directly
     * from outside the class.
     *
     * @see PartyManager
     */
    private final RedisManager redisManager;

    /**
     * The PartyManager class is responsible for managing parties and party-related actions in the EverPartySpigot plugin.
     */
    public PartyManager() {
        this.redisManager = EverPartySpigot.getInstance().getRedisManager();
    }

    /**
     * Creates a party with the specified player as the leader.
     *
     * @param leader the player to be the leader of the party
     */
    public void createParty(Player leader) {
        if(isPlayerInParty(leader)){
             return;
        }
        redisManager.saveGroup(leader.getName());
    }

    /**
     * Checks if a player is in a party.
     *
     * @param player the player to check
     * @return true if the player is in a party, false otherwise
     */
    public boolean isPlayerInParty(Player player) {
        List<String> partyMembers = redisManager.loadGroupMembers(player.getName());
        return partyMembers != null && !partyMembers.isEmpty();
    }


    /**
     * Excludes a player from the party.
     *
     * @param target The player to be excluded from the party.
     */
    public void excludePlayer(Player target) {
        redisManager.removeMemberFromGroup(target.getName());
    }

    /**
     * Removes the player from the party.
     *
     * @param player the player to be removed from the party
     */
    public void leaveParty(Player player) {
        redisManager.removeMemberFromGroup(player.getName());
    }

    /**
     * Checks if a player is the leader of a group.
     *
     * @param player the player to check
     * @return true if the player is the leader of a group, false otherwise
     */
    public boolean isPlayerLeader(Player player) {
        return redisManager.isPlayerLeader(player.getName());
    }


    /**
     * Disbands the party of the specified player.
     *
     * @param player the player whose party is being disbanded
     */
    public void disbandParty(Player player) {
        redisManager.deleteGroup(player.getName());
    }

    /**
     * Returns a list of party members for the specified player.
     *
     * @param player the player whose party members are being retrieved
     * @return a list of party members
     */
    public List<String> getPartyMembers(Player player) {
        return redisManager.loadGroupMembers(player.getName());
    }

    /**
     * Checks if there is a pending invite from the given sender to the given target.
     *
     * @param sender the Player object representing the sender of the invite
     * @param target the Player object representing the target of the invite
     * @return true if there is a pending invite, false otherwise
     */
    public boolean hasPendingInvite(Player sender, Player target) {
        return redisManager.hasPendingInvite(sender.getName(), target.getName());
    }

    /**
     * Invites a player to a group.
     *
     * @param sender the player sending the invite
     * @param target the player receiving the invite
     */
    public void invitePlayer(Player sender, Player target) {
        redisManager.savePendingInvite(sender.getName(), target.getName());
    }

    /**
     * Accepts an invitation from a sender for the specified target player.
     *
     * @param sender the player sending the invitation
     * @param target the player receiving the invitation
     * @return true if the invitation was accepted successfully, false otherwise
     */
    public Boolean acceptInvite(Player sender, Player target) {
        String senderName = redisManager.getInvitationSender(target.getName());
        if (senderName != null && senderName.equals(sender.getName())) {
            redisManager.addMemberToGroup(sender.getName(), target.getName());
            redisManager.removePendingInvite(senderName, target.getName());
            return true;
        }
        Bukkit.getLogger().warning("no invite pending for this target player");
        return false;
    }

    /**
     * Refuses an invitation from a sender for a target player.
     *
     * @param sender the player who sent the invitation
     * @param target the player who received the invitation
     * @return true if the invitation was successfully refused, false otherwise
     */
    public Boolean refuseInvite(Player sender, Player target) {
        if(redisManager.getInvitationSender(target.getName()) == null){
            Bukkit.getLogger().warning("no invite pending for this target player");
            return false;
        }
        redisManager.removePendingInvite(sender.getName(), target.getName());
        return true;
    }

    /**
     * Removes a pending invite from Redis.
     *
     * @param sender the sender of the invite
     * @param target the recipient of the invite
     */
    public void removePendingInvite(Player sender, Player target) {
        redisManager.removePendingInvite(sender.getName(), target.getName());
    }

    /**
     * Removes all pending invites for a target player.
     *
     * @param target the target player
     */
    public void removeAllPendingInvites(Player target) {
        redisManager.removeAllPendingInvites(target.getName());
    }

    /**
     * Retrieves the list of pending invites for the given player.
     *
     * @param target the player for whom to retrieve the pending invites
     * @return a list of pending invites
     */
    public List<String> getPendingInvites(Player target) {
        return redisManager.getPendingInvites(target.getName());
    }

    /**
     * Retrieves the sender of the invitation for the given receiver.
     *
     * @param receiver the receiver of the invitation
     * @return the name of the invitation sender, or null if there are no pending invitations for the receiver
     */
    public String getInvitationSender(Player receiver) {
        return redisManager.getInvitationSender(receiver.getName());
    }


}
