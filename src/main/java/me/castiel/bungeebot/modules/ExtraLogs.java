package me.castiel.bungeebot.modules;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.time.Instant;

public class ExtraLogs {

    public ExtraLogs(DiscordApi api) {
        createServerJoinsLogger(api);
        createServerLeavesLogger(api);
        createDiscriminatorLogger(api);
        createNameLogger(api);
        createAvatarLogger(api);
        createMessagesEditLogger(api);
        createMessagesDeleteLogger(api);
    }

    private void createServerJoinsLogger(DiscordApi api) {
        api.addServerMemberJoinListener(event -> {
            Server server = event.getServer();
            if (server.getId() != 623315891051954217L)
                return;
            server.getTextChannelById(652263585363525638L).ifPresent(serverTextChannel -> {
                User user = event.getUser();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setAuthor(user)
                        .setTitle("<t:" + Instant.now().getEpochSecond() + ":f>")
                        .setDescription(user.getMentionTag() + " **has joined the server**")
                        .setFooter("User ID: " + user.getIdAsString());
                serverTextChannel.sendMessage(embedBuilder);
            });
        });
    }

    private void createServerLeavesLogger(DiscordApi api) {
        api.addServerMemberLeaveListener(event -> {
            Server server = event.getServer();
            if (server.getId() != 623315891051954217L)
                return;
            server.getTextChannelById(652263585363525638L).ifPresent(serverTextChannel -> {
                User user = event.getUser();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setAuthor(user)
                        .setTitle("<t:" + Instant.now().getEpochSecond() + ":f>")
                        .setDescription(user.getMentionTag() + " **has left the server**")
                        .setFooter("User ID: " + user.getIdAsString());
                serverTextChannel.sendMessage(embedBuilder);
            });
        });
    }

    private void createDiscriminatorLogger(DiscordApi api) {
        api.addUserChangeDiscriminatorListener(event -> {
            User user = event.getUser();
            if (user.isBot())
                return;
            api.getServerById(623315891051954217L).flatMap(server -> server.getTextChannelById(652263585363525638L)).ifPresent(serverTextChannel -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.YELLOW)
                        .setAuthor(user)
                        .setTitle("Discriminator Change")
                        .setDescription(user.getMentionTag() + " has changed their discriminator")
                        .addField("Old Discriminator", event.getOldDiscriminator())
                        .addField("New Discriminator", event.getNewDiscriminator());
                serverTextChannel.sendMessage(embedBuilder);
            });
        });
    }

    private void createNameLogger(DiscordApi api) {
        api.addUserChangeNameListener(event -> {
            User user = event.getUser();
            if (user.isBot())
                return;
            api.getServerById(623315891051954217L).flatMap(server -> server.getTextChannelById(652263585363525638L)).ifPresent(serverTextChannel -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.WHITE)
                        .setAuthor(user)
                        .setTitle("Name Change")
                        .setDescription(user.getMentionTag() + " has changed their name")
                        .addField("Old Name", event.getOldName())
                        .addField("New Name", event.getNewName());
                serverTextChannel.sendMessage(embedBuilder);
            });
        });
        api.addUserChangeNicknameListener(event -> {
            User user = event.getUser();
            Server server = event.getServer();
            if (user.isBot() || server.getId() != 623315891051954217L)
                return;
            server.getTextChannelById(652263585363525638L).ifPresent(serverTextChannel -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.WHITE)
                        .setAuthor(user)
                        .setTitle("Nickname Change")
                        .setDescription(user.getMentionTag() + " has changed their nickname");
                event.getOldNickname().ifPresent(oldNickName -> embedBuilder.addField("Old Nickname", oldNickName));
                event.getNewNickname().ifPresent(newNickName -> embedBuilder.addField("New Nickname", newNickName));
                serverTextChannel.sendMessage(embedBuilder);
            });
        });
    }

    private void createAvatarLogger(DiscordApi api) {
        api.addUserChangeAvatarListener(event -> {
            User user = event.getUser();
            if (user.isBot())
                return;
            api.getServerById(623315891051954217L).flatMap(server -> server.getTextChannelById(652263585363525638L)).ifPresent(serverTextChannel -> {
                EmbedBuilder mainEmbed = new EmbedBuilder()
                        .setColor(Color.WHITE)
                        .setAuthor(user)
                        .setTitle("Avatar Change")
                        .setDescription(user.getMentionTag() + " has changed their avatar");
                EmbedBuilder oldAvatarEmbed = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Old Avatar")
                        .setImage(event.getOldAvatar());
                EmbedBuilder newAvatarEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("New Avatar")
                        .setImage(event.getNewAvatar());
                new MessageBuilder()
                        .addEmbeds(mainEmbed, oldAvatarEmbed, newAvatarEmbed)
                        .send(serverTextChannel);
            });
        });
        api.addUserChangeServerAvatarListener(event -> {
            User user = event.getUser();
            if (user.isBot())
                return;
            api.getServerById(623315891051954217L).flatMap(server -> server.getTextChannelById(652263585363525638L)).ifPresent(serverTextChannel -> {
                MessageBuilder messageBuilder = new MessageBuilder();
                EmbedBuilder mainEmbed = new EmbedBuilder()
                        .setColor(Color.WHITE)
                        .setAuthor(user)
                        .setTitle("Server Avatar Change")
                        .setDescription(user.getMentionTag() + " has changed their server avatar");
                messageBuilder.addEmbed(mainEmbed);
                event.getOldServerAvatar(2048).ifPresent(oldServerAvatar -> {
                    EmbedBuilder oldAvatarEmbed = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Old Avatar")
                            .setImage(oldServerAvatar);
                    messageBuilder.addEmbed(oldAvatarEmbed);
                });
                event.getNewServerAvatar(2048).ifPresent(newServerAvatar -> {
                    EmbedBuilder newAvatarEmbed = new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle("New Avatar")
                            .setImage(newServerAvatar);
                    messageBuilder.addEmbed(newAvatarEmbed);
                });
                messageBuilder.send(serverTextChannel);
            });
        });
    }

    private void createMessagesEditLogger(DiscordApi api) {
        api.addMessageEditListener(event -> event.getServer().ifPresent(server -> event.getServerTextChannel()
                .ifPresent(serverTextChannel -> event.getMessage().ifPresent(message -> event.getOldContent()
                        .ifPresent(oldContent -> event.getMessageAuthor().ifPresent(messageAuthor -> {
                            if (!messageAuthor.isRegularUser())
                                return;
                            EmbedBuilder mainEmbed = new EmbedBuilder()
                                    .setColor(Color.WHITE)
                                    .setAuthor(messageAuthor)
                                    .setUrl("https://discord.com/channels/" + server.getIdAsString() + "/" + serverTextChannel.getIdAsString() + "/" + message.getIdAsString())
                                    .setTitle("Click here to view the message.")
                                    .setDescription(messageAuthor.getName() + " has edited their message.");
                            EmbedBuilder oldContentEmbed = new EmbedBuilder()
                                    .setColor(Color.YELLOW)
                                    .setTitle("Old Content")
                                    .setDescription(oldContent);
                            EmbedBuilder newContentEmbed = new EmbedBuilder()
                                    .setColor(Color.GREEN)
                                    .setTitle("New Content")
                                    .setDescription(event.getNewContent());
                            server.getTextChannelById(652263585363525638L).ifPresent(logsChannel -> new MessageBuilder()
                                    .addEmbeds(mainEmbed, oldContentEmbed, newContentEmbed)
                                    .send(logsChannel));
                        }))))));
    }

    private void createMessagesDeleteLogger(DiscordApi api) {
        api.addMessageDeleteListener(event -> event.getServer().ifPresent(server -> event.getServerTextChannel()
                .ifPresent(serverTextChannel -> event.getMessage().ifPresent(message -> event.getMessageAuthor()
                        .ifPresent(messageAuthor -> {
                            if (!messageAuthor.isRegularUser())
                                return;
                            EmbedBuilder mainEmbed = new EmbedBuilder()
                                    .setColor(Color.WHITE)
                                    .setAuthor(messageAuthor)
                                    .setUrl("https://discord.com/channels/" + server.getIdAsString() + "/" + serverTextChannel.getIdAsString() + "/" + message.getIdAsString())
                                    .setTitle("Message deleted.")
                                    .setDescription(messageAuthor.getName() + "'s message has been deleted");
                            EmbedBuilder newContentEmbed = new EmbedBuilder()
                                    .setColor(Color.RED)
                                    .setTitle("Message Content")
                                    .setDescription(message.getContent());
                            server.getTextChannelById(652263585363525638L).ifPresent(logsChannel -> new MessageBuilder()
                                    .addEmbeds(mainEmbed, newContentEmbed)
                                    .send(logsChannel));
                        })))));
    }
}