package me.castiel.bungeebot.types;

import org.javacord.api.entity.emoji.Emoji;

import java.util.Optional;

public class ParsedEmoji implements Emoji {

        public final String unicode;

        public ParsedEmoji(String unicode) {
            this.unicode = unicode;
        }

        @Override
        public Optional<String> asUnicodeEmoji() {
            return Optional.of(unicode);
        }

        @Override
        public boolean isAnimated() {
            return false;
        }

        @Override
        public String getMentionTag() {
            return null;
        }
}
