package com.rbraithwaite.sleepapp.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.R;

// based on https://stackoverflow.com/a/40512310
public class VerticalTextView
        extends androidx.appcompat.widget.AppCompatTextView
{
//*********************************************************
// private properties
//*********************************************************

    private Direction mDirection;
    
//*********************************************************
// constructors
//*********************************************************

    public VerticalTextView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(R.styleable.VerticalTextView);
        try {
            // default is UPWARDS
            mDirection = Direction.fromIndex(ta.getInt(R.styleable.VerticalTextView_direction, 1));
        } finally {
            ta.recycle();
        }
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        TextPaint textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        textPaint.drawableState = getDrawableState();
        
        canvas.save();
        
        switch (mDirection) {
        case DOWNWARDS:
            canvas.translate(getWidth(), 0);
            canvas.rotate(90);
            break;
        case UPWARDS:
            canvas.translate(0, getHeight());
            canvas.rotate(-90);
        }
        
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        
        getLayout().draw(canvas);
        canvas.restore();
    }
    
//*********************************************************
// private helpers
//*********************************************************

    private enum Direction
    {
        DOWNWARDS,
        UPWARDS;
        
        public static Direction fromIndex(int idx)
        {
            return Direction.values()[idx];
        }
    }
}
