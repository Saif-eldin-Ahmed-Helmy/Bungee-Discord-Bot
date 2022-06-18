package me.castiel.bungeebot.types;

public class CachedCaptcha {

    private final int failedTries;
    private final long timestamp;
    private final String captcha;

    public CachedCaptcha(int failed_tries, long timestamp, String captcha) {
        this.failedTries = failed_tries;
        this.timestamp = timestamp;
        this.captcha = captcha;
    }

    public int getFailedTries() {
        return failedTries;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCaptcha() {
        return captcha;
    }
}
