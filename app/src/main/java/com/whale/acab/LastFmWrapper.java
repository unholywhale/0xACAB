package com.whale.acab;

import android.os.AsyncTask;
import android.util.Log;

import java.io.Serializable;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import de.umass.lastfm.scrobble.Scrobbler;

public class LastFmWrapper implements Serializable {
    public static final String TAG_ARTIST = "artist";
    public static final String TAG_TITLE = "title";
    public static final String TAG_ALBUM = "album";
    private AudioListModel currentTrack;
    private MainActivity activity;
    private String userAgent;
    private String apiKey;
    private String apiSecret;
    private String user;
    private String password;
    private Session session;
    private SelectionListener callback;
    private Scrobbler scrobbler;
    private boolean isPaused = false;


    public LastFmWrapper(MainActivity activity, String userAgent, String apiKey, String apiSecret, SelectionListener callback, boolean debugMode) {
        this.activity = activity;
        this.userAgent = userAgent;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.callback = callback;
        Caller.getInstance().setUserAgent(userAgent);
        Caller.getInstance().setCache(null);
        //Caller.getInstance().setDebugMode(debugMode);
    }

    public void authorize(String user, String password) {
        this.user = user;
        this.password = password;
        new InitializeLastFmTask().execute();
    }

    public void authorize(String sessionKey) {
        this.session = Session.createSession(apiKey, apiSecret, sessionKey);
    }

    public void stop() {
        currentTrack = null;
    }

    public void pause() {
        isPaused = true;
    }

    public void unpause() {
        isPaused = false;
    }

    private void updatePlaying() {
        if (session != null && currentTrack != null) {
            new UpdatePlayingTask().execute();
        }
    }

    public void startScrobbling(AudioListModel track) {
        if (session == null) {
            return;
        }
        if (isPaused) {
            isPaused = false;
        }
        if (currentTrack != track) {
            currentTrack = track;
            updatePlaying();
            new ScrobblingTask().execute();
        }
    }

    private class ScrobblingTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            long startTime = System.currentTimeMillis();
            int scrobbleDuration = currentTrack.getDuration() / 2;
            long currentTrackId = currentTrack.getTrackId();
            long pauseTime = 0;
            boolean scrobbled = false;
            while (!scrobbled) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isPaused) {
                    pauseTime += 1000;
                    Log.d("LSTATUS", "pause");
                } else {
                    Log.d("LSTATUS", "ok");
                    if (currentTrack == null || currentTrackId != currentTrack.getTrackId()) {
                        Log.d("LFAIL", "fail");
                        break;
                    }
                }
                if (System.currentTimeMillis() - pauseTime > startTime + scrobbleDuration || System.currentTimeMillis() - pauseTime > startTime + (60 * 4 * 1000)) {
                    scrobbled = true;
                    break;
                }
            }
//            while (System.currentTimeMillis() - pauseTime < startTime + scrobbleDuration || System.currentTimeMillis() - pauseTime < startTime + (60 * 4 * 1000)) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (isPaused) {
//                    pauseTime += 1000;
//                    Log.d("LSTATUS", "pause");
//                } else {
//                    Log.d("LSTATUS", "ok");
//                    if (currentTrack == null || currentTrackId != currentTrack.getTrackId()) {
//                        Log.d("LFAIL", "fail");
//                        return false;
//                    }
//                    break;
//                }
//            }
            if (scrobbled) {
                int now = (int) (System.currentTimeMillis() / 1000);
                ScrobbleData data = new ScrobbleData(currentTrack.getArtist(), currentTrack.getTitle(), now, 0, currentTrack.getAlbum(), currentTrack.getArtist(), "3fba46e5-a32c-460d-8548-65cdfeeebf52", currentTrack.getNumber(), null, true);
                ScrobbleResult result = Track.scrobble(data, session);
                Boolean ok = result.isSuccessful();
                Boolean ign = result.isIgnored();

                Log.d("SCROBBLE", ok.toString());
                Log.d("SCROBBLEIGN", ign.toString());
            }
            return true;
        }
    }

    private class UpdatePlayingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String artist = currentTrack.getArtist();
            String title = currentTrack.getTitle();
            ScrobbleResult result = Track.updateNowPlaying(artist, title, session);
            Boolean success = result.isSuccessful();
            Log.d("LASTUPDATE", success.toString());
            return null;
        }
    }

    private class InitializeLastFmTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            int c = 0;
            while (session == null) {
                try {
                    session = Authenticator.getMobileSession(user, password, apiKey, apiSecret);
                    Thread.sleep(500);
                    if (session != null) {
                        callback.saveLastFmSession(session.getKey(), user);
                        Log.d("LSESSION", "ok");
                    } else {
                        Log.d("LSESSION", "fail");
                        if (c == 3) {
                            break;
                        } else {
                            c++;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
