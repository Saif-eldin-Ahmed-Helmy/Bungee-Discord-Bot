package me.castiel.bungeebot.types;

import me.castiel.bungeebot.utils.CustomEmbedBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.Optional;

public class Ticket {

    private final String type, button;
    private final Long category;
    private final CustomEmbedBuilder panelEmbed;
    private final CustomEmbedBuilder ticketEmbed;
    private final Option option;

    public Ticket(String type, String button, Long category, CustomEmbedBuilder panelEmbed, CustomEmbedBuilder ticketEmbed, Option option) {
        this.type = type;
        this.button = button;
        this.category = category;
        this.panelEmbed = panelEmbed;
        this.ticketEmbed = ticketEmbed;
        this.option = option;
    }

    public String getType() {
        return type;
    }

    public String getButton() {
        return button;
    }

    public Long getCategory() {
        return category;
    }

    public CustomEmbedBuilder getPanelEmbed() {
        return panelEmbed;
    }

    public EmbedBuilder getTicketEmbed(String tag) {
        ticketEmbed.setDescription(ticketEmbed.getDescription()
                .replace("%tag%", tag));
        return ticketEmbed.buildEmbed();
    }

    public Optional<Option> getOption() {
        if (option == null)
            return Optional.empty();
        return Optional.of(option);
    }
}
