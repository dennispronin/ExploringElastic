package com.github.dennispronin.exploring.elastic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static com.github.dennispronin.exploring.elastic.SqlExample.*;
import static com.github.dennispronin.exploring.elastic.config.ElasticsearchConfig.createClient;
import static com.github.dennispronin.exploring.elastic.config.PostgresConfig.createConnection;

public class Main {

    public static void main(String[] args) throws IOException, SQLException {
        var elasticService = new SongElasticsearchService(createClient());
        var postgresService = new SongPostgresService(createConnection(), REGULAR);

        var searchStrings = List.of("peace", "space", "science", "dog");

        var elasticResults = new HashMap<>();
        var postgresResults = new HashMap<>();
        for (var string : searchStrings) {
            var timeBefore = System.currentTimeMillis();
            elasticService.searchSongByTextWildcard(string);
            elasticResults.put(string, System.currentTimeMillis() - timeBefore);

            timeBefore = System.currentTimeMillis();
            postgresService.searchSongByText(string);
            postgresResults.put(string, System.currentTimeMillis() - timeBefore);
        }

        System.out.println("\nSearch speed results in milliseconds\nElasticsearch: " + elasticResults
                + "\nPostgreSQL: " + postgresResults);
    }
}
