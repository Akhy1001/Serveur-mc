package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements Listener {

    private final Test plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(Test plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public void setupScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("sidebar", Criteria.DUMMY,
                Component.text("✦ SERVEUR TEST ✦", NamedTextColor.GOLD, TextDecoration.BOLD));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
        boards.put(player.getUniqueId(), board);

        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) return;

        Objective objective = board.getObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return;

        // Réinitialiser les anciennes lignes pour un affichage propre
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        Rank rank = data.getRank();

        int onlineCount = Bukkit.getOnlinePlayers().size();
        String formattedBalance = String.format("%.2f", data.getBalance());

        // Lignes du tableau (du score le plus haut au plus bas)
        setLine(objective, "§7§m------------------", 8);
        setLine(objective, "§fRang : " + rank.getColor() + rank.getId(), 7);
        setLine(objective, "§fPièces : §e" + formattedBalance + " ⛃", 6);
        setLine(objective, "§1", 5); // Ligne vide
        setLine(objective, "§fEn ligne : §a" + onlineCount + "§7/§a" + Bukkit.getMaxPlayers(), 4);
        setLine(objective, "§2", 3); // Ligne vide
        setLine(objective, "§eplay.monserveur.fr", 2);
        setLine(objective, "§7§m-------------------", 1);
    }

    private void setLine(Objective objective, String text, int score) {
        Score s = objective.getScore(text);
        s.setScore(score);
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 20L, 20L); // Rafraîchissement chaque seconde (20 ticks)
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setupScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        boards.remove(event.getPlayer().getUniqueId());
    }
}
