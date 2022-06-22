package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.types.Module;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.awt.*;

public class WhoIs extends Module {

    public WhoIs(DiscordApi api) {
        createWhoIsCommand(api);
    }

    private void createWhoIsCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("whois")
                    || user.isBot())
                return;
            slashCommandInteraction.getServer().ifPresent(server -> {

                User target = slashCommandInteraction.getArguments().size() == 0 ? user : slashCommandInteraction.getArguments().get(0).getUserValue().orElse(user);

                StringBuilder stringBuilder = new StringBuilder();
                server.getRoles(target).forEach(role -> {
                    if (!role.isEveryoneRole())
                        stringBuilder.append(role.getMentionTag()).append(", ");
                });

                if (stringBuilder.length() > 0)
                    stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setAuthor(target)
                        .setThumbnail(target.getAvatar(2048))
                        .addField("User ID", target.getIdAsString(), true);


                getMySQL().getInviter(target.getIdAsString()).whenCompleteAsync((sqlInviter, throwable) -> {

                    if (sqlInviter.getInvitedBy().length() > 0)
                        embedBuilder.addField("Invited By", "<@" + sqlInviter.getInvitedBy() + ">");

                    target.getJoinedAtTimestamp(server).ifPresent(joinTimeStamp
                            -> embedBuilder.addField("Joined", "<t:" + joinTimeStamp.getEpochSecond() + ":F>"));

                    embedBuilder
                            .addField("Registered", "<t:" + target.getCreationTimestamp().getEpochSecond() + ":F>")
                            .addField("Roles (" + server.getRoles(target).size() + ")", stringBuilder.toString());

                    slashCommandInteraction.createImmediateResponder()
                            .addEmbed(embedBuilder)
                            .respond();
                });
            });
        });
    }
}
