package com.whale.xacab;

import android.app.Activity;
import android.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;


public class LibraryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int LIBRARY_LOADER = 2;
    private SelectionListener mListener;
    private LibraryAdapter mAdapter;
    private ImageButton mSwitch;
    private ListView mList;

    private class LibraryGestureHelper extends GestureHelper {

        public LibraryGestureHelper(Context context) {
            super(context);
        }

        @Override
        public void onScrollTop() {
            if (mSwitch.getVisibility() == View.INVISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mSwitch.setVisibility(View.VISIBLE);
                    }
                };
                mSwitch.animate()
                        .translationY(0)
                        .alpha(1)
                        .withStartAction(action)
                        .start();
            }
        }

        @Override
        public void onScrollBottom() {
            if (mSwitch.getVisibility() == View.VISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mSwitch.setVisibility(View.INVISIBLE);
                    }
                };
                mSwitch.animate()
                        .translationY(100)
                        .alpha(0)
                        .withEndAction(action)
                        .start();
            }
        }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        mListener.setLibraryMenu();
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }


    public static LibraryFragment newInstance(int libraryTab) {
        return new LibraryFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LibraryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, null);
        mSwitch = (ImageButton) view.findViewById(R.id.library_switch);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.openLibrary(true);
            }
        });
        mList = (ListView) view.findViewById(R.id.library_list);
        getLoaderManager().initLoader(LIBRARY_LOADER, null, this);
        mAdapter = new LibraryAdapter(getActivity().getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mList.setAdapter(mAdapter);
        mList.setOnTouchListener(new LibraryGestureHelper(getActivity().getApplicationContext()));
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onLibraryItemSelected(view);
                }
            }
        });
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
        mListener = null;
    }


    public void onListItemClick(ListView l, View v, int position, long id) {

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

        return new CursorLoader(getActivity(), uri, from, null, null, sortOrder);

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


