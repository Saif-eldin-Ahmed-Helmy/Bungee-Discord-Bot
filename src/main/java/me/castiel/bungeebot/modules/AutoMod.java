package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.configs.Settings;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;

import java.util.List;

public class AutoMod {

    public AutoMod(Settings settings, DiscordApi api) {
        moderateMessages(settings, api);
        moderateMassMentions(api);
    }

    private void moderateMessages(Settings settings, DiscordApi api) {
        List<String> whitelisted_domains = settings.whitelisted_domains;
        List<String> blacklisted_words = settings.blacklisted_words;
        List<String> blacklisted_pages = settings.blacklisted_pages;
        api.addMessageCreateListener(event -> {
            if (!event.isServerMessage() && event.getMessageAuthor().isRegularUser())
               return;
            String message = MessageUtils.stripMessage(event.getMessageContent());
            for (String word : message.split(" ")) {
                String lowerCaseWord = word.toLowerCase();
                int sub = lowerCaseWord.startsWith("https://") ? 8 : lowerCaseWord.startsWith("http://") ? 7 : 0;
                String[] split = word.split("\\.");
                if (split.length == 3) sub += split[0].length() + 1;
                if (sub > 0 || split.length >= 2) {
                    String domain = word.substring(sub).split("/")[0].replace("!", "");
                    if (domain.length() >= 4) {
                        if (whitelisted_domains.stream().noneMatch(s -> s.equalsIgnoreCase(domain))
                                || blacklisted_pages.stream().anyMatch(s -> s.equalsIgnoreCase(domain))) {
                            event.getMessageAuthor().asUser().ifPresent
                                    (user -> event.getMessage().reply(user.getMentionTag() + " You can't post that link." + domain).join());
                            event.getMessage().delete();
                            return;
                        }
                    }
                }
                if (blacklisted_words.stream().anyMatch(s -> s.equalsIgnoreCase(word))) {
                    event.getMessage().getAuthor().asUser().ifPresent
                            (user -> event.getMessage().reply(user.getMentionTag() + " Swearing is not allowed.").join());
                    event.getMessage().delete();
                    return;
                }
            }
        });
        api.addMessageEditListener(event -> event.getMessage().ifPresent(m -> {
          if (!m.isServerMessage() && m.getAuthor().isRegularUser())
              return;
            String message = MessageUtils.stripMessage(event.getNewContent());
            for (String word : message.split(" ")) {
                String lowerCaseWord = word.toLowerCase();
                int sub = lowerCaseWord.startsWith("https://") ? 8 : lowerCaseWord.startsWith("http://") ? 7 : 0;
                String[] split = word.split("\\.");
                if (split.length == 3) sub += split[0].length() + 1;
                if (sub > 0 || split.length >= 2) {
                    String domain = word.substring(sub).split("/")[0].replace("!", "");
                    if (domain.length() >= 4) {
                        if (whitelisted_domains.stream().noneMatch(s -> s.equalsIgnoreCase(domain))
                                || blacklisted_pages.stream().anyMatch(s -> s.equalsIgnoreCase(domain))) {
                            m.delete();
                            m.getAuthor().asUser().ifPresent
                                    (user -> m.reply(user.getMentionTag() + " You can't post that link.").join());
                            return;
                        }
                    }
                }
                if (blacklisted_words.stream().anyMatch(s -> s.equalsIgnoreCase(word))) {
                    m.delete();
                    m.getAuthor().asUser().ifPresent
                            (user -> m.reply(user.getMentionTag() + " Swearing is not allowed.").join());
                    return;
                }
            }
        }));
    }

    private void moderateMassMentions(DiscordApi api) {
        api.addMessageCreateListener(event -> {
            if (!event.isServerMessage() && event.getMessageAuthor().isRegularUser())
                return;
            if (event.getMessage().getMentionedUsers().size() >= 10) {
                event.getMessage().delete();
                event.getServer().ifPresent(server -> event.getMessageAuthor().asUser().ifPresent(user -> {
                    if (server.canYouBanUser(user)) {
                        server.banUser(user, 0, "Mass Mention");
                        event.getMessage().reply("User has been banned for mass mentions.");
                    }
                }));
            }
        });
    }
}