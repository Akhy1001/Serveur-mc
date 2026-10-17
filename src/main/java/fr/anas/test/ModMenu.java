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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class ModMenu {

    public static final String PREFIX_TITLE = "Modération : ";

    public static void open(Player staff, Player target, Test plugin) {
        Component title = Component.text(PREFIX_TITLE, NamedTextColor.DARK_RED, TextDecoration.BOLD)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW));

        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Vitres de séparation
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Slot 13 : Tête du joueur avec infos
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(target);
            skullMeta.displayName(Component.text(target.getName(), NamedTextColor.GOLD, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            boolean isFrozen = plugin.getFreezeManager().isFrozen(target.getUniqueId());
            boolean inCombat = plugin.getCombatLogManager() != null && plugin.getCombatLogManager().isInCombat(target);

            skullMeta.lore(List.of(
                    Component.text("Santé : ", NamedTextColor.GRAY)
                            .append(Component.text(String.format("%.1f/20 ❤", target.getHealth()), NamedTextColor.RED))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Nourriture : ", NamedTextColor.GRAY)
                            .append(Component.text(target.getFoodLevel() + "/20", NamedTextColor.YELLOW))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Gelé : ", NamedTextColor.GRAY)
                            .append(Component.text(isFrozen ? "OUI" : "NON", isFrozen ? NamedTextColor.GREEN : NamedTextColor.RED))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("En combat : ", NamedTextColor.GRAY)
                            .append(Component.text(inCombat ? "OUI" : "NON", inCombat ? NamedTextColor.RED : NamedTextColor.GREEN))
                            .decoration(TextDecoration.ITALIC, false)
            ));
            skull.setItemMeta(skullMeta);
        }
        inv.setItem(13, skull);

        // Slot 10 : Geler / Dégeler (Freeze)
        boolean isFrozen = plugin.getFreezeManager().isFrozen(target.getUniqueId());
        ItemStack freezeItem = new ItemStack(isFrozen ? Material.PACKED_ICE : Material.ICE);
        ItemMeta freezeMeta = freezeItem.getItemMeta();
        if (freezeMeta != null) {
            freezeMeta.displayName(Component.text(isFrozen ? "Dégeler le joueur" : "Geler le joueur", NamedTextColor.AQUA, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            freezeMeta.lore(List.of(
                    Component.text("Clique pour basculer l'état de freeze.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            freezeItem.setItemMeta(freezeMeta);
        }
        inv.setItem(10, freezeItem);

        // Slot 12 : Inspecter l'inventaire
        ItemStack chestItem = new ItemStack(Material.CHEST);
        ItemMeta chestMeta = chestItem.getItemMeta();
        if (chestMeta != null) {
            chestMeta.displayName(Component.text("Inspecter l'inventaire", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            chestMeta.lore(List.of(
                    Component.text("Ouvre l'inventaire du joueur en direct.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            chestItem.setItemMeta(chestMeta);
        }
        inv.setItem(12, chestItem);

        // Slot 14 : Téléportation au joueur
        ItemStack tpItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = tpItem.getItemMeta();
        if (tpMeta != null) {
            tpMeta.displayName(Component.text("Se téléporter au joueur", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            tpItem.setItemMeta(tpMeta);
        }
        inv.setItem(14, tpItem);

        // Slot 16 : Expulser (Kick)
        ItemStack kickItem = new ItemStack(Material.BARRIER);
        ItemMeta kickMeta = kickItem.getItemMeta();
        if (kickMeta != null) {
            kickMeta.displayName(Component.text("Expulser (Kick)", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            kickMeta.lore(List.of(
                    Component.text("Déconnecte immédiatement le joueur.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
            ));
            kickItem.setItemMeta(kickMeta);
        }
        inv.setItem(16, kickItem);

        staff.openInventory(inv);
    }
}
