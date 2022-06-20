package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.utils.CustomEmbedBuilder;
import me.castiel.bungeebot.utils.DiscordUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.ArrayList;
import java.util.List;

public class Embed {

    private final List<Long> toBeParsedList;

    public Embed(DiscordApi api) {
        toBeParsedList = new ArrayList<>();
        createEmbedParseCommand(api);
        createEmbedCreateCommand(api);
    }

    private void createEmbedParseCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getOptionByName("parse").isPresent()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("embed")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("embed", user))
                return;
            if (!toBeParsedList.contains(user.getId()))
                toBeParsedList.add(user.getId());
            slashCommandInteraction.createImmediateResponder()
                    .setContent("Type the text you want to parse.")
                    .respond();
        });
        api.addMessageCreateListener(event -> event.getMessageAuthor().asUser().ifPresent(user -> {
            if (!toBeParsedList.contains(user.getId()) || user.isBot())
                return;
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : event.getMessageContent().split("\n"))
                stringBuilder.append(s).append("\\\\n");
            event.getMessage().reply(stringBuilder.toString());
            toBeParsedList.remove(user.getId());
        }));
    }

    private void createEmbedCreateCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getOptionByName("create").isPresent()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("embed")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("embed", user))
                return;
            CustomEmbedBuilder customEmbedBuilder = new CustomEmbedBuilder();
            for (SlashCommandInteractionOption option : event.getSlashCommandInteraction().getArguments()) {
                option.getStringValue().ifPresent(s -> {
                    switch (option.getName()) {
                        case "color" -> customEmbedBuilder.setColor(s);
                        case "author" -> customEmbedBuilder.setAuthor(s);
                        case "author_url" -> customEmbedBuilder.setAuthorUrl(s);
                        case "author_image" -> customEmbedBuilder.setAuthorImage(s);
                        case "url" -> customEmbedBuilder.setUrl(s);
                        case "title" -> customEmbedBuilder.setTitle(s, true);
                        case "description" -> customEmbedBuilder.setDescription(s, true);
                        case "image" -> customEmbedBuilder.setImage(s);
                        case "thumbnail" -> customEmbedBuilder.setThumbnail(s);
                        case "footer" -> customEmbedBuilder.setFooter(s);
                        case "footer_image" -> customEmbedBuilder.setFooterImage(s);
                    }
                });
            }
            slashCommandInteraction.createImmediateResponder()
                    .addEmbed(customEmbedBuilder.buildEmbed())
                    .respond();
        });
    }
}
