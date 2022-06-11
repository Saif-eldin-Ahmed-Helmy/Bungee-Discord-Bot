package me.castiel.bungeebot.utils;

import org.javacord.api.entity.message.embed.Embed;

public final class MessageUtils {

    public static void logMessage(Embed embed) {

    }

    public static void logMessage(String message) {

    }

    public static String stripMessage(String message) {
        return message
                .replace("*", "")
                .replace("_", "")
                .replace("`", "");
    }

    public static String randomString(int length) {
        StringBuilder captchaStringBuffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int baseCharNumber = Math.abs(NumUtils.randomInt()) % 62;
            int charNumber;
            if (baseCharNumber < 26) {
                charNumber = 65 + baseCharNumber;
            }
            else if (baseCharNumber < 52){
                charNumber = 97 + (baseCharNumber - 26);
            }
            else {
                charNumber = 48 + (baseCharNumber - 52);
            }
            captchaStringBuffer.append((char)charNumber);
        }
        return captchaStringBuffer.toString();
    }
}
