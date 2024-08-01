package com.github.dennispronin.exploring.elastic;

import com.opencsv.bean.CsvBindByName;

public class Song {

    @CsvBindByName(column = "artist")
    private String artist;

    @CsvBindByName(column = "song")
    private String song;

    @CsvBindByName(column = "link")
    private String link;

    @CsvBindByName(column = "text")
    private String text;

    public Song() {
    }

    public Song(String artist, String song, String link, String text) {
        this.artist = artist;
        this.song = song;
        this.link = link;
        this.text = text;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Song{" +
                "artist='" + artist + '\'' +
                ", song='" + song + '\'' +
                ", link='" + link + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
