package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.types.Module;
import me.castiel.bungeebot.types.Question;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public class AutoHelper extends Module {

    public AutoHelper(DiscordApi api) {
        answerQuestions(api);
    }

    private void answerQuestions(DiscordApi api) {
        api.addMessageCreateListener(event -> {
            if (!event.isServerMessage() || !event.getMessageAuthor().isRegularUser())
                return;
            Message discordMessage = event.getMessage();
            String message = MessageUtils.stripMessage(event.getMessageContent());
            for (Question question : getSettings().getQuestions()) {
                if (question.getMessages().stream().anyMatch(s -> s.equalsIgnoreCase(message))) {
                    discordMessage.reply(question.getAnswer());
                    return;
                }
            }
        });
    }
}
