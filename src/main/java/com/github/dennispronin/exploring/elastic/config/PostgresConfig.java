package com.github.dennispronin.exploring.elastic.config;

import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConfig {

    private static final String URL = "jdbc:postgresql://localhost:5432/songs_db?useUnicode=yes&characterEncoding=UTF-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    public static Connection createConnection() throws SQLException {
        DriverManager.registerDriver(new Driver());
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
