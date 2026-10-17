package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager implements Listener {

    private final Test plugin;

    // Cible UUID -> Demandeur UUID
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    private final Map<UUID, BukkitTask> requestExpirations = new HashMap<>();
    private final Map<UUID, BukkitTask> activeTeleports = new HashMap<>();

    public TeleportManager(Test plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player sender, Player target) {
        if (sender.equals(target)) {
            sender.sendMessage(Component.text("Tu ne peux pas te téléporter à toi-même !", NamedTextColor.RED));
            return;
        }

        UUID targetId = target.getUniqueId();
        UUID senderId = sender.getUniqueId();

        // Annule une précédente demande si elle existait
        cancelRequest(targetId);

        pendingRequests.put(targetId, senderId);

        // Message pour l'envoyeur
        sender.sendMessage(Component.text("Demande de téléportation envoyée à ", NamedTextColor.GOLD)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                .append(Component.text(". Elle expire dans 60 secondes.", NamedTextColor.GOLD)));

        // Message cliquable pour la cible
        Component acceptBtn = Component.text("[ACCEPTER]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/tpaccept"))
                .hoverEvent(HoverEvent.showText(Component.text("Clique pour accepter la téléportation", NamedTextColor.GREEN)));

        Component denyBtn = Component.text("[REFUSER]", NamedTextColor.RED, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/tpdeny"))
                .hoverEvent(HoverEvent.showText(Component.text("Clique pour refuser", NamedTextColor.RED)));

        Component requestMsg = Component.empty()
                .append(Component.newline())
                .append(Component.text(sender.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(" souhaite se téléporter à vous.", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(acceptBtn)
                .append(Component.text("   "))
                .append(denyBtn)
                .append(Component.newline());

        target.sendMessage(requestMsg);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);

        // Expiration auto à 60s (1200 ticks)
        BukkitTask expireTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(targetId) && pendingRequests.get(targetId).equals(senderId)) {
                pendingRequests.remove(targetId);
                requestExpirations.remove(targetId);
                sender.sendMessage(Component.text("La demande envoyée à " + target.getName() + " a expiré.", NamedTextColor.GRAY));
            }
        }, 1200L);

        requestExpirations.put(targetId, expireTask);
    }

    public void acceptRequest(Player target) {
        UUID targetId = target.getUniqueId();
        UUID senderId = pendingRequests.remove(targetId);
        cancelExpiration(targetId);

        if (senderId == null) {
            target.sendMessage(Component.text("Tu n'as aucune demande en attente.", NamedTextColor.RED));
            return;
        }

        Player sender = Bukkit.getPlayer(senderId);
        if (sender == null || !sender.isOnline()) {
            target.sendMessage(Component.text("Le joueur s'est déconnecté.", NamedTextColor.RED));
            return;
        }

        target.sendMessage(Component.text("Tu as accepté la demande de " + sender.getName() + ".", NamedTextColor.GREEN));
        sender.sendMessage(Component.text(target.getName() + " a accepté. Téléportation dans 3 secondes... Ne bouge pas !", NamedTextColor.GOLD));

        startCountdown(sender, target);
    }

    public void denyRequest(Player target) {
        UUID targetId = target.getUniqueId();
        UUID senderId = pendingRequests.remove(targetId);
        cancelExpiration(targetId);

        if (senderId == null) {
            target.sendMessage(Component.text("Tu n'as aucune demande en attente.", NamedTextColor.RED));
            return;
        }

        Player sender = Bukkit.getPlayer(senderId);
        target.sendMessage(Component.text("Demande refusée.", NamedTextColor.YELLOW));
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(Component.text(target.getName() + " a refusé ta demande.", NamedTextColor.RED));
        }
    }

    private void startCountdown(Player traveler, Player destination) {
        UUID id = traveler.getUniqueId();

        BukkitTask task = new BukkitRunnable() {
            int secondsLeft = 3;

            @Override
            public void run() {
                if (!traveler.isOnline() || !destination.isOnline()) {
                    cancelTeleport(id);
                    return;
                }

                if (secondsLeft == 0) {
                    traveler.teleport(destination.getLocation());

                    Title endTitle = Title.title(
                            Component.text("TÉLÉPORTÉ", NamedTextColor.GREEN, TextDecoration.BOLD),
                            Component.text("Arrivé auprès de " + destination.getName(), NamedTextColor.GRAY),
                            Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(300))
                    );
                    traveler.showTitle(endTitle);
                    traveler.playSound(traveler.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    traveler.getWorld().spawnParticle(Particle.PORTAL, traveler.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.1);

                    activeTeleports.remove(id);
                    cancel();
                    return;
                }

                NamedTextColor color = switch (secondsLeft) {
                    case 3 -> NamedTextColor.RED;
                    case 2 -> NamedTextColor.GOLD;
                    default -> NamedTextColor.YELLOW;
                };

                Title title = Title.title(
                        Component.text(String.valueOf(secondsLeft), color, TextDecoration.BOLD),
                        Component.text("Téléportation vers " + destination.getName(), NamedTextColor.GRAY),
                        Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(100))
                );
                traveler.showTitle(title);
                traveler.playSound(traveler.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.2f);
                traveler.getWorld().spawnParticle(Particle.PORTAL, traveler.getLocation().add(0, 0.5, 0), 15, 0.3, 0.5, 0.3, 0.1);

                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        activeTeleports.put(id, task);
    }

    private void cancelTeleport(UUID id) {
        if (activeTeleports.containsKey(id)) {
            activeTeleports.get(id).cancel();
            activeTeleports.remove(id);
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                player.clearTitle();
            }
        }
    }

    private void cancelRequest(UUID targetId) {
        pendingRequests.remove(targetId);
        cancelExpiration(targetId);
    }

    private void cancelExpiration(UUID targetId) {
        if (requestExpirations.containsKey(targetId)) {
            requestExpirations.get(targetId).cancel();
            requestExpirations.remove(targetId);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (activeTeleports.containsKey(uuid)) {
            Location f = event.getFrom();
            Location t = event.getTo();
            if (f.getBlockX() != t.getBlockX() || f.getBlockY() != t.getBlockY() || f.getBlockZ() != t.getBlockZ()) {
                cancelTeleport(uuid);
                player.sendMessage(Component.text("Téléportation annulée : tu as bougé !", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            if (activeTeleports.containsKey(uuid)) {
                cancelTeleport(uuid);
                player.sendMessage(Component.text("Téléportation annulée : dégâts subis !", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }
}
