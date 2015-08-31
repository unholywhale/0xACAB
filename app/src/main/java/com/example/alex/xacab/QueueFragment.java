package com.example.alex.xacab;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.ViewDragHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {SelectionListener}
 * interface.
 */
public class QueueFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private SelectionListener mListener;
    private final static int QUEUE_LOADER = 1;
    private QueueAdapter mAdapter;

    public static QueueFragment newInstance(String param1, String param2) {
        QueueFragment fragment = new QueueFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(QUEUE_LOADER, null, this);
        mAdapter = new QueueAdapter(getActivity().getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(mAdapter);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QueueFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, null);

        /*setListAdapter(new ArrayAdapter<File>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, getItems()));*/
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] columns = AudioListModel.getColumns();
        CursorLoader cursorLoader = new CursorLoader(getActivity(), QueueProvider.CONTENT_URI, columns, null, null, null);
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

    private class GetQueueTask extends AsyncTask<Void, Void, ArrayList<AudioListModel>> {


        @Override
        protected ArrayList<AudioListModel> doInBackground(Void... params) {
            return null;
        }
    }



    // Populate list with files
    private List<File> getItems() {
        String path = Environment.getExternalStorageDirectory().toString() + "/mobile/music/users/alex/music/itunes/itunes media/music/arena/contagion";
        File f = new File(path);
        List<File> files = Arrays.asList(f.listFiles());

        return files;
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
            mListener.onQueueItemSelected();
        }
    }

    static class QueueAdapter extends CursorAdapter {

        public QueueAdapter(Context context, Cursor c, int flag) {
            super(context, c, flag);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.fragment_queue_list_item, parent, false);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                TextView title = (TextView) view.findViewById(R.id.queue_title);
                TextView artist = (TextView) view.findViewById(R.id.queue_artist);
                TextView duration = (TextView) view.findViewById(R.id.queue_duration);

                title.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                artist.setText(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
                duration.setText(MusicUtils.makeTimeString(context, cursor.getInt(cursor.getColumnIndexOrThrow("duration")) / 1000));
            }
        }
    }



}
