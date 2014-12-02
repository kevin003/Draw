package com.kevin.draw;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.kevin.draw.color.ColorDialog;
import com.kevin.draw.view.DrawLine;

public class PaintDialog extends AlertDialog {
	
	private SeekBar 	mSeekBar;
	private TextView 	mTextSeek;
	private Spinner 	mSpinner;
	private Paint 		mPaint;
	private Paint		mPaintTemp;
	private MaskFilter 	mBlur;
	private MaskFilter 	mEmboss;
	private Button 		mButtonColor;
	private DrawLine	mDrawLine;
	
	private int mColor;
	private int mPenWidth = 1;
	
	private OnPaintChangedListener mListener;
		
		public interface OnPaintChangedListener
		{
			public void onPaintChanged(Paint paint);
		}
		
		public void setOnPaintChangedListener(OnPaintChangedListener listener)
		{
			mListener = listener;
		}

		public PaintDialog(Context context, int theme) {
			super(context, theme);
		}

		public PaintDialog(Context context) {
			super(context);
		}
		
		public void initDialog(Context context,Paint paint){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("画笔");
			View drawView = getLayoutInflater().inflate(R.layout.paint,null);
			builder.setView(drawView);
			
			mPaintTemp = new Paint(paint);
			mPaint	 = paint;
			mBlur  = new BlurMaskFilter(13,BlurMaskFilter.Blur.NORMAL);
			mEmboss= new EmbossMaskFilter(new float[]{1.0f,1.0f,1.0f},0.4f,6,3.5f);
			mSeekBar = (SeekBar)drawView.findViewById(R.id.seekbarPenWidth);
			mSpinner = (Spinner)drawView.findViewById(R.id.spinnerPaint);
			mTextSeek= (TextView)drawView.findViewById(R.id.textPenWidth);
			mDrawLine	 = (DrawLine)drawView.findViewById(R.id.lineShow);
			mButtonColor = (Button)drawView.findViewById(R.id.btnColor);
			mPenWidth	 = (int)mPaint.getStrokeWidth();
			mColor		 = mPaint.getColor();
			
			initSpinner(context,mSpinner);
			initSeekBar(mSeekBar,mPenWidth);
			initButton(mButtonColor);
			initDrawLine(mDrawLine);
			
			builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mPaint = mPaintTemp;
					if (mPaint != null)
					{
						mListener.onPaintChanged(mPaint);
					}
				}
			});
				
			builder.create().show();
		}
		
		private void initButton(Button button)
		{
			button.setBackgroundColor(mPaint.getColor());
			button.setOnClickListener(new ColorClickListener());
		}

		private void initDrawLine(DrawLine line) {
			line.setPaint(mPaint);
		}

		private void initSeekBar(SeekBar seekBar, int width) {
			seekBar.setOnSeekBarChangeListener(new SeekBarListener());
			seekBar.setProgress(width);
		}

		private void initSpinner(Context context, Spinner spinner) {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, 
					R.array.paint_spinner,android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
			spinner.setAdapter(adapter);
			MaskFilter mask = mPaint.getMaskFilter();
			int nSelected = 0;
			if (mask instanceof BlurMaskFilter)
			{
				nSelected = 0;
			}
			else 
			{
				if(mask instanceof EmbossMaskFilter)
					nSelected = 1;
				else
					nSelected = 2;
			}
			
			spinner.setSelection(nSelected);
			spinner.setOnItemSelectedListener(new SpinnerItemSelected());
		}
		
		public class SeekBarListener implements OnSeekBarChangeListener
		{
			@Override
			public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser)
			{
				mSeekBar.setProgress(progress);
				mPaint.setStrokeWidth(progress);
				mPenWidth = progress;
				mTextSeek.setText("画笔宽度:" + progress);
				
				if(mListener != null)
				{
					mListener.onPaintChanged(mPaint);
					mDrawLine.onPaintChanged(mPaint);
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				
			}
		}
		
		public class SpinnerItemSelected implements OnItemSelectedListener 
		{
			public void onItemSelected(AdapterView<?> parent, View view,int pos,long id)
			{
				switch(pos)
				{
				case 0:
					mPaint.setMaskFilter(mBlur);
					if(mListener != null)
					{
						mListener.onPaintChanged(mPaint);
						mDrawLine.onPaintChanged(mPaint);
					}
					break;
				case 1:
					mPaint.setMaskFilter(mEmboss);
					if(mListener != null)
					{
						mListener.onPaintChanged(mPaint);
						mDrawLine.onPaintChanged(mPaint);
					}
					break;
				case 2:
					mPaint.setMaskFilter(null);
					if(mListener != null)
					{
						mListener.onPaintChanged(mPaint);
						mDrawLine.onPaintChanged(mPaint);
					}
					break;
				}
			}
			public void onNothingSelected(AdapterView<?> parent)
			{
				
			}
		}
		
		public class ColorClickListener implements View.OnClickListener, 
			ColorDialog.OnColorChangedListener{
				public void onClick(View view)
				{
					boolean bAlphaSliderEnabled = false;
					boolean bHexValueEnabled = false;
					Bundle state = null;
					ColorDialog dialog = new ColorDialog(getContext(),Color.BLACK);
					dialog.setOnColorChangedListener(this);
					if (bAlphaSliderEnabled) {
					dialog.setAlphaSliderVisible(true);
					}
					if (bHexValueEnabled) {
						dialog.setHexValueEnabled(true);
					}
					dialog.show();
				}
				
				public void onColorChanged(int color)
				{
					mPaint.setColor(color);
					mButtonColor.setBackgroundColor(color);
					mColor = color;
					if(mListener != null)
				{
					mListener.onPaintChanged(mPaint);
					mDrawLine.onPaintChanged(mPaint);
				}
			}
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

	
