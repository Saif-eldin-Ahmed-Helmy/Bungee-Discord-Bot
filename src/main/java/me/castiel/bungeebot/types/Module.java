package me.castiel.bungeebot.types;

import me.castiel.bungeebot.BungeeBot;
import me.castiel.bungeebot.configs.Settings;
import me.castiel.bungeebot.database.MySQL;

import java.util.logging.Logger;

public class Module {

    public BungeeBot getInstance() {
        return BungeeBot.getInstance();
    }

    public Logger getLogger() {
        return getInstance().getLogger();
    }

    public Settings getSettings() {
        return getInstance().getSettings();
    }

    public MySQL getMySQL() {
        return getInstance().getMySQL();
    }
}
