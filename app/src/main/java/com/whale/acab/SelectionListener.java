package com.whale.acab;

import android.view.View;

public interface SelectionListener {
    void openLibrary(boolean librarySwitch);
    void deleteSelected();
    void addBulk(AudioListModel[] items);
    void addBulk(AudioListModel[] items, boolean addNext);
    void onArtistItemSelected(AudioListModel item);
    void onArtistItemSelected(AudioListModel item, int mode);
    void onArtistItemSelected(AudioListModel item, int mode, int counter);
    void updateLastDir(String dir);
    String getLastDir();
    int getQueueSize();
    void saveLastFmSession(String sessionKey, String user);
    void checkEmpty();
    void onLibraryItemSelected(View item);
    void onQueueItemSelected(int position);
    void onSeekBarChanged(int newDuration);
    void onQueueAdd();
    void setFragmentTitle(String title);
    void setFragmentTitle(int resourceId);
    void setLibraryMenu();
    void setFilesMenu();
    void setQueueMenu();
    void invalidateQueue();
    void setMenuItemVisibility(int resourceId, boolean visible);
    int getCurrentQueuePosition();
    void setCurrentQueuePosition(int position);
    void setSelectMode(boolean enabled);
}
