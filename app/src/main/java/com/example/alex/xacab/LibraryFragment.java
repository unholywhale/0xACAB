package com.example.alex.xacab;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.app.ListFragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LibraryFragment extends ListFragment {

    private SelectionListener mListener;
    private LayoutInflater mInflater;


    public static LibraryFragment newInstance(String param1, String param2) {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LibraryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mInflater = inflater;
        View view = mInflater.inflate(R.layout.fragment_library, null);

        String[] from = new String[] {
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists._ID
        };

        int[] to = null;

        CursorLoader cursorLoader = new CursorLoader(getActivity(), MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, from, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        setListAdapter(new LibraryAdapter(getActivity(), R.layout.fragment_library_list_item, cursor, from, to, 0));


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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onLibraryItemSelected(v);
        }
    }

    class LibraryAdapter extends SimpleCursorAdapter {

        public LibraryAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));

            CheckBox vCheckBox = (CheckBox) view.findViewById(R.id.library_artist_checkbox);
            TextView vArtist = (TextView) view.findViewById(R.id.library_artist);

            vCheckBox.setChecked(false);
            vCheckBox.setVisibility(View.INVISIBLE);
            vArtist.setText(artist);

        }
    }

}
