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

public class TrailMenu {

    public static final Component MENU_TITLE = Component.text("Menu des Particules", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD);

    public static void open(Player player) {
        // Crée un inventaire de 27 slots (3 lignes de 9)
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        // Slot 11 : Cœurs (Poppy)
        inv.setItem(11, createItem(Material.POPPY, "Cœurs", NamedTextColor.RED, "Affiche des cœurs flottants."));

        // Slot 12 : Flammes (Blaze Powder)
        inv.setItem(12, createItem(Material.BLAZE_POWDER, "Flammes", NamedTextColor.GOLD, "Entoure ton personnage de flammes."));

        // Slot 13 : Étoiles (Nether Star)
        inv.setItem(13, createItem(Material.NETHER_STAR, "Étoiles", NamedTextColor.YELLOW, "Projette des étincelles de feu d'artifice."));

        // Slot 14 : Fumée (Campfire)
        inv.setItem(14, createItem(Material.CAMPFIRE, "Fumée", NamedTextColor.DARK_GRAY, "Laisse une trace de fumée blanche."));

        // Slot 15 : Éclairs (Amethyst Shard)
        inv.setItem(15, createItem(Material.AMETHYST_SHARD, "Éclairs", NamedTextColor.AQUA, "Génère des étincelles électriques."));

        // Slot 22 : Désactiver (Barrier)
        inv.setItem(22, createItem(Material.BARRIER, "Désactiver", NamedTextColor.DARK_RED, "Arrête toutes les particules actives."));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, NamedTextColor color, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text(description, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
}
