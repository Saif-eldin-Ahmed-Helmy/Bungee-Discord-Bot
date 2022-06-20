package me.castiel.bungeebot;

import me.castiel.bungeebot.configs.Settings;
import me.castiel.bungeebot.database.MySQL;
import me.castiel.bungeebot.modules.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class BungeeBot extends Plugin {

    private static BungeeBot instance;
    public static BungeeBot getInstance() {
        return instance;
    }

    private Settings settings;
    public Settings getSettings() {
        return settings;
    }

    private MySQL mySQL;
    public MySQL getMySQL() {
        return mySQL;
    }

    private DiscordApi api;
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public void onEnable() {
        instance = this;
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            settings = new Settings();

            getLogger().info("Connecting to MySQL Database...");

            mySQL = new MySQL(System.getenv("BUNGEE_DB_HOST"), System.getenv("BUNGEE_DB_PORT"), System.getenv("BUNGEE_DB_USER"), System.getenv("BUNGEE_DB_PASSWORD"), System.getenv("BUNGEE_DB_NAME"));

            getLogger().info("Logging in...");

            api = new DiscordApiBuilder()
                    .setToken(settings.getToken())
                    .setIntents
                            (Intent.DIRECT_MESSAGE_REACTIONS, Intent.DIRECT_MESSAGE_TYPING, Intent.DIRECT_MESSAGES,
                                    Intent.GUILD_INTEGRATIONS, Intent.GUILD_BANS, Intent.GUILD_INVITES,
                                    Intent.GUILD_EMOJIS, Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGE_REACTIONS,
                                    Intent.GUILD_MESSAGE_TYPING, Intent.GUILD_MESSAGES, Intent.GUILD_PRESENCES,
                                    Intent.GUILD_VOICE_STATES, Intent.GUILD_WEBHOOKS, Intent.GUILDS)
                    .login().join();

            getLogger().info("The bot is connected!");

            ProxyServer.getInstance().getScheduler().schedule(this,
                    () -> api.updateActivity(ActivityType.WATCHING, ProxyServer.getInstance().getPlayers().size() + " Players"), 30L, 30L, TimeUnit.SECONDS);

            api.bulkOverwriteGlobalApplicationCommands(Arrays.asList(
                            new SlashCommandBuilder().setName("ticket").setDescription("Tickets Commands")
                                    .setOptions(Arrays.asList(
                                            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create", "Create a tickets panel",
                                                    Collections.singletonList(
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "type", "Type of ticket panel", true)
                                                    )),
                                            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "add", "Add user to the ticket",
                                                    Collections.singletonList(
                                                            SlashCommandOption.create(SlashCommandOptionType.USER, "user", "The user you want to add", true)
                                                    )),
                                            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "remove", "Remove user from the ticket",
                                                    Collections.singletonList(
                                                            SlashCommandOption.create(SlashCommandOptionType.USER, "user", "The user you want to remove", true)
                                                    )),
                                            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "transcript", "Generate ticket transcript",
                                                    Collections.singletonList(
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "ticket-id", "The id of the ticket", true)
                                                    )))),
                            new SlashCommandBuilder().setName("coupons").setDescription("Coupons commands")
                                    .setOptions(Collections.singletonList(
                                            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create", "Create a store coupon",
                                                    Arrays.asList(
                                                            SlashCommandOption.create(SlashCommandOptionType.LONG, "amount", "The coupon amount", true),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "reason", "Reason you created the coupon", true)
                                                    )))),
                            new SlashCommandBuilder().setName("embed").setDescription("Embed commands")
                                    .setOptions(Arrays.asList(
                                            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "parse", "Parse multi-line text"),
                                            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create", "Create an embed",
                                                    Arrays.asList(
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "color", "The color of the embed (e.x. WHITE)", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "author", "The author of the embed", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "author_url", "The author URL", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "author_image", "The image of the author", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "url", "The embed url", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "title", "The embed title", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "description", "The embed description", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "image", "The embed image", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "thumbnail", "The embed thumbnail", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "footer", "The embed footer", false),
                                                            SlashCommandOption.create(SlashCommandOptionType.STRING, "footer_image", "The embed footer image", false)
                                                    )))),
                            new SlashCommandBuilder().setName("avatar").setDescription("View the avatar of a user")
                                    .setOptions(
                                            Collections.singletonList(
                                                    SlashCommandOption.create(SlashCommandOptionType.USER, "user", "The discord user", false)
                                            )),
                            new SlashCommandBuilder().setName("clear").setDescription("Delete a certain amount of messages in the channel")
                                    .setOptions(
                                            Collections.singletonList(
                                                    SlashCommandOption.create(SlashCommandOptionType.LONG, "amount", "The amount of messages you want to delete", true)
                                            )),
                            new SlashCommandBuilder().setName("captcha").setDescription("Captcha commands")
                                    .setOptions(Collections.singletonList(
                                            SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "create", "Create a captcha panel")
                                    ))))
                    .join();

            getLogger().info("Registered all slash commands!");

            AutoHelper autoHelper = new AutoHelper(api);
            AutoMod autoMod = new AutoMod(api);
            Avatar avatar = new Avatar(api);
            Captcha captcha = new Captcha(api);
            Clear clear = new Clear(api);
            Coupons coupons = new Coupons(api);
            Embed embed = new Embed(api);
            ExtraLogs extraLogs = new ExtraLogs(api);
            Tickets tickets = new Tickets(api);
        });
        getLogger().info("Plugin loaded!");
    }

    @Override
    public void onDisable() {
        if (mySQL != null) {
            getLogger().info("Closing database connection...");
            try {
                mySQL.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            getLogger().info("MySQL connection has been closed.");
        }
        if (api != null) {
            getLogger().info("Disconnecting the discord bot...");
            api.disconnect().join();
            getLogger().info("The bot has disconnected.");
        }
    }
}