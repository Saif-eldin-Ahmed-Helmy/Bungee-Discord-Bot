package me.castiel.bungeebot.types;

import java.util.List;

public class Question {

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
