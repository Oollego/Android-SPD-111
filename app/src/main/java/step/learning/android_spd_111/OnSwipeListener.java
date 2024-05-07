package step.learning.android_spd_111;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener{
    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context) {


        this.gestureDetector = new GestureDetector(context, new SwipeGestureListener());
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeBottom(){}
    public void onSwipeLeft(){}
    public void onSwipeRight(){}
    public void onSwipeTop(){}

    private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener{
        private static final int MIN_DISTANCE = 70;
        private static final int MIN_VELOCITY = 80;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isDispatched = false;
            if(e1 != null){
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if(Math.abs(distanceX) > Math.abs(distanceY)){
                    if(Math.abs(distanceX) > MIN_DISTANCE && Math.abs(velocityX) > MIN_VELOCITY){
                        if(distanceX > 0){
                            onSwipeRight();
                        }
                        else {
                            onSwipeLeft();
                        }
                        isDispatched = true;
                    }
                }
                else{
                    if(Math.abs(distanceY) > MIN_DISTANCE && Math.abs(velocityY) > MIN_VELOCITY){
                        if(distanceY > 0){
                           onSwipeBottom();
                        }
                        else {
                            onSwipeTop();
                        }
                        isDispatched=true;
                    }
                }
            }


            return isDispatched;
        }
    }
}
