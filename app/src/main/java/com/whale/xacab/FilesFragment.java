package com.whale.xacab;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.app.ListFragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class FilesFragment extends Fragment {


    private SelectionListener mListener;
    private String mCurrentPath;
    private Button mAddButton;
    private ImageButton mSwitch;
    private ListView mList;
    private ArrayList<File> mFiles = new ArrayList<>();

    private FilesAdapter mAdapter;

    private class FilesGestureHelper extends GestureHelper {

        public FilesGestureHelper(Context context) {
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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, null);
        mSwitch = (ImageButton) view.findViewById(R.id.files_switch);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.openLibrary(true);
            }
        });
        mAddButton = (Button) view.findViewById(R.id.files_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItems();
            }
        });
        mAdapter = new FilesAdapter(getActivity(), R.layout.fragment_files_list_item, mFiles);
        mList = (ListView) view.findViewById(R.id.files_list);
        mList.setAdapter(mAdapter);
        mList.setOnTouchListener(new FilesGestureHelper(getActivity().getApplicationContext()));
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File file = mFiles.get(i);
                if (file.isDirectory()) {
                    mCurrentPath += "/" + file.getName();
                    mListener.updateLastDir(mCurrentPath);
                    populateFiles(mCurrentPath);
                    mAdapter.notifyDataSetChanged();
                } else {
                    AudioListModel item = getAudioData(file);
                    mListener.onArtistItemSelected(item);
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
    }

    public void addItems() {
        Integer counter = 0;
        for (FilesAdapter.FileHashHolder holder : getAdapter().hashMapChecked.values()) {
            if (holder.isChecked) {
                AudioListModel item = getAudioData(mFiles.get(holder.position));
                mListener.onArtistItemSelected(item);
            }
            counter++;
        }
        selectMode(false);
        mListener.setSelectMode(false);
        getAdapter().setCheckBoxVisibility(false);
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
                        .translationY(0)
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
                        .translationY(100)
                        .alpha(0)
                        .withEndAction(action)
                        .start();
            }
        }
    }


    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    private AudioListModel getAudioData(File file) {
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
        ArrayList<File> rows = null;
        private HashMap<Integer, FileHashHolder> hashMapChecked = new HashMap<>();

        public class FileHashHolder {
            Integer position;
            Boolean isChecked;
        }

        public FilesAdapter(Context context, int resource, ArrayList<File> rows) {
            super(context, resource, rows);
            this.context = context;
            this.layoutResourceId = resource;
            this.rows = rows;
        }

        public FileHolder getHolder(View row) {
            FileHolder holder = new FileHolder();
            holder.checked = (CheckBox) row.findViewById(R.id.files_checked);
            holder.checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    FileHashHolder fileHashHolder = (FileHashHolder) compoundButton.getTag();
                    if (fileHashHolder.position != ListView.INVALID_POSITION) {
                        fileHashHolder.isChecked = isChecked;
                        if (isChecked) {
                            hashMapChecked.put(fileHashHolder.position, fileHashHolder);
                        } else if (hashMapChecked.get(fileHashHolder.position) != null) {
                            hashMapChecked.remove(fileHashHolder.position);
                        }
                    }
                }
            });

            holder.dirIcon = (ImageView) row.findViewById(R.id.files_dir_icon);
            holder.fileIcon = (ImageView) row.findViewById(R.id.files_file_icon);
            holder.title = (TextView) row.findViewById(R.id.files_title);
            return holder;
        }

        public HashMap<Integer, FileHashHolder> getHashMapChecked() { return hashMapChecked; }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            FileHolder holder;
            File file = rows.get(position);
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            if (row == null) {
                row = inflater.inflate(R.layout.fragment_files_list_item, parent, false);
                holder = getHolder(row);
                row.setTag(holder);
            } else {
                holder = (FileHolder) row.getTag();
            }
            if (file.isDirectory()) {
                holder.title.setTextColor(getResources().getColor(R.color.header_color));
            } else {
                holder.title.setTextColor(getResources().getColor(R.color.text_light_color));
            }
            if (mCheckBoxVisible) {
                holder.dirIcon.setVisibility(View.INVISIBLE);
                holder.fileIcon.setVisibility(View.INVISIBLE);
                holder.checked.setVisibility(View.VISIBLE);
                FileHashHolder fileHashHolder = hashMapChecked.get(position);
                if (fileHashHolder == null) {
                    fileHashHolder = new FileHashHolder();
                    fileHashHolder.position = position;
                    fileHashHolder.isChecked = false;
                }
                holder.checked.setTag(fileHashHolder);
                if (fileHashHolder.isChecked != null) {
                    holder.checked.setChecked(fileHashHolder.isChecked);
                } else {
                    holder.checked.setChecked(false);
                }
            } else {
                holder.checked.setVisibility(View.INVISIBLE);
                if (file.isDirectory()) {
                    holder.dirIcon.setVisibility(View.VISIBLE);
                    holder.fileIcon.setVisibility(View.INVISIBLE);
                } else {
                    holder.dirIcon.setVisibility(View.INVISIBLE);
                    holder.fileIcon.setVisibility(View.VISIBLE);
                }
            }
            holder.title.setText(file.getName());
            return row;
        }

        public void setCheckBoxVisibility(boolean visible) {
            mCheckBoxVisible = visible;
            if (!mCheckBoxVisible) {
                hashMapChecked.clear();
            }
        }

        class FileHolder {
            CheckBox checked;
            ImageView dirIcon;
            ImageView fileIcon;
            TextView title;
        }
    }

}
