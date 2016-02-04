package com.whale.acab;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class LibraryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int LIBRARY_LOADER = 2;
    private SelectionListener mListener;
    private LibraryAdapter mAdapter;
    private Button mAddButton;
    private ImageButton mAddNextButton;
    private ImageButton mCheckButton;
    private ImageButton mBack;
    private ListView mList;

    private class LibraryGestureHelper extends GestureHelper {

        public LibraryGestureHelper(Context context) {
            super(context);
        }

        @Override
        public void onScrollTop() {
            if (mBack.getVisibility() == View.INVISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mBack.setVisibility(View.VISIBLE);
                    }
                };
                mBack.animate()
                        .translationY(0)
                        .alpha(1)
                        .withStartAction(action)
                        .start();
            }
        }

        @Override
        public void onScrollBottom() {
            if (mBack.getVisibility() == View.VISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mBack.setVisibility(View.INVISIBLE);
                    }
                };
                mBack.animate()
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
        mListener.setFragmentTitle(R.string.library_header);
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
        mBack = (ImageButton) view.findViewById(R.id.library_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mListener.openLibrary(true);
                getActivity().onBackPressed();
            }
        });
        mAddButton = (Button) view.findViewById(R.id.library_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItems();
                selectMode(false);
                mAdapter.uncheckAll();
            }
        });
        mAddNextButton = (ImageButton) view.findViewById(R.id.library_add_next);
        mAddNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItems(true);
                selectMode(false);
                mAdapter.uncheckAll();
            }
        });
        mCheckButton = (ImageButton) view.findViewById(R.id.library_check_all);
        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.checkAll();
            }
        });
        mList = (ListView) view.findViewById(R.id.library_list);
        getLoaderManager().initLoader(LIBRARY_LOADER, null, this);
        mAdapter = new LibraryAdapter(getActivity().getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mList.setAdapter(mAdapter);
        //mList.setOnTouchListener(new LibraryGestureHelper(getActivity().getApplicationContext()));
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

    private void addItems() {
        addItems(false);
    }

    private void addItems(boolean addNext) {
        ArrayList<AudioListModel> items = new ArrayList<>();
        ArrayList<String> checked = mAdapter.getCheckedArtists();
        String[] columns = new String[] {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media._ID
        };
        String where = "";
        boolean first = true;
        for (String artist : checked) {
            if (first) {
                first = false;
                where = MediaStore.Audio.Media.ARTIST + "=?";
                continue;
            }
            where += " OR " + MediaStore.Audio.Media.ARTIST + "=?";
        }
        String[] whereArgs = checked.toArray(new String[checked.size()]);
        String orderBy = MediaStore.Audio.Media.ARTIST + " ASC, "
                + MediaStore.Audio.Media.ALBUM_ID + " ASC, "
                + MediaStore.Audio.Media.TRACK + " ASC";
        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, where, whereArgs, orderBy);
        while (cursor.moveToNext()) {
            items.add(getItem(cursor));
        }
        AudioListModel[] itemsArray = items.toArray(new AudioListModel[items.size()]);
        mListener.addBulk(itemsArray, addNext);
    }

    private AudioListModel getItem(Cursor cursor) {
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
        long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        long trackId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

        AudioListModel audioItem = new AudioListModel(artist, album, title, data, duration, 0, year, albumId, trackId);
        return audioItem;
    }

    private void selectMode(boolean enabled) {
        if (enabled) {
            if (mAddButton.getVisibility() == View.INVISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mAddButton.setVisibility(View.VISIBLE);
                    }
                };
                mAddButton.animate()
                        //.translationY(0)
                        .alpha(1)
                        .withStartAction(action)
                        .start();
            }
            if (mAddNextButton.getVisibility() == View.INVISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mAddNextButton.setVisibility(View.VISIBLE);
                    }
                };
                mAddNextButton.animate()
                        //.translationY(0)
                        .alpha(1)
                        .withStartAction(action)
                        .start();
            }
            if (mCheckButton.getVisibility() == View.INVISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mCheckButton.setVisibility(View.VISIBLE);
                    }
                };
                mCheckButton.animate()
                        .alpha(1)
                        .withStartAction(action)
                        .start();
            }
        } else {
            if (mAddButton.getVisibility() == View.VISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mAddButton.setVisibility(View.INVISIBLE);
                    }
                };
                mAddButton.animate()
                        //.translationY(100)
                        .alpha(0)
                        .withEndAction(action)
                        .start();
            }
            if (mAddNextButton.getVisibility() == View.VISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mAddNextButton.setVisibility(View.INVISIBLE);
                    }
                };
                mAddNextButton.animate()
                        //.translationY(100)
                        .alpha(0)
                        .withEndAction(action)
                        .start();
            }
            if (mCheckButton.getVisibility() == View.VISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mCheckButton.setVisibility(View.INVISIBLE);
                    }
                };
                mCheckButton.animate()
                        .alpha(0)
                        .withEndAction(action)
                        .start();
            }
        }
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

        private ArrayList<String> checkedArtists = new ArrayList<>();

        public LibraryAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        public ArrayList<String> getCheckedArtists() {
            return checkedArtists;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.fragment_library_list_item, parent, false);
            LibraryHolder holder = new LibraryHolder();
            holder.checkbox = (CheckBox) view.findViewById(R.id.library_checkbox);
            holder.artist = (TextView) view.findViewById(R.id.library_artist);
            view.setTag(holder);
            return view;
        }

        private void setChecked(String artist, boolean checked) {
            if (checked) {
                if (!checkedArtists.contains(artist)) {
                    checkedArtists.add(artist);
                }
            } else {
                checkedArtists.remove(artist);
            }
        }

        private void checkAll() {
            if (checkedArtists.size() == this.getCount()) {
                uncheckAll();
            } else {
                checkedArtists.clear();
                for (int i = 0; i < this.getCount(); i++) {
                    View view = this.getView(i, null, null);
                    LibraryHolder holder = (LibraryHolder) view.getTag();
                    checkedArtists.add(holder.artist.getText().toString());
                }
                notifyDataSetChanged();
            }
        }

        private void uncheckAll() {
            checkedArtists.clear();
            notifyDataSetChanged();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                LibraryHolder holder = (LibraryHolder) view.getTag();
                if (holder != null) {
                    String sArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST));
                    holder.artist.setText(sArtist);
                    holder.checkbox.setTag(sArtist);
                    holder.checkbox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CheckBox cb = (CheckBox) view;
                            String artist = (String) cb.getTag();
                            setChecked(artist, cb.isChecked());
                            if (checkedArtists.size() == 0) {
                                selectMode(false);
                            } else {
                                selectMode(true);
                            }
                        }
                    });
                    if (checkedArtists.contains(sArtist)) {
                        holder.checkbox.setChecked(true);
                    } else {
                        holder.checkbox.setChecked(false);
                    }
                }
            }
        }

        class LibraryHolder {
            CheckBox checkbox;
            TextView artist;
        }
    }
}


