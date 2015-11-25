package com.whale.xacab;

import android.view.View;
import android.widget.ListView;

public interface DragListener {
    void onStartDrag(View itemView);
    void onDrag(int x, int y, ListView listView);
    void onStopDrag(View itemView);
}
