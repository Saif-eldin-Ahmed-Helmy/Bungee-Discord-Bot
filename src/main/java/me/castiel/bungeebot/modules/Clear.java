package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.utils.DiscordUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;

public class Clear {

    public Clear(DiscordApi api) {
        createClearCommand(api);
    }

    public void createClearCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("clear") || user.isBot() || !DiscordUtils.hasPermission("clear", user))
                return;
            long amount = slashCommandInteraction.getArguments().get(0).getLongValue().orElse(0L);
            slashCommandInteraction.getChannel().ifPresent(textChannel -> {
               textChannel.bulkDelete(textChannel.getMessages(Math.toIntExact(amount)).join()).join();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription("Successfully cleared " + amount + " messages!");
                slashCommandInteraction.createImmediateResponder()
                        .addEmbed(embedBuilder)
                        .respond();
            });
        });
    }
}