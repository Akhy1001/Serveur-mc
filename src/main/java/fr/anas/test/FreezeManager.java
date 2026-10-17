package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager implements Listener {

    private final Set<UUID> frozenPlayers = new HashSet<>();

    public boolean toggleFreeze(Player target) {
        UUID uuid = target.getUniqueId();
        if (frozenPlayers.contains(uuid)) {
            frozenPlayers.remove(uuid);
            target.clearTitle();
            target.sendMessage(Component.text("Tu as été dégelé par un modérateur.", NamedTextColor.GREEN));
            target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return false;
        } else {
            frozenPlayers.add(uuid);
            sendFreezeAlert(target);
            target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 0.8f);
            return true;
        }
    }

    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    private void sendFreezeAlert(Player player) {
        Title title = Title.title(
                Component.text("VOUS ÊTES GELÉ", NamedTextColor.RED, TextDecoration.BOLD),
                Component.text("Ne vous déconnectez pas. Suivez les instructions du staff.", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(3000), Duration.ofMillis(500))
        );
        player.showTitle(title);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!frozenPlayers.contains(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // Si le joueur essaie de changer de bloc ou de sauter, on bloque la translation
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location lockedLocation = from.clone();
            lockedLocation.setYaw(to.getYaw());
            lockedLocation.setPitch(to.getPitch());
            event.setTo(lockedLocation);

            sendFreezeAlert(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Enlève le joueur de la mémoire s'il quitte
        frozenPlayers.remove(event.getPlayer().getUniqueId());
    }
}
