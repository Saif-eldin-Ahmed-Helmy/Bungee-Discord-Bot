package me.castiel.bungeebot.utils;

import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.List;

public class CustomEmbedBuilder {

    private String author, authorUrl, authorImage, url, title, description;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getAuthorImage() {
        return authorImage;
    }

    public void setAuthorImage(String authorImage) {
        this.authorImage = authorImage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public CustomEmbedBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CustomEmbedBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(List<String> descriptions) {
        if (descriptions.isEmpty())
            return;
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : descriptions) stringBuilder.append(line).append("\n");
        setDescription(stringBuilder.toString());
    }

    public CustomEmbedBuilder copy(CustomEmbedBuilder customEmbedBuilder) {
        setAuthor(customEmbedBuilder.getAuthor());
        setAuthorUrl(customEmbedBuilder.getAuthorUrl());
        setAuthorImage(customEmbedBuilder.getAuthorImage());
        setTitle(customEmbedBuilder.getTitle());
        setDescription(customEmbedBuilder.getDescription());
        return this;
    }

    public EmbedBuilder buildEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
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
        return embedBuilder;
    }
}