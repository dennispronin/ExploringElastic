package com.github.dennispronin.exploring.elastic;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class SongCSVParser {

    private static final String SONGS_CSV_FILE = "spotify_millsongdata.csv";

    public List<Song> parse() {
        try (Reader reader = new FileReader(SONGS_CSV_FILE)) {
            return new CsvToBeanBuilder<Song>(reader)
                    .withType(Song.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
