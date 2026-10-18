package fr.anas.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimManager implements Listener {

    private final Test plugin;
    private final File file;
    private final Gson gson;
    private final Map<String, Claim> claims = new HashMap<>();

    public static final double CLAIM_PRICE = 100.0;

    public ClaimManager(Test plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "claims.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadClaims();
    }

    private String getChunkKey(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }

    public Claim getClaim(Chunk chunk) {
        return claims.get(getChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
    }

    public boolean isChunkClaimed(Chunk chunk) {
        return getClaim(chunk) != null;
    }

    public void addClaim(Claim claim) {
        claims.put(getChunkKey(claim.getWorld(), claim.getChunkX(), claim.getChunkZ()), claim);
        saveClaims();
    }

    public void removeClaim(Chunk chunk) {
        claims.remove(getChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
        saveClaims();
    }

    public void saveClaims() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(claims, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder claims.json !");
        }
    }

    public void loadClaims() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Claim>>() {}.getType();
            Map<String, Claim> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                claims.putAll(loaded);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de charger claims.json !");
        }
    }

    private boolean canInteract(Player player, Location loc) {
        if (player.hasPermission("test.bypass.protection") || player.isOp()) {
            return true;
        }
        Claim claim = getClaim(loc.getChunk());
        if (claim == null) return true;
        return claim.isAllowed(player.getUniqueId());
    }

    // --- PROTECTIONS EN JEU ---

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!canInteract(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Cette zone est protégée !", NamedTextColor.RED));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!canInteract(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Cette zone est protégée !", NamedTextColor.RED));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        Material mat = block.getType();

        // Conteneurs et mécanismes interactifs
        if (mat.name().contains("CHEST") || mat.name().contains("SHULKER") || mat.name().contains("BARREL")
                || mat.name().contains("FURNACE") || mat.name().contains("DOOR") || mat.name().contains("GATE")
                || mat.name().contains("BUTTON") || mat == Material.LEVER || mat == Material.HOPPER
                || mat == Material.DISPENSER || mat == Material.DROPPER || mat == Material.BREWING_STAND) {

            if (!canInteract(player, block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Tu n'as pas la permission d'interagir ici !", NamedTextColor.RED));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getEntity() instanceof Animals || event.getEntity() instanceof ArmorStand) {
            if (!canInteract(player, event.getEntity().getLocation())) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Tu ne peux pas attaquer les créatures de ce claim !", NamedTextColor.RED));
            }
        }
    }

    // Protection contre les explosions (TNT & Creepers)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> isChunkClaimed(block.getChunk()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> isChunkClaimed(block.getChunk()));
    }
}
