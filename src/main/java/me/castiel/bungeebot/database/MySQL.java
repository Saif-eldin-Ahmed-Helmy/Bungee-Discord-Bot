package me.castiel.bungeebot.database;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.castiel.bungeebot.types.Module;
import me.castiel.bungeebot.types.SQLInviter;
import me.castiel.bungeebot.types.SQLTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MySQL extends Module {

    private HikariDataSource dataSource;
    private final Gson gson;

    public MySQL(String ip, String port, String username, String password, String database) {
        gson = new Gson();
        try {
            HikariConfig config = new HikariConfig();
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("BungeeBot Pool");
            //config.setDriverClassName("com.mysql.jdbc.Driver");

            boolean useSSL = false;
            boolean publicKeyRetrieval = true;

            config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=" + useSSL);
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=%b",
                    ip, Integer.parseInt(port), database, useSSL, publicKeyRetrieval));
            config.setUsername(username);
            config.setPassword(password);
            config.setMinimumIdle(5);
            config.setMaximumPoolSize(16);
            config.setConnectionTimeout(60000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("characterEncoding", "utf8");
            config.addDataSourceProperty("useUnicode", "true");

            dataSource = new HikariDataSource(config);
            getLogger().info("Connected to the database!");

            execute("CREATE TABLE IF NOT EXISTS INVITESTBL(" +
                    "ID VARCHAR(18) NOT NULL, " +
                    "INVITED_BY TEXT NOT NULL, " +
                    "INVITED_NAMES TEXT NOT NULL, " +
                    "INVITED_IDS TEXT NOT NULL, " +
                    "INVITED_JOIN_TIMESTAMPS TEXT NOT NULL, " +
                    "EXTRA_INVITES INT NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(ID))");

            execute("CREATE TABLE IF NOT EXISTS TICKETSTBL(" +
                    "ID VARCHAR(18) NOT NULL, " +
                    "UID TEXT NOT NULL, " +
                    "CATEGORY TEXT NOT NULL, " +
                    "CREATOR_NAME TEXT NOT NULL, " +
                    "CREATOR_ID TEXT NOT NULL, " +
                    "PARTICIPANTS_NAMES TEXT NOT NULL, " +
                    "PARTICIPANTS_IDS TEXT NOT NULL, " +
                    "PASSWORDS TEXT NOT NULL, " +
                    "STATUS TEXT NOT NULL, " +
                    "OPEN_DATE TEXT NOT NULL, " +
                    "CLOSE_DATE TEXT NOT NULL, " +
                    "CLOSED_BY_NAME TEXT NOT NULL, " +
                    "CLOSED_BY_ID TEXT NOT NULL, " +
                    "PRIMARY KEY(ID))");

            getLogger().info("Created the MySQL tables");
        } catch (Throwable error) {
            getLogger().severe("Failed to connect to the database :(");
            error.printStackTrace();
        }
    }

    public void insertTicket(SQLTicket ticket) {
        Preconditions.checkNotNull(dataSource, "ERROR database session is NULL.");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement
                    ("INSERT INTO TICKETSTBL " +
                            "(ID, " +
                            "UID, " +
                            "CATEGORY, " +
                            "CREATOR_NAME, " +
                            "CREATOR_ID, " +
                            "PARTICIPANTS_NAMES, " +
                            "PARTICIPANTS_IDS, " +
                            "PASSWORDS, " +
                            "STATUS, " +
                            "OPEN_DATE, " +
                            "CLOSE_DATE, " +
                            "CLOSED_BY_NAME, " +
                            "CLOSED_BY_ID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "ID=VALUES(ID), " +
                            "UID=VALUES(UID), " +
                            "CATEGORY=VALUES(CATEGORY), " +
                            "CREATOR_NAME=VALUES(CREATOR_NAME), " +
                            "CREATOR_ID=VALUES(CREATOR_ID), " +
                            "PARTICIPANTS_NAMES=VALUES(PARTICIPANTS_NAMES), " +
                            "PARTICIPANTS_IDS=VALUES(PARTICIPANTS_IDS), " +
                            "PASSWORDS=VALUES(PASSWORDS), " +
                            "STATUS=VALUES(STATUS), " +
                            "OPEN_DATE=VALUES(OPEN_DATE), " +
                            "CLOSE_DATE=VALUES(CLOSE_DATE), " +
                            "CLOSED_BY_NAME=VALUES(CLOSED_BY_NAME), " +
                            "CLOSED_BY_ID=VALUES(CLOSED_BY_ID)");

            statement.setString(1, ticket.getTicketID());
            statement.setString(2, ticket.getTicketUID());
            statement.setString(3, ticket.getCategory());
            statement.setString(4, ticket.getCreatorName());
            statement.setString(5, ticket.getCreatorID());
            statement.setString(6, gson.toJson(ticket.getParticipantsNames()));
            statement.setString(7, gson.toJson(ticket.getParticipantsIDS()));
            statement.setString(8, gson.toJson(ticket.getPasswords()));
            statement.setString(9, ticket.getStatus());
            statement.setString(10, ticket.getOpenDate());
            statement.setString(11, ticket.getCloseDate());
            statement.setString(12, ticket.getClosedByName());
            statement.setString(13, ticket.getClosedByID());
            statement.execute();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Optional<SQLTicket>> getTicket(String id) {
        CompletableFuture<Optional<SQLTicket>> completableFuture = new CompletableFuture<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT * FROM TICKETSTBL WHERE (ID=? OR UID=?)");
            statement.setString(1, id);
            statement.setString(2, id);
            ResultSet set = statement.executeQuery();
            SQLTicket sqlTicket = null;
            while (set.next()) {
                String ticketID = set.getString("ID");
                String ticketUID = set.getString("UID");
                String category = set.getString("CATEGORY");
                String creatorName = set.getString("CREATOR_NAME");
                String creatorID = set.getString("CREATOR_ID");
                LinkedList<String> participantsNames = gson.fromJson(set.getString("PARTICIPANTS_NAMES"), LinkedList.class);
                LinkedList<String> participantsIDS = gson.fromJson(set.getString("PARTICIPANTS_IDS"), LinkedList.class);
                LinkedList<String> passwords = gson.fromJson(set.getString("PASSWORDS"), LinkedList.class);
                String status = set.getString("STATUS");
                String openDate = set.getString("OPEN_DATE");
                String closeDate = set.getString("CLOSE_DATE");
                String closedByName = set.getString("CLOSED_BY_NAME");
                String closedById = set.getString("CLOSED_BY_ID");
                sqlTicket = new SQLTicket(ticketID, ticketUID, category, creatorName, creatorID, participantsNames, participantsIDS, passwords, status, openDate, closeDate, closedByName, closedById);
            }
            set.close();
            completableFuture.complete(sqlTicket == null ? Optional.empty() : Optional.of(sqlTicket));
            return completableFuture;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        completableFuture.complete(Optional.empty());
        return completableFuture;
    }

    public CompletableFuture<Integer> getAmountOfTickets() {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT COUNT(*) FROM TICKETSTBL");
            ResultSet set = statement.executeQuery();
            set.next();
            int rows = set.getInt(1);
            set.close();
            connection.close();
            completableFuture.complete(rows);
            return completableFuture;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        completableFuture.complete(0);
        return completableFuture;
    }

    public void insertInviter(SQLInviter inviter) {
        Preconditions.checkNotNull(dataSource, "ERROR database session is NULL.");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement
                    ("INSERT INTO INVITESTBL " +
                            "(ID, " +
                            "INVITED_NAMES, " +
                            "INVITED_IDS, " +
                            "INVITED_JOIN_TIMESTAMPS, " +
                            "EXTRA_INVITES) " +
                            "VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "ID=VALUES(ID), " +
                            "INVITED_NAMES=VALUES(INVITED_NAMES), " +
                            "INVITED_IDS=VALUES(INVITED_IDS), " +
                            "INVITED_JOIN_TIMESTAMPS=VALUES(INVITED_JOIN_TIMESTAMPS), " +
                            "EXTRA_INVITES=VALUES(EXTRA_INVITES)");

            statement.setString(1, inviter.getId());
            statement.setString(2, gson.toJson(inviter.getInvitedNames()));
            statement.setString(3, gson.toJson(inviter.getInvitedIDS()));
            statement.setString(4, gson.toJson(inviter.getInvitedJoinTimestamps()));
            statement.setInt(5, inviter.getExtraInvites());
            statement.execute();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<SQLInviter> getInviter(String id) {
        CompletableFuture<SQLInviter> completableFuture = new CompletableFuture<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT * FROM INVITESTBL WHERE ID=?");
            statement.setString(1, id);
            ResultSet set = statement.executeQuery();
            SQLInviter sqlInviter = null;
            while (set.next()) {
                String invitedBy = set.getString("INVITED_BY");
                LinkedList<String> invitedNames = gson.fromJson(set.getString("INVITED_NAMES"), LinkedList.class);
                LinkedList<String> invitedIDS = gson.fromJson(set.getString("INVITED_IDS"), LinkedList.class);
                LinkedList<String> invitedJoinTimeStamps = gson.fromJson(set.getString("INVITED_JOIN_TIMESTAMPS"), LinkedList.class);
                int extraInvites = set.getInt("EXTRA_INVITES");
                sqlInviter = new SQLInviter(id, invitedBy, invitedNames, invitedIDS, invitedJoinTimeStamps, extraInvites);
            }
            set.close();
            completableFuture.complete(sqlInviter == null ? new SQLInviter(id, "", new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), 0) : sqlInviter);
            return completableFuture;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        completableFuture.complete(new SQLInviter(id, "", new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), 0));
        return completableFuture;
    }

    public void execute(String sql) throws SQLException {
        Preconditions.checkNotNull(dataSource, "ERROR database session is NULL.");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            statement.close();
        }
    }

    public void close() throws SQLException {
        Preconditions.checkNotNull(dataSource, "ERROR database session is NULL.");
        if (!dataSource.isClosed())
            dataSource.close();
    }
}