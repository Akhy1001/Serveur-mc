package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum Rank {
    ADMIN("ADMIN", "[Admin] ", NamedTextColor.RED, 1),
    MODO("MODO", "[Modo] ", NamedTextColor.GOLD, 2),
    VIP("VIP", "[VIP] ", NamedTextColor.YELLOW, 3),
    JOUEUR("JOUEUR", "[Joueur] ", NamedTextColor.GRAY, 4);

    private final String id;
    private final String prefixText;
    private final NamedTextColor color;
    private final int priority;

    Rank(String id, String prefixText, NamedTextColor color, int priority) {
        this.id = id;
        this.prefixText = prefixText;
        this.color = color;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public Component getPrefix() {
        return Component.text(prefixText, color, TextDecoration.BOLD);
    }

    public NamedTextColor getColor() {
        return color;
    }

    public int getPriority() {
        return priority;
    }

    public static Rank fromString(String text) {
        for (Rank r : values()) {
            if (r.name().equalsIgnoreCase(text) || r.id.equalsIgnoreCase(text)) {
                return r;
            }
        }
        return JOUEUR;
    }
}
