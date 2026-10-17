package fr.anas.test;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData {
    private String activeParticle;
    private long lastKitClaim;
    private String rank;
    private long muteExpiration;
    private double balance;

    // Métiers
    private Map<String, Integer> jobLevels;
    private Map<String, Double> jobExp;

    // Quêtes journalières
    private String questDate;
    private List<Quest> dailyQuests;

    // Coordonnées Home
    private String homeWorld;
    private double homeX;
    private double homeY;
    private double homeZ;
    private float homeYaw;
    private float homePitch;

    public PlayerData() {
        this.activeParticle = null;
        this.lastKitClaim = 0;
        this.rank = Rank.JOUEUR.name();
        this.muteExpiration = 0;
        this.balance = 250.0;
        this.jobLevels = new HashMap<>();
        this.jobExp = new HashMap<>();
        this.questDate = "";
        this.dailyQuests = new ArrayList<>();
    }

    // ==========================================
    // QUÊTES JOURNALIÈRES
    // ==========================================

    public String getQuestDate() {
        return questDate;
    }

    public void setQuestDate(String questDate) {
        this.questDate = questDate;
    }

    public List<Quest> getDailyQuests() {
        if (dailyQuests == null) dailyQuests = new ArrayList<>();
        return dailyQuests;
    }

    public void setDailyQuests(List<Quest> dailyQuests) {
        this.dailyQuests = dailyQuests;
    }

    // ==========================================
    // MÉTIERS
    // ==========================================

    public int getJobLevel(String jobName) {
        if (jobLevels == null) jobLevels = new HashMap<>();
        return jobLevels.getOrDefault(jobName.toUpperCase(), 1);
    }

    public void setJobLevel(String jobName, int level) {
        if (jobLevels == null) jobLevels = new HashMap<>();
        jobLevels.put(jobName.toUpperCase(), level);
    }

    public double getJobExp(String jobName) {
        if (jobExp == null) jobExp = new HashMap<>();
        return jobExp.getOrDefault(jobName.toUpperCase(), 0.0);
    }

    public void setJobExp(String jobName, double exp) {
        if (jobExp == null) jobExp = new HashMap<>();
        jobExp.put(jobName.toUpperCase(), exp);
    }

    // ==========================================
    // ÉCONOMIE
    // ==========================================

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }

    public boolean removeBalance(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    // ==========================================
    // MODÉRATION (MUTE)
    // ==========================================

    public boolean isMuted() {
        if (muteExpiration == -1) return true;
        if (muteExpiration == 0) return false;
        return System.currentTimeMillis() < muteExpiration;
    }

    public long getMuteExpiration() {
        return muteExpiration;
    }

    public void setMuteExpiration(long muteExpiration) {
        this.muteExpiration = muteExpiration;
    }

    // ==========================================
    // RANGS
    // ==========================================

    public Rank getRank() {
        if (rank == null) return Rank.JOUEUR;
        return Rank.fromString(rank);
    }

    public void setRank(Rank rank) {
        this.rank = rank.name();
    }

    // ==========================================
    // PARTICULES & KITS
    // ==========================================

    public String getActiveParticle() {
        return activeParticle;
    }

    public void setActiveParticle(String activeParticle) {
        this.activeParticle = activeParticle;
    }

    public long getLastKitClaim() {
        return lastKitClaim;
    }

    public void setLastKitClaim(long lastKitClaim) {
        this.lastKitClaim = lastKitClaim;
    }

    // ==========================================
    // HOME & TÉLÉPORTATION
    // ==========================================

    public boolean hasHome() {
        return homeWorld != null;
    }

    public void setHome(Location loc) {
        if (loc.getWorld() != null) {
            this.homeWorld = loc.getWorld().getName();
            this.homeX = loc.getX();
            this.homeY = loc.getY();
            this.homeZ = loc.getZ();
            this.homeYaw = loc.getYaw();
            this.homePitch = loc.getPitch();
        }
    }

    public Location getHomeLocation() {
        if (!hasHome()) return null;
        World world = Bukkit.getWorld(homeWorld);
        if (world == null) return null;
        return new Location(world, homeX, homeY, homeZ, homeYaw, homePitch);
    }
}
