package com.example.alex.xacab;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;


public class SeekBarFragment extends Fragment {

    public static final int MAX_PROGRESS = 200;
    private SelectionListener mListener;
    public SeekBar mSeekBar;
    private TextView mPlayerProgress;
    private int mProgress = 0;
    private int mStep;
    private TextView mPlayerDuration;
    private Integer mCurrentDuration;
    private boolean isPlaying;
    private MainActivity mActivity;
    private SeekBarRefresh mSeekBarRefresh;
    private ProgressRefresh mProgressRefresh;

    public void startTasks(int duration, int position, int step) {
        mCurrentDuration = duration;
        mProgress = position;
        mStep = step;
        mSeekBar.setProgress(position / step);
        mPlayerProgress.setText(MusicUtils.makeTimeString(mActivity.getApplicationContext(), position / 1000));
        mPlayerDuration.setText(MusicUtils.makeTimeString(mActivity.getApplicationContext(), duration / 1000));
        if (mSeekBarRefresh == null) {
            mSeekBarRefresh = new SeekBarRefresh();
            mSeekBarRefresh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, step);
        } else if (mSeekBarRefresh.getStatus() == AsyncTask.Status.FINISHED) {
            mSeekBarRefresh = new SeekBarRefresh();
            mSeekBarRefresh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, step);
        }
        if (mProgressRefresh == null) {
            mProgressRefresh = new ProgressRefresh();
            mProgressRefresh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
        } else if (mProgressRefresh.getStatus() == AsyncTask.Status.FINISHED) {
            mProgressRefresh = new ProgressRefresh();
            mProgressRefresh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
        }
    }

    public void setCurrentDuration(int duration) {
        mCurrentDuration = duration;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public static SeekBarFragment newInstance(String param1, String param2) {
        SeekBarFragment fragment = new SeekBarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SeekBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState == null) {
            mActivity = (MainActivity) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seek_bar, container, false);
//        if (savedInstanceState == null) {
            mSeekBar = (SeekBar) view.findViewById(R.id.player_slider);
            mSeekBar.setMax(MAX_PROGRESS);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        return;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int newDuration = mCurrentDuration * seekBar.getProgress() / MAX_PROGRESS;
                    mListener.onSeekBarChanged(newDuration);
                }
            });
            mPlayerProgress = (TextView) view.findViewById(R.id.player_progress);
            mPlayerDuration = (TextView) view.findViewById(R.id.player_duration);
//        }
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
    }

    private class SeekBarRefresh extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            Integer step = params[0];
            while (mActivity.isPlaying) {
                try {
                    Thread.sleep(mStep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBar.setProgress(mSeekBar.getProgress() + 1);
                    }
                });
            }
            return null;
        }
    }

    private class ProgressRefresh extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            while (mActivity.isPlaying) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mProgress += 500;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerProgress.setText(MusicUtils.makeTimeString(getActivity().getApplicationContext(), mProgress / 1000));
                    }
                });
            }
            return null;
        }
    }


}
