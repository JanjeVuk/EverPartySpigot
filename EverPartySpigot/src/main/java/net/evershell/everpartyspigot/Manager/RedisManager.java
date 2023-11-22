package net.evershell.everpartyspigot.Manager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisManager {

    private final JedisPool jedisPool;

    public RedisManager(String host, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // Configuration de la connexion Redis avec mot de passe
        this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
    }

    // Méthode pour enregistrer un groupe dans Redis
    public void saveGroup(List<String> members) {
        String groupName = getGroupNameByPlayer(members.get(0));
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hmset("group:" + groupName, members.stream()
                    .collect(Collectors.toMap(member -> "member:" + member, member -> "true")));
        }
    }

    // Méthode pour charger les membres d'un groupe depuis Redis
    public List<String> loadGroupMembers(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> membersMap = jedis.hgetAll("group:" + groupName);
            return new ArrayList<>(membersMap.keySet());
        }
    }

    // Méthode pour ajouter un membre à un groupe dans Redis
    public void addMemberToGroup(String member) {
        String groupName = getGroupNameByPlayer(member);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("group:" + groupName, "member:" + member, "true");
        }
    }

    // Méthode pour supprimer un membre d'un groupe dans Redis
    public void removeMemberFromGroup(String member) {
        String groupName = getGroupNameByPlayer(member);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel("group:" + groupName, "member:" + member);
        }
    }

    // Méthode pour vérifier si un membre appartient à un groupe dans Redis
    public boolean isMemberOfGroup(String member) {
        String groupName = getGroupNameByPlayer(member);
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists("group:" + groupName, "member:" + member);
        }
    }

    public String getGroupNameByPlayer(String playerName) {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> groupKeys = jedis.keys("group:*");
            for (String groupKey : groupKeys) {
                String groupName = groupKey.replace("group:", "");
                if (jedis.hexists(groupKey, "member:" + playerName)) {
                    return groupName;
                }
            }
        }
        return null; // Retourne null si le joueur n'est membre d'aucun groupe
    }

    // Méthode pour supprimer un groupe entier de Redis
    public void deleteGroup(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("group:" + groupName);
        }
    }

    // Méthode pour vérifier si une invitation est en attente dans Redis
    public boolean hasPendingInvite(String senderName, String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists("pending_invites:" + targetName, "sender:" + senderName);
        }
    }

    public void savePendingInvite(String senderName, String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("pending_invites:" + targetName, "sender:" + senderName, "true");
        }
    }

    public void removePendingInvite(String senderName, String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel("pending_invites:" + targetName, "sender:" + senderName);
        }
    }

    public void removeAllPendingInvites(String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("pending_invites:" + targetName);
        }
    }

    public List<String> getPendingInvites(String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> invitesMap = jedis.hgetAll("pending_invites:" + targetName);
            return new ArrayList<>(invitesMap.keySet());
        }
    }

    public String getInvitationSender(String receiverName) {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> pendingInvites = getPendingInvites(receiverName);
            if (pendingInvites != null && !pendingInvites.isEmpty()) {
                // Récupérer le premier sender de la liste des invitations en attente
                String pendingInvite = pendingInvites.get(0);
                return pendingInvite.split(":")[1]; // Récupérer le nom du sender à partir de la clé de l'invitation
            }
        }
        return null; // Retourner null si aucune invitation en attente pour ce joueur
    }

    // Méthode pour fermer le pool de connexions Jedis
    public void close() {
        jedisPool.close();
    }
}
