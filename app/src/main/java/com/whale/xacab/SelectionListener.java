package com.whale.xacab;

import android.provider.MediaStore;
import android.view.View;

public interface SelectionListener {
    void openLibrary(boolean librarySwitch);
    void deleteSelected();
    void onArtistItemSelected(AudioListModel item);
    void onArtistItemSelected(AudioListModel item, int mode);
    void onArtistItemSelected(AudioListModel item, int mode, int counter);

    void updateLastDir(String dir);

    String getLastDir();

    void checkEmpty();
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
