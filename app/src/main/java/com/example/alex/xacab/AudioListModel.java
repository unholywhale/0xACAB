package com.example.alex.xacab;

import android.graphics.Bitmap;


public class AudioListModel {

    private String artist, album, title, data;

    private Integer duration, year, number;

    private long albumId;

    private Bitmap albumArt;
    public boolean isAlbum;
    public AudioListModel(String artist, String album, String title, String data, Integer duration, Integer number, Integer year, long albumId, Bitmap albumArt) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.data = data;
        this.duration = duration;
        this.number = number;
        this.year = year;
        this.albumId = albumId;
        this.albumArt = albumArt;
        this.isAlbum = false;
    }
    public AudioListModel(String artist, String album, Integer year, long albumId, Bitmap albumArt) {
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.albumId = albumId;
        this.albumArt = albumArt;
        this.isAlbum = true;
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt = albumArt;
    }
}
