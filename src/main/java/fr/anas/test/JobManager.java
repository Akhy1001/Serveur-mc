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

public class JobManager implements Listener {

    private final Test plugin;

    public JobManager(Test plugin) {
        this.plugin = plugin;
    }

    public void addJobProgress(Player player, JobType job, double expReward, double moneyReward) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        String name = job.name();

        int currentLevel = data.getJobLevel(name);
        double currentExp = data.getJobExp(name) + expReward;
        double requiredExp = getRequiredExp(currentLevel);

        // Crédit de l'argent
        data.addBalance(moneyReward);

        // Vérification de Level Up
        if (currentExp >= requiredExp) {
            currentExp -= requiredExp;
            currentLevel++;
            data.setJobLevel(name, currentLevel);
            data.setJobExp(name, currentExp);

            double bonusReward = currentLevel * 50.0;
            data.addBalance(bonusReward);

            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("✦ NIVEAU SUPÉRIEUR ✦", NamedTextColor.GOLD, TextDecoration.BOLD));
            player.sendMessage(Component.text("Ton métier ", NamedTextColor.YELLOW)
                    .append(Component.text(job.getDisplayName(), NamedTextColor.AQUA, TextDecoration.BOLD))
                    .append(Component.text(" passe au niveau ", NamedTextColor.YELLOW))
                    .append(Component.text(currentLevel, NamedTextColor.GREEN, TextDecoration.BOLD))
                    .append(Component.text(" ! (+", NamedTextColor.YELLOW))
                    .append(Component.text(bonusReward + " ⛃ bonus)", NamedTextColor.GOLD)));
            player.sendMessage(Component.empty());

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
        } else {
            data.setJobExp(name, currentExp);
            // Actionbar discrète
            player.sendActionBar(Component.text("+ " + String.format("%.2f", moneyReward) + " ⛃", NamedTextColor.GOLD)
                    .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("+" + expReward + " XP " + job.getDisplayName(), NamedTextColor.GREEN)));
        }

        plugin.getDataManager().save();
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateScoreboard(player);
        }
    }

    public double getRequiredExp(int level) {
        return level * 100.0;
    }

    // --- ÉVÉNEMENT : CASSAGE DE BLOCS (Mineur, Bûcheron, Fermier) ---
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        // 1. MINEUR
        switch (mat) {
            case STONE, DEEPSLATE -> addJobProgress(player, JobType.MINEUR, 1.0, 0.25);
            case COAL_ORE, DEEPSLATE_COAL_ORE, COPPER_ORE, DEEPSLATE_COPPER_ORE -> addJobProgress(player, JobType.MINEUR, 3.0, 1.0);
            case IRON_ORE, DEEPSLATE_IRON_ORE -> addJobProgress(player, JobType.MINEUR, 5.0, 2.5);
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> addJobProgress(player, JobType.MINEUR, 8.0, 4.0);
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> addJobProgress(player, JobType.MINEUR, 8.0, 3.5);
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> addJobProgress(player, JobType.MINEUR, 20.0, 15.0);
            case ANCIENT_DEBRIS -> addJobProgress(player, JobType.MINEUR, 50.0, 40.0);
            default -> {}
        }

        // 2. BÛCHERON
        if (mat.name().endsWith("_LOG")) {
            addJobProgress(player, JobType.BUCHERON, 4.0, 1.5);
        }

        // 3. FERMIER (uniquement si mature)
        if (event.getBlock().getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                switch (mat) {
                    case WHEAT, CARROTS, POTATOES, BEETROOTS -> addJobProgress(player, JobType.FERMIER, 5.0, 2.0);
                    case NETHER_WART -> addJobProgress(player, JobType.FERMIER, 8.0, 3.5);
                    default -> {}
                }
            }
        }
    }

    // --- ÉVÉNEMENT : COMBAT (Chasseur) ---
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();

        if (event.getEntity() instanceof Monster monster) {
            switch (monster.getType()) {
                case ZOMBIE, SKELETON, SPIDER, CREEPER -> addJobProgress(killer, JobType.CHASSEUR, 5.0, 3.0);
                case ENDERMAN, WITCH -> addJobProgress(killer, JobType.CHASSEUR, 12.0, 8.0);
                case WITHER_SKELETON, BLAZE -> addJobProgress(killer, JobType.CHASSEUR, 15.0, 10.0);
                case WARDEN -> addJobProgress(killer, JobType.CHASSEUR, 250.0, 150.0);
                case WITHER, ENDER_DRAGON -> addJobProgress(killer, JobType.CHASSEUR, 500.0, 300.0);
                default -> addJobProgress(killer, JobType.CHASSEUR, 3.0, 1.5);
            }
        }
    }
}
