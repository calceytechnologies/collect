package org.odk.collect.android.listeners;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.core.widget.NestedScrollView;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.ODKView;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.FlingRegister;
import org.odk.collect.android.utilities.ScreenUtils;

import timber.log.Timber;

public class SwipeHandler {
    private final GestureDetector gestureDetector;
    private final OnSwipeListener onSwipe;
    private ODKView odkView;
    private boolean allowSwiping = true;
    private boolean beenSwiped;

    public interface OnSwipeListener {
        void showPreviousView();
        void showNextView();
    }

    public SwipeHandler(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        this.onSwipe = (OnSwipeListener) context;
    }

    public void setOdkView(ODKView odkView) {
        this.odkView = odkView;
    }

    public void setAllowSwiping(boolean allowSwiping) {
        this.allowSwiping = allowSwiping;
    }

    public void setBeenSwiped(boolean beenSwiped) {
        this.beenSwiped = beenSwiped;
    }

    public boolean beenSwiped() {
        return beenSwiped;
    }

    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }

    public class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // The onFling() captures the 'up' event so our view thinks it gets long pressed. We don't want that, so cancel it.
            if (odkView != null) {
                odkView.cancelLongPress();
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            FlingRegister.flingDetected();

            // only check the swipe if it's enabled in preferences
            String navigation = (String) GeneralSharedPreferences.getInstance()
                    .get(GeneralKeys.KEY_NAVIGATION);

            if (e1 != null && e2 != null
                    && navigation.contains(GeneralKeys.NAVIGATION_SWIPE) && allowSwiping) {
                // Looks for user swipes. If the user has swiped, move to the
                // appropriate screen.

                // for all screens a swipe is left/right of at least
                // .25" and up/down of less than .25"
                // OR left/right of > .5"
                int xpixellimit = (int) (ScreenUtils.getScreenWidth() * .25);
                int ypixellimit = (int) (ScreenUtils.getScreenHeight() * .25);

                if (odkView != null && odkView.suppressFlingGesture(e1, e2, velocityX, velocityY)) {
                    return false;
                }

                if (beenSwiped) {
                    return false;
                }

                float diffX = e1.getX() - e2.getX();
                float diffY = e1.getY() - e2.getY();

                if (odkView != null && canScrollVertically() && getGestureAngle(diffX, diffY) > 30) {
                    return false;
                }

                if ((Math.abs(diffX) > xpixellimit && Math.abs(diffY) < ypixellimit) || Math.abs(diffX) > xpixellimit * 2) {
                    beenSwiped = true;
                    if (velocityX > 0) {
                        if (e1.getX() > e2.getX()) {
                            Timber.e("showNextView VelocityX is bogus! %f > %f", e1.getX(), e2.getX());
                            onSwipe.showNextView();
                        } else {
                            onSwipe.showPreviousView();
                        }
                    } else {
                        if (e1.getX() < e2.getX()) {
                            Timber.e("showPreviousView VelocityX is bogus! %f < %f", e1.getX(), e2.getX());
                            onSwipe.showPreviousView();
                        } else {
                            onSwipe.showNextView();
                        }
                    }
                    return true;
                }
            }

            return false;
        }

        private double getGestureAngle(float diffX, float diffY) {
            return Math.toDegrees(Math.atan2(Math.abs(diffY), Math.abs(diffX)));
        }

        public boolean canScrollVertically() {
            NestedScrollView scrollView = odkView.findViewById(R.id.odk_view_container);
            int screenHeight = scrollView.getHeight();
            int viewHeight = scrollView.getChildAt(0).getHeight();
            return viewHeight > screenHeight;
        }
    }
}
