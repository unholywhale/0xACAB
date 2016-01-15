package com.whale.xacab;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {SelectionListener}
 * interface.
 */
public class QueueFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private boolean isOptionsVisible = false;
    private View prevView;
    private SelectionListener mListener;
    private final static int QUEUE_LOADER = 1;
    private QueueAdapter mAdapter;
    private DragSortListView mList;
    private Button mAdd;

    public static QueueFragment newInstance() {
        QueueFragment fragment = new QueueFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QueueFragment() {
    }

    private class QueueGestureHelper extends GestureHelper {

        public QueueGestureHelper(Context context) {
            super(context);
        }

        @Override
        public void onScrollTop() {
            if (mAdd.getVisibility() == View.INVISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mAdd.setVisibility(View.VISIBLE);
                        invalidateOptions();
                    }
                };
                mAdd.animate()
                        .translationY(0)
                        .alpha(1)
                        .withStartAction(action)
                        .start();
            }
        }

        @Override
        public void onScrollBottom() {
            if (mAdd.getVisibility() == View.VISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mAdd.setVisibility(View.INVISIBLE);
                        invalidateOptions();
                    }
                };
                mAdd.animate()
                        .translationY(100)
                        .alpha(0)
                        .withEndAction(action)
                        .start();
            }
        }
    }

    private DragSortListView.DropListener mDropListener = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            int current = mListener.getCurrentQueuePosition();
            if (current == from) {
                mListener.setCurrentQueuePosition(to);
            } else if (current > from && current < to) {
                mListener.setCurrentQueuePosition(current - 1);
            } else if (current > to && current < from) {
                mListener.setCurrentQueuePosition(current + 1);
            }
            mAdapter.onDrop(from, to);
            mListener.invalidateQueue();

        }
    };

    private DragSortListView.RemoveListener mRemoveListener = new DragSortListView.RemoveListener() {

        @Override
        public void remove(int i) {

            mAdapter.onRemove(i);
            mList.invalidateViews();
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, null);
        //mListener.onQueueFragmentShow();

        getLoaderManager().initLoader(QUEUE_LOADER, null, this);
        mAdapter = new QueueAdapter(getActivity().getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mList = (DragSortListView) view.findViewById(R.id.queue_list);


        mList.setOnTouchListener(new QueueGestureHelper(getActivity().getApplicationContext()));

        mList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    if (view != prevView) {
                        invalidateOptions();
                    }
                    mListener.onQueueItemSelected(position);
                }
            }
        });

        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                toggleOptions(view);
                return true;
            }
        });


        mAdd = (Button) view.findViewById(R.id.queue_add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onQueueAdd();
            }
        });

        mList.setAdapter(mAdapter);
        return view;
    }

    private void invalidateOptions() {
        if (prevView != null) {
            ImageView prevInfo = (ImageView) prevView.findViewById(R.id.queue_info);
            ImageView prevDelete = (ImageView) prevView.findViewById(R.id.queue_delete);
            TextView prevDuration = (TextView) prevView.findViewById(R.id.queue_duration);

            prevDuration.setVisibility(View.VISIBLE);
            prevInfo.setVisibility(View.INVISIBLE);
            prevDelete.setVisibility(View.INVISIBLE);
            prevView = null;
            isOptionsVisible = false;
        }
    }

    private void toggleOptions(View view) {
        if (isOptionsVisible) {
            invalidateOptions();
        }
        isOptionsVisible = true;
        prevView = view;
        ImageView info = (ImageView) view.findViewById(R.id.queue_info);
        ImageView delete = (ImageView) view.findViewById(R.id.queue_delete);
        TextView duration = (TextView) view.findViewById(R.id.queue_duration);

        duration.setVisibility(View.INVISIBLE);
        info.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
    }

    public QueueAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onStart() {
        mListener.setQueueMenu();
        mListener.checkEmpty();
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mListener.onQueueFragmentHide();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] columns = AudioListModel.getColumns();
        CursorLoader cursorLoader = new CursorLoader(getActivity(), QueueProvider.CONTENT_URI, columns, null, null, QueueDB.KEY_SORT);
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

    public void reorderMode(boolean enabled) {
        if (enabled) {
            mList.setDropListener(mDropListener);
            mList.setRemoveListener(mRemoveListener);
            DragSortController controller = new DragSortController(mList);
            controller.setDragHandleId(R.id.queue_number);
            controller.setBackgroundColor(R.color.background_color);
            mList.setFloatViewManager(controller);
            mList.setOnTouchListener(controller);
        } else {
            mList.setOnTouchListener(new QueueGestureHelper(getActivity().getApplicationContext()));
        }
    }

    public class QueueAdapter extends CursorAdapter implements RemoveListener, DropListener {

        private Integer counter = 0;
        private boolean mCheckBoxVisible = false;
        private HashMap<Integer, QueueHashHolder>  hashMapChecked = new HashMap<>();

        @Override
        public void onDrop(int from, int to) {
            ArrayList<ContentValues> values = new ArrayList<>();
            ContentValues cv;
            Cursor cursor = getCursor();
            int start, end, inc;
            // If dragging down, sort + 1, otherwise sort - 1
            if (from < to) {
                start = from;
                end = to;
                inc = 1;
            } else {
                start = to;
                end = from;
                inc = -1;
            }
            for (int i = start; i <= end; i++) {
                cursor.moveToPosition(i);
                cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, cv);
                values.add(cv);
            }

            synchronized (this) {
                for (ContentValues v : values) {
                    long sort = v.getAsLong(QueueDB.KEY_SORT);
                    v.remove(QueueDB.KEY_SORT);
                    if (sort - 1 == from) {
                        v.put(QueueDB.KEY_SORT, to + 1);
                    } else {
                        v.put(QueueDB.KEY_SORT, sort - inc);
                    }
                    Long id = v.getAsLong(QueueDB.KEY_ID);
                    v.remove(QueueDB.KEY_ID);
                    String where = QueueDB.KEY_ID + "=?";
                    String[] args = { id.toString() };
                    getActivity().getContentResolver().update(QueueProvider.CONTENT_URI, v, where, args);
                }
            }
            int j = 0;
        }

        @Override
        public void onRemove(int which) {

        }

        public class QueueHashHolder {
            Long id;
            Integer position;
            Boolean isChecked;
        }

        public QueueAdapter(Context context, Cursor c, int flag) {
            super(context, c, flag);
        }

        public HashMap<Integer, QueueHashHolder> getHashMapChecked() {
            return hashMapChecked;
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
            holder.checked = (CheckBox) view.findViewById(R.id.queue_checked);
            holder.checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    QueueHashHolder queueHashHolder = (QueueHashHolder) buttonView.getTag();
                    if (queueHashHolder.position != ListView.INVALID_POSITION) {
                        if (isChecked) {
                            queueHashHolder.isChecked = isChecked;
                            hashMapChecked.put(queueHashHolder.position, queueHashHolder);
                        } else if (hashMapChecked.get(queueHashHolder.position) != null){
                            hashMapChecked.remove(queueHashHolder.position);
                        }
                    }
                }
            });
            holder.delete = (ImageView) view.findViewById(R.id.queue_delete);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    QueueHashHolder deleteHashHolder = (QueueHashHolder) view.getTag();
                    hashMapChecked.clear();
                    hashMapChecked.put(deleteHashHolder.position, deleteHashHolder);
                    mListener.deleteSelected();
                }
            });
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                QueueHolder holder = (QueueHolder) view.getTag();
                Integer number = cursor.getPosition() + 1;//getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_SORT));
                holder.number.setText(number.toString());
                holder.title.setText(cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_TITLE)));
                holder.artist.setText(cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_ARTIST)));
                holder.duration.setText(MusicUtils.makeTimeString(context, cursor.getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_DURATION)) / 1000));
                holder.data.setText(cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_DATA)));
                QueueHashHolder deleteHashHolder = new QueueHashHolder();
                deleteHashHolder.id = cursor.getLong(cursor.getColumnIndexOrThrow(QueueDB.KEY_ID));
                deleteHashHolder.position = cursor.getPosition();
                holder.delete.setTag(deleteHashHolder);
                if (mCheckBoxVisible) {
                    holder.playing.setVisibility(View.INVISIBLE);
                    holder.number.setVisibility(View.INVISIBLE);
                    holder.checked.setVisibility(View.VISIBLE);
                    QueueHashHolder queueHashHolder = hashMapChecked.get(cursor.getPosition());
                    if (queueHashHolder == null) {
                        queueHashHolder = new QueueHashHolder();
                        queueHashHolder.id = cursor.getLong(cursor.getColumnIndexOrThrow(QueueDB.KEY_ID));
                        queueHashHolder.position = cursor.getPosition();
                        queueHashHolder.isChecked = false;
                    }
                    Boolean checked = queueHashHolder.isChecked;
                    holder.checked.setTag(queueHashHolder);
                    if (checked != null) {
                        holder.checked.setChecked(checked);
                    } else {
                        holder.checked.setChecked(false);
                    }
                } else {
                    holder.checked.setVisibility(View.INVISIBLE);
                    holder.checked.setChecked(false);
                    if (cursor.getPosition() == (mListener.getCurrentQueuePosition())) {
                        holder.playing.setVisibility(View.VISIBLE);
                        holder.number.setVisibility(View.INVISIBLE);
                    } else {
                        holder.playing.setVisibility(View.INVISIBLE);
                        holder.number.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        public void setCheckboxVisibility(boolean visibility) {
            mCheckBoxVisible = visibility;
            if (!mCheckBoxVisible) {
                hashMapChecked.clear();
            }
        }

        public class QueueHolder {
            TextView number;
            TextView title;
            TextView artist;
            TextView duration;
            TextView data;
            ImageView playing;
            CheckBox checked;
            ImageView delete;
            ImageView info;
        }
    }

}
