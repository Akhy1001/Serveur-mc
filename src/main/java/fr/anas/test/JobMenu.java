package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class JobMenu {

    public static final Component MENU_TITLE = Component.text("⚒ Vos Métiers & Progression", NamedTextColor.GOLD, TextDecoration.BOLD);

    public static void open(Player player, Test plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(Component.empty());
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        // Slots : 10 = Mineur, 12 = Bûcheron, 14 = Chasseur, 16 = Fermier
        inv.setItem(10, createJobItem(JobType.MINEUR, data, plugin));
        inv.setItem(12, createJobItem(JobType.BUCHERON, data, plugin));
        inv.setItem(14, createJobItem(JobType.CHASSEUR, data, plugin));
        inv.setItem(16, createJobItem(JobType.FERMIER, data, plugin));

        player.openInventory(inv);
    }

    private static ItemStack createJobItem(JobType job, PlayerData data, Test plugin) {
        ItemStack item = new ItemStack(job.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Métier : " + job.getDisplayName(), NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            int level = data.getJobLevel(job.name());
            double exp = data.getJobExp(job.name());
            double reqExp = plugin.getJobManager().getRequiredExp(level);

            double percentage = Math.min(100.0, (exp / reqExp) * 100.0);
            String progressBar = buildProgressBar(exp, reqExp);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(job.getDescription(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Niveau : ", NamedTextColor.WHITE)
                    .append(Component.text(level, NamedTextColor.GREEN, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Progression : ", NamedTextColor.WHITE)
                    .append(Component.text(String.format("%.1f/%.0f XP", exp, reqExp), NamedTextColor.AQUA))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(progressBar, NamedTextColor.GRAY)
                    .append(Component.text(String.format(" %.0f%%", percentage), NamedTextColor.YELLOW))
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String buildProgressBar(double current, double max) {
        int totalBars = 10;
        int filledBars = (int) Math.round((current / max) * totalBars);
        filledBars = Math.min(totalBars, Math.max(0, filledBars));

        return "§a" + "■".repeat(filledBars) + "§7" + "□".repeat(totalBars - filledBars);
    }
}
