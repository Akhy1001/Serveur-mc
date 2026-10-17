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

public class QuestMenu {

    public static final Component MENU_TITLE = Component.text("📜 Quêtes Journalières", NamedTextColor.GOLD, TextDecoration.BOLD);

    public static void open(Player player, Test plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        if (fMeta != null) {
            fMeta.displayName(Component.empty());
            filler.setItemMeta(fMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        plugin.getQuestManager().checkAndRenewQuests(player);
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        List<Quest> quests = data.getDailyQuests();

        int[] slots = {11, 13, 15};
        for (int i = 0; i < quests.size() && i < slots.length; i++) {
            Quest q = quests.get(i);
            inv.setItem(slots[i], createQuestItem(q));
        }

        player.openInventory(inv);
    }

    private static ItemStack createQuestItem(Quest q) {
        Material icon;
        NamedTextColor titleColor;

        if (q.isClaimed()) {
            icon = Material.MINECART;
            titleColor = NamedTextColor.GRAY;
        } else if (q.isCompleted()) {
            icon = Material.CHEST;
            titleColor = NamedTextColor.GREEN;
        } else {
            icon = Material.WRITABLE_BOOK;
            titleColor = NamedTextColor.YELLOW;
        }

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(q.getTitle(), titleColor, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(q.getDescription(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Progression : ", NamedTextColor.WHITE)
                    .append(Component.text(q.getProgress() + "/" + q.getTarget(), NamedTextColor.AQUA))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Récompense : ", NamedTextColor.WHITE)
                    .append(Component.text(String.format("%.2f ⛃ pièces", q.getReward()), NamedTextColor.GOLD))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());

            if (q.isClaimed()) {
                lore.add(Component.text("✔ Récompense déjà récupérée", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            } else if (q.isCompleted()) {
                lore.add(Component.text("▶ Clic pour réclamer la récompense !", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("✖ En cours d'accomplissement...", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
