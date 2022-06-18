package me.castiel.bungeebot.modules;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;

public class Avatar {

    public Avatar(DiscordApi api) {
        createAvatarCommand(api);
    }

    public void createAvatarCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("avatar") || user.isBot())
                return;
            User target = slashCommandInteraction.getArguments().size() == 0 ? user : slashCommandInteraction.getArguments().get(0).getUserValue().orElse(user);
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setImage(target.getAvatar(2048));
            slashCommandInteraction.createImmediateResponder()
                    .addEmbed(embedBuilder)
                    .respond();
        });
    }
}
