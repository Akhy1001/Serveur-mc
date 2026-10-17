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

import java.util.List;

public class ShopMenu {

    public static final Component MENU_TITLE = Component.text("🏪 Boutique du Serveur", NamedTextColor.DARK_GREEN, TextDecoration.BOLD);

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

        // Slot 10 : Diamant (Achat: 100$ | Vente: 50$)
        inv.setItem(10, createShopItem(Material.DIAMOND, "Diamant Brut", 100.0, 50.0, 1));

        // Slot 12 : Pain x16 (Achat: 20$ | Vente: 8$)
        inv.setItem(12, createShopItem(Material.BREAD, "Lot de Pain (x16)", 20.0, 8.0, 16));

        // Slot 14 : Bûches de Chêne x32 (Achat: 30$ | Vente: 10$)
        inv.setItem(14, createShopItem(Material.OAK_LOG, "Bûches de Chêne (x32)", 30.0, 10.0, 32));

        // Slot 16 : Pomme Dorée (Achat: 200$ | Vente: 80$)
        inv.setItem(16, createShopItem(Material.GOLDEN_APPLE, "Pomme Dorée", 200.0, 80.0, 1));

        // Slot 22 : Affichage du solde actuel du joueur
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        ItemStack balItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balMeta = balItem.getItemMeta();
        if (balMeta != null) {
            balMeta.displayName(Component.text("Ton Solde Actuel", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            balMeta.lore(List.of(
                    Component.text(String.format("%.2f ⛃ pièces", data.getBalance()), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
            ));
            balItem.setItemMeta(balMeta);
        }
        inv.setItem(22, balItem);

        player.openInventory(inv);
    }

    private static ItemStack createShopItem(Material mat, String name, double buyPrice, double sellPrice, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.AQUA, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.empty(),
                    Component.text("▶ Clic gauche pour acheter : ", NamedTextColor.GRAY)
                            .append(Component.text(buyPrice + " ⛃", NamedTextColor.GREEN, TextDecoration.BOLD))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("▶ Clic droit pour vendre : ", NamedTextColor.GRAY)
                            .append(Component.text(sellPrice + " ⛃", NamedTextColor.RED, TextDecoration.BOLD))
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
}
