package me.castiel.bungeebot.configs;

import me.castiel.bungeebot.modules.AutoHelper;
import me.castiel.bungeebot.utils.YamlUtils;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    public String token, logs_cid, invites_cid, tickets_cid, staffchat_cid, reports_cid;
    public List<AutoHelper.Question> questions;
    public List<String> whitelisted_domains, blacklisted_pages, blacklisted_words;

    public Settings() {
        Configuration config = YamlUtils.loadConfig("config.yml");
        assert config != null;

        token = config.getString("Token");
        logs_cid = config.getString("Logs.Channel-ID");
        invites_cid = config.getString("Invites.Welcome-Channel-ID");
        tickets_cid = config.getString("Tickets.Transcripts-Channel-ID");
        staffchat_cid = config.getString("Staff-Chat.Channel-ID");
        reports_cid = config.getString("Reports-Channel.Channel-ID");

        questions = new ArrayList<>();
        Configuration questionsSection = config.getSection("Auto-Helper.Questions");
        for (String q : questionsSection.getKeys()) {
            List<String> messages = questionsSection.getStringList(q + ".Messages");
            String answer = questionsSection.getString(q + ".Answer");
            AutoHelper.Question question = new AutoHelper.Question(messages, answer);
            questions.add(question);
        }

        whitelisted_domains = config.getStringList("Auto-Mod.Whitelisted.Domains");
        blacklisted_pages = config.getStringList("Auto-Mod.Blacklisted.Pages");
        blacklisted_words = config.getStringList("Auto-Mod.Blacklisted.Words");
    }
}
