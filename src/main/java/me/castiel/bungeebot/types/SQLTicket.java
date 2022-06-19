package me.castiel.bungeebot.types;

import java.util.LinkedList;

public class SQLTicket {

    private String status, closeDate, closedByName, closedByID;
    private final String ticketID, ticketUID, category, creatorName, creatorID, openDate;
    private final LinkedList<String> participantsName, participantsIDS, passwords;

    public SQLTicket(String ticketID, String ticketUID, String category, String creatorName, String creatorID, LinkedList<String> participantsNames, LinkedList<String> participantsIDS, LinkedList<String> passwords, String status, String openDate, String closeDate, String closedByName, String closedByID) {
        this.ticketID = ticketID;
        this.ticketUID = ticketUID;
        this.category = category;
        this.creatorName = creatorName;
        this.creatorID = creatorID;
        this.participantsName = participantsNames;
        this.participantsIDS = participantsIDS;
        this.passwords = passwords;
        this.status = status;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.closedByName = closedByName;
        this.closedByID = closedByID;
    }

    public String getTicketID() {
        return ticketID;
    }

    public String getTicketUID() {
        return ticketUID;
    }

    public String getCategory() {
        return category;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public LinkedList<String> getParticipantsNames() {
        return participantsName;
    }

    public LinkedList<String> getParticipantsIDS() {
        return participantsIDS;
    }

    public LinkedList<String> getPasswords() {
        return passwords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOpenDate() {
        return openDate;
    }

    public String getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate;
    }

    public String getClosedByName() {
        return closedByName;
    }

    public void setClosedByName(String closedByName) {
        this.closedByName = closedByName;
    }

    public String getClosedByID() {
        return closedByID;
    }

    public void setClosedByID(String closedByID) {
        this.closedByID = closedByID;
    }
}
