package me.castiel.bungeebot.utils;

import me.castiel.bungeebot.BungeeBot;
import me.castiel.bungeebot.types.ParsedEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DiscordUtils {

    private static final List<Long> managementRoles;

    static {
        managementRoles = new ArrayList<>();
        managementRoles.add(696078834860032060L);
    }

    public static boolean hasPermission(String command, User user) {
        Optional<Server> optionalServer = BungeeBot.getInstance().getApi().getServerById(623315891051954217L);
        if (!optionalServer.isPresent())
            return false;
        Server server = optionalServer.get();
        return server.getRoles(user).stream().anyMatch(role -> managementRoles.contains(role.getId()));
    }

    public static Emoji getEmoji(String unicode) {
        return new ParsedEmoji(unicode);
    }
}
