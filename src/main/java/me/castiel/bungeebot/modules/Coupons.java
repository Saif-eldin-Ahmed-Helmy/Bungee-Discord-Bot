package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.utils.DiscordUtils;
import me.castiel.bungeebot.utils.HttpUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;

public class Coupons {

    public Coupons(DiscordApi api) {
        createCouponCommand(api);
    }

    private void createCouponCommand(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            User user = slashCommandInteraction.getUser();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("coupons")
                    || user.isBot()
                    || !DiscordUtils.hasPermission("coupons", user))
                return;
            slashCommandInteraction.getServer().ifPresent(server -> {
                String code = UUID.randomUUID().toString().toUpperCase();
                Long amount = slashCommandInteraction.getArguments().get(0).getLongValue().orElse(0L);
                String reason = slashCommandInteraction.getArguments().get(1).getStringValue().orElse("N/A");

                HashMap<String, String> headers = new HashMap<>();
                String tebexSecret = System.getenv("TEBEX_SECRET");
                if (tebexSecret == null || tebexSecret.isBlank()) {
                    slashCommandInteraction.createImmediateResponder()
                            .setContent("Coupon integration is not configured.")
                            .respond();
                    return;
                }
                headers.put("X-Tebex-Secret", tebexSecret);
                headers.put("Content-Type", "application/x-www-form-urlencoded");

                String body = "code=" + code +
                        "&effective_on=package" +
                        "&packages%5B%5D=5073067&packages%5B%5D=5073068&packages%5B%5D=5073069&packages%5B%5D=5073070&packages%5B%5D=5073071&packages%5B%5D=5073072&packages%5B%5D=4729683&packages%5B%5D=4729684&packages%5B%5D=4730147&packages%5B%5D=4731811&packages%5B%5D=4858381&packages%5B%5D=4858377&packages%5B%5D=4729685&packages%5B%5D=4729686&packages%5B%5D=5072374&packages%5B%5D=4698661&packages%5B%5D=4698663&packages%5B%5D=4698666&packages%5B%5D=4698671&packages%5B%5D=5069051&packages%5B%5D=5069057&packages%5B%5D=5069064&packages%5B%5D=5069066&packages%5B%5D=5069063&packages%5B%5D=5072457" +
                        "&discount_type=value" +
                        "&discount_percentage=0" +
                        "&discount_amount=" + amount +
                        "&start_date=May+29%2C+2022+22%3A15" +
                        "&basket_type=both" +
                        "&expire_limit=1" +
                        "&expire_never=true" +
                        "&minimum=0" +
                        "&user_limit=0" +
                        "&discount_application_method=1" +
                        "&username=" +
                        "&note=" + reason;

                if (HttpUtils.sendPostRequest("https://plugin.tebex.io/coupons", headers, body) == 200) {
                    EmbedBuilder couponEmbed = new EmbedBuilder()
                            .setDescription("**:gift: Coupon generated!**\n``You can use this coupon at https://store.tea-mc.com, the coupon won't expire until it gets used once.``")
                            .addField("Coupon Details:", ":key: **Coupon:** " + code + ".\n:money_with_wings: **Amount:** $" + amount + ".\n:paperclip: **Reason:** " + reason + ".");
                    slashCommandInteraction.createImmediateResponder()
                            .addEmbed(couponEmbed)
                            .respond();

                    api.getServerById(623315891051954217L).flatMap(teaServer -> teaServer.getTextChannelById(652263585363525638L)).ifPresent(serverTextChannel -> {
                        EmbedBuilder logsEmbed = new EmbedBuilder()
                                .setColor(Color.YELLOW)
                                .setAuthor(user)
                                .setDescription(user.getName() + " has generated a coupon.");
                        new MessageBuilder()
                                .addEmbeds(logsEmbed, couponEmbed)
                                .send(serverTextChannel);
                    });
                } else {
                    slashCommandInteraction.createImmediateResponder()
                            .setContent("Coupon creation failed... Try again later?")
                            .respond();
                }
            });
        });
    }
}