package me.castiel.bungeebot.modules;

import com.google.gson.Gson;
import me.castiel.bungeebot.types.Module;
import me.castiel.bungeebot.types.Option;
import me.castiel.bungeebot.types.SQLTicket;
import me.castiel.bungeebot.types.Ticket;
import me.castiel.bungeebot.utils.DiscordUtils;
import me.castiel.bungeebot.utils.HttpUtils;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Tickets extends Module {

    private final Gson gson;

    public Tickets(DiscordApi api) {
        gson = new Gson();
        createTicketAddCommand(api);
        createTicketRemoveCommand(api);
        createTicketGetTranscriptButton(api);
        createTicketTranscriptButton(api);
        createTicketTranscriptCommand(api);
        createTicketPanelCommand(api);
        createTicketSelectMenuResponder(api);
        createTicketPanelResponder(api);
    }

    private void createTicketAddCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> event.getSlashCommandInteraction().getChannel().flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (slashCommandInteraction.getOptionByName("add").isEmpty()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("ticket")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("ticket", user))
                return;
            InteractionOriginalResponseUpdater updater = slashCommandInteraction.respondLater().join();
            User target = slashCommandInteraction.getArguments().get(0).getUserValue().orElse(user);
            getMySQL().getTicket(serverTextChannel.getIdAsString()).whenCompleteAsync((sqlTicket, throwable) -> sqlTicket.ifPresent(ticket -> {
                if (!ticket.getParticipantsIDS().contains(target.getIdAsString())) {
                    PermissionsBuilder permissionsBuilder = new PermissionsBuilder()
                            .setAllowed(PermissionType.ATTACH_FILE)
                            .setAllowed(PermissionType.ADD_REACTIONS)
                            .setAllowed(PermissionType.READ_MESSAGES)
                            .setAllowed(PermissionType.SEND_MESSAGES)
                            .setAllowed(PermissionType.READ_MESSAGE_HISTORY);

                    serverTextChannel.createUpdater()
                            .addPermissionOverwrite(target, permissionsBuilder.build())
                            .update();

                    ticket.getParticipantsNames().add(target.getName());
                    ticket.getParticipantsIDS().add(target.getIdAsString());

                    getMySQL().insertTicket(ticket);

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setDescription("**:white_check_mark: added " + target.getMentionTag() + " to the ticket.**");

                    updater.addEmbed(embedBuilder)
                            .update();
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setColor(Color.CYAN)
                            .setDescription("**:x: " + target.getMentionTag() + " is already a participant in this ticket.**");

                    updater.addEmbed(embedBuilder)
                            .update();
                }
            }));
        }));
    }

    private void createTicketRemoveCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> event.getSlashCommandInteraction().getChannel().flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (slashCommandInteraction.getOptionByName("remove").isEmpty()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("ticket")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("ticket", user))
                return;
            InteractionOriginalResponseUpdater updater = slashCommandInteraction.respondLater().join();
            User target = slashCommandInteraction.getArguments().get(0).getUserValue().orElse(user);
            getMySQL().getTicket(serverTextChannel.getIdAsString()).whenCompleteAsync((sqlTicket, throwable) -> sqlTicket.ifPresent(ticket -> {
                serverTextChannel.createUpdater()
                        .removePermissionOverwrite(target)
                        .update();

                int index = ticket.getParticipantsIDS().indexOf(target.getIdAsString());
                if (index != -1) {
                    ticket.getParticipantsNames().remove(index);
                    ticket.getParticipantsIDS().remove(index);

                    getMySQL().insertTicket(ticket);

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setDescription("**:white_check_mark: removed " + target.getMentionTag() + " from the ticket.**");

                    updater.addEmbed(embedBuilder)
                            .update();
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setColor(Color.CYAN)
                            .setDescription("**:x: " + target.getMentionTag() + " is not a participant in this ticket.**");

                    updater.addEmbed(embedBuilder)
                            .update();
                }
            }));
        }));
    }

    private void createTicketGetTranscriptButton(DiscordApi api) {
        api.addButtonClickListener(event -> {
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            User user = buttonInteraction.getUser();
            if (user.isBot() || !DiscordUtils.hasPermission("ticket", user))
                return;
            String customId = buttonInteraction.getCustomId();
            Message message = buttonInteraction.getMessage();
            if (customId.startsWith("pass-")) {
                String[] split = customId.split("-");
                String uid = split[1];
                String password = split[2];

                message.createUpdater()
                        .removeAllComponents()
                        .addComponents(ActionRow.of(DiscordUtils.removeButtonByCustomID(message.getComponents(), customId)))
                        .applyChanges();

                buttonInteraction.createImmediateResponder()
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .setContent("Created transcript link: https://transcripts.tea-mc.com/api/TeaMC/ticket.html?uid=" + uid + "&auth=" + password)
                        .respond();
            }
        });
    }

    private void createTicketTranscriptButton(DiscordApi api) {
        api.addButtonClickListener(event -> {
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            User user = buttonInteraction.getUser();
            if (user.isBot() || !DiscordUtils.hasPermission("ticket", user))
                return;
            String customId = buttonInteraction.getCustomId();
            Message message = buttonInteraction.getMessage();
            if (customId.startsWith("transcript-")) {
                buttonInteraction.acknowledge();
                String uid = customId.split("-")[1];
                getMySQL().getTicket(uid).whenCompleteAsync((sqlTicket, throwable) -> sqlTicket.ifPresent(ticket -> {
                    String password = MessageUtils.randomString(8);
                    ticket.getPasswords().add(password);
                    MessageUpdater messageUpdater = message.createUpdater();

                    boolean added = false;

                    for (HighLevelComponent highLevelComponent : message.getComponents()) {
                        Optional<ActionRow> optionalActionRow = highLevelComponent.asActionRow();
                        if (optionalActionRow.isEmpty())
                            continue;
                        ActionRow actionRow = optionalActionRow.get();
                        if (actionRow.getComponents().size() < 5) {
                            List<LowLevelComponent> lowLevelComponents = actionRow.getComponents();
                            if (!added) {
                                added = true;
                                lowLevelComponents.add(Button.secondary("pass-" + uid + "-" + password, user.getName(), "📰"));
                            }
                            messageUpdater.addComponents(ActionRow.of(lowLevelComponents));
                        } else {
                            messageUpdater.addComponents(highLevelComponent);
                        }
                    }

                    if (!added && message.getComponents().size() < 5) {
                        added = true;
                        messageUpdater.addComponents(ActionRow.of(Button.secondary("pass-" + uid + "-" + password, user.getName(), "📰")));
                    }

                    if (!added)
                        return;

                    messageUpdater.applyChanges();
                    getMySQL().insertTicket(ticket);
                }));
            }
        });
    }

    private void createTicketTranscriptCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> event.getSlashCommandInteraction().getChannel().ifPresent(textChannel -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (slashCommandInteraction.getOptionByName("transcript").isEmpty()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("ticket")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("ticket", user))
                return;
            String ticket = slashCommandInteraction.getArguments().get(0).getStringValue().orElse("N/A");
            slashCommandInteraction.createImmediateResponder()
                    .setContent("Created transcript link: https://transcripts.tea-mc.com/api/TeaMC/Ticket-" + ticket + ".html\n" +
                            "**Note: this link will expire after you click it.**")
                    .respond();
        }));
    }

    private void createTicketPanelCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> event.getSlashCommandInteraction().getChannel().ifPresent(textChannel -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (slashCommandInteraction.getOptionByName("create").isEmpty()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("ticket")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("ticket", user))
                return;
            String type = slashCommandInteraction.getArguments().get(0).getStringValue().orElse("N/A");
            for (Ticket ticket : getSettings().getTickets()) {
                if (type.equalsIgnoreCase(ticket.getType())) {
                    slashCommandInteraction.createImmediateResponder()
                            .setContent("OK")
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();
                    new MessageBuilder()
                            .setEmbed(ticket.getPanelEmbed().buildEmbed())
                            .addActionRow(Button.success(ticket.getType().toLowerCase(), ticket.getButton()))
                            .send(textChannel);
                    return;
                }
            }
        }));
    }

    private void createTicketSelectMenuResponder(DiscordApi api) {
        api.addSelectMenuChooseListener(event -> {
            SelectMenuInteraction selectMenuInteraction = event.getSelectMenuInteraction();
            User user = selectMenuInteraction.getUser();
            SelectMenuOption selectMenuOption = selectMenuInteraction.getChosenOptions().get(0);
            String value = selectMenuOption.getValue();
            for (Ticket ticket : getSettings().getTickets()) {
                Optional<Option> optionalOption = ticket.getOption();
                if (optionalOption.isEmpty())
                    continue;
                Option option = optionalOption.get();
                if (option.hasEmbed(value)) {
                    createTicketChannel(user, option.getMenuEmbed(user.getMentionTag(), user.getName(), value), ticket, event.getInteraction());
                    return;
                }
                return;
            }
        });
    }

    private void createTicketPanelResponder(DiscordApi api) {
        api.addButtonClickListener(event -> {
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            String customId = buttonInteraction.getCustomId();
            User user = buttonInteraction.getUser();
            Message message = buttonInteraction.getMessage();
            if (customId.equals("delete")) {
                message.createUpdater()
                        .removeAllComponents()
                        .addComponents(ActionRow.of(DiscordUtils.disableAllButtons(message.getComponents())))
                        .applyChanges();
                buttonInteraction.createImmediateResponder()
                        .setContent("The ticket is being archived...")
                        .respond();
                event.getButtonInteraction().getServer().ifPresent(server -> event.getButtonInteraction().getChannel().flatMap(textChannel -> server.getTextChannelById(textChannel.getId())).ifPresent(serverTextChannel -> server.getTextChannelById(912822165945085963L).ifPresent(transcriptsChannel -> {
                    HashMap<String, String> keys = new HashMap<>();
                    keys.put("cid", serverTextChannel.getIdAsString());
                    keys.put("token", getSettings().getToken());
                    HttpUtils.sendPostRequest("https://transcripts.tea-mc.com/api/api.php", keys);

                    serverTextChannel.delete("Ticket closed by " + user.getName());

                    getMySQL().getTicket(serverTextChannel.getIdAsString()).whenCompleteAsync((sqlTicket, throwable) -> sqlTicket.ifPresent(ticket -> {

                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setAuthor(user.getName(), "", user.getAvatar())
                                .setDescription("**:x: Ticket closed by " + user.getMentionTag() + "**\n``The ticket has been closed and a transcript of the chat has been archived.``")
                                .addField("Ticket Name", serverTextChannel.getName())
                                .addField("Ticket Category", ticket.getCategory())
                                .addField("Ticket Open Date", "<t:" + ticket.getOpenDate() + ":f>")
                                .addField("Ticket Creator", ticket.getCreatorName() + " (" + ticket.getCreatorID() + ")");

                        if (ticket.getParticipantsNames().size() > 0) {
                            String participants = gson.toJson(ticket.getParticipantsNames());
                            embedBuilder.addField("Participants", participants
                                    .substring(1, participants.length() - 1));
                        }

                        new MessageBuilder()
                                .setEmbed(embedBuilder)
                                .addActionRow(Button.success("transcript-" + ticket.getTicketUID(), "Get Transcript"))
                                .send(transcriptsChannel);

                        ticket.setStatus("CLOSED");
                        ticket.setCloseDate(String.valueOf(Instant.now().getEpochSecond()));
                        ticket.setClosedByName(user.getName());
                        ticket.setClosedByID(user.getIdAsString());

                        getMySQL().insertTicket(ticket);
                    }));
                })));
                return;
            }
            buttonInteraction.getChannel().ifPresent(textChannel -> {
                for (Ticket ticket : getSettings().getTickets()) {
                    if (customId.equalsIgnoreCase(ticket.getType())) {
                        Optional<Option> optionalOption = ticket.getOption();
                        if (optionalOption.isPresent()) {
                            Option option = optionalOption.get();
                            buttonInteraction.createImmediateResponder()
                                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                    .setContent(option.getText())
                                    .addComponents(ActionRow.of(option.getSelectMenu()))
                                    .respond();
                        } else {
                            createTicketChannel(user, ticket.getTicketEmbed(user.getMentionTag(), user.getName()), ticket, event.getInteraction());
                        }
                        return;
                    }
                }
            });
        });
    }

    public void createTicketChannel(User user, EmbedBuilder embedBuilder, Ticket ticket, Interaction interaction) {
        getInstance().getApi().getServerById(623315891051954217L).ifPresent(server -> server.getChannelCategoryById(ticket.getCategory()).ifPresent(channelCategory -> {
            Permissions permissions = new PermissionsBuilder()
                    .setAllowed(PermissionType.ATTACH_FILE)
                    .setAllowed(PermissionType.ADD_REACTIONS)
                    .setAllowed(PermissionType.READ_MESSAGES)
                    .setAllowed(PermissionType.SEND_MESSAGES)
                    .setAllowed(PermissionType.READ_MESSAGE_HISTORY)
                    .build();

            ServerTextChannelBuilder serverTextChannelBuilder = server.createTextChannelBuilder()
                    .setCategory(channelCategory)
                    .setAuditLogReason("Created a ticket for " + user.getMentionTag())
                    .setName(ticket.getType().toLowerCase() + "-" + user.getName() + "-" + MessageUtils.randomString(6).toLowerCase())
                    .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setAllDenied().build())
                    .addPermissionOverwrite(user, permissions);

            for (Long role : ticket.getSupportRoles())
                server.getRoleById(role).ifPresent(serverRole -> serverTextChannelBuilder.addPermissionOverwrite(serverRole, permissions));

            ServerTextChannel serverTextChannel = serverTextChannelBuilder.create().join();

            interaction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .setContent("Created Ticket! <#" + serverTextChannel.getIdAsString() + ">")
                    .respond();

            new MessageBuilder()
                    .setContent("Hello, " + user.getMentionTag() + ".")
                    .setEmbed(embedBuilder)
                    .addActionRow(Button.success("delete", "Close Ticket"))
                    .send(serverTextChannel);

            getMySQL().getAmountOfTickets().whenCompleteAsync((integer, throwable) -> {
                SQLTicket sqlTicket = new SQLTicket(serverTextChannel.getIdAsString(), String.valueOf(integer + 1), ticket.getType(), user.getName(), user.getIdAsString(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), "OPEN", String.valueOf(Instant.now().getEpochSecond()), "", "", "");
                getMySQL().insertTicket(sqlTicket);
            });
        }));
    }
}