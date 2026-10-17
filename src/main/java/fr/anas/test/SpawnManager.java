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

public class SpawnManager implements Listener {

    private final Test plugin;
    private final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();

    public SpawnManager(Test plugin) {
        this.plugin = plugin;
    }

    public void startTeleport(Player player) {
        UUID uuid = player.getUniqueId();

        if (pendingTeleports.containsKey(uuid)) {
            player.sendMessage(Component.text("Téléportation déjà en cours !", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Téléportation en cours... Ne bouge pas !", NamedTextColor.GOLD));

        BukkitTask task = new BukkitRunnable() {
            int secondsLeft = 3;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTeleport(uuid);
                    return;
                }

                // Déclenchement de la téléportation quand le compteur arrive à 0
                if (secondsLeft == 0) {
                    Location spawnLoc = player.getWorld().getSpawnLocation().add(0.5, 0, 0.5);
                    player.teleport(spawnLoc);

                    // Title de succès
                    Title endTitle = Title.title(
                            Component.text("SPAWN", NamedTextColor.GREEN, TextDecoration.BOLD),
                            Component.text("Téléportation réussie !", NamedTextColor.GRAY),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1200), Duration.ofMillis(400))
                    );
                    player.showTitle(endTitle);

                    // Sons et particules d'arrivée
                    player.playSound(spawnLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, spawnLoc.add(0, 1, 0), 35, 0.5, 0.8, 0.5, 0.05);

                    pendingTeleports.remove(uuid);
                    cancel();
                    return;
                }

                // Particules autour du joueur pendant la canalisation
                Location loc = player.getLocation().add(0, 0.6, 0);
                player.getWorld().spawnParticle(Particle.PORTAL, loc, 20, 0.3, 0.5, 0.3, 0.1);

                // Couleur du décompte : Rouge (3), Doré (2), Jaune (1)
                NamedTextColor countColor = switch (secondsLeft) {
                    case 3 -> NamedTextColor.RED;
                    case 2 -> NamedTextColor.GOLD;
                    default -> NamedTextColor.YELLOW;
                };

                // Affichage du gros chiffre à l'écran
                Title countdownTitle = Title.title(
                        Component.text(String.valueOf(secondsLeft), countColor, TextDecoration.BOLD),
                        Component.text("Téléportation imminente...", NamedTextColor.GRAY),
                        Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(100))
                );
                player.showTitle(countdownTitle);

                // Son de tic-tac
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.2f);

                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 seconde pile

        pendingTeleports.put(uuid, task);
    }

    public void cancelTeleport(UUID uuid) {
        if (pendingTeleports.containsKey(uuid)) {
            pendingTeleports.get(uuid).cancel();
            pendingTeleports.remove(uuid);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // Efface le title immédiatement si la canalisation est rompue
                player.clearTitle();
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (pendingTeleports.containsKey(uuid)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // Bloque si le joueur change de bloc (laisse la liberté de bouger la tête)
            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
                cancelTeleport(uuid);
                player.sendMessage(Component.text("Téléportation annulée : tu as bougé !", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
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
