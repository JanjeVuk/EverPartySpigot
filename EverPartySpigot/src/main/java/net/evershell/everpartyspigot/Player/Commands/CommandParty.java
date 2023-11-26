package net.evershell.everpartyspigot.Player.Commands;

import net.evershell.everpartyspigot.Manager.PartyManager;
import net.evershell.everpartyspigot.Manager.RedisManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CommandParty implements TabExecutor {

    private final PartyManager partyManager;


    public CommandParty(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande peut être utilisée uniquement par les joueurs.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Traitement si la commande ne comporte aucun argument
            // Par exemple, afficher l'aide

            return false;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if(partyManager.isPlayerInParty(player)){
                    player.sendMessage("Vous êtes déjà dans un groupe");
                }else{
                    partyManager.createParty(player);
                    player.sendMessage("Groupe créé !");
                }
                break;
            case "invite":
                if (args.length >= 2) {
                    Player target = player.getServer().getPlayer(args[1]);
                    if (target != null) {
                        partyManager.createParty(player);
                        sendInvite(player, target);
                    } else {
                        player.sendMessage("Le joueur spécifié n'est pas en ligne.");
                    }
                } else {
                    player.sendMessage("Utilisation : /party invite <joueur>");
                }
                break;
            case "exclude":
                if (args.length >= 2) {
                    Player target = player.getServer().getPlayer(args[1]);
                    if (target != null) {
                        if(partyManager.isPlayerLeader(player)){
                            partyManager.excludePlayer(target);
                        }else {
                            player.sendMessage("Vous devez être le chef du groupe pour exclure un joueur.");
                        }
                    } else {
                        player.sendMessage("Le joueur spécifié n'est pas en ligne.");
                    }
                } else {
                    player.sendMessage("Utilisation : /party exclude <joueur>");
                }
                break;
            case "leave":
                partyManager.leaveParty(player);
                player.sendMessage("Vous avez quitté le groupe.");
                break;
            case "disband":
                if (partyManager.isPlayerLeader(player)) {
                    partyManager.disbandParty(player);
                    player.sendMessage("Le groupe a été dissous.");
                } else {
                    player.sendMessage("Vous devez être le chef du groupe pour dissoudre le groupe.");
                }
                break;
            case "list":
                if (partyManager.isPlayerInParty(player)) {
                    player.sendMessage("Membres du groupe : " + partyManager.getPartyMembers(player).toString());
                } else {
                    player.sendMessage("Vous n'êtes pas dans un groupe.");
                }
                break;
            case "accept":
                if (args.length >= 2) {
                    acceptInvite(player, args[1]); // Appel de la méthode acceptInvite avec le paramètre manquant
                } else {
                    player.sendMessage("Utilisation : /party accept <chef_du_groupe>");
                }
                break;
            case "refuse":
                if (args.length >= 2) {
                    refuseInvite(player, args[1]); // Appel de la méthode refuseInvite avec le paramètre manquant
                } else {
                    player.sendMessage("Utilisation : /party refuse <chef_du_groupe>");
                }
                break;
            default:
                // Traitement si la sous-commande n'est pas reconnue
                // Par exemple, afficher un message d'erreur ou l'aide
                return false;
        }

        return true;
    }

    private void sendInvite(Player sender, Player target) {
        TextComponent inviteMessage = Component.text()
                .append(Component.text("Vous avez reçu une invitation de groupe de "))
                .append(Component.text(sender.getName()).color(TextColor.color(255, 255, 0)).hoverEvent(Component.text("Cliquez pour voir le profil")))
                .append(Component.text(". Cliquez sur "))
                .append(Component.text("[Accepter]").color(TextColor.color(0, 255, 0)).clickEvent(ClickEvent.runCommand("/party accept " + sender.getName())))
                .append(Component.text(" ou "))
                .append(Component.text("[Refuser]").color(TextColor.color(255, 0, 0)).clickEvent(ClickEvent.runCommand("/party refuse " + sender.getName())))
                .append(Component.text(" pour répondre."))
                .build();

        target.sendMessage(inviteMessage);
        partyManager.invitePlayer(sender, target);
    }


    private void acceptInvite(Player target, String ownerName) {
        Player sender = Bukkit.getPlayer(ownerName);
        if (sender != null && partyManager.acceptInvite(sender, target)) {
            target.sendMessage(Component.text("Vous avez accepté l'invitation au groupe."));
            sender.sendMessage(Component.text(target.getName() + " a accepté votre invitation au groupe."));
            Bukkit.getLogger().log(Level.INFO, target.getName() + " a accepté l'invitation de " + sender.getName() + " au groupe.");
        } else {
            target.sendMessage(Component.text("L'invitation n'est pas valide ou n'a pas été envoyée par le chef du groupe."));
        }
    }

    private void refuseInvite(Player target, String ownerName) {
        Player sender = Bukkit.getPlayer(ownerName);
        if (sender != null && partyManager.refuseInvite(sender, target)) {
            target.sendMessage(Component.text("Vous avez refusé l'invitation au groupe."));
            sender.sendMessage(Component.text(target.getName() + " a refusé votre invitation au groupe."));
            Bukkit.getLogger().log(Level.INFO, target.getName() + " a refusé l'invitation de " + sender.getName() + " au groupe.");
        } else {
            target.sendMessage(Component.text("Le propriétaire de l'invitation est introuvable."));
        }
    }



    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (sender instanceof Player) {
            if (args.length == 1) {
                completions.add("create");
                completions.add("invite");
                completions.add("exclude");
                completions.add("leave");
                completions.add("disband");
                completions.add("list");
                completions.add("accept");
                completions.add("refuse");
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("exclude"))) {
                if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("refuse")) {
                    Player player = (Player) sender;
                    List<String> pendingInvites = partyManager.getPendingInvites(player);

                    for (String pendingInvite : pendingInvites) {
                        String[] parts = pendingInvite.split(":");
                        if (parts.length == 2 && parts[0].equals("sender")) {
                            completions.add(parts[1]);
                        }
                    }
                } else {
                    for (Player onlinePlayer : sender.getServer().getOnlinePlayers()) {
                        completions.add(onlinePlayer.getName());
                    }
                }
            }
        }

        return completions;
    }

}
