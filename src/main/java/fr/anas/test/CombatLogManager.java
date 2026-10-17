package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class CombatLogManager implements Listener {

    private final Test plugin;
    private static final long COMBAT_DURATION_MILLIS = 15_000L; // 15 secondes
    private final Map<UUID, Long> combatTags = new HashMap<>();

    // Commandes interdites en combat
    private static final List<String> BLOCKED_COMMANDS = List.of(
            "/spawn", "/tpa", "/tpaccept", "/tpdeny", "/home", "/warp"
    );

    public CombatLogManager(Test plugin) {
        this.plugin = plugin;
        startActionBarTask();
    }

    public boolean isInCombat(Player player) {
        if (!combatTags.containsKey(player.getUniqueId())) {
            return false;
        }
        long timeLeft = combatTags.get(player.getUniqueId()) - System.currentTimeMillis();
        if (timeLeft <= 0) {
            combatTags.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public void tagPlayer(Player player) {
        boolean alreadyTagged = isInCombat(player);
        combatTags.put(player.getUniqueId(), System.currentTimeMillis() + COMBAT_DURATION_MILLIS);

        if (!alreadyTagged) {
            player.sendMessage(Component.text("Tu es entré en combat ! Déconnexion interdite pendant 15s.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 0.5f);
        }
    }

    private void startActionBarTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            Iterator<Map.Entry<UUID, Long>> it = combatTags.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                Player player = Bukkit.getPlayer(entry.getKey());
                long diff = entry.getValue() - now;

                if (diff <= 0) {
                    it.remove();
                    if (player != null && player.isOnline()) {
                        player.sendActionBar(Component.text("Tu n'es plus en combat.", NamedTextColor.GREEN));
                    }
                } else if (player != null && player.isOnline()) {
                    int secondsLeft = (int) Math.ceil(diff / 1000.0);
                    Component bar = Component.text("⚔ En combat : ", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.text(secondsLeft + "s", NamedTextColor.YELLOW, TextDecoration.BOLD));
                    player.sendActionBar(bar);
                }
            }
        }, 10L, 10L); // Toutes les demi-secondes
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker != null && !attacker.equals(victim)) {
            tagPlayer(victim);
            tagPlayer(attacker);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isInCombat(player)) {
            // Mort punitive et drop d'inventaire
            player.setHealth(0.0);
            combatTags.remove(player.getUniqueId());

            Component alert = Component.text("[COMBAT] ", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                    .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                    .append(Component.text(" s'est déconnecté en combat et a été éliminé !", NamedTextColor.RED));

            Bukkit.broadcast(alert);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!isInCombat(player)) {
            return;
        }

        String raw = event.getMessage().toLowerCase();
        for (String blocked : BLOCKED_COMMANDS) {
            if (raw.equals(blocked) || raw.startsWith(blocked + " ")) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Tu ne peux pas fuir avec cette commande en combat !", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }
    }
}
