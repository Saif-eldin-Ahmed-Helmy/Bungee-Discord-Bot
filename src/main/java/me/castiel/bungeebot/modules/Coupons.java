package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.configs.Settings;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class Coupons {

    public Coupons(Settings settings, DiscordApi api) {
        createCouponCommand(settings, api);
    }

    private void createCouponCommand(Settings settings, DiscordApi api) {
        SlashCommand.with("coupons", "Coupons commands",
                        Collections.singletonList(
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create", "Create a store coupon",
                                        Arrays.asList(
                                                SlashCommandOption.create(SlashCommandOptionType.LONG, "amount", "The coupon amount", true),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "reason", "Reason you created the coupon", true)
                                        ))))
                .createGlobal(api)
                .join();
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            if (!slashCommandInteraction.getCommandName().equalsIgnoreCase("coupons"))
                return;
            String code = UUID.randomUUID().toString().toUpperCase();
            Long amount = slashCommandInteraction.getArguments().get(0).getLongValue().orElse(0L);
            String reason = slashCommandInteraction.getArguments().get(1).getStringValue().orElse("N/A");
            try {
                String content = "code=%code%&effective_on=package&packages%5B%5D=5073067&packages%5B%5D=5073068&packages%5B%5D=5073069&packages%5B%5D=5073070&packages%5B%5D=5073071&packages%5B%5D=5073072&packages%5B%5D=4729683&packages%5B%5D=4729684&packages%5B%5D=4730147&packages%5B%5D=4731811&packages%5B%5D=4858381&packages%5B%5D=4858377&packages%5B%5D=4729685&packages%5B%5D=4729686&packages%5B%5D=5072374&packages%5B%5D=4698661&packages%5B%5D=4698663&packages%5B%5D=4698666&packages%5B%5D=4698671&packages%5B%5D=5069051&packages%5B%5D=5069057&packages%5B%5D=5069064&packages%5B%5D=5069066&packages%5B%5D=5069063&packages%5B%5D=5072457&discount_type=value&discount_percentage=0&discount_amount=%amount%&start_date=May+29%2C+2022+22%3A15&basket_type=both&expire_limit=1&expire_never=true&minimum=0&user_limit=0&discount_application_method=1&username=&note=%reason%"
                        .replace("%code%", code)
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%reason%", reason);
                URL url = new URL("https://plugin.tebex.io/coupons");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.addRequestProperty("X-Tebex-Secret", "***REMOVED***");
                connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", Integer.toString(content.length()));
                connection.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
                if (connection.getResponseCode() == 200) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setDescription("**:gift: Coupon generated!**\n``You can use this coupon at https://store.tea-mc.com, the coupon won't expire until it gets used once.``")
                            .addField("Coupon Details:", ":key: **Coupon:** " + code + ".\n:money_with_wings: **Amount:** " + amount + ".\n:paperclip: **Reason:** " + reason + ".");
                    event.getInteraction().createImmediateResponder()
                            .addEmbed(embed)
                            .respond();
                } else {
                    event.getInteraction().createImmediateResponder()
                            .setContent("Coupon creation failed... wtf?")
                            .respond();
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        });
    }
}