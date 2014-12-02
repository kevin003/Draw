package com.kevin.draw.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class DrawLine extends LinearLayout {

	private Paint mPaint = null;
	
	public DrawLine(Context context) {
		super(context);
		mPaint = new Paint();
	}

	public DrawLine(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}

	public DrawLine(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
	}
	
	public void onPaintChanged(Paint paint)
	{
		mPaint = paint;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		float width = (float)this.getWidth();
		float height= (float)this.getHeight()/2;
		canvas.drawLine(0, height, width, height, mPaint);
	}
	
	public void setPaint(Paint paint)
	{
		mPaint = paint;
	}
	
	public Paint getPaint()
	{
		return mPaint;
	}
	
}
