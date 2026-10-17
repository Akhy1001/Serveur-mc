package fr.anas.test;

import org.bukkit.Material;

public enum JobType {
    MINEUR("Mineur", Material.DIAMOND_PICKAXE, "Mine des pierres et minerais."),
    BUCHERON("Bûcheron", Material.IRON_AXE, "Abats des arbres et récolte du bois."),
    CHASSEUR("Chasseur", Material.BOW, "Élimine les créatures hostiles."),
    FERMIER("Fermier", Material.GOLDEN_HOE, "Récolte du blé, carottes et patates mûres.");

    private final String displayName;
    private final Material icon;
    private final String description;

    JobType(String displayName, Material icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }
}
