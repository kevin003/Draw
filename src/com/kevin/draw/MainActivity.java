package com.kevin.draw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.kevin.draw.color.ColorDialog;
import com.kevin.draw.view.DrawView;
import com.kevin.draw.view.OkCancleDialog;
import com.kevin.draw.view.OnClickOkListener;

public class MainActivity extends Activity {

	final int SELECT_IMAGE = 1;
	private DrawView drawView = null;
	
	private boolean mAlphaSliderEnabled = false;
	private boolean mHexValueEnabled = false;
	private String imagePath = null;
	private Paint paint;// 画笔
	
	private Intent intent;
	private ImageView undo;
	private ImageView redo;
	private ImageView save;
	private ImageView share;
	private ImageView eraser;
	private ImageView clear;
	private ImageView pen;
	private ImageView color;
	private ImageView picture;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		drawView = (DrawView) findViewById(R.id.drawview);
		//上面的功能
		undo = (ImageView) findViewById(R.id.backout);
		redo = (ImageView) findViewById(R.id.reform);
		save = (ImageView) findViewById(R.id.save);
		share = (ImageView) findViewById(R.id.shared);
		clear = (ImageView) findViewById(R.id.clear);
		
		//下面的功能
		eraser = (ImageView) findViewById(R.id.eraser);
		pen = (ImageView) findViewById(R.id.pen);
		color = (ImageView) findViewById(R.id.color);
		picture = (ImageView) findViewById(R.id.picture);
		
		//上面功能添加点击事件
		undo.setOnClickListener(new ButtonListener());
		redo.setOnClickListener(new ButtonListener());
		save.setOnClickListener(new ButtonListener());
		share.setOnClickListener(new ButtonListener());
		clear.setOnClickListener(new ButtonListener());
		
		//下面功能添加点击事件
		pen.setOnClickListener(new ButtonListener());
		color.setOnClickListener(new ButtonListener());
		picture.setOnClickListener(new ButtonListener());
		eraser.setOnClickListener(new ButtonListener());
		
		initPaint();
		drawView.setPaint(paint);
	}

	private void initPaint() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(0xff000000);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(12);
	}
	
	class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.backout:
				drawView.undo();
				break;
			case R.id.reform:
				drawView.redo();
			case R.id.save:
				drawView.saveImage(imagePath);
			case R.id.shared:
				intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_SUBJECT, "好友分享");
				intent.putExtra(Intent.EXTRA_TEXT,
						"我画的涂鸦，分享给你~");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MainActivity.this.startActivity(Intent.createChooser(intent, "好友分享"));
				break;
			case R.id.clear:
				drawView.clearImage();
				break;
			case R.id.eraser:
				drawView.setCurrentType(DrawView.ERASER);
			case R.id.pen:
				choosePaint();
				break;
			case R.id.color:
				chooseColor();
				break;
			case R.id.picture:
				chooseBackground();
				break;
			default:
				break;
			}
		}
	}
	
	
	
	public class BackgroundColorListener implements ColorDialog.OnColorChangedListener
	{
		public void onColorChanged(int color)
		{
			drawView.setBitmapColor(color);
		}
	}
	
	public class PaintColorChangedListener implements ColorDialog.OnColorChangedListener
	{
		public void onColorChanged(int color)
		{
			drawView.setBitmapColor(color);
		}
	}
	
	private void choosePaint()
	{
		PaintDialog dialog = new PaintDialog(MainActivity.this);
		dialog.initDialog(dialog.getContext(),paint);
		dialog.setOnPaintChangedListener( new PaintChangeListener() );
	}
	
	public class PaintChangeListener implements PaintDialog.OnPaintChangedListener
	{
		public void onPaintChanged(Paint paint)
		{
			drawView.setPaint(paint);
			paint = paint;
		}
	}
	
	private void chooseBackground()
	{
		String[] itemsTo = {"选择图片","选择颜色"};
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("选择背景");
		builder.setItems(itemsTo, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if(arg1 == 0)
				{
					pickupLocalImage(SELECT_IMAGE);
				
				}
				if(arg1 == 1)
				{
					showColorDialog(null);
				}
			}
		});
		builder.create().show();
	}
	
	protected void pickupLocalImage(int return_num) 
	{
		try {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			startActivityForResult(intent,return_num);
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode == RESULT_OK) {
    		switch(requestCode) {
    			case SELECT_IMAGE:
    				try{
    					Uri imgUri = data.getData();
    					if(imgUri != null){
    						ContentResolver cr = this.getContentResolver();
    						String[] columnStr = new String[]{MediaStore.Images.Media.DATA};
    						Cursor cursor = cr.query(imgUri, columnStr, null, null, null);
    						if(cursor != null){
    							int nID = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
    							if(cursor.moveToFirst()){
    								imagePath = cursor.getString(nID);
    								drawView.setBitmap(imagePath);
    							}
    						}
    					}
    				}catch(Exception e){
    					e.printStackTrace();
    				}
			  
    				break;
    			default:
    				break;
    		};
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	protected void showColorDialog(Bundle state) {
		
		ColorDialog dialog = new ColorDialog(MainActivity.this,Color.BLACK);
		dialog.setOnColorChangedListener(new BackgroundColorListener());
		if (mAlphaSliderEnabled) {
			dialog.setAlphaSliderVisible(true);
		}
		if (mHexValueEnabled) {
			dialog.setHexValueEnabled(true);
		}
		if (state != null) {
			dialog.onRestoreInstanceState(state);
		}
		dialog.show();
	}
	
	private void chooseColor(){
//		boolean bAlphaSliderEnabled = false;
//		boolean bHexValueEnabled = false;
//		Bundle state = null;
//		ColorDialog dialog = new ColorDialog(MainActivity.this, Color.BLACK);
//		dialog.setOnColorChangedListener((OnColorChangedListener) this);
//		if (bAlphaSliderEnabled) {
//			dialog.setAlphaSliderVisible(true);
//		}
//		if (bHexValueEnabled) {
//			dialog.setHexValueEnabled(true);
//		}
//		dialog.show();
		showColorDialog(null);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// 点击返回
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			OkCancleDialog returnDialog = new OkCancleDialog(this,
					new OnClickOkListener() {
						@Override
						public void onClickOk() {
							finish();
						}
					});
			returnDialog.show();
			returnDialog.setMessage("确定要退出么？");
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
}
