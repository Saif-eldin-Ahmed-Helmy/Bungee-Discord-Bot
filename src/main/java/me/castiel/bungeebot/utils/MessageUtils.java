package me.castiel.bungeebot.utils;

import java.util.Optional;

public final class MessageUtils {

    public static Optional<String> parseDomain(String word) {
        String link = word.split("/")[0];
        int sub = link.startsWith("https://") ? 8 : word.startsWith("http://") ? 7 : 0;
        String[] split = link.split("\\.");
        if (split.length == 3) sub += split[0].length() + 1;
        if (sub > 0 || split.length >= 2) {
            return Optional.of(word.substring(sub));
        }
        return Optional.empty();
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
