package me.castiel.bungeebot.types;

import me.castiel.bungeebot.utils.CustomEmbedBuilder;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.HashMap;

public class Option {

    private final String text;
    private final SelectMenu selectMenu;
    private final HashMap<String, CustomEmbedBuilder> menuEmbeds;

    public Option(String text, SelectMenu selectMenu, HashMap<String, CustomEmbedBuilder> menuEmbeds) {
        this.text = text;
        this.selectMenu = selectMenu;
        this.menuEmbeds = menuEmbeds;
    }

    public String getText() {
        return text;
    }

    public SelectMenu getSelectMenu() {
        return selectMenu;
    }

    public EmbedBuilder getMenuEmbed(String tag, String type) {
        CustomEmbedBuilder customEmbedBuilder = menuEmbeds.get(type);
        customEmbedBuilder.setDescription(customEmbedBuilder.getDescription()
                .replace("%tag%", tag));
        return customEmbedBuilder.buildEmbed();
    }

    public boolean hasEmbed(String type) {
        return menuEmbeds.containsKey(type);
    }
}
