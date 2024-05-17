package me.refracdevelopment.simpletags;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import me.refracdevelopment.simpletags.commands.*;
import me.refracdevelopment.simpletags.hooks.ItemsAdderListener;
import me.refracdevelopment.simpletags.listeners.MenuListener;
import me.refracdevelopment.simpletags.listeners.PlayerListener;
import me.refracdevelopment.simpletags.manager.CommandManager;
import me.refracdevelopment.simpletags.manager.MenuManager;
import me.refracdevelopment.simpletags.manager.ProfileManager;
import me.refracdevelopment.simpletags.manager.TagManager;
import me.refracdevelopment.simpletags.manager.configuration.ConfigFile;
import me.refracdevelopment.simpletags.manager.configuration.cache.Commands;
import me.refracdevelopment.simpletags.manager.configuration.cache.Config;
import me.refracdevelopment.simpletags.manager.configuration.cache.Menus;
import me.refracdevelopment.simpletags.manager.configuration.cache.Tags;
import me.refracdevelopment.simpletags.manager.data.DataType;
import me.refracdevelopment.simpletags.manager.data.MySQLManager;
import me.refracdevelopment.simpletags.manager.data.SQLiteManager;
import me.refracdevelopment.simpletags.menu.TagsMenu;
import me.refracdevelopment.simpletags.utilities.DownloadUtil;
import me.refracdevelopment.simpletags.utilities.Tasks;
import me.refracdevelopment.simpletags.utilities.chat.PAPIExpansion;
import me.refracdevelopment.simpletags.utilities.chat.RyMessageUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.adventure.MorePaperLibAdventure;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

@Getter
public final class SimpleTags extends JavaPlugin {

    @Getter
    private static SimpleTags instance;

    // Managers
    private DataType dataType;
    private MySQLManager mySQLManager;
    private SQLiteManager sqLiteManager;
    private ProfileManager profileManager;
    private TagManager tagManager;
    private MenuManager menuManager;
    private CommandManager commandManager;

    // Files
    private ConfigFile configFile;
    private ConfigFile tagsFile;
    private ConfigFile menusFile;
    private ConfigFile commandsFile;
    private ConfigFile localeFile;

    // Cache
    private Config settings;
    private Tags tags;
    private Menus menus;
    private Commands commands;

    // Utilities
    private MorePaperLib paperLib;
    private MorePaperLibAdventure paperLibAdventure;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        DownloadUtil.downloadAndEnable();

        new Metrics(this, 13205);

        paperLib = new MorePaperLib(this);
        paperLibAdventure = new MorePaperLibAdventure(paperLib);

        loadFiles();

        RyMessageUtils.sendConsole(false,
                "<#A020F0> _____ _           _     _____               " + "Running <#7D0DC3>v" + getDescription().getVersion(),
                "<#A020F0>|   __|_|_____ ___| |___|_   _| __ ___ ___   " + "Server <#7D0DC3>" + getServer().getName() + " <#A020F0>v" + getServer().getVersion(),
                "<#A020F0>|__   | |     | . | | -_| | |  |. | . |_ -|  " + "Discord support: <#7D0DC3>" + getDescription().getWebsite(),
                "<#7D0DC3>|_____|_|_|_|_|  _|_|___| |_| |___|_  |___|  " + "Thanks for using my plugin ❤ !",
                "<#7D0DC3>              |_|                 |___|    ",
                "        <#A020F0>Developed by <#7D0DC3>RefracDevelopment",
                ""
        );

        loadManagers();
        loadCommands();
        loadListeners();
        loadHooks();

        // Paper is recommended but not required
        PaperLib.suggestPaper(this);

        Tasks.runAsync(this::updateCheck);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (Objects.requireNonNull(dataType) == DataType.MYSQL)
            getMySQLManager().shutdown();
        else if (Objects.requireNonNull(dataType) == DataType.SQLITE)
            getSqLiteManager().shutdown();

        paperLib.scheduling().cancelGlobalTasks();
    }

    public void loadFiles() {
        // Files
        configFile = new ConfigFile("config.yml", true);
        tagsFile = new ConfigFile("tags.yml", false);
        menusFile = new ConfigFile("menus.yml", false);
        commandsFile = new ConfigFile("commands/tags.yml", true);
        localeFile = new ConfigFile("locale/" + getConfigFile().getString("locale") + ".yml", true);

        // Cache
        settings = new Config();
        tags = new Tags();
        menus = new Menus();
        commands = new Commands();

        RyMessageUtils.sendConsole(true, "&aLoaded all files.");
    }

    private void loadManagers() {
        switch (getSettings().DATA_TYPE.toUpperCase()) {
            case "MARIADB":
            case "MYSQL":
                dataType = DataType.MYSQL;
                mySQLManager = new MySQLManager();
                break;
            default:
                dataType = DataType.SQLITE;
                sqLiteManager = new SQLiteManager(getDataFolder().getAbsolutePath() + File.separator + "tags.db");
                break;
        }

        profileManager = new ProfileManager();
        tagManager = new TagManager();
        menuManager = new MenuManager();
        commandManager = new CommandManager();

        // Loads all available tags
        if (getServer().getPluginManager().isPluginEnabled("ItemsAdder"))
            getServer().getPluginManager().registerEvents(new ItemsAdderListener(), this);
        else
            getTagManager().loadTags();

        RyMessageUtils.sendConsole(true, "&aLoaded managers.");
    }

    private void loadCommands() {
        try {
            getCommandManager().createCoreCommand(this, getCommands().TAGS_COMMAND_NAME,
                    getLocaleFile().getString("command-tags-description"),
                    "/" + getCommands().TAGS_COMMAND_NAME, (commandSender, list) -> {
                        // Make sure the sender is a player.
                        if (!(commandSender instanceof Player player)) {
                            RyMessageUtils.sendPluginMessage(commandSender, "no-console");
                            return;
                        }

                        if (getTagManager().getLoadedTags().stream().noneMatch(tag -> player.hasPermission("simpletags.tag." + tag.getConfigName()))) {
                            RyMessageUtils.sendPluginMessage(player, "no-available-tags");
                            return;
                        }

                        new TagsMenu(SimpleTags.getInstance().getMenuManager().getPlayerMenuUtility(player)).open();
                    },
                    getCommands().TAGS_COMMAND_ALIASES,
                    CreateCommand.class,
                    DeleteCommand.class,
                    EditCommand.class,
                    HelpCommand.class,
                    ListCommand.class,
                    ReloadCommand.class,
                    SetCommand.class,
                    VersionCommand.class
            );

            RyMessageUtils.sendConsole(true, "&aLoaded commands.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            RyMessageUtils.sendConsole(true, "&cFailed to load commands.");
            e.printStackTrace();
        }
    }

    private void loadListeners() {
        PluginManager pluginManager = this.getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new MenuListener(), this);

        RyMessageUtils.sendConsole(true, "&aLoaded listeners.");
    }

    private void loadHooks() {
        PluginManager pluginManager = getServer().getPluginManager();

        if (pluginManager.isPluginEnabled("Skulls")) {
            RyMessageUtils.sendConsole(true, "&aHooked into Skulls for heads support.");
        }

        if (pluginManager.isPluginEnabled("HeadDatabase")) {
            RyMessageUtils.sendConsole(true, "&aHooked into HeadDatabase for heads support.");
        }

        if (pluginManager.isPluginEnabled("ItemsAdder")) {
            RyMessageUtils.sendConsole(true, "&aHooked into ItemsAdder for custom items support.");
        }

        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new PAPIExpansion().register();
            RyMessageUtils.sendConsole(true, "&aHooked into PlaceholderAPI for placeholders.");
        }
    }

    public void updateCheck() {
        try {
            String urlString = "https://refracdev-updatecheck.refracdev.workers.dev/";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuilder response = new StringBuilder();
            while ((input = reader.readLine()) != null) {
                response.append(input);
            }
            reader.close();
            JsonObject object = new JsonParser().parse(response.toString()).getAsJsonObject();

            if (object.has("plugins")) {
                JsonObject plugins = object.get("plugins").getAsJsonObject();
                JsonObject info = plugins.get(getDescription().getName()).getAsJsonObject();
                String version = info.get("version").getAsString();
                boolean archived = info.get("archived").getAsBoolean();

                if (archived) {
                    RyMessageUtils.sendConsole(true, "&cThis plugin has been marked as &e&l'Archived' &cby RefracDevelopment.");
                    RyMessageUtils.sendConsole(true, "&cThis version will continue to work but will not receive updates or support.");
                } else if (version.equals(getDescription().getVersion())) {
                    RyMessageUtils.sendConsole(true, "&a" + getDescription().getName() + " is on the latest version.");
                } else {
                    RyMessageUtils.sendConsole(true, "&cYour " + getDescription().getName() + " version &7(" + getDescription().getVersion() + ") &cis out of date! Newest: &e&lv" + version);
                }
            } else {
                RyMessageUtils.sendConsole(true, "&cWrong response from update API, contact plugin developer!");
            }
        } catch (
                Exception ex) {
            RyMessageUtils.sendConsole(true, "&cFailed to get updater check. (" + ex.getMessage() + ")");
        }
    }
}
