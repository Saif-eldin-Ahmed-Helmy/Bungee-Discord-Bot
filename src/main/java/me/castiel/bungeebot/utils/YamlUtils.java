package me.castiel.bungeebot.utils;

import me.castiel.bungeebot.BungeeBot;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import java.io.*;

public final class YamlUtils {

    public static Configuration loadConfig(String name) {
        File folder = BungeeBot.getInstance().getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File configFile = new File(folder, name);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                InputStream in = BungeeBot.getInstance().getResourceAsStream(name);
                OutputStream out = new FileOutputStream(configFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
