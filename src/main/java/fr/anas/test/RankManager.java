package fr.anas.test;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankManager implements Listener {

    private final Test plugin;

    public RankManager(Test plugin) {
        this.plugin = plugin;
    }

    public void setPlayerRank(Player target, Rank rank) {
        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        data.setRank(rank);
        plugin.getDataManager().save();

        updateTabName(target);
        target.sendMessage(Component.text("Ton rang a été mis à jour : ", NamedTextColor.GOLD).append(rank.getPrefix()));
    }

    public void updateTabName(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        Rank rank = data.getRank();

        Component tabComponent = rank.getPrefix()
                .append(Component.text(player.getName(), rank.getColor()));

        player.playerListName(tabComponent);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateTabName(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        // Vérification de sanction (Mute)
        if (data.isMuted()) {
            event.setCancelled(true);
            long exp = data.getMuteExpiration();
            if (exp == -1) {
                player.sendMessage(Component.text("Tu es réduit au silence de manière permanente.", NamedTextColor.RED));
            } else {
                long leftMs = exp - System.currentTimeMillis();
                long seconds = (leftMs / 1000) % 60;
                long minutes = (leftMs / (1000 * 60)) % 60;
                long hours = (leftMs / (1000 * 60 * 60));
                String formatted = String.format("%02dh %02dm %02ds", hours, minutes, seconds);
                player.sendMessage(Component.text("Tu es réduit au silence. Temps restant : " + formatted, NamedTextColor.RED));
            }
            return;
        }

        // Rendu du chat standard avec le rang
        Rank rank = data.getRank();
        event.renderer((source, sourceDisplayName, message, viewer) ->
                rank.getPrefix()
                        .append(Component.text(source.getName(), rank.getColor()))
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(message.color(NamedTextColor.WHITE))
        );
    }
}
