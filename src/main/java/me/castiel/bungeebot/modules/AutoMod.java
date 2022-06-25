package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.types.Module;
import me.castiel.bungeebot.utils.DiscordUtils;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public class AutoMod extends Module {

    public AutoMod(DiscordApi api) {
        moderateMessages(api);
        moderateMassMentions(api);
    }

    private void moderateMessages(DiscordApi api) {
        api.addMessageCreateListener(event -> event.getMessageAuthor().asUser().ifPresent(user -> {
            event.getServer().ifPresent(server -> {
                Message discordMessage = event.getMessage();
                String message = MessageUtils.stripMessage(event.getMessageContent());
                if (!discordMessage.isServerMessage()
                        || server.getId() != 623315891051954217L
                        || !event.getMessageAuthor().isRegularUser()
                        || DiscordUtils.hasPermission("automod", user))
                for (String word : message.split(" ")) {
                    if (isBlackListedLink(word)) {
                        discordMessage.reply(user.getMentionTag() + " You can't post that link.");
                        discordMessage.delete("Posted the link " + word + ".");
                    }
                    if (isBlackListedWord(word)) {
                        discordMessage.reply(user.getMentionTag() + " Swearing is not allowed.");
                        discordMessage.delete("Said the word: " + word + ".");
                        return;
                    }
                }
            });
        }));
        api.addMessageEditListener(event -> event.getMessage().ifPresent(discordMessage
                -> discordMessage.getAuthor().asUser().ifPresent(user
                -> event.getServer().ifPresent(server -> {
            String message = MessageUtils.stripMessage(event.getNewContent());
            if (!discordMessage.isServerMessage()
                    || server.getId() != 623315891051954217L
                    || user.isBot()
                    || DiscordUtils.hasPermission("automod", user))
                return;
            for (String word : message.split(" ")) {
                if (isBlackListedLink(word)) {
                    discordMessage.reply(user.getMentionTag() + " You can't post that link.");
                    discordMessage.delete("Posted the link " + word + ".");
                    return;
                }
                if (isBlackListedWord(word)) {
                    discordMessage.reply(user.getMentionTag() + " Swearing is not allowed.");
                    discordMessage.delete("Said the word: " + word + ".");
                    return;
                }
            }
        }))));
    }

    private boolean isBlackListedLink(String word) {
        return false;
        /*Optional<String> optionalDomain = MessageUtils.parseDomain(word);
        if (!optionalDomain.isPresent())
            return false;
        String domain = optionalDomain.get();
        return getSettings().getWhitelistedDomains().stream().noneMatch(s -> s.equalsIgnoreCase(domain))
                || getSettings().getBlacklistedPages().stream().anyMatch(word::contains);*/
    }

    private boolean isBlackListedWord(String word) {
        return getSettings().getBlacklistedWords().stream().anyMatch(s -> s.equalsIgnoreCase(word));
    }

    private void moderateMassMentions(DiscordApi api) {
        api.addMessageCreateListener(event -> event.getServer().ifPresent(server -> event.getMessageAuthor().asUser().ifPresent(user -> {
            Message message = event.getMessage();
            if (!event.isServerMessage()
                    || server.getId() != 623315891051954217L
                    || !event.getMessageAuthor().isRegularUser()
                    || DiscordUtils.hasPermission("automod", user))
                return;
            if (message.getMentionedUsers().size() >= 10) {
                message.delete();
                if (server.canYouBanUser(user)) {
                    server.banUser(user, 0, "Mass Mention");
                    message.reply("User has been banned for mass mentions.");
                }
            }
        })));
    }
}