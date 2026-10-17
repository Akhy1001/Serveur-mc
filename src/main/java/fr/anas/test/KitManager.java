package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class KitManager {

    private final Test plugin;
    private static final long COOLDOWN_MILLIS = 24L * 60 * 60 * 1000;

    public KitManager(Test plugin) {
        this.plugin = plugin;
    }

    public void giveStarterKit(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);

        if (!player.hasPermission("test.kit.bypass")) {
            long lastClaim = data.getLastKitClaim();
            long timeRemaining = (lastClaim + COOLDOWN_MILLIS) - now;

            if (timeRemaining > 0) {
                String formattedTime = formatDuration(timeRemaining);
                player.sendMessage(Component.text(
                        "Tu dois encore attendre " + formattedTime + " avant de réclamer ce kit.",
                        NamedTextColor.RED
                ));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Component.text("Ton inventaire est plein ! Fais de la place avant de réclamer le kit.", NamedTextColor.RED));
            return;
        }

        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        if (swordMeta != null) {
            swordMeta.displayName(Component.text("Épée d'aventurier", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            sword.setItemMeta(swordMeta);
        }

        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 16);
        ItemStack torches = new ItemStack(Material.TORCH, 32);

        player.getInventory().addItem(sword, pickaxe, steak, torches);

        data.setLastKitClaim(now);
        plugin.getDataManager().save();

        player.sendMessage(Component.text("Tu as reçu ton kit Starter !", NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    private String formatDuration(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}
