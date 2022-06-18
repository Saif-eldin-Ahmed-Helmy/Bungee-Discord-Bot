package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.types.Module;
import me.castiel.bungeebot.types.Option;
import me.castiel.bungeebot.types.Ticket;
import me.castiel.bungeebot.utils.DiscordUtils;
import me.castiel.bungeebot.utils.HttpUtils;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;

import java.util.HashMap;
import java.util.Optional;

public class Tickets extends Module {

    public Tickets(DiscordApi api) {
        createTicketTranscriptCommand(api);
        createTicketPanelCommand(api);
        createTicketSelectMenuResponder(api);
        createTicketPanelResponder(api);
    }

    private void createTicketTranscriptCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> event.getSlashCommandInteraction().getChannel().ifPresent(textChannel -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getOptionByName("transcript").isPresent()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("tickets")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("tickets", user))
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
            if (!slashCommandInteraction.getOptionByName("create").isPresent()
                    || !slashCommandInteraction.getCommandName().equalsIgnoreCase("tickets")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("tickets", user))
                return;
            String type = slashCommandInteraction.getArguments().get(0).getStringValue().orElse("N/A");
            for (Ticket ticket : getSettings().getTickets()) {
                if (type.equalsIgnoreCase(ticket.getType())) {
                    slashCommandInteraction.createImmediateResponder()
                            .setContent("OK")
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
                if (!optionalOption.isPresent())
                    continue;
                Option option = optionalOption.get();
                if (option.hasEmbed(value)) {
                    createTicketChannel(user, option.getMenuEmbed(user.getMentionTag(), value), ticket.getCategory(), value + "-" + ticket.getType().toLowerCase(), event.getInteraction());
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
            if (customId.equals("delete")) {
                event.getButtonInteraction().getServer().ifPresent(server -> event.getButtonInteraction().getChannel().flatMap(textChannel -> server.getTextChannelById(textChannel.getId())).ifPresent(serverTextChannel -> server.getTextChannelById(912822165945085963L).ifPresent(transcriptsChannel -> {
                    HashMap<String, String> keys = new HashMap<>();
                    keys.put("cid", serverTextChannel.getIdAsString());
                    keys.put("token", getSettings().getToken());
                    HttpUtils.sendPostRequest("https://transcripts.tea-mc.com/api/api.php", keys);

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setAuthor(user.getName(), "", user.getAvatar())
                            .setDescription("**:x: Ticket closed by " + user.getMentionTag() + "**\n``The ticket has been closed and a transcript of the chat has been archived.``")
                            .addField("Ticket Name", serverTextChannel.getName())
                            .addField("Generate Transcript Link", "Type /tickets transcript " + serverTextChannel.getIdAsString() + " to generate a one time use link.");

                    serverTextChannel.delete("Ticket closed by " + user.getName());

                    new MessageBuilder()
                            .setEmbed(embedBuilder)
                            .send(transcriptsChannel);
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
                            createTicketChannel(user, ticket.getTicketEmbed(user.getMentionTag()), ticket.getCategory(), ticket.getType().toLowerCase(), event.getInteraction());
                        }
                        return;
                    }
                }
            });
        });
    }

    public void createTicketChannel(User user, EmbedBuilder embedBuilder, Long category, String prefix, Interaction interaction) {
        getInstance().getApi().getServerById(623315891051954217L).ifPresent(server -> server.getChannelCategoryById(category).ifPresent(channelCategory -> {
            PermissionsBuilder ticketOwnerPermissions = new PermissionsBuilder()
                    .setAllowed(PermissionType.ATTACH_FILE)
                    .setAllowed(PermissionType.ADD_REACTIONS)
                    .setAllowed(PermissionType.READ_MESSAGES)
                    .setAllowed(PermissionType.SEND_MESSAGES)
                    .setAllowed(PermissionType.READ_MESSAGE_HISTORY);

            ServerTextChannel serverTextChannel = server.createTextChannelBuilder()
                    .setCategory(channelCategory)
                    .setAuditLogReason("Created a ticket for " + user.getMentionTag())
                    .setName(prefix + "-" + user.getName() + "-" + MessageUtils.randomString(6).toLowerCase())
                    .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setAllDenied().build())
                    .addPermissionOverwrite(user, ticketOwnerPermissions.build())
                    .create().join();

            interaction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .setContent("Created Ticket! <#" + serverTextChannel.getIdAsString() + ">")
                    .respond();

            new MessageBuilder()
                    .setContent(user.getMentionTag())
                    .setEmbed(embedBuilder)
                    .addActionRow(Button.success("delete", "Close Ticket"))
                    .send(serverTextChannel);
        }));
    }
}