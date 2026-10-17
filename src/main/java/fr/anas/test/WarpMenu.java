package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WarpMenu {

    public static final Component MENU_TITLE = Component.text("⚡ Destinations Rapides", NamedTextColor.DARK_AQUA, TextDecoration.BOLD);

    public static void open(Player player, Test plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        // Vitres grises pour décorer les bords
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.empty());
            filler.setItemMeta(fillerMeta);
        }

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // 1. Bouton SPAWN (Slot 11)
        ItemStack spawnItem = new ItemStack(Material.COMPASS);
        ItemMeta spawnMeta = spawnItem.getItemMeta();
        if (spawnMeta != null) {
            spawnMeta.displayName(Component.text("✦ Le Spawn", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            spawnMeta.lore(List.of(
                    Component.text("Rejoins le point central du serveur.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("▶ Clic gauche pour te téléporter (3s)", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
            ));
            spawnItem.setItemMeta(spawnMeta);
        }
        inv.setItem(11, spawnItem);

        // 2. Bouton HOME (Slot 15)
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        ItemStack homeItem = new ItemStack(Material.RED_BED);
        ItemMeta homeMeta = homeItem.getItemMeta();
        if (homeMeta != null) {
            homeMeta.displayName(Component.text("⌂ Ton Home", NamedTextColor.AQUA, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> homeLore = new ArrayList<>();
            if (data.hasHome()) {
                Location loc = data.getHomeLocation();
                String coords = loc != null ? String.format("X: %.1f | Y: %.1f | Z: %.1f", loc.getX(), loc.getY(), loc.getZ()) : "Coordonnées inconnues";
                homeLore.add(Component.text("Position : " + coords, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                homeLore.add(Component.empty());
                homeLore.add(Component.text("▶ Clic gauche pour rentrer chez toi (3s)", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            } else {
                homeLore.add(Component.text("Aucun home défini !", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                homeLore.add(Component.text("Utilise /sethome pour en poser un.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            }

            homeMeta.lore(homeLore);
            homeItem.setItemMeta(homeMeta);
        }
        inv.setItem(15, homeItem);

        player.openInventory(inv);
    }
}
