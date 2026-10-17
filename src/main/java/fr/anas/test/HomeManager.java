package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
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

public class HomeManager implements Listener {

    private final Test plugin;
    private final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();

    public HomeManager(Test plugin) {
        this.plugin = plugin;
    }

    public void setHome(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        data.setHome(player.getLocation());
        plugin.getDataManager().save();

        player.sendMessage(Component.text("Point de base enregistré avec succès !", NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
    }

    public void startTeleportHome(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);

        if (!data.hasHome()) {
            player.sendMessage(Component.text("Tu n'as pas encore défini de home. Utilise /sethome d'abord !", NamedTextColor.RED));
            return;
        }

        Location targetLoc = data.getHomeLocation();
        if (targetLoc == null || targetLoc.getWorld() == null) {
            player.sendMessage(Component.text("Le monde de ton home est introuvable !", NamedTextColor.RED));
            return;
        }

        if (pendingTeleports.containsKey(uuid)) {
            player.sendMessage(Component.text("Téléportation déjà en cours !", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Téléportation vers ton home dans 3 secondes... Ne bouge pas !", NamedTextColor.GOLD));

        BukkitTask task = new BukkitRunnable() {
            int secondsLeft = 3;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTeleport(uuid);
                    return;
                }

                if (secondsLeft == 0) {
                    player.teleport(targetLoc);

                    Title endTitle = Title.title(
                            Component.text("HOME", NamedTextColor.GREEN, TextDecoration.BOLD),
                            Component.text("Bienvenue chez toi !", NamedTextColor.GRAY),
                            Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1000), Duration.ofMillis(300))
                    );
                    player.showTitle(endTitle);
                    player.playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    player.getWorld().spawnParticle(Particle.PORTAL, targetLoc.clone().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.1);

                    pendingTeleports.remove(uuid);
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
                        Component.text("Retour à la base...", NamedTextColor.GRAY),
                        Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(100))
                );
                player.showTitle(title);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.2f);
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 0.5, 0), 15, 0.3, 0.5, 0.3, 0.1);

                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        pendingTeleports.put(uuid, task);
    }

    public void cancelTeleport(UUID uuid) {
        if (pendingTeleports.containsKey(uuid)) {
            pendingTeleports.get(uuid).cancel();
            pendingTeleports.remove(uuid);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.clearTitle();
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (pendingTeleports.containsKey(uuid)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
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
            if (pendingTeleports.containsKey(uuid)) {
                cancelTeleport(uuid);
                player.sendMessage(Component.text("Téléportation annulée : dégâts subis !", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }
}
