package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.format.TextDecoration;


public class MenuListener implements Listener {

    private final Test plugin;

    public MenuListener(Test plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // --- MENU TRAILS ---
        if (event.getView().title().equals(TrailMenu.MENU_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Material mat = clickedItem.getType();
            switch (mat) {
                case POPPY -> plugin.applyParticle(player, "coeur");
                case BLAZE_POWDER -> plugin.applyParticle(player, "flamme");
                case NETHER_STAR -> plugin.applyParticle(player, "etoile");
                case CAMPFIRE -> plugin.applyParticle(player, "fumee");
                case LIGHTNING_ROD -> plugin.applyParticle(player, "eclair");
                case BARRIER -> plugin.removeParticle(player);
                default -> { return; }
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            player.closeInventory();
            return;
        }

        // --- MENU WARP / DESTINATIONS ---
        if (event.getView().title().equals(WarpMenu.MENU_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (plugin.getCombatLogManager() != null && plugin.getCombatLogManager().isInCombat(player)) {
                player.sendMessage(Component.text("Tu ne peux pas te téléporter en plein combat !", NamedTextColor.RED));
                player.closeInventory();
                return;
            }

            if (clickedItem.getType() == Material.COMPASS) {
                player.closeInventory();
                plugin.getSpawnManager().startTeleport(player);
            } else if (clickedItem.getType() == Material.RED_BED) {
                player.closeInventory();
                plugin.getHomeManager().teleportToHome(player);
            }
            return;
        }

        // --- MENU MODÉRATION (/mod <joueur>) ---
        String rawTitle = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (rawTitle.startsWith(ModMenu.PREFIX_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            String targetName = rawTitle.replace(ModMenu.PREFIX_TITLE, "").trim();
            Player target = Bukkit.getPlayerExact(targetName);

            if (target == null || !target.isOnline()) {
                player.sendMessage(Component.text("Le joueur ciblé s'est déconnecté.", NamedTextColor.RED));
                player.closeInventory();
                return;
            }

            Material type = clickedItem.getType();
            switch (type) {
                case ICE, PACKED_ICE -> {
                    plugin.getFreezeManager().toggleFreeze(target);
                    ModMenu.open(player, target, plugin);
                }
                case CHEST -> {
                    player.closeInventory();
                    player.openInventory(target.getInventory());
                }
                case ENDER_PEARL -> {
                    player.closeInventory();
                    player.teleport(target.getLocation());
                    player.sendMessage(Component.text("Téléporté à " + target.getName() + " !", NamedTextColor.GREEN));
                }
                case BARRIER -> {
                    player.closeInventory();
                    target.kick(Component.text("Tu as été expulsé par un modérateur.", NamedTextColor.RED));
                    player.sendMessage(Component.text(target.getName() + " a été expulsé !", NamedTextColor.YELLOW));
                }
                default -> {}
            }
            return;
        }

        // --- MENU APERÇU KIT ---
        if (event.getView().title().equals(KitMenu.PREVIEW_TITLE)) {
            event.setCancelled(true);
            return;
        }

        // --- MENU KITS (/kits) ---
        if (event.getView().title().equals(KitMenu.MENU_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.CHEST || clickedItem.getType() == Material.MINECART) {
                if (event.isRightClick()) {
                    KitMenu.openPreview(player);
                } else if (event.isLeftClick()) {
                    player.closeInventory();
                    plugin.getKitManager().giveStarterKit(player);
                }
            }
        }

        // --- MENU MÉTIERS (/jobs) ---
        if (event.getView().title().equals(JobMenu.MENU_TITLE)) {
            event.setCancelled(true);
            return;
        }
        // --- MENU MÉTIERS (/jobs) ---
        if (event.getView().title().equals(JobMenu.MENU_TITLE)) {
            event.setCancelled(true);
            return;
        }
        // --- MENU QUÊTES (/quests) ---
        if (event.getView().title().equals(QuestMenu.MENU_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            int index = switch (slot) {
                case 11 -> 0;
                case 13 -> 1;
                case 15 -> 2;
                default -> -1;
            };

            if (index == -1) return;

            PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            if (index < data.getDailyQuests().size()) {
                Quest q = data.getDailyQuests().get(index);
                if (q.isCompleted() && !q.isClaimed()) {
                    q.setClaimed(true);
                    data.addBalance(q.getReward());
                    plugin.getDataManager().save();

                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                    player.sendMessage(Component.text("Récompense obtenue : ", NamedTextColor.GREEN)
                            .append(Component.text(String.format("+%.2f ⛃ pièces", q.getReward()), NamedTextColor.GOLD, TextDecoration.BOLD)));

                    if (plugin.getScoreboardManager() != null) {
                        plugin.getScoreboardManager().updateScoreboard(player);
                    }
                    QuestMenu.open(player, plugin);
                } else if (!q.isCompleted()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }
            return;
        }

    }
}
