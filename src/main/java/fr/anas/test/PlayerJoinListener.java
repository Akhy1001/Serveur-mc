package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Test plugin;

    public PlayerJoinListener(Test plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        if (data.getActiveParticle() != null) {
            // Reconnexion : relance la boucle visuelle
            plugin.applyParticle(player, data.getActiveParticle(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase().trim();

        if (message.equals("/deop") || message.startsWith("/deop ")
                || message.equals("/minecraft:deop") || message.startsWith("/minecraft:deop ")) {

            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text(
                    "Cette commande ne peut être exécutée que depuis le terminal du serveur !",
                    NamedTextColor.RED
            ));
        }
    }
}
