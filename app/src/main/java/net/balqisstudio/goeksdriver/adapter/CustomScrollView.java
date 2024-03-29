package net.balqisstudio.goeksdriver.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import net.balqisstudio.goeksdriver.R;


/**
 * Created by Balqis Studio on 17/10/2017.
 */
public class CustomScrollView extends ScrollView {

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(checkCoordinateCross(ev, R.id.mapOrder)) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean checkCoordinateCross(MotionEvent ev, int resId) {
        View target = findViewById(resId);
        if(target == null) {
            return false;
        }
        if(ev.getX() > target.getX() && ev.getX() < target.getX() + target.getWidth() && ev.getY() > target.getY() && ev.getY() < target.getY() + target.getHeight()) {
            return true;
        }
        return false;
    }
}
