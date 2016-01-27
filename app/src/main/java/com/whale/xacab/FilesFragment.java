package com.whale.xacab;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;


public class FilesFragment extends Fragment {

    private int nextCounter = 0;
    private SelectionListener mListener;
    private String mCurrentPath;
    private Button mAddButton;
    private ImageButton mCheckButton;
    private ImageButton mBack;
    private ListView mList;
    private ArrayList<File> mFiles = new ArrayList<>();

    private FilesAdapter mAdapter;

//    private class FilesGestureHelper extends GestureHelper {
//
//        public FilesGestureHelper(Context context) {
//            super(context);
//        }
//
//        @Override
//        public void onScrollTop() {
//            if (mBack.getVisibility() == View.INVISIBLE) {
//                Runnable action = new Runnable() {
//                    @Override
//                    public void run() {
//                        mBack.setVisibility(View.VISIBLE);
//                    }
//                };
//                mBack.animate()
//                        .translationY(0)
//                        .alpha(1)
//                        .withStartAction(action)
//                        .start();
//            }
//        }
//
//        @Override
//        public void onScrollBottom() {
//            if (mBack.getVisibility() == View.VISIBLE) {
//                Runnable action = new Runnable() {
//                    @Override
//                    public void run() {
//                        mBack.setVisibility(View.INVISIBLE);
//                    }
//                };
//                mBack.animate()
//                        .translationY(100)
//                        .alpha(0)
//                        .withEndAction(action)
//                        .start();
//            }
//        }
//    }

    private class FilesGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private ListView mListView;
        private FilesFragment mFragment;

        public FilesGestureListener(Fragment fragment, ListView l) {
            try {
                mFragment = (FilesFragment) fragment;
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

    }

    private void animateRight(int position) {

    }

    private void addFirst(int position) {
        animateLeft(position);
        File file = mFiles.get(position);
        mListener.onArtistItemSelected(getAudioData(file), MainActivity.ADD_FIRST);
    }

    private void addNext(int position) {
        animateRight(position);
        File file = mFiles.get(position);
        mListener.onArtistItemSelected(getAudioData(file), MainActivity.ADD_NEXT, nextCounter);
        nextCounter++;
    }

    private void addLast(int position) {
        animateLeft(position);
        animateRight(position);
        File file = mFiles.get(position);
        mListener.onArtistItemSelected(getAudioData(file));
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, null);
        mBack = (ImageButton) view.findViewById(R.id.files_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                //mListener.openLibrary(true);
            }
        });
        mAddButton = (Button) view.findViewById(R.id.files_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItems();
            }
        });
        mCheckButton = (ImageButton) view.findViewById(R.id.files_check_all);
        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapter.getChecked().size() != mAdapter.getCount() - 1) { // except ".."
                    mAdapter.setCheckBoxVisibility(true, true);
                } else {
                    mAdapter.setCheckBoxVisibility(true, false);
                }
            }
        });
        mAdapter = new FilesAdapter(getActivity(), R.layout.fragment_files_list_item, mFiles);
        mList = (ListView) view.findViewById(R.id.files_list);
        mList.setAdapter(mAdapter);
//        final FilesGestureListener gestureListener = new FilesGestureListener(this, mList);
//        final GestureDetector gestureDetector = new GestureDetector(getActivity().getApplicationContext(), gestureListener);
//        mList.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return gestureDetector.onTouchEvent(motionEvent);
//            }
//        });
        //mList.setOnTouchListener(new FilesGestureHelper(getActivity().getApplicationContext()));
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                File file = mFiles.get(position);
                if (file.isDirectory()) {
                    mCurrentPath += "/" + file.getName();
                    mListener.updateLastDir(mCurrentPath);
                    populateFiles(mCurrentPath);
                    selectMode(false);
                    mAdapter.notifyDataSetChanged();
                    mAdapter.setCheckboxes();
                } else {
//                    selectMode(true);
//                    mAdapter.setChecked(view);
//                    mAdapter.setCheckBoxVisibility(true);
                    //AudioListModel item = getAudioData(file);
                    //addLast(position);
                    //mListener.onArtistItemSelected(item);
                }

            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = mListener.getLastDir();
        if (path == null) {
            path = Environment.getExternalStorageDirectory().toString();
            mListener.updateLastDir(path);
        }
        if (mCurrentPath == null) {
            mCurrentPath = path;
        }
        Log.d("files", "Path " + path);
        populateFiles(mCurrentPath);

    }

    @Override
    public void onStart() {
        mListener.setFilesMenu();
        mListener.setFragmentTitle(R.string.files_header);
        super.onStart();
    }

    public FilesAdapter getAdapter() {
        return mAdapter;
    }

    private void populateFiles(String path) {
        mFiles.clear();
        if (!path.equals(Environment.getExternalStorageDirectory().toString())) {
            mFiles.add(new File("..", ".."));
        }
        File f = new File(path);
        File file[] = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".mp3");
            }
        });
        Collections.addAll(mFiles, file);
        Collections.sort(mFiles);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        nextCounter = 0;
    }

    public void addItems() {
        Integer counter = 0;
        FileHandler fileHandler = new FileHandler();
        for (Integer i : getAdapter().getChecked()) {
            fileHandler.addResult(mFiles.get(i));
        }
        ArrayList<AudioListModel> result = fileHandler.getResult();
        AudioListModel[] items = result.toArray(new AudioListModel[result.size()]);
        mListener.addBulk(items);
        selectMode(false);
        mListener.setSelectMode(false);
        getAdapter().notifyDataSetChanged();
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
            if (mCheckButton.getVisibility() == View.VISIBLE) {
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        mCheckButton.setVisibility(View.INVISIBLE);
                    }
                };
                mCheckButton.animate()
                        .alpha(0)
                        .withStartAction(action)
                        .start();
            }
        }
    }


    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    private AudioListModel getAudioData(File file) {
        if (file.getName().equals("..")) {
            return null;
        }
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file.getPath());
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        if (artist == null) {
            artist = "Unknown artist";
        }
        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (title == null) {
            title = file.getName();
        }
        Integer duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        Integer number = 0;
        try {
            number = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
        } catch (NumberFormatException nf) {
            Log.e("number", nf.getMessage());
        }
        Integer year = 0;
        try {
            year = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR));
        } catch (NumberFormatException nf) {
            Log.e("year", nf.getMessage());
        }
        return new AudioListModel(artist, album, title, file.getPath(), duration, number, year, 0, 0);
    }

    public class FilesAdapter extends ArrayAdapter<File> {

        Context context;
        int layoutResourceId;
        boolean mCheckBoxVisible = false;
        boolean mCheckAll = false;
        ArrayList<File> rows = null;
        ArrayList<FileHashHolder> checkboxes = new ArrayList<>();
        private TreeMap<Integer, FileHashHolder> treeMapChecked = new TreeMap<>();

        public void setChecked(View view) {
            FileHolder holder = (FileHolder) view.getTag();
            if (holder != null) {
                FileHashHolder fh = (FileHashHolder) holder.checked.getTag();
                fh.isChecked = true;
                checkboxes.set(fh.position, fh);
                notifyDataSetChanged();
            }
        }

        public class FileHashHolder {
            Integer position;
            Boolean isChecked;

            public FileHashHolder(Integer position, Boolean isChecked) {
                this.position = position;
                this.isChecked = isChecked;
            }
        }

        public FilesAdapter(Context context, int resource, ArrayList<File> rows) {
            super(context, resource, rows);
            this.context = context;
            this.layoutResourceId = resource;
            this.rows = rows;
            setCheckboxes();
        }

        public void setCheckboxes() {
            checkboxes.clear();
            for (int i = 0; i < rows.size(); i++) {
                checkboxes.add(new FileHashHolder(i, false));
            }
        }

        public FileHolder getHolder(final View row, final int position, final boolean isDir) {
            FileHolder holder = new FileHolder();
            holder.checked = (CheckBox) row.findViewById(R.id.files_checked);
            holder.dirIcon = (ImageView) row.findViewById(R.id.files_dir_icon);
            holder.fileIcon = (ImageView) row.findViewById(R.id.files_file_icon);
            holder.title = (TextView) row.findViewById(R.id.files_title);
            return holder;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            FileHolder holder;
            File file = rows.get(position);
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            if (row == null) {
                row = inflater.inflate(R.layout.fragment_files_list_item, parent, false);
                holder = getHolder(row, position, file.isDirectory());
                row.setTag(holder);
                holder.checked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox cb = (CheckBox) view;
                        FileHashHolder fh = (FileHashHolder) cb.getTag();
                        fh.isChecked = cb.isChecked();
                        checkboxes.set(fh.position, fh);
                        if (!fh.isChecked) {
                            if (getChecked().size() == 0) {
                                selectMode(false);
                            } else {
                                selectMode(true);
                            }
                        } else {
                            selectMode(true);
                        }
                    }
                });
            } else {
                holder = (FileHolder) row.getTag();
            }
            if (file.isDirectory()) {
                holder.title.setTextColor(getResources().getColor(R.color.header_color));
            } else {
                holder.title.setTextColor(getResources().getColor(R.color.text_light_color));
            }
            FileHashHolder fh = checkboxes.get(position);
            holder.checked.setChecked(fh.isChecked);
            holder.checked.setTag(fh);
            RelativeLayout.LayoutParams params;
            int checkboxSize = getResources().getDimensionPixelSize(R.dimen.checkbox_size);
            if (file.isDirectory()) {
                holder.dirIcon.setVisibility(View.VISIBLE);
                holder.fileIcon.setVisibility(View.INVISIBLE);
                params = (RelativeLayout.LayoutParams) holder.checked.getLayoutParams();
                params.width = checkboxSize;
                holder.checked.setLayoutParams(params);
            } else {
                holder.dirIcon.setVisibility(View.INVISIBLE);
                holder.fileIcon.setVisibility(View.VISIBLE);
                params = (RelativeLayout.LayoutParams) holder.checked.getLayoutParams();
                params.width = mList.getWidth();
                holder.checked.setLayoutParams(params);
            }
//            if (mCheckBoxVisible) {
//                if (!file.getName().equals("..")) {
//                    holder.dirIcon.setVisibility(View.INVISIBLE);
//                    holder.fileIcon.setVisibility(View.INVISIBLE);
//                    holder.checked.setVisibility(View.VISIBLE);
//                } else {
//                    holder.dirIcon.setVisibility(View.VISIBLE);
//                    holder.checked.setVisibility(View.INVISIBLE);
//                    //holder.checked.setChecked(false);
//                }
//            } else {
//                holder.checked.setVisibility(View.INVISIBLE);
//                holder.checked.setChecked(false);
//                if (file.isDirectory()) {
//                    holder.dirIcon.setVisibility(View.VISIBLE);
//                    holder.fileIcon.setVisibility(View.INVISIBLE);
//                } else {
//                    holder.dirIcon.setVisibility(View.INVISIBLE);
//                    holder.fileIcon.setVisibility(View.VISIBLE);
//                }
//            }
            holder.title.setText(file.getName());
            return row;
        }

        private void checkAll(boolean checked) {
            for (int i = 0; i < this.getCount(); i++) {
                View view = this.getView(i, null, null);
                FileHolder holder = (FileHolder) view.getTag();
                if (!holder.title.getText().equals("..")) {
                    FileHashHolder fh = new FileHashHolder(i, checked);
                    checkboxes.set(i, fh);
                }
            }
            this.notifyDataSetChanged();
        }

        public ArrayList<Integer> getChecked() {
            ArrayList<Integer> checkedList = new ArrayList<>();
            for (int i = 0; i < checkboxes.size(); i++) {
                if (checkboxes.get(i).isChecked) {
                    checkedList.add(i);
                }
            }
            return checkedList;
        }

        public void setCheckBoxVisibility(boolean visible, boolean checkAll) {
            mCheckBoxVisible = visible;
            checkAll(checkAll);
            notifyDataSetChanged();
        }

        public void setCheckBoxVisibility(boolean visible) {
            setCheckBoxVisibility(visible, false);
        }

        class FileHolder {
            CheckBox checked;
            ImageView dirIcon;
            ImageView fileIcon;
            TextView title;
        }
    }

    public class FileHandler {
        private ArrayList<File> files;
        private ArrayList<AudioListModel> result;

        public FileHandler() {
            files = new ArrayList<>();
            result = new ArrayList<>();
        }

        public FileHandler(ArrayList<File> files) {
            this.files = files;
            result = new ArrayList<>();
        }

        public void add(File file) {
            files.add(file);
        }

        public void addResult(File file) {
            if (file != null) {
                if (!file.isDirectory()) {
                    AudioListModel item = getAudioData(file);
                    if (item != null) {
                        result.add(item);
                    }
                } else {
                    if (!file.getName().equals("..")) {
                        File f[] = file.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return f.isDirectory() || f.getName().toLowerCase().endsWith(".mp3");
                            }
                        });
                        ArrayList<File> fileList = new ArrayList<>();
                        Collections.addAll(fileList, f);
                        for (File fi : fileList) {
                            addResult(fi);
                        }
                    }
                }
            }
        }

        public ArrayList<AudioListModel> getResult() {
            return result;
        }
    }

    private class AddFilesTask extends AsyncTask<ArrayList<File>, Void, Void> {

        private Activity activity;
        private ArrayList<File> files = new ArrayList<>();
        private ArrayList<AudioListModel> result = new ArrayList<>();
        private Integer filesCount = 0;

        public AddFilesTask(Activity activity) {

        }

        private void getFileCount(File file) {
            if (file != null) {
                if (!file.isDirectory()) {
                    filesCount++;
                } else {
                    if (!file.getName().equals("..")) {
                        File f[] = file.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return f.isDirectory() || f.getName().toLowerCase().endsWith(".mp3");
                            }
                        });
                        ArrayList<File> fileList = new ArrayList<>();
                        Collections.addAll(fileList, f);
                        for (File fi : fileList) {
                            this.getFileCount(fi);
                        }
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        public void addResult(File file) {
            if (file != null) {
                if (!file.isDirectory()) {
                    AudioListModel item = getAudioData(file);
                    if (item != null) {
                        result.add(item);
                        onProgressUpdate();
                    }
                } else {
                    if (!file.getName().equals("..")) {
                        File f[] = file.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return f.isDirectory() || f.getName().toLowerCase().endsWith(".mp3");
                            }
                        });
                        ArrayList<File> fileList = new ArrayList<>();
                        Collections.addAll(fileList, f);
                        for (File fi : fileList) {
                            addResult(fi);
                        }
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(ArrayList<File>... arrayLists) {
            ArrayList<File> files = arrayLists[0];
            FileHandler fileHandler = new FileHandler();
            for (Integer i : getAdapter().getChecked()) {
                fileHandler.addResult(files.get(i));
            }
            ArrayList<AudioListModel> result = fileHandler.getResult();
            AudioListModel[] items = result.toArray(new AudioListModel[result.size()]);
            mListener.addBulk(items);
            selectMode(false);
            mListener.setSelectMode(false);
            getAdapter().notifyDataSetChanged();
            return null;
        }
    }


}
