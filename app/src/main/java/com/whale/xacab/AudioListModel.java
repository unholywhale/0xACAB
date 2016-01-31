package com.whale.xacab;

import android.graphics.Bitmap;


public class AudioListModel {

    private String artist, album, title, data;

    private int duration, year, number, sort;

    private long albumId;
    private long trackId;

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    private long dbId;

    private Bitmap albumArt;
    public boolean isAlbum;

    public AudioListModel(String artist, String album, String title, String data, int duration, int number, int year, long albumId, long trackId) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.data = data;
        this.duration = duration;
        this.number = number;
        this.year = year;
        this.albumId = albumId;
        this.trackId = trackId;
        this.sort = -1;
        this.isAlbum = false;
    }

    public AudioListModel(String artist, String album, String title, String data, int duration, int number, int year, long albumId, long trackId, int sort) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.data = data;
        this.duration = duration;
        this.number = number;
        this.year = year;
        this.albumId = albumId;
        this.trackId = trackId;
        this.sort = sort;
        this.isAlbum = false;
    }

    public AudioListModel(String artist, String album, int year, long albumId, Bitmap albumArt) {
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.albumId = albumId;
        this.albumArt = albumArt;
        this.isAlbum = true;
    }

    public static String[] getColumns() {
        String[] columns = new String[] {
                "artist",
                "album",
                "title",
                "data",
                "duration",
                "number",
                "year",
                "album_id",
                "track_id",
                "sort",
                "_id"

        };

        return columns;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

}
