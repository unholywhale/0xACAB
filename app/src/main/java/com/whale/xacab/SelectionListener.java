package com.whale.xacab;

import android.view.View;

/**
 * Created by alex on 10/07/15.
 */
public interface SelectionListener {
    void onArtistItemSelected(AudioListModel item);
    void onLibraryItemSelected(View item);
    void onQueueItemSelected(int position);
    void onQueueFragmentShow();
    void onQueueFragmentHide();

    void onSeekBarChanged(int newDuration);
}
