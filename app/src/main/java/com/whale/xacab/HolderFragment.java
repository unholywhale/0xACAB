package com.whale.xacab;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created for holding objects between configuration changes
 */
public class HolderFragment extends Fragment {
    private LastFmWrapper lastFm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setLastFM(LastFmWrapper lastFm) {
        this.lastFm = lastFm;
    }

    public LastFmWrapper getLastFm() {
        return lastFm;
    }
}
