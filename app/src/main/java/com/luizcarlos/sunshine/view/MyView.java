package com.luizcarlos.sunshine.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by luizcarlos on 08/09/14.
 */
public class MyView extends View
{
    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
