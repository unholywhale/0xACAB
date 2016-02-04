package com.whale.acab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

public class GestureHelper implements View.OnTouchListener {

    private final GestureDetector mGestureDetector;
    ListView mListView;
    ImageView mDragView;
    View mDraggingItem;
    View mHoverItem;
    int mHoverPosition;
    int mStartPosition;
    int mEndPosition;
    int mDragPointOffset;

    DropListener mDropListener;
    RemoveListener mRemoveListener;
    DragListener mDragListener;

    public GestureHelper(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureListener(this));
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }


    public void onSwipeBottom() {
    }

    public void onScrollRight() {
    }

    public void onScrollLeft() {
    }

    public void onScrollTop() {
    }

    public void onScrollBottom() {
    }

    public void onDoubleTap() {
    }

    public void onClick() {
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        if (true)
            return mGestureDetector.onTouchEvent(ev);
        if (ev.getX() > 180) {
            return mGestureDetector.onTouchEvent(ev);
        } else {
            mListView = (ListView) v;
            int action = ev.getAction();
            int x = (int) ev.getX();
            int y = (int) ev.getY();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartPosition = mListView.pointToPosition(x, y);
                    if (mStartPosition != ListView.INVALID_POSITION) {
                        int mItemPosition = mStartPosition - mListView.getFirstVisiblePosition();
                        mDraggingItem = mListView.getChildAt(mItemPosition);
                        ViewGroup.LayoutParams params = mDraggingItem.getLayoutParams();
                        params.height = 1;
                        mDraggingItem.setLayoutParams(params);
                        mDragPointOffset = y - mListView.getChildAt(mItemPosition).getTop();
                        mDragPointOffset -= ((int) ev.getRawY()) - y;
                        startDrag(mItemPosition, y);
                        drag(0, y);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    scrollList(y);
                    int hoverPosition = mListView.pointToPosition(0, y);
                    if (mHoverPosition != mStartPosition && mHoverPosition != hoverPosition && mHoverPosition != ListView.INVALID_POSITION) {
                        mHoverPosition = hoverPosition;
                        Log.d("HOVER", ((Integer) mHoverPosition).toString());
                        View hoverItem = mListView.getChildAt(hoverPosition);
                        if (hoverItem != null) {
                            Log.d("ITEM", hoverItem.toString());
                            ViewGroup.LayoutParams params = hoverItem.getLayoutParams();
                            params.height = mDragView.getHeight() * 2;
                            hoverItem.setLayoutParams(params);
                            if (mHoverItem == null) {
                                mHoverItem = hoverItem;
                            }
                            ViewGroup.LayoutParams hoverParams = mHoverItem.getLayoutParams();
                            hoverParams.height = mDragView.getHeight();
                            mHoverItem.setLayoutParams(hoverParams);
                            mHoverItem = hoverItem;
                        }
                    }
                    drag(0, y);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                default:
                    mEndPosition = mListView.pointToPosition(x, y);
                    stopDrag(mStartPosition - mListView.getFirstVisiblePosition());
            }
            return true;
        }
    }

    private void scrollList(int y) {
        int upper = mListView.getHeight() / 3;
        int lower = mListView.getHeight() * 2 / 3;
        int speed = 0;
        if (y > lower) {
            speed = y > (mListView.getHeight() + lower) / 2 ? 16 : 4;
        } else if (y < upper) {
            speed = y < upper / 2 ? -16 : -4;
        }
        if (speed != 0) {
            int ref = mListView.pointToPosition(0, mListView.getHeight() / 2);
            if (ref != ListView.INVALID_POSITION) {
                ref = mListView.pointToPosition(0, mListView.getHeight() / 2 + mListView.getDividerHeight() + 64);
            }
            View view = mListView.getChildAt(ref - mListView.getFirstVisiblePosition());
            if (view != null) {
                int pos = view.getTop();
                mListView.setSelectionFromTop(ref, pos - speed);
            }
        }
    }

    private void drag(int x, int y) {
        if (mDragView != null) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
            layoutParams.x = x;
            layoutParams.y = y - mDragPointOffset;
            WindowManager mWindowManager = (WindowManager) mListView.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            mWindowManager.updateViewLayout(mDragView, layoutParams);
            if (mDragListener != null)
                mDragListener.onDrag(x, y, null);// change null to "this" when ready to use
        }
    }

    private void startDrag(int itemIndex, int y) {
        stopDrag(itemIndex);

        View item = mListView.getChildAt(itemIndex);
        if (item == null) return;
        item.setDrawingCacheEnabled(true);
        if (mDragListener != null)
            mDragListener.onStartDrag(item);

        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());

        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPointOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        Context context = mListView.getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bitmap);

        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }

    private void stopDrag(int itemIndex) {
        if (mDragView != null) {
            if (mDragListener != null)
                mDragListener.onStopDrag(mListView.getChildAt(itemIndex));
            mDragView.setVisibility(ListView.GONE);
            WindowManager wm = (WindowManager) mListView.getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
    }

    private static final class GestureListener implements GestureDetector.OnGestureListener {

        private static final int SWIPE_THRESHOLD = 30;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private GestureHelper mHelper;

        public GestureListener(GestureHelper helper) {
            mHelper = helper;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //mHelper.onClick();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (Math.abs(distanceX) > SWIPE_THRESHOLD) {
                    if (distanceX > 0) {
                        mHelper.onScrollRight();
                    } else {
                        mHelper.onScrollLeft();
                    }
                }
            } else {
                if (Math.abs(distanceY) > SWIPE_THRESHOLD) {
                    if (distanceY > 0) {
                        mHelper.onScrollBottom();
                    } else {
                        mHelper.onScrollTop();
                    }
                }
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            mHelper.onSwipeRight();
                        } else {
                            mHelper.onSwipeLeft();
                        }
                        result = true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            mHelper.onSwipeBottom();
                        } else {
                            mHelper.onSwipeTop();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }
}
