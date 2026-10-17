package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class QuestManager implements Listener {

    private final Test plugin;

    public QuestManager(Test plugin) {
        this.plugin = plugin;
    }

    public void checkAndRenewQuests(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        String today = LocalDate.now().toString();

        if (!today.equals(data.getQuestDate()) || data.getDailyQuests().isEmpty()) {
            data.setQuestDate(today);
            data.setDailyQuests(generateRandomQuests());
            plugin.getDataManager().save();
            player.sendMessage(Component.text("📜 Vos quêtes journalières ont été renouvelées ! Tapez ", NamedTextColor.GOLD)
                    .append(Component.text("/quests", NamedTextColor.YELLOW, TextDecoration.UNDERLINED)));
        }
    }

    private List<Quest> generateRandomQuests() {
        List<Quest> pool = new ArrayList<>();
        pool.add(new Quest("MINE_IRON", "Mineur de fer", "Miner 24 minerais de fer", 24, 60.0));
        pool.add(new Quest("MINE_COAL", "Mineur de charbon", "Miner 32 minerais de charbon", 32, 40.0));
        pool.add(new Quest("MINE_DIAMOND", "Chasseur de gemmes", "Miner 3 minerais de diamant", 3, 120.0));
        pool.add(new Quest("CHOP_LOGS", "Bûcheron assidu", "Couper 32 bûches de bois", 32, 45.0));
        pool.add(new Quest("KILL_MONSTERS", "Nettoyeur nocturne", "Éliminer 15 monstres hostiles", 15, 75.0));
        pool.add(new Quest("HARVEST_WHEAT", "Moissonneur", "Récolter 20 blés mûrs", 20, 50.0));

        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, 3));
    }

    public void triggerProgress(Player player, String questId, int amount) {
        checkAndRenewQuests(player);
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        for (Quest quest : data.getDailyQuests()) {
            if (quest.getId().equalsIgnoreCase(questId) && !quest.isCompleted()) {
                quest.addProgress(amount);
                if (quest.isCompleted()) {
                    player.sendMessage(Component.text("✔ Quête terminée : ", NamedTextColor.GREEN, TextDecoration.BOLD)
                            .append(Component.text(quest.getTitle(), NamedTextColor.YELLOW))
                            .append(Component.text(" ! Ouvre /quests pour récupérer ta récompense.", NamedTextColor.GREEN)));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                }
                plugin.getDataManager().save();
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkAndRenewQuests(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        if (mat == Material.IRON_ORE || mat == Material.DEEPSLATE_IRON_ORE) {
            triggerProgress(player, "MINE_IRON", 1);
        } else if (mat == Material.COAL_ORE || mat == Material.DEEPSLATE_COAL_ORE) {
            triggerProgress(player, "MINE_COAL", 1);
        } else if (mat == Material.DIAMOND_ORE || mat == Material.DEEPSLATE_DIAMOND_ORE) {
            triggerProgress(player, "MINE_DIAMOND", 1);
        } else if (mat.name().endsWith("_LOG")) {
            triggerProgress(player, "CHOP_LOGS", 1);
        } else if (mat == Material.WHEAT && event.getBlock().getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                triggerProgress(player, "HARVEST_WHEAT", 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        if (event.getEntity() instanceof Monster) {
            triggerProgress(event.getEntity().getKiller(), "KILL_MONSTERS", 1);
        }
    }
}
