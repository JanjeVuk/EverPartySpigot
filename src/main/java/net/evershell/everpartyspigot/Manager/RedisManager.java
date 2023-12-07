package net.evershell.everpartyspigot.Manager;

import net.evershell.everpartyspigot.EverPartySpigot;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;


public class RedisManager {


    /**
     * A JedisPool object for managing connections to Redis.
     */
    private JedisPool jedisPool;
    /**
     * The host variable represents the Redis host.
     */
    private final String host;
    /**
     * Represents the port used for the Redis connection.
     *
     * This variable is a private final integer field.
     */
    private final int port;
    /**
     * The password used for Redis connection.
     * Note: This variable is used to store the password for Redis connection.
     */
    private final String password;

    /**
     * The RedisManager class provides methods to manage the Redis connection and perform operations on Redis.
     */
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

    /**
     * Schedule a periodic task to check the Redis connection.
     * This method creates a new BukkitRunnable that runs periodically to check the Redis connection and perform a simple operation (PING) to verify the connection.
     * If an exception occurs during the operation, it logs the error and recreates the JedisPool to establish a new connection.
     */
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

    /**
     * Recreates the JedisPool by destroying the existing connection pool and creating a new one.
     * This method is synchronized to ensure thread safety.
     */
    private synchronized void recreateJedisPool() {
        // Détruire le pool existant
        jedisPool.destroy();

        // Recréer un nouveau pool de connexion
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        Bukkit.getLogger().info("Reconnexion Redis réussie.");
    }

    /**
     * Saves a group to Redis.
     *
     * @param sender the player name of the group creator
     */
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


    /**
     * Generates a unique group name for saving a group to Redis.
     *
     * @param jedis The Jedis instance used to interact with Redis.
     * @return The generated unique group name.
     */
    private String generateUniqueGroupName(Jedis jedis) {
        long index = 0;
        String groupName;
        do {
            index++;
            groupName = "group_" + index;
        } while (jedis.exists("group:" + groupName));
        return groupName;
    }

    /**
     * Loads the members of a group from Redis.
     *
     * @param playerName the name of the player whose group is being loaded
     * @return a list of group members
     */
    public List<String> loadGroupMembers(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> membersMap = jedis.hgetAll("group:" + groupName);
            return new ArrayList<>(membersMap.keySet());
        }
    }

    /**
     * Adds a member to a group in Redis.
     *
     * @param sender the player name of the group creator
     * @param target the player name of the member to be added
     */
    public void addMemberToGroup(String sender, String target) {
        String groupName = getGroupNameByPlayer(sender);
        try (Jedis jedis = jedisPool.getResource()) {
            // Ajouter le nouveau membre avec la valeur "false"
            jedis.hset("group:" + groupName, target, "false");
        }
    }



    /**
     * Removes a member from a group in Redis.
     *
     * @param member the name of the member to be removed
     */
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

    /**
     * Checks if a player is the leader of a group in Redis.
     *
     * @param playerName the name of the player to check
     * @return true if the player is the leader of a group, false otherwise
     */
    public boolean isPlayerLeader(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.hget("group:" + groupName, playerName);
            return value != null && value.equals("true");
        }
    }

    /**
     * Deletes a group from Redis.
     *
     * @param playerName the name of the player whose group is being deleted
     */
    public void deleteGroup(String playerName) {
        String groupName = getGroupNameByPlayer(playerName);
        try (Jedis jedis = jedisPool.getResource()) {
            // Supprimer tous les membres du groupe
            jedis.del("group:" + groupName);
        }
    }


    /**
     * Retrieves the group name associated with the given player name from Redis.
     *
     * @param playerName the name of the player
     * @return the group name if the player is a member of a group, otherwise null
     */
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


    /**
     * Checks if there is a pending invite from the given sender to the given target.
     *
     * @param senderName the name of the sender
     * @param targetName the name of the target
     * @return true if there is a pending invite, false otherwise
     */
    public boolean hasPendingInvite(String senderName, String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists("pending_invites:" + targetName, "sender:" + senderName);
        }
    }

    /**
     * Saves a pending invite in Redis.
     *
     * @param senderName  the name of the player sending the invite
     * @param targetName  the name of the player receiving the invite
     */
    public void savePendingInvite(String senderName, String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("pending_invites:" + targetName, "sender:" + senderName, "true");
        }
    }

    /**
     * Removes a pending invite from Redis.
     *
     * @param senderName the name of the sender of the invite
     * @param targetName the name of the recipient of the invite
     */
    public void removePendingInvite(String senderName, String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel("pending_invites:" + targetName, "sender:" + senderName);
        }
    }

    /**
     * Removes all pending invites for a target player from Redis.
     *
     * @param targetName the name of the target player
     */
    public void removeAllPendingInvites(String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("pending_invites:" + targetName);
        }
    }

    /**
     * Retrieves the list of pending invites for the given target name.
     *
     * @param targetName the name of the target player
     * @return a list of pending invites
     */
    public List<String> getPendingInvites(String targetName) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> invitesMap = jedis.hgetAll("pending_invites:" + targetName);
            return new ArrayList<>(invitesMap.keySet());
        }
    }

    /**
     * Retrieves the sender of the invitation for the given receiver name.
     *
     * @param receiverName the name of the receiver for whom the invitation sender needs to be retrieved
     * @return the name of the invitation sender, or null if there are no pending invitations for the receiver
     */
    public String getInvitationSender(String receiverName) {
        List<String> pendingInvites = getPendingInvites(receiverName);
        if (pendingInvites != null && !pendingInvites.isEmpty()) {
            // Récupérer le premier sender de la liste des invitations en attente
            String pendingInvite = pendingInvites.get(0);
            return pendingInvite.split(":")[1]; // Récupérer le nom du sender à partir de la clé de l'invitation
        }
        return null; // Retourner null si aucune invitation en attente pour ce joueur
    }

    /**
     * Closes the Redis connection pool.
     */
    // Méthode publique pour fermer le pool de connexion Redis
    public void closeJedisPool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }



}
