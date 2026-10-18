package fr.anas.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Claim {
    private String world;
    private int chunkX;
    private int chunkZ;
    private UUID owner;
    private List<UUID> trusted;

    public Claim() {
        this.trusted = new ArrayList<>();
    }

    public Claim(String world, int chunkX, int chunkZ, UUID owner) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.owner = owner;
        this.trusted = new ArrayList<>();
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getTrusted() {
        if (trusted == null) trusted = new ArrayList<>();
        return trusted;
    }

    public boolean isAllowed(UUID playerUuid) {
        return owner.equals(playerUuid) || (trusted != null && trusted.contains(playerUuid));
    }
}
