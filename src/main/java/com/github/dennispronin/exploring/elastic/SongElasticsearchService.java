package com.github.dennispronin.exploring.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.TotalHits;

import java.io.IOException;

import static java.util.Optional.ofNullable;

public class SongElasticsearchService {

    private static final String SONGS_INDEX = "songs";
    private final ElasticsearchClient client;

    public SongElasticsearchService(ElasticsearchClient client) throws IOException {
        this.client = client;
        init();
    }

    public void searchSongByTextWildcard(String searchString) throws IOException {
        var response = client.search(s -> s
                .index(SONGS_INDEX)
                .query(q -> q.wildcard(t -> t
                        .field("text")
                        .value("*" + searchString + "*"))), Song.class);
        System.out.println("Elasticsearch searchSongByTextWildcard " + searchString + " results counts: "
                + ofNullable(response.hits().total()).map(TotalHits::value).orElse(0L));
    }

    public void searchSongByTextRegexp(String searchString) throws IOException {
        var response = client.search(s -> s
                .index(SONGS_INDEX)
                .query(q -> q.regexp(t -> t.field("text").value(".*" + searchString + ".*"))), Song.class);
        System.out.println("Elasticsearch searchSongByTextRegexp " + searchString + " results counts: "
                + ofNullable(response.hits().total()).map(TotalHits::value).orElse(0L));
    }

    public void searchSongByText(String searchString) throws IOException {
        var response = client.search(s -> s.
                index(SONGS_INDEX)
                .query(q -> q.match(t -> t
                        .field("text")
                        .query(searchString))), Song.class);
        System.out.println("Elasticsearch search " + searchString + " results counts: "
                + ofNullable(response.hits().total()).map(TotalHits::value).orElse(0L));
    }

    private void init() throws IOException {
        if (isFirstInitialization()) {
            System.out.println("SongElasticsearchService initialization started");
            createSongsIndex();
            var parser = new SongCSVParser();
            var songs = parser.parse();
            for (Song song : songs) {
                saveSong(song);
            }
            System.out.println("SongElasticsearchService initialization finished");
        }
    }

    private boolean isFirstInitialization() throws IOException {
        var countRecordsResponse = client.cat().count().valueBody().getFirst().count();
        return Long.parseLong(countRecordsResponse) == 0;
    }

    private void createSongsIndex() throws IOException {
        client.indices().create(c -> c.index(SONGS_INDEX));
    }

    private void saveSong(Song song) throws IOException {
        client.index(i -> i.index(SONGS_INDEX).document(song));
    }
}
