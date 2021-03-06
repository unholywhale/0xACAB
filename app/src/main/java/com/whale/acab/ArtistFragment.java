package com.whale.acab;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class ArtistFragment extends Fragment {

    public static final String ARTIST_NAME = "ARTIST_NAME";
    private int nextCounter = 0;
    private SelectionListener mListener;
    private String mArtistName;
    private ArrayList<AudioListModel> audioList = new ArrayList<>();
    private LayoutInflater mInflater;
    private Button mAddButton;
    private ImageButton mAddNextButton;
    private ImageButton mCheckButton;
    private ImageButton mBack;
    private ListView mList;
    private AudioListAdapter mAdapter;

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

    private class ArtistGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private ListView mListView;
        private ArtistFragment mFragment;

        public ArtistGestureListener(Fragment fragment, ListView l) {
            try {
                mFragment = (ArtistFragment) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString());
            }
            this.mListView = l;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            if (Math.abs(distanceX) > 30) {
//                return true;
//            }
            if (Math.abs(distanceY) > 30) {
                if (distanceY < 0) {  // scroll top
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
                } else { // scroll bottom
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
            return super.onScroll(e1, e2, distanceX, distanceY);
        }



        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffY) < 80) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        result = true;
                        int position = mListView.pointToPosition((int) e1.getX(),(int) e1.getY());
                        View v = mListView.getChildAt(position - mListView.getFirstVisiblePosition());
                        if (diffX > 0) {
                            mFragment.addNext(position);
                            //mHelper.onSwipeRight();
                        } else {
                            mFragment.addFirst(position);
                            //mHelper.onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            //mHelper.onSwipeBottom();
                        } else {
                            //mHelper.onSwipeTop();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }

    private void animateLeft(int position) {
        int itemPosition = position - mList.getFirstVisiblePosition();
        View view = mList.getChildAt(itemPosition);
        ArrayList<ObjectAnimator> animators = new ArrayList<>();
        if (view.getId() == R.id.artist_list_header_layout) {
            for (int i = itemPosition + 1; i <= mList.getLastVisiblePosition() - mList.getFirstVisiblePosition(); i++) {
                View item = mList.getChildAt(i);
                if (item.getId() == R.id.artist_list_header_layout) {
                    break;
                }
                ImageView itemLeftIndicator = (ImageView) item.findViewById(R.id.artist_left_indicator);
                TextView itemNumber = (TextView) item.findViewById(R.id.track_number);
                ObjectAnimator itemArrowAnimation = ObjectAnimator.ofFloat(itemLeftIndicator, "alpha", 1f);
                itemArrowAnimation.setRepeatCount(1);
                itemArrowAnimation.setRepeatMode(ValueAnimator.REVERSE);
                itemArrowAnimation.setDuration(500);
                animators.add(itemArrowAnimation);
                ObjectAnimator itemNumberAnimation = ObjectAnimator.ofFloat(itemNumber, "alpha", 0f);
                itemNumberAnimation.setRepeatCount(1);
                itemNumberAnimation.setRepeatMode(ValueAnimator.REVERSE);
                itemNumberAnimation.setDuration(500);
                animators.add(itemNumberAnimation);
            }
            ObjectAnimator[] a = animators.toArray(new ObjectAnimator[animators.size()]);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(a);
            animatorSet.start();
        } else {
            ImageView leftIndicator = (ImageView) view.findViewById(R.id.artist_left_indicator);
            TextView number = (TextView) view.findViewById(R.id.track_number);
            ObjectAnimator arrowAnimation = ObjectAnimator.ofFloat(leftIndicator, "alpha", 1f);
            arrowAnimation.setRepeatCount(1);
            arrowAnimation.setRepeatMode(ValueAnimator.REVERSE);
            arrowAnimation.setDuration(500);
            ObjectAnimator numberAnimation = ObjectAnimator.ofFloat(number, "alpha", 0f);
            numberAnimation.setRepeatCount(1);
            numberAnimation.setRepeatMode(ValueAnimator.REVERSE);
            numberAnimation.setDuration(500);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(arrowAnimation, numberAnimation);
            animatorSet.start();
        }
    }

    private void animateRight(int position) {
        int itemPosition = position - mList.getFirstVisiblePosition();
        View view = mList.getChildAt(itemPosition);
        ArrayList<ObjectAnimator> animators = new ArrayList<>();
        if (view.getId() == R.id.artist_list_header_layout) {
            for (int i = itemPosition + 1; i <= mList.getLastVisiblePosition() - mList.getFirstVisiblePosition(); i++) {
                View item = mList.getChildAt(i);
                if (item.getId() == R.id.artist_list_header_layout) {
                    break;
                }
                ImageView itemRightIndicator = (ImageView) item.findViewById(R.id.artist_right_indicator);
                TextView itemDuration = (TextView) item.findViewById(R.id.track_duration);
                ObjectAnimator itemArrowAnimation = ObjectAnimator.ofFloat(itemRightIndicator, "alpha", 1f);
                itemArrowAnimation.setRepeatCount(1);
                itemArrowAnimation.setRepeatMode(ValueAnimator.REVERSE);
                itemArrowAnimation.setDuration(500);
                animators.add(itemArrowAnimation);
                ObjectAnimator itemDurationAnimation = ObjectAnimator.ofFloat(itemDuration, "alpha", 0f);
                itemDurationAnimation.setRepeatCount(1);
                itemDurationAnimation.setRepeatMode(ValueAnimator.REVERSE);
                itemDurationAnimation.setDuration(500);
                animators.add(itemDurationAnimation);
            }
            ObjectAnimator[] a = animators.toArray(new ObjectAnimator[animators.size()]);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(a);
            animatorSet.start();
        } else {
            ImageView rightIndicator = (ImageView) view.findViewById(R.id.artist_right_indicator);
            TextView duration = (TextView) view.findViewById(R.id.track_duration);
            ObjectAnimator arrowAnimation = ObjectAnimator.ofFloat(rightIndicator, "alpha", 1f);
            arrowAnimation.setRepeatCount(1);
            arrowAnimation.setRepeatMode(ValueAnimator.REVERSE);
            arrowAnimation.setDuration(500);
            ObjectAnimator durationAnimation = ObjectAnimator.ofFloat(duration, "alpha", 0f);
            durationAnimation.setRepeatCount(1);
            durationAnimation.setRepeatMode(ValueAnimator.REVERSE);
            durationAnimation.setDuration(500);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(arrowAnimation, durationAnimation);
            animatorSet.start();
        }
    }

    private void addLast(int position) {
        animateLeft(position);
        animateRight(position);
        mListener.onArtistItemSelected(audioList.get(position));
    }

    private void addNext(int position) {
        animateRight(position);
        mListener.onArtistItemSelected(audioList.get(position), MainActivity.ADD_NEXT, nextCounter);
        nextCounter++;
    }

    private void addFirst(int position) {
        animateLeft(position);
        mListener.onArtistItemSelected(audioList.get(position), MainActivity.ADD_FIRST);
    }

    public void selectMode(boolean enabled) {
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
//            if (mCheckButton.getVisibility() == View.INVISIBLE) {
//                Runnable action = new Runnable() {
//                    @Override
//                    public void run() {
//                        mCheckButton.setVisibility(View.VISIBLE);
//                    }
//                };
//                mCheckButton.animate()
//                        .alpha(1)
//                        .withStartAction(action)
//                        .start();
//            }
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
//            if (mCheckButton.getVisibility() == View.VISIBLE) {
//                Runnable action = new Runnable() {
//                    @Override
//                    public void run() {
//                        mCheckButton.setVisibility(View.INVISIBLE);
//                    }
//                };
//                mCheckButton.animate()
//                        .alpha(0)
//                        .withEndAction(action)
//                        .start();
//            }
        }
    }

    @Override
    public void onStop() {
        mListener.setFragmentTitle(MainActivity.APP_TITLE);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.setFragmentTitle(mArtistName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARTIST_NAME, mArtistName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mArtistName = savedInstanceState.getString(ARTIST_NAME);
        }
        if (mArtistName == null) {
            return;
            //mArtistName = "Squarepusher"; // default artist for testing
        }
        mListener.setFragmentTitle(mArtistName);
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
        CursorLoader cursorLoader = new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, from, where, whereArgs, MediaStore.Audio.Media.ALBUM_ID + "," + MediaStore.Audio.Media.TRACK);
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

            Bitmap bitmap = null;
            if (!currentAlbum.equals(album)) {
                AudioListModel albumAudioItem = new AudioListModel(artist, album, year, albumId, null);
                audioList.add(albumAudioItem);
                currentAlbum = album;
                trackNumber = 0;
            }
            trackNumber++;
            AudioListModel audioItem = new AudioListModel(artist, album, title, data, duration, trackNumber, year, albumId, trackId);
            audioList.add(audioItem);
        }

    }

    private void addItems() {
        addItems(false);
    }

    private void addItems(boolean addNext) {
        ArrayList<Integer> checked = mAdapter.getChecked();
        ArrayList<AudioListModel> items = new ArrayList<>();
        for (Integer num : checked) {
            if (!audioList.get(num).isAlbum) {
                items.add(audioList.get(num));
            }
        }
        AudioListModel[] result = items.toArray(new AudioListModel[items.size()]);
        mListener.addBulk(result, addNext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = mInflater.inflate(R.layout.fragment_artist, null);
        mAddButton = (Button) view.findViewById(R.id.artist_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItems();
                mAdapter.uncheckAll();
                selectMode(false);
            }
        });
        mAddNextButton = (ImageButton) view.findViewById(R.id.artist_add_next);
        mAddNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItems(true);
                mAdapter.uncheckAll();
                selectMode(false);
            }
        });
        mCheckButton = (ImageButton) view.findViewById(R.id.artist_check_all);
        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.checkAll();
            }
        });
        mBack = (ImageButton) view.findViewById(R.id.artist_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mAdapter = new AudioListAdapter(getActivity(), R.layout.fragment_artist_list_item, R.layout.fragment_artist_list_header, audioList);
        mList = (ListView) view.findViewById(R.id.artist_list);
//        final ArtistGestureListener gestureListener = new ArtistGestureListener(this, mList);
//        final GestureDetector gestureDetector = new GestureDetector(getActivity().getApplicationContext(), gestureListener);
//        mList.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return gestureDetector.onTouchEvent(motionEvent);
//            }
//        });
//        mList.setOnItemClickListener(new ListView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                addLast(position);
//            }
//        });
        mList.setAdapter(mAdapter);
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
        nextCounter = 0;
    }

    public void setArtist(String artistName) {
        mArtistName = artistName;
    }


    private class AudioListAdapter extends ArrayAdapter<AudioListModel> {

        Context context;
        int layoutItemResourceId, layoutHeaderResourceId;
        ArrayList<AudioListModel> rows = null;
        ArrayList<Integer> checked = new ArrayList<>();

        public ArrayList<Integer> getChecked() {
            return checked;
        }

        public AudioListAdapter(Context context, int layoutItemResourceId, int layoutHeaderResourceId, ArrayList<AudioListModel> rows) {
            super(context, layoutItemResourceId, rows);
            this.context = context;
            this.layoutItemResourceId = layoutItemResourceId;
            this.layoutHeaderResourceId = layoutHeaderResourceId;
            this.rows = rows;
        }

        private void setChecked(Integer position, boolean isChecked) {
            if (isChecked) {
                if (!checked.contains(position)) {
                    checked.add(position);
                }
                selectMode(true);
            } else {
                checked.remove(position);
            }
        }

        private AudioListHolder getHeaderHolder(View row, Integer position) {
            AudioListHolder holder = new AudioListHolder();
            holder.checkbox = (CheckBox) row.findViewById(R.id.artist_header_checkbox);
            holder.checkbox.setTag(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox cb = (CheckBox) view;
                    Integer pos = (Integer) cb.getTag();
                    long albumId = rows.get(pos).getAlbumId();
                    setChecked(pos, cb.isChecked());
                    boolean start = true;
                    for (int i = 0; i < rows.size(); i++) {
                        if (rows.get(i).getAlbumId() != albumId) {
                            if (!start) {
                                break;
                            }
                        } else {
                            if (!rows.get(i).isAlbum) {
                                start = false;
                                setChecked(i, cb.isChecked());
                            }
                        }
                    }
                    if (checked.size() == 0) {
                        selectMode(false);
                    }
                    notifyDataSetChanged();
                }
            });
            holder.headerText = (TextView) row.findViewById(R.id.artist_header_text);
            holder.headerSeparator = row.findViewById(R.id.artist_header_separator);
            holder.isHeader = true;
            return holder;
        }

        private AudioListHolder getItemHolder(View row, Integer position) {
            AudioListHolder holder = new AudioListHolder();
            holder.checkbox = (CheckBox) row.findViewById(R.id.artist_checkbox);
            holder.checkbox.setTag(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox cb = (CheckBox) view;
                    Integer pos = (Integer) cb.getTag();
                    setChecked(pos, cb.isChecked());
                    if (checked.size() == 0) {
                        selectMode(false);
                    }
                }
            });
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
                    Log.d("ALBUM", audioItem.getAlbum());
                    Log.d("POSITION", String.valueOf(position));
                    holder = getHeaderHolder(row, position);
                } else {
                    row = inflater.inflate(layoutItemResourceId, parent, false);
                    holder = getItemHolder(row, position);
                }
                row.setTag(holder);
            } else {
                holder = (AudioListHolder) row.getTag();
                if (holder.isHeader) {
                    if (audioItem.isAlbum) {
                        holder.checkbox.setTag(position);
                    } else {
                        row = inflater.inflate(layoutItemResourceId, parent, false);
                        holder = getItemHolder(row, position);
                        row.setTag(holder);
                    }
                } else {
                    if (audioItem.isAlbum) {
                        row = inflater.inflate(layoutHeaderResourceId, parent, false);
                        holder = getHeaderHolder(row, position);
                        row.setTag(holder);
                    } else {
                        holder.checkbox.setTag(position);
                    }
                }
                // getView() provides us with a previous recycled view. Make sure it's the correct one, otherwise reinflate
                if (holder.isHeader && !audioItem.isAlbum) {
                } else if (!holder.isHeader && audioItem.isAlbum) {
                }
            }
            if (checked.contains(position)) {
                holder.checkbox.setChecked(true);
            } else {
                holder.checkbox.setChecked(false);
            }
            if (holder.isHeader) {
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

        @Override
        public int getCount() {
            if (rows != null) {
                return rows.size();
            } else {
                return 0;
            }
        }

        public void checkAll() {
            if (checked.size() == rows.size()) {
                uncheckAll();
            } else {
                checked.clear();
                for (int i = 0; i < rows.size(); i++) {
                    checked.add(i);
                }
                notifyDataSetChanged();
                selectMode(true);
            }
        }

        public void uncheckAll() {
            checked.clear();
            selectMode(false);
            notifyDataSetChanged();
        }

        class AudioListHolder {
            Boolean isHeader;
            TextView headerText;
            View headerSeparator;
            TextView title;
            View dividerDuration;
            TextView trackDuration;
            TextView trackNumber;
            CheckBox checkbox;
        }
    }

}
