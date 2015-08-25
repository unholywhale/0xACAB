package com.example.alex.xacab;

import android.view.View;

/**
 * Created by alex on 10/07/15.
 */
public interface SelectionListener {
    void onItemSelected(View item);
    void onArtistItemSelected(AudioListModel item);
    void onLibraryItemSelected(View item);
}
