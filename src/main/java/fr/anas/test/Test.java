package fr.anas.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;


import java.util.*;


public final class Test extends JavaPlugin implements CommandExecutor, TabCompleter {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_SECONDS = 30;

    private final Map<UUID, BukkitTask> activeTrails = new HashMap<>();
    private SpawnManager spawnManager;
    private ScoreboardManager scoreboardManager;
    private AnnouncementManager announcementManager;
    private FreezeManager freezeManager;
    private KitManager kitManager;
    private DataManager dataManager;
    private TeleportManager teleportManager;
    private CombatLogManager combatLogManager;
    private HomeManager homeManager;
    private RankManager rankManager;
    private ProtectionManager protectionManager;
    private JobManager jobManager;
    private QuestManager questManager;


    private static final List<String> PARTICLES_OPTIONS = List.of(
            "coeur", "flamme", "etoile", "fumee", "eclair", "stop"
    );

    @Override
    public void onEnable() {
        this.dataManager = new DataManager(this);
        this.spawnManager = new SpawnManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.announcementManager = new AnnouncementManager(this);
        this.freezeManager = new FreezeManager();
        this.kitManager = new KitManager(this);
        this.teleportManager = new TeleportManager(this);
        this.combatLogManager = new CombatLogManager(this);
        this.homeManager = new HomeManager(this);
        this.rankManager = new RankManager(this);
        this.protectionManager = new ProtectionManager(this);
        this.jobManager = new JobManager(this);
        this.questManager = new QuestManager(this);

        // Enregistrement des listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(spawnManager, this);
        getServer().getPluginManager().registerEvents(scoreboardManager, this);
        getServer().getPluginManager().registerEvents(freezeManager, this);
        getServer().getPluginManager().registerEvents(teleportManager, this);
        getServer().getPluginManager().registerEvents(combatLogManager, this);
        getServer().getPluginManager().registerEvents(homeManager, this);
        getServer().getPluginManager().registerEvents(rankManager, this);
        getServer().getPluginManager().registerEvents(protectionManager, this);
        getServer().getPluginManager().registerEvents(jobManager, this);
        getServer().getPluginManager().registerEvents(questManager, this);

        // Initialisation pour les joueurs en ligne
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            scoreboardManager.setupScoreboard(onlinePlayer);
            PlayerData data = dataManager.getPlayerData(onlinePlayer.getUniqueId());
            if (data.getActiveParticle() != null) {
                applyParticle(onlinePlayer, data.getActiveParticle(), false);
            }
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            rankManager.updateTabName(onlinePlayer);
        }

        // Enregistrement des commandes
        registerCommand("bienvenue");
        registerCommand("trail");
        registerCommand("trails");
        registerCommand("spawn");
        registerCommand("broadcast");
        registerCommand("freeze");
        registerCommand("clearchat");
        registerCommand("kit");
        registerCommand("tpa");
        registerCommand("tpaccept");
        registerCommand("tpdeny");
        registerCommand("sethome");
        registerCommand("home");
        registerCommand("menu");
        registerCommand("mod");
        registerCommand("kits");
        registerCommand("setrank");
        registerCommand("mute");
        registerCommand("unmute");
        registerCommand("money");
        registerCommand("pay");
        registerCommand("shop");
        registerCommand("eco");
        registerCommand("jobs");
        registerCommand("quests");




        getLogger().info("Plugin Test activé avec succès !");
    }

    private void registerCommand(String name) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(this);
            getCommand(name).setTabCompleter(this);
        }
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        cooldowns.clear();
        for (BukkitTask task : activeTrails.values()) {
            task.cancel();
        }
        activeTrails.clear();

        if (announcementManager != null) {
            announcementManager.stop();
        }

        getLogger().info("Plugin Test désactivé !");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        return switch (cmdName) {
            case "menu" -> handleMenu(sender);
            case "sethome" -> handleSetHome(sender);
            case "home" -> handleHome(sender);
            case "tpa" -> handleTpa(sender, args);
            case "tpaccept" -> handleTpAccept(sender);
            case "tpdeny" -> handleTpDeny(sender);
            case "kit" -> handleKit(sender, args);
            case "freeze" -> handleFreeze(sender, args);
            case "clearchat" -> handleClearChat(sender);
            case "broadcast" -> handleBroadcast(sender, args);
            case "spawn" -> handleSpawn(sender);
            case "trails" -> handleTrailsMenu(sender);
            case "bienvenue" -> handleBienvenue(sender, args);
            case "trail" -> handleTrail(sender, args);
            case "mod" -> handleMod(sender, args);
            case "kits" -> handleKitsMenu(sender);
            case "setrank" -> handleSetRank(sender, args);
            case "mute" -> handleMute(sender, args);
            case "unmute" -> handleUnmute(sender, args);
            case "money" -> handleMoney(sender, args);
            case "pay" -> handlePay(sender, args);
            case "shop" -> handleShop(sender);
            case "eco" -> handleEco(sender, args);
            case "jobs" -> handleJobs(sender);
            case "quests" -> handleQuests(sender);



            default -> false;
        };
    }

    // --- HANDLERS DES COMMANDES ---

    private boolean handleQuests(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut voir ses quêtes.", NamedTextColor.RED));
            return true;
        }
        QuestMenu.open(player, this);
        return true;
    }


    private boolean handleJobs(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut ouvrir le menu des métiers.", NamedTextColor.RED));
            return true;
        }
        JobMenu.open(player, this);
        return true;
    }


    private boolean handleEco(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.admin.eco")) {
            sender.sendMessage(Component.text("Tu n'as pas la permission requise.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Component.text("Utilisation : /eco <give|take|set> <joueur> <montant>", NamedTextColor.RED));
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Joueur introuvable ou hors-ligne.", NamedTextColor.RED));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Montant invalide.", NamedTextColor.RED));
            return true;
        }

        if (amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            sender.sendMessage(Component.text("Le montant doit être positif.", NamedTextColor.RED));
            return true;
        }

        PlayerData targetData = dataManager.getPlayerData(target.getUniqueId());

        switch (action) {
            case "give" -> {
                targetData.addBalance(amount);
                dataManager.save();
                scoreboardManager.updateScoreboard(target);
                sender.sendMessage(Component.text("Tu as ajouté " + String.format("%.2f", amount) + " ⛃ à " + target.getName() + ".", NamedTextColor.GREEN));
                target.sendMessage(Component.text("Un administrateur t'a crédité de " + String.format("%.2f", amount) + " ⛃.", NamedTextColor.GOLD));
            }
            case "take" -> {
                double newBalance = Math.max(0, targetData.getBalance() - amount);
                targetData.setBalance(newBalance);
                dataManager.save();
                scoreboardManager.updateScoreboard(target);
                sender.sendMessage(Component.text("Tu as retiré " + String.format("%.2f", amount) + " ⛃ à " + target.getName() + ".", NamedTextColor.YELLOW));
                target.sendMessage(Component.text("Un administrateur t'a retiré " + String.format("%.2f", amount) + " ⛃.", NamedTextColor.RED));
            }
            case "set" -> {
                targetData.setBalance(amount);
                dataManager.save();
                scoreboardManager.updateScoreboard(target);
                sender.sendMessage(Component.text("Le solde de " + target.getName() + " est désormais de " + String.format("%.2f", amount) + " ⛃.", NamedTextColor.GREEN));
                target.sendMessage(Component.text("Ton solde a été ajusté à " + String.format("%.2f", amount) + " ⛃.", NamedTextColor.GOLD));
            }
            default -> {
                sender.sendMessage(Component.text("Action inconnue. Utilise give, take ou set.", NamedTextColor.RED));
                return true;
            }
        }

        return true;
    }


    private boolean handleShop(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut ouvrir la boutique.", NamedTextColor.RED));
            return true;
        }
        ShopMenu.open(player, this);
        return true;
    }

    private boolean handleMoney(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Utilisation console : /money <joueur>", NamedTextColor.RED));
                return true;
            }
            PlayerData data = dataManager.getPlayerData(player.getUniqueId());
            player.sendMessage(Component.text("Ton solde : ", NamedTextColor.GOLD)
                    .append(Component.text(String.format("%.2f ⛃ pièces", data.getBalance()), NamedTextColor.YELLOW, TextDecoration.BOLD)));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Joueur introuvable.", NamedTextColor.RED));
            return true;
        }

        PlayerData targetData = dataManager.getPlayerData(target.getUniqueId());
        sender.sendMessage(Component.text("Solde de " + target.getName() + " : ", NamedTextColor.GOLD)
                .append(Component.text(String.format("%.2f ⛃ pièces", targetData.getBalance()), NamedTextColor.YELLOW, TextDecoration.BOLD)));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut faire un virement.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Utilisation : /pay <joueur> <montant>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Joueur introuvable ou déconnecté.", NamedTextColor.RED));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("Tu ne peux pas t'envoyer de l'argent à toi-même !", NamedTextColor.RED));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Montant invalide.", NamedTextColor.RED));
            return true;
        }

        if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            player.sendMessage(Component.text("Le montant doit être supérieur à 0 !", NamedTextColor.RED));
            return true;
        }

        PlayerData senderData = dataManager.getPlayerData(player.getUniqueId());
        if (!senderData.removeBalance(amount)) {
            player.sendMessage(Component.text("Fonds insuffisants ! Ton solde : " + String.format("%.2f", senderData.getBalance()) + " pièces.", NamedTextColor.RED));
            return true;
        }

        PlayerData targetData = dataManager.getPlayerData(target.getUniqueId());
        targetData.addBalance(amount);
        dataManager.save();

        player.sendMessage(Component.text("Tu as envoyé " + String.format("%.2f", amount) + " pièces à " + target.getName() + ".", NamedTextColor.GREEN));
        target.sendMessage(Component.text("Tu as reçu " + String.format("%.2f", amount) + " pièces de la part de " + player.getName() + " !", NamedTextColor.GOLD));
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        return true;
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.moderation.mute")) {
            sender.sendMessage(Component.text("Tu n'as pas la permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Utilisation : /mute <joueur> [durée ex: 10m, 1h, 1d]", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Joueur introuvable.", NamedTextColor.RED));
            return true;
        }

        PlayerData data = dataManager.getPlayerData(target.getUniqueId());

        if (args.length == 1) {
            // Mute permanent
            data.setMuteExpiration(-1);
            dataManager.save();
            sender.sendMessage(Component.text(target.getName() + " est maintenant réduit au silence définitivement.", NamedTextColor.YELLOW));
            target.sendMessage(Component.text("Tu as été réduit au silence définitivement par un modérateur.", NamedTextColor.RED));
            return true;
        }

        long durationMs = parseDuration(args[1]);
        if (durationMs <= 0) {
            sender.sendMessage(Component.text("Format de durée invalide ! Exemples : 30s, 10m, 2h, 1d", NamedTextColor.RED));
            return true;
        }

        data.setMuteExpiration(System.currentTimeMillis() + durationMs);
        dataManager.save();
        sender.sendMessage(Component.text(target.getName() + " est mute pour " + args[1] + ".", NamedTextColor.YELLOW));
        target.sendMessage(Component.text("Tu as été réduit au silence pour " + args[1] + ".", NamedTextColor.RED));
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.moderation.mute")) {
            sender.sendMessage(Component.text("Tu n'as pas la permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Utilisation : /unmute <joueur>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Joueur introuvable.", NamedTextColor.RED));
            return true;
        }

        PlayerData data = dataManager.getPlayerData(target.getUniqueId());
        data.setMuteExpiration(0);
        dataManager.save();

        sender.sendMessage(Component.text(target.getName() + " n'est plus réduit au silence.", NamedTextColor.GREEN));
        target.sendMessage(Component.text("Tu peux à nouveau parler dans le chat.", NamedTextColor.GREEN));
        return true;
    }

    private long parseDuration(String input) {
        try {
            char unit = input.charAt(input.length() - 1);
            long value = Long.parseLong(input.substring(0, input.length() - 1));
            return switch (Character.toLowerCase(unit)) {
                case 's' -> value * 1000L;
                case 'm' -> value * 60L * 1000L;
                case 'h' -> value * 60L * 60L * 1000L;
                case 'd' -> value * 24L * 60L * 60L * 1000L;
                default -> -1;
            };
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean handleSetRank(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.admin.rank")) {
            sender.sendMessage(Component.text("Tu n'as pas la permission requise.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Utilisation : /setrank <joueur> <JOUEUR|VIP|MODO|ADMIN>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Joueur introuvable ou hors-ligne.", NamedTextColor.RED));
            return true;
        }

        Rank rank = Rank.fromString(args[1]);
        rankManager.setPlayerRank(target, rank);
        sender.sendMessage(Component.text("Le rang de " + target.getName() + " est désormais : ", NamedTextColor.GREEN)
                .append(rank.getPrefix()));
        return true;
    }


    private boolean handleKitsMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut ouvrir ce menu.", NamedTextColor.RED));
            return true;
        }
        KitMenu.open(player, this);
        return true;
    }

    private boolean handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut ouvrir ce menu.", NamedTextColor.RED));
            return true;
        }
        WarpMenu.open(player, this);
        return true;
    }

    private boolean handleSetHome(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut définir un home.", NamedTextColor.RED));
            return true;
        }
        homeManager.setHome(player);
        return true;
    }

    private boolean handleHome(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut utiliser /home.", NamedTextColor.RED));
            return true;
        }
        if (combatLogManager != null && combatLogManager.isInCombat(player)) {
            player.sendMessage(Component.text("Tu ne peux pas te téléporter en plein combat !", NamedTextColor.RED));
            return true;
        }
        homeManager.startTeleportHome(player);
        return true;
    }

    private boolean handleTpa(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut utiliser /tpa.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text("Utilisation : /tpa <joueur>", NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Joueur non connecté.", NamedTextColor.RED));
            return true;
        }
        teleportManager.sendRequest(player, target);
        return true;
    }

    private boolean handleTpAccept(CommandSender sender) {
        if (!(sender instanceof Player player)) return true;
        teleportManager.acceptRequest(player);
        return true;
    }

    private boolean handleTpDeny(CommandSender sender) {
        if (!(sender instanceof Player player)) return true;
        teleportManager.denyRequest(player);
        return true;
    }

    private boolean handleKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut réclamer un kit.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("test.kit.use")) {
            player.sendMessage(Component.text("Tu n'as pas la permission requise.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("starter")) {
            player.sendMessage(Component.text("Utilisation : /kit starter", NamedTextColor.RED));
            return true;
        }

        kitManager.giveStarterKit(player);
        return true;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.moderation.freeze")) {
            sender.sendMessage(Component.text("Tu n'as pas la permission requise.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Utilisation : /freeze <joueur>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Joueur introuvable ou hors-ligne.", NamedTextColor.RED));
            return true;
        }

        boolean nowFrozen = freezeManager.toggleFreeze(target);
        if (nowFrozen) {
            sender.sendMessage(Component.text(target.getName() + " a été gelé avec succès !", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text(target.getName() + " a été dégelé.", NamedTextColor.GREEN));
        }
        return true;
    }

    private boolean handleClearChat(CommandSender sender) {
        if (!sender.hasPermission("test.moderation.clearchat")) {
            sender.sendMessage(Component.text("Tu n'as pas la permission requise.", NamedTextColor.RED));
            return true;
        }

        Component emptyLine = Component.empty();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("test.moderation.clearchat")) {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage(emptyLine);
                }
            }
        }

        Component notice = Component.text("Le chat a été nettoyé par ", NamedTextColor.GOLD)
                .append(Component.text(sender.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(".", NamedTextColor.GOLD));

        Bukkit.broadcast(notice);
        return true;
    }

    private boolean handleSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut utiliser /spawn.", NamedTextColor.RED));
            return true;
        }
        if (combatLogManager != null && combatLogManager.isInCombat(player)) {
            player.sendMessage(Component.text("Tu ne peux pas te téléporter en plein combat !", NamedTextColor.RED));
            return true;
        }
        spawnManager.startTeleport(player);
        return true;
    }

    private boolean handleTrailsMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut ouvrir ce menu.", NamedTextColor.RED));
            return true;
        }
        TrailMenu.open(player);
        return true;
    }

    private boolean handleMod(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Seul un joueur peut ouvrir ce menu.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("test.moderation.mod")) {
            player.sendMessage(Component.text("Tu n'as pas la permission d'utiliser cette commande.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Utilisation : /mod <joueur>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Joueur introuvable ou hors-ligne.", NamedTextColor.RED));
            return true;
        }

        ModMenu.open(player, target, this);
        return true;
    }


    private boolean handleBroadcast(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.broadcast.admin")) {
            sender.sendMessage(Component.text("Cette commande est réservée aux administrateurs.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Utilisation : /broadcast <message>", NamedTextColor.RED));
            return true;
        }

        String rawMessage = String.join(" ", args);
        Component alert = Component.empty()
                .append(Component.newline())
                .append(Component.text("[ALERTE] ", NamedTextColor.DARK_RED, TextDecoration.BOLD))
                .append(Component.text(rawMessage, NamedTextColor.WHITE))
                .append(Component.newline());

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(alert);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.0f);
        }
        return true;
    }

    private boolean handleBienvenue(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.bienvenue")) {
            sender.sendMessage(Component.text("Tu n'as pas la permission requise.", NamedTextColor.RED));
            return true;
        }

        if (sender instanceof Player player && !player.hasPermission("test.bienvenue.bypass")) {
            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(id)) {
                long left = ((cooldowns.get(id) + (COOLDOWN_SECONDS * 1000)) - now) / 1000;
                if (left > 0) {
                    player.sendMessage(Component.text("Patiente encore " + left + " seconde(s).", NamedTextColor.RED));
                    return true;
                }
            }
            cooldowns.put(id, now);
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Utilisation : /bienvenue <joueur>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Le joueur '" + args[0] + "' n'est pas connecté.", NamedTextColor.RED));
            return true;
        }

        Component msg = Component.text("Bienvenue de la part de ", NamedTextColor.GOLD)
                .append(Component.text(sender.getName(), NamedTextColor.AQUA))
                .append(Component.text(" !", NamedTextColor.GOLD));

        target.sendMessage(msg);
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        sender.sendMessage(Component.text("Message envoyé à " + target.getName() + " !", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleTrail(CommandSender sender, String[] args) {
        if (!sender.hasPermission("test.trail.admin")) {
            sender.sendMessage(Component.text("Cette commande est réservée aux administrateurs.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Utilisation : /trail <joueur> <particule|stop>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Joueur non connecté.", NamedTextColor.RED));
            return true;
        }

        String action = args[1].toLowerCase();
        if (action.equals("stop")) {
            removeParticle(target);
            sender.sendMessage(Component.text("Particules retirées pour " + target.getName() + ".", NamedTextColor.YELLOW));
            return true;
        }

        applyParticle(target, action, true);
        sender.sendMessage(Component.text("Effet '" + action + "' appliqué sur " + target.getName() + ".", NamedTextColor.GREEN));
        return true;
    }

    // --- LOGIQUE DES PARTICULES ---

    public void applyParticle(Player target, String type) {
        applyParticle(target, type, true);
    }

    public void applyParticle(Player target, String type, boolean notify) {
        Particle particle = switch (type) {
            case "flamme" -> Particle.FLAME;
            case "etoile" -> Particle.FIREWORK;
            case "fumee"  -> Particle.CAMPFIRE_COSY_SMOKE;
            case "eclair" -> Particle.ELECTRIC_SPARK;
            default       -> Particle.HEART;
        };

        removeParticle(target);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!target.isOnline()) {
                if (activeTrails.containsKey(target.getUniqueId())) {
                    activeTrails.get(target.getUniqueId()).cancel();
                    activeTrails.remove(target.getUniqueId());
                }
                return;
            }
            Location loc = target.getLocation().add(0, 0.8, 0);
            target.getWorld().spawnParticle(particle, loc, 5, 0.3, 0.4, 0.3, 0.02);
        }, 0L, 10L);

        activeTrails.put(target.getUniqueId(), task);

        PlayerData data = dataManager.getPlayerData(target.getUniqueId());
        data.setActiveParticle(type);
        dataManager.save();

        if (notify) {
            target.sendMessage(Component.text("Particule active : " + type + " !", NamedTextColor.GREEN));
        }
    }

    public void removeParticle(Player target) {
        if (activeTrails.containsKey(target.getUniqueId())) {
            activeTrails.get(target.getUniqueId()).cancel();
            activeTrails.remove(target.getUniqueId());
            target.sendMessage(Component.text("Particules désactivées.", NamedTextColor.GRAY));
        }
        PlayerData data = dataManager.getPlayerData(target.getUniqueId());
        data.setActiveParticle(null);
        dataManager.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("spawn") || name.equals("trails") || name.equals("broadcast")
                || name.equals("clearchat") || name.equals("menu")
                || name.equals("sethome") || name.equals("home")
                || name.equals("kits") || name.equals("shop")
                || name.equals("jobs") || name.equals("quests")) {
            return Collections.emptyList();
        }

        if (name.equals("spawn") || name.equals("trails") || name.equals("broadcast")
                || name.equals("clearchat") || name.equals("menu")
                || name.equals("sethome") || name.equals("home")
                || name.equals("kits") || name.equals("shop") || name.equals("jobs")) {
            return Collections.emptyList();
        }
        if (name.equals("eco")) {
            if (args.length == 1) {
                return List.of("give", "take", "set");
            }
            if (args.length == 2) {
                return null; // Noms des joueurs connectés
            }
            if (args.length == 3) {
                return List.of("10", "100", "500", "1000");
            }
            return Collections.emptyList();
        }

        if (name.equals("money") && args.length == 1) {
            return null;
        }
        if (name.equals("pay") && args.length == 1) {
            return null;
        }

        if (name.equals("mute")) {
            if (args.length == 1) return null;
            if (args.length == 2) return List.of("5m", "15m", "30m", "1h", "1d");
            return Collections.emptyList();
        }
        if (name.equals("unmute")) {
            if (args.length == 1) return null;
            return Collections.emptyList();
        }

        if (name.equals("setrank")) {
            if (args.length == 1) {
                return null; // Propose les pseudos connectés
            }
            if (args.length == 2) {
                return List.of("JOUEUR", "VIP", "MODO", "ADMIN");
            }
            return Collections.emptyList();
        }

        if (name.equals("kit") && args.length == 1) {
            return List.of("starter");
        }

        if (name.equals("spawn") || name.equals("trails") || name.equals("broadcast")
                || name.equals("clearchat") || name.equals("menu")
                || name.equals("sethome") || name.equals("home") || name.equals("kits")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return null;
        }

        if (name.equals("trail") && args.length == 2) {
            List<String> matches = new ArrayList<>();
            for (String opt : PARTICLES_OPTIONS) {
                if (opt.startsWith(args[1].toLowerCase())) {
                    matches.add(opt);
                }
            }
            return matches;
        }

        return Collections.emptyList();
    }



    // --- GETTERS PUBLICS ---

    public QuestManager getQuestManager() {
        return questManager;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public CombatLogManager getCombatLogManager() {
        return combatLogManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}
