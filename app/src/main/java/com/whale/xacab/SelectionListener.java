package com.whale.xacab;

import android.view.View;

/**
 * Created by alex on 10/07/15.
 */
public interface SelectionListener {
    void onArtistItemSelected(AudioListModel item);
    void onLibraryItemSelected(View item);
    void onQueueItemSelected(int position);
    void onSeekBarChanged(int newDuration);
    void onQueueAdd();
    void setFragmentTitle(String title);
    void setLibraryMenu();
    void setQueueMenu();
}
