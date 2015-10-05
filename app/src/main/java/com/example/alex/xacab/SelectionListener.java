package com.example.alex.xacab;

import android.view.View;
import android.widget.TextView;

/**
 * Created by alex on 10/07/15.
 */
public interface SelectionListener {
    void onItemSelected(View item);
    void onArtistItemSelected(AudioListModel item);
    void onLibraryItemSelected(View item);
    void onQueueItemSelected(int position);
    void onQueueFragmentShow();
    void onQueueFragmentHide();
}
