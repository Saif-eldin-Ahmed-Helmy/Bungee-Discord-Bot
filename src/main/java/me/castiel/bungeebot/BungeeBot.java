package me.castiel.bungeebot;

import me.castiel.bungeebot.configs.Settings;
import me.castiel.bungeebot.modules.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;

public class BungeeBot extends Plugin {

    private static BungeeBot instance;
    public static BungeeBot getInstance() {
        return instance;
    }

    private DiscordApi api;
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public void onEnable() {
        instance = this;
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            Settings settings = new Settings();
            getLogger().info("Logging in...");
            api = new DiscordApiBuilder()
                    .setToken(settings.token)
                    .setIntents
                            (Intent.DIRECT_MESSAGE_REACTIONS, Intent.DIRECT_MESSAGE_TYPING, Intent.DIRECT_MESSAGES,
                                    Intent.GUILD_INTEGRATIONS, Intent.GUILD_BANS, Intent.GUILD_INVITES,
                                    Intent.GUILD_EMOJIS, Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGE_REACTIONS,
                                    Intent.GUILD_MESSAGE_TYPING, Intent.GUILD_MESSAGES, Intent.GUILD_PRESENCES,
                                    Intent.GUILD_VOICE_STATES, Intent.GUILD_WEBHOOKS, Intent.GUILDS)
                    .login().join();
            getLogger().info("The bot is connected!");

            AutoHelper autoHelper = new AutoHelper(settings, api);
            AutoMod autoMod = new AutoMod(settings, api);
            Captcha captcha = new Captcha(settings, api);
            Coupons coupons = new Coupons(settings, api);
            Tickets tickets = new Tickets(settings, api);
        });
        getLogger().info("Plugin loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disconnecting...");
        api.disconnect().join();
        getLogger().info("The bot has disconnected!");
    }
}
