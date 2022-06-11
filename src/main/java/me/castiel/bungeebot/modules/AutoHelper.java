package me.castiel.bungeebot.modules;

import me.castiel.bungeebot.configs.Settings;
import me.castiel.bungeebot.utils.MessageUtils;
import org.javacord.api.DiscordApi;

import java.util.List;

public class AutoHelper {

    public AutoHelper(Settings settings, DiscordApi api) {
        answerQuestions(settings, api);
    }

    private void answerQuestions(Settings settings, DiscordApi api) {
        api.addMessageCreateListener(event -> {
            String message = MessageUtils.stripMessage(event.getMessageContent());
            for (Question question : settings.questions) {
                if (question.getMessages().stream().anyMatch(s -> s.equalsIgnoreCase(message))) {
                    event.getMessage().reply(question.getAnswer());
                    return;
                }
            }
        });
    }

    public static class Question {

        private final List<String> messages;
        private final String answer;

        public Question(List<String> messages, String answer) {
            this.messages = messages;
            this.answer = answer;
        }

        public List<String> getMessages() {
            return messages;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
