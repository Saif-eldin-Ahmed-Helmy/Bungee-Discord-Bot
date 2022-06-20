package me.castiel.bungeebot.utils;

import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class CustomEmbedBuilder {

    private Color color;
    private String author, authorUrl, authorImage, url, title, image, thumbnail, footer, footerImage, description;

    public Color getColor() {
        return color;
    }

    public CustomEmbedBuilder setColor(Color color) {
        this.color = color;
        return this;
    }

    public CustomEmbedBuilder setColor(String s) {
        try {
            Color color = (Color) Color.class.getField(s.toUpperCase()).get(null);
            if (color != null)
                setColor(color);
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {

        }
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public CustomEmbedBuilder setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public CustomEmbedBuilder setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
        return this;
    }

    public String getAuthorImage() {
        return authorImage;
    }

    public CustomEmbedBuilder setAuthorImage(String authorImage) {
        this.authorImage = authorImage;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public CustomEmbedBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CustomEmbedBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomEmbedBuilder setTitle(String title, boolean split) {
        if (split) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : title.split("\\\\n")) {
                stringBuilder.append(s).append("\n");
            }
            this.title = stringBuilder.toString();
        }
        else {
            this.title = title;
        }
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CustomEmbedBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CustomEmbedBuilder setDescription(List<String> descriptions) {
        if (descriptions.isEmpty())
            return this;
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : descriptions) stringBuilder.append(line).append("\n");
        setDescription(stringBuilder.toString());
        return this;
    }

    public CustomEmbedBuilder setDescription(String description, boolean split) {
        if (split) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : description.split("\\\\n")) {
                stringBuilder.append(s).append("\n");
            }
            this.description = stringBuilder.toString();
        }
        else {
            this.description = description;
        }
        return this;
    }

    public CustomEmbedBuilder setImage(String image) {
        this.image = image;
        return this;
    }

    public String getImage() {
        return image;
    }

    public CustomEmbedBuilder setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public CustomEmbedBuilder setFooter(String footer) {
        this.footer = footer;
        return this;
    }

    public String getFooter() {
        return footer;
    }

    public CustomEmbedBuilder setFooterImage(String footerImage) {
        this.footerImage = footerImage;
        return this;
    }

    public String getFooterImage() {
        return footerImage;
    }

    public CustomEmbedBuilder copy(CustomEmbedBuilder customEmbedBuilder) {
        setColor(customEmbedBuilder.getColor());
        setAuthor(customEmbedBuilder.getAuthor());
        setAuthorUrl(customEmbedBuilder.getAuthorUrl());
        setAuthorImage(customEmbedBuilder.getAuthorImage());
        setUrl(customEmbedBuilder.getUrl());
        setTitle(customEmbedBuilder.getTitle());
        setDescription(customEmbedBuilder.getDescription());
        setImage(customEmbedBuilder.getImage());
        setThumbnail(customEmbedBuilder.getThumbnail());
        setFooter(customEmbedBuilder.getFooter());
        setFooterImage(customEmbedBuilder.getFooterImage());
        return this;
    }

    public EmbedBuilder buildEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (color != null)
            embedBuilder.setColor(color);
        if (author != null && authorUrl != null && authorImage != null)
            embedBuilder.setAuthor(author, authorUrl, authorImage);
        else if (author != null)
            embedBuilder.setAuthor(author);
        if (url != null)
            embedBuilder.setUrl(url);
        if (title != null)
            embedBuilder.setTitle(title);
        if (description != null)
            embedBuilder.setDescription(description);
        if (image != null)
            embedBuilder.setImage(image);
        if (thumbnail != null)
            embedBuilder.setThumbnail(thumbnail);
        if (footer != null && footerImage != null)
            embedBuilder.setFooter(footer, footerImage);
        else if (footer != null)
            embedBuilder.setFooter(footer);
        return embedBuilder;
    }
}