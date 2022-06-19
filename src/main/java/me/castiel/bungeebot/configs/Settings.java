package me.castiel.bungeebot.configs;

import me.castiel.bungeebot.types.Option;
import me.castiel.bungeebot.types.Question;
import me.castiel.bungeebot.types.Ticket;
import me.castiel.bungeebot.utils.CustomEmbedBuilder;
import me.castiel.bungeebot.utils.DiscordUtils;
import me.castiel.bungeebot.utils.YamlUtils;
import net.md_5.bungee.config.Configuration;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Settings {

    private final String token;
    private final List<Question> questions;
    private final List<Ticket> tickets;
    private final List<String> whitelistedDomains, blacklistedPages, blacklistedWords;

    public Settings() {
        Configuration config = YamlUtils.loadConfig("config.yml");
        assert config != null;
        token = config.getString("Token");
        questions = new ArrayList<>();
        Configuration questionsSection = config.getSection("Auto-Helper.Questions");
        for (String _question : questionsSection.getKeys()) {
            List<String> messages = questionsSection.getStringList(_question + ".Messages");
            String answer = questionsSection.getString(_question + ".Answer");
            Question question = new Question(messages, answer);
            questions.add(question);
        }
        tickets = new ArrayList<>();
        Configuration ticketsSection = config.getSection("Tickets.Types");
        for (String type : ticketsSection.getKeys()) {
            CustomEmbedBuilder panelEmbed = new CustomEmbedBuilder();
            panelEmbed.setUrl("https://tea-mc.com");
            panelEmbed.setTitle(ticketsSection.getString(type + ".Panel-Embed.Title", null));
            panelEmbed.setDescription(ticketsSection.getStringList(type + ".Panel-Embed.Description"));

            List<String> ticketEmbedDescription = ticketsSection.getStringList(type + ".Ticket-Embed.Description");

            CustomEmbedBuilder ticketEmbed = new CustomEmbedBuilder();
            ticketEmbed.setUrl("https://tea-mc.com");
            ticketEmbed.setTitle(ticketsSection.getString(type + ".Ticket-Embed.Title", null));
            ticketEmbed.setDescription(ticketEmbedDescription);

            Option option = null;
            String optionsText = ticketsSection.getString(type + ".Options.Text", null);
            if (optionsText != null) {
                List<SelectMenuOption> selectMenuOptions = new ArrayList<>();
                HashMap<String, CustomEmbedBuilder> menus = new HashMap<>();
                for (String t : ticketsSection.getSection(type + ".Options.List").getKeys()) {
                    String name = ticketsSection.getString(type + ".Options.List." + t + ".Name");
                    String description = ticketsSection.getString(type + ".Options.List." + t + ".Description");
                    String emojiUnicode = ticketsSection.getString(type + ".Options.List." + t + ".Emoji");
                    selectMenuOptions.add(SelectMenuOption.create(name, t.toLowerCase(), description, DiscordUtils.getEmoji(emojiUnicode)));
                    CustomEmbedBuilder menuEmbedBuilder = new CustomEmbedBuilder();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String line : ticketEmbedDescription) {
                        if (line.equalsIgnoreCase("%variable%")) {
                            for (String var : ticketsSection.getStringList(type + ".Options.List." + t + ".Variable"))
                                stringBuilder.append(var).append("\n");
                            if (stringBuilder.toString().endsWith("\n"))
                                stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length() - 1);
                        } else stringBuilder.append(line).append("\n");
                        menuEmbedBuilder.setDescription(stringBuilder.toString());
                    }
                    menus.put(t.toLowerCase(), menuEmbedBuilder);
                }
                SelectMenu selectMenu = SelectMenu.create(type.toLowerCase(), "Click here to select an option.", 0, 1, selectMenuOptions);
                option = new Option(optionsText, selectMenu, menus);
            }
            String button = ticketsSection.getString(type + ".Button");
            Long category = ticketsSection.getLong(type + ".Category");
            Ticket ticket = new Ticket(type, button, category, panelEmbed, ticketEmbed, option);
            tickets.add(ticket);
        }
        whitelistedDomains = config.getStringList("Auto-Mod.Whitelisted.Domains");
        blacklistedPages = config.getStringList("Auto-Mod.Blacklisted.Pages");
        blacklistedWords = config.getStringList("Auto-Mod.Blacklisted.Words");
    }

    public String getToken() {
        return token;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public List<String> getWhitelistedDomains() {
        return whitelistedDomains;
    }

    public List<String> getBlacklistedPages() {
        return blacklistedPages;
    }

    public List<String> getBlacklistedWords() {
        return blacklistedWords;
    }
}