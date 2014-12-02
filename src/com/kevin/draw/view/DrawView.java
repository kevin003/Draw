package com.kevin.draw.view;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kevin.draw.MainActivity;
import com.kevin.draw.tools.Eraser;
import com.kevin.draw.tools.ToolInterface;

public class DrawView extends View {

	public static final int PLAIN_PEN = 1;
	public static final int ERASER = 2;
	private ToolInterface curTool = null;
	public final static int MIDDLE_ERASER_WIDTH = 50;
	private int eraserSize = MIDDLE_ERASER_WIDTH;
	
	Paint mPaint = null;
	Paint mBitmapPaint = null;
	Path mPath = null;
	Bitmap mBitmap = null;
	Bitmap mBottomBitmap = null;
	Canvas mCanvas = null;
	float posX,posY;
	private final float TOUCH_TOLERANCE = 4;
	
	private DrawPath mDrawPath = null;
	private List<DrawPath> mSavePath 	= null;
	private List<DrawPath> mDeletePath	= null;
	private String mImagePath = null;
	private Intent intent;
	private MainActivity mainActivity;
	
	private int mImageWidth = 480;
	private int mImageHeight = 695;
	private int mBottomBitmapDrawHeight = 0;
	
	public DrawView(Context context) {
		super(context);
		init();
	}
	
	public DrawView(Context context,AttributeSet attr){
		super(context,attr);
		init();
	}
	
	public DrawView(Context context,AttributeSet attr,int defStyle) {
		super(context,attr,defStyle);
		init();
	}

	
	private void init()
	{
		mPaint = new Paint();
	    mPaint.setAntiAlias(true);//去锯齿
	    mPaint.setDither(true);//设置递色
	    mPaint.setColor(0xfff8efe0);//设置画笔颜色
	    mPaint.setStyle(Paint.Style.FILL);//设置画笔的style。FILL为实心，STROKE为空心
	    mPaint.setStrokeJoin(Paint.Join.ROUND);// 默认MITER 
	    mPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔变为圆滑状
	    mPaint.setStrokeWidth(5);//设置线的宽度
	    
	    mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	    
	    mSavePath = new ArrayList<DrawPath>();
	    mDeletePath = new ArrayList<DrawPath>();
	    mImagePath = initPath();
	}
	
	private String initPath()
	{
		String ph = Environment.getExternalStorageDirectory().getAbsolutePath();
		if(ph == null)
		{
			return null;
		}
		ph += "/ddxxtuya";
		File imageFile = new File(ph);
		if( !imageFile.exists() )
		{
			imageFile.mkdir();
		}
		return ph;
	}
	
	private class DrawPath
	{
		Path path;
		Paint paint;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		 mBottomBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         mCanvas = new Canvas(mBottomBitmap);
	}
	
	public void setCurrentType(int type){
		switch (type) {
		case PLAIN_PEN:
		case ERASER:
			curTool = new Eraser(eraserSize);
			break;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0xF5F5DC);
		int nCanvasWidth = canvas.getWidth();
		int nCanvasHeight = canvas.getHeight();
		int nBitmapWidth = mBottomBitmap.getWidth();
		int nBitmapHeight = mBottomBitmap.getHeight();
		mBottomBitmapDrawHeight = (nCanvasHeight - nBitmapHeight)/2;
		canvas.drawBitmap(mBottomBitmap,0,mBottomBitmapDrawHeight,mBitmapPaint);
		if(mPath != null)
		{
			canvas.drawPath(mPath, mPaint);	
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				mPath = new Path();//创建画笔路径
				mDrawPath = new DrawPath();
				mPath.moveTo(x, y);
				mDrawPath.paint = new Paint(mPaint);
				mDrawPath.path	= mPath;
				posX = x;
				posY = y;
				postInvalidate();
				
				break;
			case MotionEvent.ACTION_MOVE:
				float dx = Math.abs(x - posX);
				float dy = Math.abs(y - posY);
				if(dx >= TOUCH_TOLERANCE || dy > TOUCH_TOLERANCE)
				{
					mPath.quadTo(posX, posY, (x + posX)/2, (y + posY)/2);
					posX = x;
					posY = y;
				}
				postInvalidate();
				break;
			case MotionEvent.ACTION_UP:
				mPath.lineTo(posX, posY);
				mPath.offset(0, -mBottomBitmapDrawHeight);
				mCanvas.drawPath(mPath,mPaint); 
				mSavePath.add(mDrawPath);
				mPath = null;
				postInvalidate();
				break;
		}
		return true;
	}
	
	public boolean setBitmap(String imagePath)
	{
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float nxScale = -1;
		float nyScale = -1;
		if( width!=0 && height!=0)
		{
			nxScale = (float)width/mImageWidth;	
			nyScale = (float)height/mImageHeight;
			if (nxScale>=1 && nyScale >=1 || nxScale<1 && nyScale<1)
			{
				if(nxScale > nyScale)
				{
					width = (int)(width/nxScale);
					height = (int)(height/nxScale);
				}
				else
				{
					width = (int)(width/nyScale);
					height = (int)(height/nyScale);
				}
				
			}
			if (nxScale >=1 && nyScale <1)
			{
				width = mImageWidth;
			}
			if(nxScale <=1 && nyScale >=1)
			{
				height = mImageHeight;
			}
			mBitmap = Bitmap.createScaledBitmap(bitmap,width,height,true);
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);		
			mSavePath.clear();
			mDeletePath.clear();
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
			postInvalidate();
			
			return true;
		}
		else 
			return false;
		
	}
	
	public void setBitmapColor(int color)
	{
		mBottomBitmap.eraseColor(color);
		mSavePath.clear();
		mDeletePath.clear();
		postInvalidate();
	}
	
	public void setPaint(Paint paint)
	{
		mPaint = paint;
		postInvalidate();
	}
	
	//保存到相册
	public void saveImage(String imagePath)
	{
		if (mImagePath == null || mBitmap == null)
		{
			return;
		}
		String imageName = null;
		int nStart = imagePath.lastIndexOf('/');
		int nEnd   = imagePath.lastIndexOf('.');
		
		imageName = imagePath.substring(nStart,nEnd);
		imageName += ".png";
		imageName = mImagePath + imageName;
		File file = new File(imageName);
		
		try {
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			mBottomBitmap.compress(CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//新建，清除当前内容
	public void clearImage()
	{
		mSavePath.clear();
		mDeletePath.clear();
		
		if(mBitmap != null)
		{
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap, 0,0, mBitmapPaint);
		}
		else
		{
			int width = mCanvas.getWidth();
			int height = mCanvas.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			
		}
		postInvalidate();
		
	}
	
	//撤销操作
	public void undo()
	{
		int nSize = mSavePath.size();
		if (nSize >= 1)
		{
			mDeletePath.add(0, mSavePath.get(nSize-1) );
			mSavePath.remove(nSize -1);
		}
		else
			return;
		
		
		if(mBitmap != null)
		{
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap, 0,0, mBitmapPaint);
		}
		else
		{
			int width = mCanvas.getWidth();
			int height = mCanvas.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
		}
		
		Iterator<DrawPath> iter = mSavePath.iterator();
		DrawPath temp;
		while(iter.hasNext())
		{
			temp = iter.next();
			mCanvas.drawPath(temp.path, temp.paint);
		}
		postInvalidate();
	}

	//恢复上次操作
	public void redo(){

		int nSize = mDeletePath.size();
		if (nSize >= 1)
		{
			mSavePath.add( mDeletePath.get(0) );
			mDeletePath.remove(0);
		}
		else
			return;
		
		
		if(mBitmap != null)
		{
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap, 0,0, mBitmapPaint);
		}
		else
		{
			int width = mCanvas.getWidth();
			int height = mCanvas.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
		}
		
		Iterator<DrawPath> iter = mSavePath.iterator();
		DrawPath temp;
		while(iter.hasNext())
		{
			temp = iter.next();
			mCanvas.drawPath(temp.path, temp.paint);
		}
		postInvalidate();
	}

}
