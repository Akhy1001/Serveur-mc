package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class AnnouncementManager {

    private final Test plugin;
    private BukkitTask task;
    private int currentIndex = 0;

    // Messages diffusés en boucle
    private final List<Component> messages = List.of(
            Component.text("Pense à utiliser ", NamedTextColor.GRAY)
                    .append(Component.text("/trails", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" pour choisir tes particules cosmétiques !", NamedTextColor.GRAY)),

            Component.text("Besoin de retourner au point de départ ? Tape ", NamedTextColor.GRAY)
                    .append(Component.text("/spawn", NamedTextColor.AQUA, TextDecoration.BOLD))
                    .append(Component.text(" et patiente 3 secondes.", NamedTextColor.GRAY)),

            Component.text("Souhaite la bienvenue aux nouveaux arrivants avec la commande ", NamedTextColor.GRAY)
                    .append(Component.text("/bienvenue <joueur>", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text(" !", NamedTextColor.GRAY))
    );

    public AnnouncementManager(Test plugin) {
        this.plugin = plugin;
        start();
    }

    public void start() {
        // Démarre après 1 minute (1200 ticks) et tourne toutes les 3 minutes (3600 ticks)
        // Note : pour tester rapidement, tu peux réduire à 20L * 30 (toutes les 30 secondes)
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty() || messages.isEmpty()) {
                return;
            }

            Component prefix = Component.text("[", NamedTextColor.DARK_GRAY)
                    .append(Component.text("INFO", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("] ", NamedTextColor.DARK_GRAY));

            Component fullMessage = prefix.append(messages.get(currentIndex));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(fullMessage);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.6f, 1.2f);
            }

            currentIndex = (currentIndex + 1) % messages.size();
        }, 1200L, 3600L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
