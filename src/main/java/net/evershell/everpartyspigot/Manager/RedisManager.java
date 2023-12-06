package net.evershell.everpartyspigot.Manager;

import net.evershell.everpartyspigot.EverPartySpigot;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

public class RedisManager {


    private JedisPool jedisPool;
    private final String host;
    private final int port;
    private final String password;

    public RedisManager(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
        initializeJedisPool();
        scheduleRedisConnectionCheck();
    }

    private void initializeJedisPool() {
        // Initialiser la connexion Redis avec le pool de connexions
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = new JedisPool(poolConfig, host, port, 5000, password);
    }

    private void scheduleRedisConnectionCheck() {
        // Planifier une tâche périodique pour vérifier la connexion Redis
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Jedis jedis = jedisPool.getResource()) {
                    // Effectuer une opération simple pour vérifier la connexion, comme PING
                    String pingResult = jedis.ping();
                    Bukkit.getLogger().info("Ping result: " + pingResult);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Erreur de connexion Redis : " + e.getMessage());
                    // En cas d'erreur de connexion, détruire le pool et recréer une connexion
                    recreateJedisPool();
                }
            }
        }.runTaskTimerAsynchronously(EverPartySpigot.getInstance(), 0L, 5000L); // Vérifier la connexion toutes les 5 secondes (100 ticks = 5 secondes)
    }

    private synchronized void recreateJedisPool() {
        // Détruire le pool existant
        jedisPool.destroy();

        // Recréer un nouveau pool de connexion
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        Bukkit.getLogger().info("Reconnexion Redis réussie.");
    }

    public void saveGroup(String sender) {
        String groupName = getGroupNameByPlayer(sender);
        try (Jedis jedis = jedisPool.getResource()) {
            if (groupName == null || jedis.exists("group:" + groupName)) {
                groupName = generateUniqueGroupName(jedis);
            }
            // Ajouter le créateur du groupe avec la valeur "true"
            jedis.hset("group:" + groupName, sender, "true");
        }
    }


    private String generateUniqueGroupName(Jedis jedis) {
        long index = 0;
        String groupName;
        do {
            index++;
            groupName = "group_" + index;
        } while (jedis.exists("group:" + groupName));
        return groupName;
    }

    // Méthode pour charger les membres d'un groupe depuis Redis
    public List<String> loadGroupMembers(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> membersMap = jedis.hgetAll("group:" + groupName);
            return new ArrayList<>(membersMap.keySet());
        }
    }

    public void addMemberToGroup(String sender, String target) {
        String groupName = getGroupNameByPlayer(sender);
        try (Jedis jedis = jedisPool.getResource()) {
            // Ajouter le nouveau membre avec la valeur "false"
            jedis.hset("group:" + groupName, target, "false");
        }
    }



    // Méthode pour supprimer un membre d'un groupe dans Redis
    public void removeMemberFromGroup(String member) {
        String groupName = getGroupNameByPlayer(member);
        try (Jedis jedis = jedisPool.getResource()) {

            // Si le membre est le chef, aléatoirement définir un autre membre en tant que chef s'il en reste au moins un
            if (isPlayerLeader(member)) {
                List<String> members = loadGroupMembers(member);

                jedis.hdel("group:" + groupName, member);
                members.remove(member);
                if (!members.isEmpty()) {
                    Random rand = new Random();
                    String newLeader = members.get(rand.nextInt(members.size()));
                    jedis.hset("group:" + groupName, newLeader, "true");
                }
            }else{
                jedis.hdel("group:" + groupName, member);
            }
        }
    }

    public boolean isPlayerLeader(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.hget("group:" + groupName, playerName);
            return value != null && value.equals("true");
        }
    }

    public void deleteGroup(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            // Supprimer tous les membres du groupe
            jedis.del("group:" + groupName);
        }
    }


    public String getGroupNameByPlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> groupKeys = jedis.keys("*"); // Récupère toutes les clés existantes dans Redis
            for (String groupKey : groupKeys) {
                Map<String, String> members = jedis.hgetAll(groupKey); // Récupère tous les membres du groupe
                if (members.containsKey(playerName)) {
                    String[] parts = groupKey.split(":"); // Séparation de la clé selon ":"
                    if (parts.length > 1) {
                        return parts[1].replaceAll("\\[", "").replaceAll("]", ""); // Retourne le nom du groupe sans crochets
                    }
                }
            }
        } catch (Exception e) {
            // Gérer les exceptions
            Bukkit.getLogger().warning(e.getMessage());
        }
        return null; // Retourne null si le joueur n'est membre d'aucun groupe
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
        List<String> pendingInvites = getPendingInvites(receiverName);
        if (pendingInvites != null && !pendingInvites.isEmpty()) {
            // Récupérer le premier sender de la liste des invitations en attente
            String pendingInvite = pendingInvites.get(0);
            return pendingInvite.split(":")[1]; // Récupérer le nom du sender à partir de la clé de l'invitation
        }
        return null; // Retourner null si aucune invitation en attente pour ce joueur
    }

    // Méthode publique pour fermer le pool de connexion Redis
    public void closeJedisPool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }



}
