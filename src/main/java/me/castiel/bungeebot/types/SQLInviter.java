package me.castiel.bungeebot.types;

import java.util.LinkedList;

public class SQLInviter {

    private String invitedBy;
    private final String id;
    private final LinkedList<String> invitedNames, invitedIDS, invitedJoinTimestamps;
    private final int extraInvites;

    public SQLInviter(String id, String invitedBy, LinkedList<String> invitedNames, LinkedList<String> invitedIDS, LinkedList<String> invitedJoinTimestamps, int extraInvites) {
        this.id = id;
        this.invitedBy = invitedBy;
        this.invitedNames = invitedNames;
        this.invitedIDS = invitedIDS;
        this.invitedJoinTimestamps = invitedJoinTimestamps;
        this.extraInvites = extraInvites;
    }

    public String getId() {
        return id;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public LinkedList<String> getInvitedNames() {
        return invitedNames;
    }

    public LinkedList<String> getInvitedIDS() {
        return invitedIDS;
    }

    public LinkedList<String> getInvitedJoinTimestamps() {
        return invitedJoinTimestamps;
    }

    public int getExtraInvites() {
        return extraInvites;
    }
}
