package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.types.CachedCaptcha;
import me.castiel.bungeebot.utils.DiscordUtils;
import me.castiel.bungeebot.utils.ImageUtils;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;

import java.io.InputStream;
import java.util.HashMap;

public class Captcha {

    private final HashMap<Long, CachedCaptcha> cachedCaptchaMap;

    public Captcha(DiscordApi api) {
        cachedCaptchaMap = new HashMap<>();
        createCaptchaPanelCommand(api);
        createCaptchaButtonResponder(api);
        createCaptchaResponder(api);
    }

    private void createCaptchaPanelCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> event.getSlashCommandInteraction().getChannel().ifPresent(textChannel -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("captcha") || user.isBot() || !DiscordUtils.hasPermission("captcha", user))
                return;
            slashCommandInteraction.createImmediateResponder()
                    .setContent("OK")
                    .respond();
            EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("**:robot: Verification Required!**\n``You need to verify you're a human in order to gain access to the rest of the server.``")
                    .addField("How to verify?", ":black_square_button: Click the 'Verify' button below in order to start the verification process.");
            new MessageBuilder()
                    .addEmbed(embed)
                    .addActionRow(Button.success("captcha", "Verify", "\uD83D\uDD11"))
                    .send(textChannel).join();
        }));
    }

    private void createCaptchaButtonResponder(DiscordApi api) {
        api.addButtonClickListener(event -> {
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            User user = buttonInteraction.getUser();
            if (buttonInteraction.getCustomId().equalsIgnoreCase("captcha")) {
                if (user.openPrivateChannel().join().canYouWrite()) {
                    if (!cachedCaptchaMap.containsKey(user.getId())) sendCaptchaToUser(user, false);
                    buttonInteraction.createImmediateResponder()
                            .setContent("Solve the captcha in your private messages to gain access to the rest of the server!")
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();
                } else {
                    buttonInteraction.createImmediateResponder()
                            .setContent("You need to open your private messages so we can send you the captcha!")
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();
                }
            }
        });
    }

    private void createCaptchaResponder(DiscordApi api) {
        api.addMessageCreateListener(event -> event.getMessageAuthor().asUser().ifPresent(user -> {
            Message discordMessage = event.getMessage();
            String message = MessageUtils.stripMessage(event.getMessageContent());
            if (!discordMessage.isPrivateMessage() || !cachedCaptchaMap.containsKey(event.getMessageAuthor().getId()))
                return;
            CachedCaptcha captcha = cachedCaptchaMap.get(event.getMessageAuthor().getId());
            if (captcha.getCaptcha().equals(message) && System.currentTimeMillis() - captcha.getTimestamp() < 300000L) {
                EmbedBuilder embed = new EmbedBuilder()
                        .addField("You have been verified!", ":white_check_mark: Thank you for completing the verification process. You now have full access to tea-mc!");
                discordMessage.reply(embed);
                api.getServerById(623315891051954217L).ifPresent(server -> server.getRoleById(697252485580193892L).ifPresent(role -> server.addRoleToUser(user, role)));
                cachedCaptchaMap.remove(user.getId());
            } else if (captcha.getFailedTries() == 4) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setDescription(":x: You have failed the challenge.");
                discordMessage.reply(embed).join();
                api.getServerById(623315891051954217L).ifPresent(server -> event.getMessageAuthor().asUser().ifPresent(server::kickUser));
                cachedCaptchaMap.remove(user.getId());
            } else {
                sendCaptchaToUser(user, true);
            }
        }));
    }

    private void sendCaptchaToUser(User user, boolean fail) {
        String captcha = MessageUtils.randomString(6);
        long userID = user.getId();
        int failed_tries = cachedCaptchaMap.containsKey(userID) ? cachedCaptchaMap.get(userID).getFailedTries() : 0;
        int extra = fail ? failed_tries + 1 : failed_tries;
        cachedCaptchaMap.put(userID, new CachedCaptcha(extra, System.currentTimeMillis(), captcha));
        InputStream is = ImageUtils.createCaptchaImage(MessageUtils.randomString(6), captcha);
        EmbedBuilder embed = new EmbedBuilder()
                .setDescription("**:robot: Beep Boop... Are you a robot?**\n``You need to verify you're a human in order to gain access to the rest of the server.``")
                .addField("Additional Notes:", ":green_circle: You need to type the green text to get verified.\n:spider_web: Ignore the decoy gray text spread around.\n:capital_abcd: The captcha is CaSe SeNsiTVe.\n:hourglass: The captcha will expire in 5 minutes.")
                .setImage(is, "image.png");
        if (extra > 0)
            embed.setFooter("You have " + (5 - extra) + " tries left.");
        user.sendMessage(embed);
    }
}