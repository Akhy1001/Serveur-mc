package fr.anas.test;

public class Quest {
    private String id;
    private String title;
    private String description;
    private int target;
    private int progress;
    private double reward;
    private boolean claimed;

    public Quest() {}

    public Quest(String id, String title, String description, int target, double reward) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.target = target;
        this.progress = 0;
        this.reward = reward;
        this.claimed = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getTarget() { return target; }
    public int getProgress() { return progress; }
    public double getReward() { return reward; }
    public boolean isClaimed() { return claimed; }

    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public boolean isCompleted() {
        return progress >= target;
    }

    public void addProgress(int amount) {
        this.progress = Math.min(target, this.progress + amount);
    }
}
