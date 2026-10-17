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

public class KitMenu {

    public static final Component MENU_TITLE = Component.text("📦 Sélection des Kits", NamedTextColor.GOLD, TextDecoration.BOLD);
    public static final Component PREVIEW_TITLE = Component.text("Aperçu : Kit Starter", NamedTextColor.DARK_AQUA, TextDecoration.BOLD);
    private static final long COOLDOWN_MILLIS = 24L * 60 * 60 * 1000;

    public static void open(Player player, Test plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        // Vitres de remplissage
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.empty());
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Slot 13 : Kit Starter
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        long now = System.currentTimeMillis();
        long lastClaim = data.getLastKitClaim();
        long remaining = (lastClaim + COOLDOWN_MILLIS) - now;
        boolean canClaim = player.hasPermission("test.kit.bypass") || remaining <= 0;

        ItemStack kitItem = new ItemStack(canClaim ? Material.CHEST : Material.MINECART);
        ItemMeta meta = kitItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Kit Starter", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Équipement de départ pour bien débuter.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Contenu :", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" • 1x Épée d'aventurier en fer", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" • 1x Pioche en fer", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" • 16x Steaks cuits", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" • 32x Torches", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());

            if (canClaim) {
                lore.add(Component.text("✔ Disponible immédiatement", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("▶ Clic gauche pour réclamer", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            } else {
                long seconds = (remaining / 1000) % 60;
                long minutes = (remaining / (1000 * 60)) % 60;
                long hours = remaining / (1000 * 60 * 60);
                String formatted = String.format("%02dh %02dm %02ds", hours, minutes, seconds);
                lore.add(Component.text("✖ En recharge : " + formatted, NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            }

            lore.add(Component.text("▶ Clic droit pour prévisualiser", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            kitItem.setItemMeta(meta);
        }

        inv.setItem(13, kitItem);
        player.openInventory(inv);
    }

    public static void openPreview(Player player) {
        Inventory preview = Bukkit.createInventory(null, 9, PREVIEW_TITLE);

        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        if (swordMeta != null) {
            swordMeta.displayName(Component.text("Épée d'aventurier", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            sword.setItemMeta(swordMeta);
        }

        preview.setItem(1, sword);
        preview.setItem(3, new ItemStack(Material.IRON_PICKAXE));
        preview.setItem(5, new ItemStack(Material.COOKED_BEEF, 16));
        preview.setItem(7, new ItemStack(Material.TORCH, 32));

        player.openInventory(preview);
    }
}
