package com.example.alex.xacab;

import android.app.Activity;
import android.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {SelectionListener}
 * interface.
 */
public class QueueFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private SelectionListener mListener;
    private final static int QUEUE_LOADER = 1;
    private QueueAdapter mAdapter;
    private ListView mList;

    public static QueueFragment newInstance() {
        QueueFragment fragment = new QueueFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getLoaderManager().initLoader(QUEUE_LOADER, null, this);
        mAdapter = new QueueAdapter(getActivity().getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        super.onCreate(savedInstanceState);

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
        mListener.onQueueFragmentShow();

        mList = (ListView) view.findViewById(R.id.queue_list);

        mList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onQueueItemSelected(position);
                }
            }
        });

        mList.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListener.onQueueFragmentHide();
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

   // @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onQueueItemSelected(position);
        }
    }

    public class QueueAdapter extends CursorAdapter {

        private Integer counter = 0;

        public QueueAdapter(Context context, Cursor c, int flag) {
            super(context, c, flag);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            counter++;
            View view = LayoutInflater.from(context).inflate(R.layout.fragment_queue_list_item, parent, false);
            QueueHolder holder = new QueueHolder();
            holder.number = (TextView) view.findViewById(R.id.queue_number);
            holder.title = (TextView) view.findViewById(R.id.queue_title);
            holder.artist = (TextView) view.findViewById(R.id.queue_artist);
            holder.duration = (TextView) view.findViewById(R.id.queue_duration);
            holder.data = (TextView) view.findViewById(R.id.queue_data);
            holder.playing = (ImageView) view.findViewById(R.id.queue_playing);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                QueueHolder holder = (QueueHolder) view.getTag();
                Integer number = cursor.getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_SORT));
                holder.number.setText(number.toString());
                holder.title.setText(cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_TITLE)));
                holder.artist.setText(cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_ARTIST)));
                holder.duration.setText(MusicUtils.makeTimeString(context, cursor.getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_DURATION)) / 1000));
                holder.data.setText(cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_DATA)));
                if (cursor.getPosition() == ((MainActivity) getActivity()).getCurrentQueuePosition()) {
                    holder.playing.setVisibility(View.VISIBLE);
                    holder.number.setVisibility(View.INVISIBLE);
                } else {
                    holder.playing.setVisibility(View.INVISIBLE);
                    holder.number.setVisibility(View.VISIBLE);
                }
            }
        }

        public class QueueHolder {
            TextView number;
            TextView title;
            TextView artist;
            TextView duration;
            TextView data;
            ImageView playing;
        }
    }



}
