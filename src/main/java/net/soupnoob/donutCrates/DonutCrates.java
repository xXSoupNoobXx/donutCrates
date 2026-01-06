package net.soupnoob.donutCrates;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public final class DonutCrates extends JavaPlugin {
    public static DonutCrates instance;
    public ConfigManager cfg;
    public CrateManager crateMgr;
    public DataManager dataMgr;
    public GUIManager guiMgr;
    public final Map<UUID, String> pendingCrate;
    public final Map<UUID, Integer> pendingSlot;
    public final Map<UUID, String> pendingEditorCrate;
    public final Map<UUID, String> pendingEditorItemKey;
    public final Map<UUID, Boolean> pendingEditorIsLore;
    public final Map<UUID, Boolean> pendingEditorIsRename;

    public DonutCrates() {
        super();
        this.pendingCrate = new HashMap();
        this.pendingSlot = new HashMap();
        this.pendingEditorCrate = new HashMap();
        this.pendingEditorItemKey = new HashMap();
        this.pendingEditorIsLore = new HashMap();
        this.pendingEditorIsRename = new HashMap();
    }

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.cfg = new ConfigManager(this);
        this.crateMgr = new CrateManager(this);
        this.dataMgr = new DataManager(this);
        this.guiMgr = new GUIManager(this);
        DonutCrateCommand executor = new DonutCrateCommand(this);
        this.getCommand("donutcrates").setExecutor(executor);
        this.getCommand("donutcrates").setTabCompleter(executor);
        KeysCommand executor2 = new KeysCommand(this);
        this.getCommand("keys").setExecutor(executor2);
        this.getCommand("keys").setTabCompleter(executor2);
        this.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        this.getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            (new DonutCratesPlaceholder()).register();
        }
        /*
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⢰⣧⣼⣯⠄⣸⣠⣶⣶⣦⣾⠄⠄⠄⠄⡀⠄⢀⣿⣿⠄⠄⠄⢸⡇⠄⠄");
        getLogger().info("⠄⠄⠄⣾⣿⠿⠿⠶⠿⢿⣿⣿⣿⣿⣦⣤⣄⢀⡅⢠⣾⣛⡉⠄⠄⠄⠸⢀⣿⠄");
        getLogger().info("⠄⠄⢀⡋⣡⣴⣶⣶⡀⠄⠄⠙⢿⣿⣿⣿⣿⣿⣴⣿⣿⣿⢃⣤⣄⣀⣥⣿⣿⠄");
        getLogger().info("⠄⠄⢸⣇⠻⣿⣿⣿⣧⣀⢀⣠⡌⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠿⠿⣿⣿⣿⠄");
        getLogger().info("⠄⢀⢸⣿⣷⣤⣤⣤⣬⣙⣛⢿⣿⣿⣿⣿⣿⣿⡿⣿⣿⡍⠄⠄⢀⣤⣄⠉⠋⣰");
        getLogger().info("⠄⣼⣖⣿⣿⣿⣿⣿⣿⣿⣿⣿⢿⣿⣿⣿⣿⣿⢇⣿⣿⡷⠶⠶⢿⣿⣿⠇⢀⣤");
        getLogger().info("⠘⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣽⣿⣿⣿⡇⣿⣿⣿⣿⣿⣿⣷⣶⣥⣴⣿⡗");
        getLogger().info("⢀⠈⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡟⠄");
        getLogger().info("⢸⣿⣦⣌⣛⣻⣿⣿⣧⠙⠛⠛⡭⠅⠒⠦⠭⣭⡻⣿⣿⣿⣿⣿⣿⣿⣿⡿⠃⠄");
        getLogger().info("⠘⣿⣿⣿⣿⣿⣿⣿⣿⡆⠄⠄⠄⠄⠄⠄⠄⠄⠹⠈⢋⣽⣿⣿⣿⣿⣵⣾⠃⠄");
        getLogger().info("⠄⠘⣿⣿⣿⣿⣿⣿⣿⣿⠄⣴⣿⣶⣄⠄⣴⣶⠄⢀⣾⣿⣿⣿⣿⣿⣿⠃⠄⠄");
        getLogger().info("⠄⠄⠈⠻⣿⣿⣿⣿⣿⣿⡄⢻⣿⣿⣿⠄⣿⣿⡀⣾⣿⣿⣿⣿⣛⠛⠁⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠈⠛⢿⣿⣿⣿⠁⠞⢿⣿⣿⡄⢿⣿⡇⣸⣿⣿⠿⠛⠁⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠉⠻⣿⣿⣾⣦⡙⠻⣷⣾⣿⠃⠿⠋⠁⠄⠄⠄⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣝⡿⣿⣿⡆⣿⡿⠃⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");
*/
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣀⣠⣤⣶⣶⣶⣤⣄⣀⣀⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⣀⣤⣤⣶⣿⣿⣿⣿⣿⣿⣿⣟⢿⣿⣿⣿⣶⣤⡀⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⢀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣜⠿⠿⣿⣿⣧⢓");
        getLogger().info("⠄⠄⠄⠄⠄⡠⢛⣿⣿⣿⡟⣿⣿⣽⣋⠻⢻⣿⣿⣿⣿⡻⣧⡠⣭⣭⣿⡧");
        getLogger().info("⠄⠄⠄⠄⠄⢠⣿⡟⣿⢻⠃⣻⣨⣻⠿⡀⣝⡿⣿⣿⣷⣜⣜⢿⣝⡿⡻⢔");
        getLogger().info("⠄⠄⠄⠄⠄⢸⡟⣷⢿⢈⣚⣓⡡⣻⣿⣶⣬⣛⣓⣉⡻⢿⣎⠢⠻⣴⡾⠫");
        getLogger().info("⠄⠄⠄⠄⠄⢸⠃⢹⡼⢸⣿⣿⣿⣦⣹⣿⣿⣿⠿⠿⠿⠷⣎⡼⠆⣿⠵⣫");
        getLogger().info("⠄⠄⠄⠄⠄⠈⠄⠸⡟⡜⣩⡄⠄⣿⣿⣿⣿⣶⢀⢀⣿⣷⣿⣿⡐⡇⡄⣿");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠁⢶⢻⣧⣖⣿⣿⣿⣿⣿⣿⣿⣿⡏⣿⣇⡟⣇⣷⣿");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⢸⣆⣤⣽⣿⡿⠿⠿⣿⣿⣦⣴⡇⣿⢨⣾⣿⢹⢸");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⢸⣿⠊⡛⢿⣿⣿⣿⣿⡿⣫⢱⢺⡇⡏⣿⣿⣸⡼");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⢸⡿⠄⣿⣷⣾⡍⣭⣶⣿⣿⡌⣼⣹⢱⠹⣿⣇⣧");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⣼⠁⣤⣭⣭⡌⢁⣼⣿⣿⣿⢹⡇⣭⣤⣶⣤⡝⡼");
        getLogger().info("⠄⣀⠤⡀⠄⠄⠄⠄⠄⡏⣈⡻⡿⠃⢀⣾⣿⣿⣿⡿⡼⠁⣿⣿⣿⡿⢷⢸");
        getLogger().info("⢰⣷⡧⡢⠄⠄⠄⠄⠠⢠⡛⠿⠄⠠⠬⠿⣿⠭⠭⢱⣇⣀⣭⡅⠶⣾⣷⣶");
        getLogger().info("⠈⢿⣿⣧⠄⠄⠄⠄⢀⡛⠿⠄⠄⠄⠄⢠⠃⠄⠄⡜⠄⠄⣤⢀⣶⣮⡍⣴");
        getLogger().info("⠄⠈⣿⣿⡀⠄⠄⠄⢩⣝⠃⠄⠄⢀⡄⡎⠄⠄⠄⠇⠄⠄⠅⣴⣶⣶⠄⣶");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");
/*
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣠⣤⣤⣤⣶⣶⣶⣦⣤⣄⡀⠄⠄⠄⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⢀⣴⣿⡿⠛⠉⠙⠛⠛⠛⠛⠻⢿⣿⣷⣤⡀⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⣼⣿⠋⠄⠄⠄⠄⠄⠄⠄⠄⢀⣀⣀⠈⢻⣿⣿⡄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⣸⣿⡏⠄⠄⠄⠄⣠⣶⣾⣿⣿⣿⠿⠿⠿⢿⣿⣿⣿⣄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⣿⣿⠁⠄⠄⠄⢰⣿⣿⣯⠁⠄⠄⠄⠄⠄⠄⠄⠈⠙⢿⣷⡄⠄");
        getLogger().info("⠄⠄⣀⣤⣴⣶⣶⣿⡟⠄⠄⠄⢸⣿⣿⣿⣆⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣿⣷⠄");
        getLogger().info("⠄⢰⣿⡟⠋⠉⣹⣿⡇⠄⠄⠄⠘⣿⣿⣿⣿⣷⣦⣤⣤⣤⣶⣶⣶⣶⣿⣿⣿⠄");
        getLogger().info("⠄⢸⣿⡇⠄⠄⣿⣿⡇⠄⠄⠄⠄⠹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠃⠄");
        getLogger().info("⠄⣸⣿⡇⠄⠄⣿⣿⡇⠄⠄⠄⠄⠄⠉⠻⠿⣿⣿⣿⣿⡿⠿⠿⠛⢻⣿⡇⠄⠄");
        getLogger().info("⠄⣿⣿⠁⠄⣿⣿⡇⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⢸⣿⣧⠄⠄");
        getLogger().info("⠄⣿⣿⠄⠄⠄⣿⣿⡇⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⢸⣿⣿⠄⠄");
        getLogger().info("⠄⣿⣿⠄⠄⠄⣿⣿⡇⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⢸⣿⣿⠄⠄");
        getLogger().info("⠄⢿⣿⡆⠄⠄⣿⣿⡇⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⢸⣿⡇⠄⠄");
        getLogger().info("⠄⠸⣿⣧⡀⠄⣿⣿⡇⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣿⣿⠃⠄⠄");
        getLogger().info("⠄⠄⠛⢿⣿⣿⣿⣿⣇⠄⠄⠄⠄⠄⣰⣿⣿⣷⣶⣶⣶⣶⠶⢠⣿⣿⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⣿⣿⠄⠄⠄⠄⠄⣿⣿⡇⠀⣽⣿⡏⠁⠄⠄⢸⣿⡇⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⣿⣿⠄⠄⠄⠄⠄⣿⣿⡇⠀⢹⣿⡆⠄⠄⠄⣸⣿⠇⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⢿⣿⣦⣄⣀⣠⣴⣿⣿⠁⠄⠈⠻⣿⣿⣿⣿⡿⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠈⠛⠻⠿⠿⠿⠿⠋⠁⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");
        getLogger().info("⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄");

 */
    }

    public void onDisable() {
        this.dataMgr.saveAll();
        this.cfg.saveAll();
    }
}

