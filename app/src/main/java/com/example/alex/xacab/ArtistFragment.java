package com.example.alex.xacab;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.ListFragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class ArtistFragment extends ListFragment {

    private SelectionListener mListener;
    private String mArtistName;
    private ArrayList<AudioListModel> audioList = new ArrayList<>();
    private LayoutInflater mInflater;

    public static ArtistFragment newInstance(String param1, String param2) {
        ArtistFragment fragment = new ArtistFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mInflater = inflater;

        View view = mInflater.inflate(R.layout.fragment_artist, null);

        if (mArtistName == null) {
            mArtistName = "Squarepusher"; // default artist for testing
        }
        String[] from = new String[] {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };
        String where = MediaStore.Audio.Media.ARTIST + "=?";
        String[] whereArgs = {mArtistName};
        String currentAlbum = "";
        CursorLoader cursorLoader = new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, from, where, whereArgs, null);
        Cursor cursor = cursorLoader.loadInBackground();
        Integer trackNumber = 0;
        while (cursor.moveToNext()) {
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
            long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            long trackId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
/*
            Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(artworkUri, albumId);
*/

            Bitmap bitmap = null;
/*            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), albumArtUri);
                bitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_album);
                bitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, true);

            } catch (IOException e) {
                e.printStackTrace();
            }*/
            if (!currentAlbum.equals(album)) {
                AudioListModel albumAudioItem = new AudioListModel(artist, album, year, albumId, bitmap);
                audioList.add(albumAudioItem);
                currentAlbum = album;
                trackNumber = 0;
            }
            trackNumber++;
            AudioListModel audioItem = new AudioListModel(artist, album, title, data, duration, trackNumber, year, albumId, trackId);
            audioList.add(audioItem);
        }
        AudioListAdapter adapter = new AudioListAdapter(getActivity(), R.layout.fragment_artist_list_item, R.layout.fragment_artist_list_header, audioList);
        setListAdapter(adapter);

        return view;

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setArtist(String artistName) {
        mArtistName = artistName;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onArtistItemSelected(audioList.get(position));
        }
    }

    static class AudioListAdapter extends ArrayAdapter<AudioListModel> {

        Context context;
        int layoutItemResourceId, layoutHeaderResourceId;
        ArrayList<AudioListModel> rows = null;

        public AudioListAdapter(Context context, int layoutItemResourceId, int layoutHeaderResourceId, ArrayList<AudioListModel> rows) {
            super(context, layoutItemResourceId, rows);
            this.context = context;
            this.layoutItemResourceId = layoutItemResourceId;
            this.layoutHeaderResourceId = layoutHeaderResourceId;
            this.rows = rows;
        }

        private AudioListHolder getHeaderHolder(View row) {
            AudioListHolder holder = new AudioListHolder();
            holder.headerText = (TextView) row.findViewById(R.id.artist_header_text);
            holder.headerSeparator = row.findViewById(R.id.artist_header_separator);
            holder.isHeader = true;
            return holder;
        }

        private AudioListHolder getItemHolder(View row) {
            AudioListHolder holder = new AudioListHolder();
            holder.title = (TextView) row.findViewById(R.id.track_title);
            holder.trackDuration = (TextView) row.findViewById(R.id.track_duration);
            holder.trackNumber = (TextView) row.findViewById(R.id.track_number);
            holder.dividerDuration = row.findViewById(R.id.divider_duration);
            holder.isHeader = false;
            return holder;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            AudioListHolder holder = null;

            AudioListModel audioItem = rows.get(position);
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            if (row == null) {
                if (audioItem.isAlbum) {
                    row = inflater.inflate(layoutHeaderResourceId, parent, false);
                    holder = getHeaderHolder(row);
                } else {
                    row = inflater.inflate(layoutItemResourceId, parent, false);
                    holder = getItemHolder(row);
                }
                row.setTag(holder);
            } else {
                holder = (AudioListHolder) row.getTag();
                if (holder.isHeader && !audioItem.isAlbum) {
                    row = inflater.inflate(layoutItemResourceId, parent, false);
                    holder = getItemHolder(row);
                    row.setTag(holder);
                } else if (!holder.isHeader && audioItem.isAlbum) {
                    row = inflater.inflate(layoutHeaderResourceId, parent, false);
                    holder = getHeaderHolder(row);
                    row.setTag(holder);
                }
            }

            if (audioItem.isAlbum) {
                if (audioItem.getYear() != 0) {
                    holder.headerText.setText(audioItem.getAlbum() + " - " + String.valueOf(audioItem.getYear()));
                } else {
                    holder.headerText.setText(audioItem.getAlbum());
                }
            } else {
                holder.trackNumber.setText(String.valueOf(audioItem.getNumber()));
                holder.title.setText(audioItem.getTitle());
                //holder.title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                //holder.dividerDuration.setVisibility(View.VISIBLE);
                if (audioItem.getDuration() == 0) {
                    holder.trackDuration.setText("");
                } else {
                    holder.trackDuration.setText(MusicUtils.makeTimeString(context, audioItem.getDuration() / 1000));
                }

            }

            return row;
        }

        static class AudioListHolder {
            Boolean isHeader;
            TextView headerText;
            View headerSeparator;
            TextView title;
            View dividerDuration;
            TextView trackDuration;
            TextView trackNumber;
        }
    }

}
