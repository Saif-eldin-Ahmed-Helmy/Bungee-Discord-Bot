package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.configs.Settings;
import me.castiel.bungeebot.utils.ImageUtils;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;

public class Captcha {

    private final HashMap<Long, CachedCaptcha> cachedCaptchas;

    public Captcha(Settings settings, DiscordApi api) {
        cachedCaptchas = new HashMap<>();
        createCaptchaPanelCommand(settings, api);
        createCaptchaButtonResponder(settings, api);
        createCaptchaResponder(settings, api);
    }

    private void createCaptchaPanelCommand(Settings settings, DiscordApi api) {
        SlashCommand.with("captcha", "Captcha commands",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "create", "Create a captcha panel")
                        ))
                .createGlobal(api)
                .join();
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("captcha"))
                return;
            EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("**:robot: Verification Required!**\n``You need to verify you're a human in order to gain access to the rest of the server.``")
                    .addField("How to verify?", ":black_square_button: Click the 'Verify' button below in order to start the verification process.");
            event.getSlashCommandInteraction().createImmediateResponder()
                    .setContent("OK")
                    .respond();
            event.getSlashCommandInteraction().getChannel().ifPresent(textChannel -> new MessageBuilder()
                    .addEmbed(embed)
                    .addActionRow(Button.success("captcha", "Verify", "\uD83D\uDD11"))
                    .send(textChannel));
        });
    }

    private void createCaptchaButtonResponder(Settings settings, DiscordApi api) {
        api.addButtonClickListener(event -> {
            if (event.getButtonInteraction().getCustomId().equalsIgnoreCase("captcha")) {
                if (event.getButtonInteraction().getUser().openPrivateChannel().join().canYouWrite()) {
                    sendCaptchaToUser(event.getButtonInteraction().getUser());
                    event.getButtonInteraction().createImmediateResponder()
                            .setContent("Solve the captcha in your private messages to gain access to the rest of the server!")
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();
                } else {
                    event.getButtonInteraction().createImmediateResponder()
                            .setContent("You need to open your private messages so we can send you the captcha!")
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();
                }
            }
        });
    }

    private void createCaptchaResponder(Settings settings, DiscordApi api) {
        api.addMessageCreateListener(event -> {
            if (!event.getPrivateChannel().isPresent() || !cachedCaptchas.containsKey(event.getMessageAuthor().getId()))
                return;
            String message = MessageUtils.stripMessage(event.getMessageContent());
            CachedCaptcha captcha = cachedCaptchas.get(event.getMessageAuthor().getId());
            if (captcha.getCaptcha().equals(message) && System.currentTimeMillis() - captcha.getTimestamp() < 300000L) {
                EmbedBuilder embed = new EmbedBuilder()
                        .addField("You have been verified!", ":white_check_mark: Thank you for completing the verification process. You now have full access to tea-mc!");
                event.getMessage().reply(embed);
            } else if (captcha.failed_tries == 4) {
                api.getServerById(623315891051954217L).ifPresent(server -> event.getMessageAuthor().asUser().ifPresent(server::kickUser));
            } else {
                event.getMessageAuthor().asUser().ifPresent(this::sendCaptchaToUser);
            }
        });
    }

    private void sendCaptchaToUser(User user) {
        String captcha = MessageUtils.randomString(6);
        long userID = user.getId();
        int failed_tries = cachedCaptchas.containsKey(userID) ? cachedCaptchas.get(userID).failed_tries : 0;
        cachedCaptchas.put(userID, new CachedCaptcha(failed_tries, System.currentTimeMillis(), captcha));
        InputStream is = ImageUtils.createCaptchaImage(captcha, MessageUtils.randomString(6));
        EmbedBuilder embed = new EmbedBuilder()
                .setDescription("**:robot: Beep Boop... Are you a robot?**\n``You need to verify you're a human in order to gain access to the rest of the server.``")
                .addField("Additional Notes:", ":green_circle: You need to type the green text to get verified.\n:spider_web: Ignore the decoy gray text spread around.\n:capital_abcd: The captcha is CaSe SeNsiTVe.\n:hourglass: The captcha will expire in 5 minutes.")
                .setImage(is, "image.png");
        if (failed_tries > 0)
            embed.setFooter("You have " + (5 - failed_tries) + " tries left.");
        user.sendMessage(embed);
    }

    private static class CachedCaptcha {

        private final int failed_tries;
        private final long timestamp;
        private final String captcha;

        private CachedCaptcha(int failed_tries, long timestamp, String captcha) {
            this.failed_tries = failed_tries;
            this.timestamp = timestamp;
            this.captcha = captcha;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getCaptcha() {
            return captcha;
        }
    }
}