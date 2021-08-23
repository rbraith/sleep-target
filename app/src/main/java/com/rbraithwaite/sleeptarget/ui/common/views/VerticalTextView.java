/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.rbraithwaite.sleeptarget.R;

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
