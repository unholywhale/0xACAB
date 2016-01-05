package com.whale.xacab;

import android.provider.MediaStore;
import android.view.View;

public interface SelectionListener {
    void onArtistItemSelected(AudioListModel item);
    void onArtistItemSelected(AudioListModel item, int mode);
    void onLibraryItemSelected(View item);
    void onQueueItemSelected(int position);
    void onSeekBarChanged(int newDuration);
    void onQueueAdd();
    void setFragmentTitle(String title);
    void setLibraryMenu();
    void setFilesMenu();
    void setQueueMenu();
    void invalidateQueue();
    void setMenuItemVisibility(int resourceId, boolean visible);
    int getCurrentQueuePosition();
    void setCurrentQueuePosition(int position);
    void setSelectMode(boolean enabled);
}
