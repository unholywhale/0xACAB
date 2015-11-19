package com.whale.xacab;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.ListFragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;


public class LibraryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int LIBRARY_LOADER = 2;
    private SelectionListener mSelectionListener;
    private LayoutInflater mInflater;
    private LibraryAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSelectionListener = (SelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LIBRARY_LOADER, null, this);
        mAdapter = new LibraryAdapter(getActivity().getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }


    public static LibraryFragment newInstance(int libraryTab) {
        LibraryFragment fragment = new LibraryFragment();
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
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSelectionListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mSelectionListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mSelectionListener.onLibraryItemSelected(v);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] from;
        Uri uri;
        String sortOrder;
        from = new String[] {
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists._ID
        };
        uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        sortOrder = MediaStore.Audio.Artists.ARTIST;

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, from, null, null, sortOrder);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    class LibraryAdapter extends CursorAdapter {

//        Character mFirstLetter;

        public LibraryAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
           // String sArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST));
            View view;
//            if (mFirstLetter == null) {
//                mFirstLetter = sArtist.charAt(0);
                view = LayoutInflater.from(context).inflate(R.layout.fragment_library_list_item, parent, false);
//            } else if (mFirstLetter != sArtist.charAt(0)) {
//                mFirstLetter = sArtist.charAt(0);
//                view = LayoutInflater.from(context).inflate(R.layout.fragment_library_list_item_with_header, parent, false);
//            } else {
//                view = LayoutInflater.from(context).inflate(R.layout.fragment_library_list_item, parent, false);
//            }
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String sArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST));
//            TextView header = (TextView) view.findViewById(R.id.library_header);
//            if (header != null) {
//                Character c = sArtist.charAt(0);
//                header.setText(c.toString());
//            }
            TextView artist = (TextView) view.findViewById(R.id.library_artist);
            artist.setText(sArtist);
        }
    }
}


