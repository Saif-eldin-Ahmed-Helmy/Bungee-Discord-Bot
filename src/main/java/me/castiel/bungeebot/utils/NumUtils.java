package me.castiel.bungeebot.utils;

import java.util.Random;

public final class NumUtils {

    private static final Random random;

    static {
        random = new Random();
    }

    public static double random(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static int randomInt() {
        return random.nextInt();
    }
}
