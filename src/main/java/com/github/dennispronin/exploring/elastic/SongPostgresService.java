package com.github.dennispronin.exploring.elastic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SongPostgresService {

    private final Connection connection;
    private final SqlExample sqlExample;

    public SongPostgresService(Connection connection, SqlExample sqlExample) {
        this.connection = connection;
        this.sqlExample = sqlExample;
        init();
    }

    public void searchSongByText(String searchString) {
        try (var preparedStatement = sqlExample.makePreparedStatement(connection, searchString)) {
            var resultSet = preparedStatement.executeQuery();
            var resultsCount = 0;
            while (resultSet.next()) {
                resultsCount++;
            }
            System.out.println("PostgreSQL search " + searchString + " results counts: " + resultsCount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        if (isFirstInitialization()) {
            System.out.println("SongPostgresService initialization started");
            createSongsTable();
            var parser = new SongCSVParser();
            var songs = parser.parse();
            for (Song song : songs) {
                saveSong(song.getArtist(), song.getSong(), song.getLink(), song.getText());
            }
            System.out.println("SongPostgresService initialization finished");
        }
    }

    private boolean isFirstInitialization() {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT EXISTS (\n" +
                "    SELECT 1\n" +
                "    FROM information_schema.tables \n" +
                "    WHERE table_schema = 'public' \n" +
                "    AND table_name = '" + sqlExample.getTableName() + "'\n" +
                ");")) {
            var resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return !resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveSong(String artist, String song, String link, String text) {
        var sql = "INSERT INTO " + sqlExample.getTableName() + " (artist, song, link, text) VALUES (? , ? , ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, artist);
            preparedStatement.setString(2, song);
            preparedStatement.setString(3, link);
            preparedStatement.setString(4, text);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSongsTable() {
        try (var statement = connection.createStatement()) {
            statement.execute(sqlExample.getCreateTableString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
