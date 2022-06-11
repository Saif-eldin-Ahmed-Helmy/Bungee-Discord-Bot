package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.configs.Settings;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;

import java.util.Collections;

public class Tickets {

    public Tickets(Settings settings, DiscordApi api) {
        createTicketPanelCommand(settings, api);
        createTicketPanelResponder(settings, api);
    }

    private void createTicketPanelCommand(Settings settings, DiscordApi api) {
        SlashCommand.with("tickets", "Tickets commands",
                        Collections.singletonList(
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create", "Create a tickets panel",
                                        Collections.singletonList(
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "type", "Type of ticket panel", true)
                                        ))))
                .createGlobal(api)
                .join();
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("tickets"))
                return;
            event.getSlashCommandInteraction().getChannel().ifPresent(textChannel -> {
                String type = slashCommandInteraction.getArguments().get(0).getStringValue().orElse("N/A");
                if (type.equalsIgnoreCase("support")) {
                    event.getSlashCommandInteraction().createImmediateResponder()
                            .setContent("OK")
                            .respond();
                    EmbedBuilder embed = new EmbedBuilder()
                            .setUrl("https://tea-mc.com/")
                            .setTitle("Support Tickets  :tickets:")
                            .setDescription("" +
                                    "Need assistance? Create a support ticket and one of our staff members will help you as soon as possible!\n\n" +
                                    "**Valid reasons to open a support ticket:**\n" +
                                    ":question: Questions/Support.\n" +
                                    ":gift: Claiming Payouts/Giveaways.\n" +
                                    ":exclamation: Player Reports.\n" +
                                    ":bug: Bug Reports.\n" +
                                    "<:logo:711350804472266833> Anything related to Tea-MC.\n\n" +
                                    "**Notes:**\n" +
                                    ":stopwatch: Staff try to respond to tickets as soon as possible so please refrain from tagging staff members unless its urgent.\n" +
                                    ":information_source: You should try to provide as much information as possible so our staff team can give you an instant response, You can send screenshots, videos, gifs, logs, etc.");
                    new MessageBuilder()
                            .setEmbed(embed)
                            .addActionRow(Button.success("support", "Open ticket"))
                            .send(textChannel);
                } else if (type.equalsIgnoreCase("applications")) {
                    event.getSlashCommandInteraction().createImmediateResponder()
                            .setContent("OK")
                            .respond();
                    EmbedBuilder embed = new EmbedBuilder()
                            .setUrl("https://tea-mc.com/")
                            .setTitle("Staff & Builder Applications  :hammer:")
                            .setDescription("" +
                                    "Tea-MC is always in constant development and we're always looking to expand our team!\n\n" +
                                    "**Applications Template & Requirements:**\n" +
                                    ":hammer: Staff Application: https://tea-mc.com/threads/staff-application-template-open.8/\n" +
                                    ":construction_worker::skin-tone-1: Builder Application: https://tea-mc.com/threads/builder-application-template.41/\n\n" +
                                    "**Please ensure you meet the requirements before creating a ticket - It is also recommended to prepare your application before creating the ticket to reduce wait times. Once ready click the 'Create application' button below.**\n\n" +
                                    "**Goodluck with your application!**");
                    new MessageBuilder()
                            .setEmbed(embed)
                            .addActionRow(Button.success("applications", "Create application"))
                            .send(textChannel);
                } else if (type.equalsIgnoreCase("Appeals")) {
                    event.getSlashCommandInteraction().createImmediateResponder()
                            .setContent("OK")
                            .respond();
                    EmbedBuilder embed = new EmbedBuilder()
                            .setUrl("https://tea-mc.com/")
                            .setTitle("Punishments Appeals  :hammer:")
                            .setDescription("" +
                                    "if you have recently been punished (Last 3 months) you may appeal it.\n\n" +
                                    "**Appeal Template:**\n" +
                                    "Username: Castiel\n" +
                                    "Ban/Mute Reason: Cheating\n" +
                                    "Guilty: No\n" +
                                    "Personal Statement: I wasn't cheating im skilled i swear\n" +
                                    "Proof: (Only if Not Guilty - Image/Video proof disputing your ban reason)\n\n" +
                                    "**Please prepare your appeal before opening a ticket to help decrease wait times, once ready click the 'Create appeal' button below.**\n\n" +
                                    "**Notes:**\n" +
                                    ":receipt: If you are providing proof, please make sure that it is verifiable by our staff team.\n" +
                                    ":negative_squared_cross_mark: You may not appeal the same punishment twice. The staff team decision is final.\n" +
                                    ":x: Just because your punishment reason is not specifically includes in the rules that doesn't mean its not valid.\n" +
                                    ":hammer: Lying in an appeal will cause your appeal to be instantly denied, and if staff time is wasted due to it, your punishment could be further extended upon.\n" +
                                    ":red_circle: You should use your personal statement to express your opinion on why your punishment is incorrect or to admit to your wrong doings and explain why we should allow you back onto the network.\n\n" +
                                    "**Goodluck with your appeal! We hope to see you on the network again soon!**");
                    new MessageBuilder()
                            .setEmbed(embed)
                            .addActionRow(Button.success("appeals", "Create appeal"))
                            .send(textChannel);
                }
            });
        });
    }

    private void createTicketPanelResponder(Settings settings, DiscordApi api) {
        api.addButtonClickListener(event -> {
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            String customId = buttonInteraction.getCustomId();
            User user = buttonInteraction.getUser();
            if (customId.equals("support")) {
                api.getServerById(623315891051954217L).ifPresent(server -> server.getChannelCategoryById(882644591088635924L).ifPresent(channelCategory -> {
                    ServerTextChannel serverTextChannel = server.createTextChannelBuilder()
                            .setCategory(channelCategory)
                            .setAuditLogReason("Created a ticket for " + user.getMentionTag())
                            .setName("support-" + user.getName() + "-" + MessageUtils.randomString(6).toLowerCase())
                            .create().join();
                    EmbedBuilder embed = new EmbedBuilder()
                            .setAuthor("Tea-MC Support Ticket", "", "https://tea-mc.com/assets/imgs/logo.png")
                            .setDescription(user.getMentionTag() + " please describe the reason for your ticket and our staff team will get back to you as soon as possible!")
                            .setDescription("");
                    new MessageBuilder()
                            .setEmbed(embed)
                            .addActionRow(Button.danger("delete", "Close Ticket"))
                            .send(serverTextChannel);
                }));
            } else if (customId.equals("applications")) {

            } else if (customId.equals("appeals")) {

            }
        });
    }
}