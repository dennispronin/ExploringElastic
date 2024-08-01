package com.github.dennispronin.exploring.elastic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum SqlExample {
    REGULAR("songs",
            "SELECT * FROM public.songs WHERE text ILIKE ?",
            "CREATE TABLE public.songs (id BIGSERIAL PRIMARY KEY, artist VARCHAR(2048), song VARCHAR(2048), link VARCHAR(2048), text TEXT)") {
        @Override
        public PreparedStatement makePreparedStatement(Connection connection, String searchString) throws SQLException {
            var preparedStatement = connection.prepareStatement(getSearchString());
            preparedStatement.setString(1, "%" + searchString + "%");
            return preparedStatement;
        }
    },
    INDEXED("indexed_songs",
            "SELECT * FROM public.indexed_songs WHERE text ILIKE ?",
            "CREATE TABLE public.indexed_songs (id BIGSERIAL PRIMARY KEY, artist VARCHAR(2048), song VARCHAR(2048), link VARCHAR(2048), text TEXT);" +
                    "CREATE EXTENSION IF NOT EXISTS pg_trgm;" +
                    "CREATE INDEX idx_text_trgm ON public.indexed_songs USING GIST (text gist_trgm_ops);") {
        @Override
        public PreparedStatement makePreparedStatement(Connection connection, String searchString) throws SQLException {
            var preparedStatement = connection.prepareStatement(getSearchString());
            preparedStatement.setString(1, "%" + searchString + "%");
            return preparedStatement;
        }
    },
    FULL_TEXT("full_text_songs",
            "SELECT * FROM public.full_text_songs WHERE text_search @@ to_tsquery('english', ?)",
            "CREATE TABLE public.full_text_songs (id BIGSERIAL PRIMARY KEY, artist VARCHAR(2048), song VARCHAR(2048), link VARCHAR(2048), text TEXT);" +
                    "ALTER TABLE public.full_text_songs ADD COLUMN text_search TSVECTOR; " +

                    "CREATE OR REPLACE FUNCTION update_text_search() RETURNS TRIGGER AS $$\n" +
                    "BEGIN\n" +
                    "  NEW.text_search := to_tsvector('english', NEW.text);\n" +
                    "  RETURN NEW;\n" +
                    "END;\n" +
                    "$$ LANGUAGE plpgsql;\n" +

                    "CREATE TRIGGER text_search_update\n" +
                    "BEFORE INSERT OR UPDATE ON public.full_text_songs\n" +
                    "FOR EACH ROW\n" +
                    "EXECUTE FUNCTION update_text_search();\n" +

                    "CREATE INDEX idx_text_search ON public.full_text_songs USING GIN (text_search);") {
        @Override
        public PreparedStatement makePreparedStatement(Connection connection, String searchString) throws SQLException {
            var preparedStatement = connection.prepareStatement(getSearchString());
            preparedStatement.setString(1, searchString);
            return preparedStatement;
        }
    };

    private final String tableName;
    private final String searchString;
    private final String createTableString;

    SqlExample(String tableName, String searchString, String createTableString) {
        this.tableName = tableName;
        this.searchString = searchString;
        this.createTableString = createTableString;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSearchString() {
        return searchString;
    }

    public String getCreateTableString() {
        return createTableString;
    }

    public abstract PreparedStatement makePreparedStatement(Connection connection, String searchString) throws SQLException;
}
