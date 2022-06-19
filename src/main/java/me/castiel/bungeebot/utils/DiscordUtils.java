package me.castiel.bungeebot.utils;

import me.castiel.bungeebot.BungeeBot;
import me.castiel.bungeebot.types.ParsedEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
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

    public static List<LowLevelComponent> removeButtonByName(List<HighLevelComponent> highLevelComponents, String name) {
        List<LowLevelComponent> buttons = new ArrayList<>();
        for (HighLevelComponent highLevelComponent : highLevelComponents) {
            highLevelComponent.asActionRow().ifPresent(actionRow -> {
                for (LowLevelComponent lowLevelComponent : actionRow.getComponents()) {
                    lowLevelComponent.asButton().ifPresent(button -> {
                        if (!button.getLabel().isPresent() || !button.getLabel().get().equals(name)) {
                            ButtonBuilder buttonBuilder = new ButtonBuilder()
                                    .copy(button)
                                    .setDisabled(true);
                            buttons.add(buttonBuilder.build());
                        }
                    });
                }
            });
        }
        return buttons;
    }

    public static List<LowLevelComponent> removeButtonByCustomID(List<HighLevelComponent> highLevelComponents, String customID) {
        List<LowLevelComponent> buttons = new ArrayList<>();
        for (HighLevelComponent highLevelComponent : highLevelComponents) {
            highLevelComponent.asActionRow().ifPresent(actionRow -> {
                for (LowLevelComponent lowLevelComponent : actionRow.getComponents()) {
                    lowLevelComponent.asButton().ifPresent(button -> {
                        if (!button.getCustomId().isPresent() || !button.getCustomId().get().equals(customID)) {
                            buttons.add(button);
                        }
                    });
                }
            });
        }
        return buttons;
    }

    public static List<LowLevelComponent> disableAllButtons(List<HighLevelComponent> highLevelComponents) {
        List<LowLevelComponent> buttons = new ArrayList<>();
        for (HighLevelComponent highLevelComponent : highLevelComponents) {
            highLevelComponent.asActionRow().ifPresent(actionRow -> {
                for (LowLevelComponent lowLevelComponent : actionRow.getComponents()) {
                    lowLevelComponent.asButton().ifPresent(button -> {
                        ButtonBuilder buttonBuilder = new ButtonBuilder()
                                .copy(button)
                                .setDisabled(true);
                        buttons.add(buttonBuilder.build());
                    });
                }
            });
        }
        return buttons;
    }
}
