package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectionManager implements Listener {

    private final Test plugin;

    // Limites par chunk
    private static final int MAX_VEHICLES_PER_CHUNK = 25;
    private static final int MAX_ARMOR_STANDS_PER_CHUNK = 20;

    // Anti-spam drop d'items
    private final Map<UUID, Long> dropCooldown = new HashMap<>();
    private final Map<UUID, Integer> dropCount = new HashMap<>();

    // Anti-clock redstone abusive
    private final Map<Long, Integer> redstoneFrequency = new HashMap<>();
    private long currentSecond = 0;

    public ProtectionManager(Test plugin) {
        this.plugin = plugin;
    }

    // 1. Bloquer la surcharge d'entités par chunk
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        EntityType type = event.getEntityType();
        Chunk chunk = event.getBlock().getChunk();

        if (type == EntityType.ARMOR_STAND) {
            long count = countEntitiesInChunk(chunk, EntityType.ARMOR_STAND);
            if (count >= MAX_ARMOR_STANDS_PER_CHUNK) {
                event.setCancelled(true);
                warnPlayer(event.getPlayer(), "Limite d'Armor Stands atteinte dans ce chunk !");
            }
        } else if (type == EntityType.MINECART || type == EntityType.OAK_BOAT) {
            long count = countVehiclesInChunk(chunk);
            if (count >= MAX_VEHICLES_PER_CHUNK) {
                event.setCancelled(true);
                warnPlayer(event.getPlayer(), "Limite de véhicules atteinte dans ce chunk !");
            }
        }
    }

    // 2. Anti-spam de drop d'items (crash par chute de TPS)
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("test.bypass.protection")) return;

        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();

        dropCooldown.putIfAbsent(id, now);
        dropCount.putIfAbsent(id, 0);

        if (now - dropCooldown.get(id) < 1000) {
            int count = dropCount.get(id) + 1;
            if (count > 15) { // Plus de 15 items jetés en 1 seconde
                event.setCancelled(true);
                warnPlayer(player, "Ralentis tes jets d'objets !");
                return;
            }
            dropCount.put(id, count);
        } else {
            dropCooldown.put(id, now);
            dropCount.put(id, 1);
        }
    }

    // 3. Limitation des clocks de redstone hyperactives
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRedstone(BlockRedstoneEvent event) {
        long sec = System.currentTimeMillis() / 1000;
        if (sec != currentSecond) {
            currentSecond = sec;
            redstoneFrequency.clear();
        }

        Block block = event.getBlock();
        long chunkKey = (((long) block.getChunk().getX()) << 32) | (block.getChunk().getZ() & 0xFFFFFFFFL);
        int updates = redstoneFrequency.getOrDefault(chunkKey, 0) + 1;
        redstoneFrequency.put(chunkKey, updates);

        // Au-delà de 250 impulsions/sec dans un seul chunk, on coupe temporairement le signal
        if (updates > 250) {
            event.setNewCurrent(0);
        }
    }

    private long countEntitiesInChunk(Chunk chunk, EntityType type) {
        int total = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity.getType() == type) total++;
        }
        return total;
    }

    private long countVehiclesInChunk(Chunk chunk) {
        int total = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity.getType() == EntityType.MINECART || entity.getType().name().endsWith("_BOAT")) {
                total++;
            }
        }
        return total;
    }

    private void warnPlayer(Player player, String message) {
        if (player != null) {
            player.sendMessage(Component.text("⚠ " + message, NamedTextColor.RED));
        }
    }
}
