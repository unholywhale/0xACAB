package com.whale.xacab;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.ListFragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.whale.xacab.dummy.DummyContent;

import java.io.File;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;


public class FilesFragment extends ListFragment {


    private SelectionListener mListener;

    private class FilesModel {
        boolean isDir;
        String path;

        public FilesModel(boolean isDir, String path) {
            this.isDir = isDir;
            this.path = path;
        }

    }

    private ArrayList<File> mFiles = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, null);
        FilesAdapter adapter = new FilesAdapter(getActivity(), R.layout.fragment_files_list_item, mFiles);
        setListAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = Environment.getExternalStorageDirectory().toString() + "/mobile/music/users/alex/music/itunes/itunes media/music";
        Log.d("files", "Path " + path);
        populateFiles(path);

    }

    @Override
    public void onStart() {
        mListener.setLibraryMenu();
        super.onStart();
    }

    private void populateFiles(String path) {
        mFiles.clear();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i = 0; i < file.length; i++) {
            mFiles.add(file[i]);
        }
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {


        }
    }

    public class FilesAdapter extends ArrayAdapter<File> {
        Context context;
        int layoutResourceId;
        ArrayList<File> rows = null;


        public FilesAdapter(Context context, int resource, ArrayList<File> rows) {
            super(context, resource, rows);
            this.context = context;
            this.layoutResourceId = resource;
            this.rows = rows;
        }

        public FileHolder getHolder(View row) {
            FileHolder holder = new FileHolder();
            holder.dirIcon = (ImageView) row.findViewById(R.id.files_dir_icon);
            holder.fileIcon = (ImageView) row.findViewById(R.id.files_file_icon);
            holder.title = (TextView) row.findViewById(R.id.files_title);
            return holder;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            FileHolder holder = null;
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
                holder.dirIcon.setVisibility(View.VISIBLE);
                holder.fileIcon.setVisibility(View.INVISIBLE);
            } else {
                holder.dirIcon.setVisibility(View.INVISIBLE);
                holder.fileIcon.setVisibility(View.VISIBLE);
            }
            holder.title.setText(file.getName());
            return row;
        }

        class FileHolder {
            ImageView dirIcon;
            ImageView fileIcon;
            TextView title;
        }
    }

}
