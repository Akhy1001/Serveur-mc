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

        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0f, 1.2f);
        player.sendMessage(Component.text("✔ Ton point de base (home) a été enregistré ici !", NamedTextColor.GREEN));
    }

    public void startTeleportHome(Player player) {
        teleportToHome(player);
    }

    public void teleportToHome(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        if (!data.hasHome() || data.getHomeLocation() == null) {
            player.sendMessage(Component.text("Tu n'as défini aucun home ! Utilise d'abord /sethome.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Vérification du combat tag
        if (plugin.getCombatLogManager() != null && plugin.getCombatLogManager().isInCombat(player)) {
            player.sendMessage(Component.text("Impossible de te téléporter en plein combat !", NamedTextColor.RED));
            return;
        }

        UUID uuid = player.getUniqueId();
        cancelTeleport(uuid, false);

        // Téléportation instantanée pour les administrateurs
        if (player.hasPermission("test.teleport.instant") || player.isOp()) {
            executeTeleport(player, data.getHomeLocation());
            return;
        }

        Location startLoc = player.getLocation();

        BukkitTask task = new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    pendingTeleports.remove(uuid);
                    return;
                }

                if (countdown > 0) {
                    Title title = Title.title(
                            Component.text("Téléportation...", NamedTextColor.GOLD, TextDecoration.BOLD),
                            Component.text(countdown + " seconde" + (countdown > 1 ? "s" : "") + " (ne bouge pas)", NamedTextColor.YELLOW),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(200))
                    );
                    player.showTitle(title);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.0f);
                    countdown--;
                } else {
                    cancel();
                    pendingTeleports.remove(uuid);
                    executeTeleport(player, data.getHomeLocation());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        pendingTeleports.put(uuid, task);
    }

    private void executeTeleport(Player player, Location targetLoc) {
        player.teleportAsync(targetLoc).thenAccept(success -> {
            if (success) {
                player.playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                targetLoc.getWorld().spawnParticle(Particle.PORTAL, targetLoc.clone().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
                player.sendMessage(Component.text("✦ Téléportation à ton home réussie !", NamedTextColor.GREEN));
            }
        });
    }

    public void cancelTeleport(UUID uuid, boolean notify) {
        BukkitTask task = pendingTeleports.remove(uuid);
        if (task != null) {
            task.cancel();
            if (notify) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                    player.clearTitle();
                    player.sendMessage(Component.text("✖ Téléportation annulée (tu as bougé ou subi des dégâts) !", NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!pendingTeleports.containsKey(event.getPlayer().getUniqueId())) return;

        // Annulation seulement si déplacement de bloc entier (pas simple mouvement de tête)
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            cancelTeleport(event.getPlayer().getUniqueId(), true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            cancelTeleport(player.getUniqueId(), true);
        }
    }
}
