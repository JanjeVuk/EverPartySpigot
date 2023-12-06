package net.evershell.everpartyspigot.Manager;

import net.evershell.everpartyspigot.EverPartySpigot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PartyManager {

    private final RedisManager redisManager;

    public PartyManager() {
        this.redisManager = EverPartySpigot.getInstance().getRedisManager();
    }

    public void createParty(Player leader) {
        if(isPlayerInParty(leader)){
             return;
        }
        redisManager.saveGroup(leader.getName());
    }

    public boolean isPlayerInParty(Player player) {
        List<String> partyMembers = redisManager.loadGroupMembers(player.getName());
        return partyMembers != null && !partyMembers.isEmpty();
    }


    public void excludePlayer(Player target) {
        redisManager.removeMemberFromGroup(target.getName());
    }

    public void leaveParty(Player player) {
        redisManager.removeMemberFromGroup(player.getName());
    }

    public boolean isPlayerLeader(Player player) {
        return redisManager.isPlayerLeader(player.getName());
    }


    public void disbandParty(Player player) {
        redisManager.deleteGroup(player.getName());
    }

    public List<String> getPartyMembers(Player player) {
        return redisManager.loadGroupMembers(player.getName());
    }

    public boolean hasPendingInvite(Player sender, Player target) {
        return redisManager.hasPendingInvite(sender.getName(), target.getName());
    }

    public void invitePlayer(Player sender, Player target) {
        redisManager.savePendingInvite(sender.getName(), target.getName());
    }

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

    public Boolean refuseInvite(Player sender, Player target) {
        if(redisManager.getInvitationSender(target.getName()) == null){
            Bukkit.getLogger().warning("no invite pending for this target player");
            return false;
        }
        redisManager.removePendingInvite(sender.getName(), target.getName());
        return true;
    }

    public void removePendingInvite(Player sender, Player target) {
        redisManager.removePendingInvite(sender.getName(), target.getName());
    }

    public void removeAllPendingInvites(Player target) {
        redisManager.removeAllPendingInvites(target.getName());
    }

    public List<String> getPendingInvites(Player target) {
        return redisManager.getPendingInvites(target.getName());
    }

    public String getInvitationSender(Player receiver) {
        return redisManager.getInvitationSender(receiver.getName());
    }


}
