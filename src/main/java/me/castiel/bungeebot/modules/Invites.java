package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.types.Module;
import me.castiel.bungeebot.utils.DiscordUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

public class Invites extends Module {

    private final HashMap<String, Integer> invitesList;

    public Invites(DiscordApi api) {
        invitesList = new HashMap<>();
        api.getServerById(623315891051954217L).ifPresent(server -> server.getInvites().whenCompleteAsync((richInvites, throwable)
                -> richInvites.forEach(richInvite -> invitesList.put(richInvite.getCode(), richInvite.getUses()))));
        createInvitesListCommand(api);
        createInvitesListPanel(api);
        createJoinsLogger(api);
        createLeavesLogger(api);
    }

    private void createInvitesResetCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (slashCommandInteraction.getOptionByName("list").isEmpty()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("invites")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("invites", user))
                return;
            ;
            InteractionOriginalResponseUpdater updater = slashCommandInteraction.respondLater().join();
            User target = slashCommandInteraction.getArguments().size() == 0 ? user : slashCommandInteraction.getArguments().get(0).getUserValue().orElse(user);
        });
    }

    private void createInvitesListCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (slashCommandInteraction.getOptionByName("list").isEmpty()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("invites")
                    || user.isBot())
                return;
            InteractionOriginalResponseUpdater updater = slashCommandInteraction.respondLater().join();
            User target = slashCommandInteraction.getArguments().size() == 0 ? user : slashCommandInteraction.getArguments().get(0).getUserValue().orElse(user);
            getMySQL().getInviter(target.getIdAsString()).whenCompleteAsync((sqlInviter, throwable) -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setAuthor(target)
                        .setThumbnail(target.getAvatar(2048))
                        .setUrl("https://tea-mc.com")
                        .setTitle(target.getName() + "'s Invites (" + (sqlInviter.getInvitedNames().size()) + ")")
                        .setFooter("Invites: " + (sqlInviter.getInvitedNames().size() - sqlInviter.getExtraInvites()) + ", Extra Invites: " + sqlInviter.getExtraInvites() + ", Page: 1.");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 9; i++) {
                    if (sqlInviter.getInvitedNames().size() <= i)
                        break;
                    stringBuilder.append("<@").append(sqlInviter.getInvitedIDS().get(i)).append("> | <t:").append(sqlInviter.getInvitedJoinTimestamps().get(i)).append(":F>").append("\n");
                }

                embedBuilder.setDescription(stringBuilder.toString());

                updater.addEmbed(embedBuilder);
                if (!sqlInviter.getInvitedNames().isEmpty())
                    updater.addComponents(ActionRow.of(Button.secondary("page-2-" + target.getIdAsString(), "Next Page")));
                updater.update();
            });
        });
    }

    private void createInvitesListPanel(DiscordApi api) {
        api.addButtonClickListener(event -> {
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            User user = buttonInteraction.getUser();
            if (user.isBot())
                return;
            String customId = buttonInteraction.getCustomId();
            Message message = buttonInteraction.getMessage();
            if (customId.startsWith("page-")) {
                buttonInteraction.acknowledge();

                message.createUpdater()
                        .removeAllComponents()
                        .applyChanges().join();

                String[] split = customId.split("-");
                int page = Integer.parseInt(split[1]);
                api.getUserById(split[2]).whenCompleteAsync((target, exception) -> getMySQL().getInviter(target.getIdAsString()).whenCompleteAsync((sqlInviter, throwable) -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setColor(Color.ORANGE)
                            .setAuthor(target)
                            .setThumbnail(target.getAvatar(2048))
                            .setUrl("https://tea-mc.com")
                            .setTitle(target.getName() + "'s Invites (" + (sqlInviter.getInvitedNames().size()) + ")")
                            .setFooter("Invites: " + (sqlInviter.getInvitedNames().size() - sqlInviter.getExtraInvites()) + ", Extra Invites: " + sqlInviter.getExtraInvites() + ", Page: " + page + ".");
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = (page - 1) * 9; i < ((page - 1) * 9) + 9; i++) {
                        if (sqlInviter.getInvitedNames().size() <= i)
                            break;
                        stringBuilder.append("<@").append(sqlInviter.getInvitedIDS().get(i)).append("> | <t:").append(sqlInviter.getInvitedJoinTimestamps().get(i)).append(":F>").append("\n");
                    }

                    embedBuilder.setDescription(stringBuilder.toString());

                    MessageUpdater messageUpdater = message.createUpdater()
                            .removeAllComponents()
                            .removeAllEmbeds()
                            .addEmbed(embedBuilder);

                    if (sqlInviter.getInvitedNames().size() > 9 && page > 1)
                        messageUpdater.addActionRow(Button.danger("page-" + (page - 1) + "-" + target.getIdAsString(), "Previous Page"));
                    if (sqlInviter.getInvitedNames().size() > page * 9)
                        messageUpdater.addActionRow(Button.secondary("page-" + (page + 1) + "-" + target.getIdAsString(), "Next Page"));

                    messageUpdater.applyChanges();
                }));
            }
        });
    }

    private void createJoinsLogger(DiscordApi api) {
        api.addServerMemberJoinListener(event -> {
            User user = event.getUser();
            Server server = event.getServer();
            if (server.getId() != 623315891051954217L)
                return;
            server.getInvites().whenCompleteAsync((richInvites, throwable) -> {
                for (RichInvite richInvite : richInvites) {
                    int uses = invitesList.getOrDefault(richInvite.getCode(), 0);
                    if (richInvite.getUses() <= uses)
                        continue;
                    server.getTextChannelById(862688383574343710L).ifPresent(serverTextChannel -> {
                        Optional<User> optionalInviter = richInvite.getInviter();
                        if (optionalInviter.isPresent()) {
                            User inviter = optionalInviter.get();
                            getMySQL().getInviter(inviter.getIdAsString()).whenCompleteAsync((sqlInviter, exception) -> {
                                serverTextChannel
                                        .sendMessage(user.getMentionTag() + " just joined. They were invited by **"
                                                + inviter.getName() + " (" + (sqlInviter.getInvitedIDS().size() + 1 + sqlInviter.getExtraInvites()) + ").**");
                                sqlInviter.getInvitedNames().add(user.getName());
                                sqlInviter.getInvitedIDS().add(user.getIdAsString());
                                sqlInviter.getInvitedJoinTimestamps().add(String.valueOf(Instant.now().getEpochSecond()));
                                getMySQL().insertInviter(sqlInviter);

                                getMySQL().getInviter(user.getIdAsString()).whenCompleteAsync((sqlUser, ex) -> {
                                    sqlUser.setInvitedBy(inviter.getIdAsString());
                                    getMySQL().insertInviter(sqlUser);
                                });
                            });
                        } else {
                            serverTextChannel.sendMessage(user.getMentionTag() + " just joined.");
                        }
                    });
                    return;
                }
            });
        });
    }

    private void createLeavesLogger(DiscordApi api) {
        api.addServerMemberLeaveListener(event -> {
            User user = event.getUser();
            Server server = event.getServer();
            if (server.getId() != 623315891051954217L)
                return;
            getMySQL().getInviter(user.getIdAsString()).whenCompleteAsync((sqlUser, exception) -> {
                if (sqlUser.getInvitedBy().length() > 0)
                    getMySQL().getInviter(sqlUser.getInvitedBy()).whenCompleteAsync((sqlInviter, throwable) -> {
                        int index = sqlInviter.getInvitedIDS().indexOf(sqlUser.getId());
                        if (index != -1) {
                            sqlInviter.getInvitedNames().remove(index);
                            sqlInviter.getInvitedIDS().remove(index);
                            sqlInviter.getInvitedJoinTimestamps().remove(index);
                            getMySQL().insertInviter(sqlInviter);
                        }
                    });
            });
        });
    }
}