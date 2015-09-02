package com.zly.screenshot;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	ImageView selectView;
	DrawView drawView;
	FRect resRect,picRect;
	Uri path;
	float[] fM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		selectView = (ImageView)findViewById(R.id.imageView1);
		drawView = (DrawView)findViewById(R.id.imageView2);
		Button sure = (Button)findViewById(R.id.button1);
		Button back = (Button)findViewById(R.id.button2);
		
		drawView.setFocusable(true);
		sure.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(path!=null)
				{
					FRect dr = drawView.getRect();
					Rect r = new FRect(dr.left*1080/picRect.width(),dr.top*1920/picRect.height(),
							dr.right*1080/picRect.width(),dr.bottom*1920/picRect.height()).getIRect();
					ContentResolver contentProvider = getContentResolver();
					try {
						Bitmap bmp = BitmapFactory.decodeStream(contentProvider.openInputStream(path));
						if(r.width()>1&&r.height()>1)
						{
							Bitmap nbmp = Bitmap.createBitmap(bmp, r.left,r.top,r.width(),r.height());
							nbmp.compress(Bitmap.CompressFormat.PNG,100,contentProvider.openOutputStream(path));
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				resRect = null;
				picRect = null;
				path = null;
				fM = null;
				finish();
			}
			
		});
		
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				resRect = null;
				picRect = null;
				path = null;
				fM = null;
				finish();
			}
			
		});
		setIntent(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		 super.onNewIntent(intent);
		 setIntent(intent);
		}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(getIntent().getData()!=null)
			path = getIntent().getData();
		if(path!=null)
		{
			selectView.setImageURI(path);
			FRect resR = new FRect(selectView.getDrawable().getBounds());
			resRect = new FRect(0,0,360,640);
			if(resR.right>1&&resR.bottom>1)
				resRect = resR;
			float[] f = new float[9]; 
			selectView.getImageMatrix().getValues(f);
			fM = new float[]{2.3921876f,0.0f,109.40622f,0.0f,2.3921876f,0.0f,0.0f,0.0f,1.0f};
			if(f[0]>1.0001)
				fM = f;
		}
		if(fM!=null&&resRect!=null)
			picRect = new FRect(fM[2],fM[5],fM[2]+resRect.width()*fM[0],fM[5]+resRect.height()*fM[0]);
		if(picRect!=null)
			drawView.setRect(picRect);
		drawView.requestFocus();
	}
}

class DrawView extends View implements OnTouchListener
{
	float startx,starty,movex,movey;
	Paint p;
	FRect rect;
	public DrawView(Context context,AttributeSet attrs)
	{
		// TODO Auto-generated constructor stub
		super(context,attrs);
		startx = 0;
		starty = 0;
		movex = 0;
		movey = 0;
		p = new Paint();
		p.setColor(Color.RED);
		p.setStrokeWidth(5);
		p.setStyle(Paint.Style.STROKE);
		this.setOnTouchListener(this);
		if(rect==null)
			rect = new FRect(0,0,0,0);
	}
	void setRect(FRect r)
	{
		if(rect.right<1&&rect.bottom<1)
			rect = r;
	}
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch(arg1.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				if(rect.contains(arg1.getX(), rect.top))
					startx = arg1.getX();
				else
					startx = arg1.getX()<rect.left?rect.left:rect.right;
				if(rect.contains(rect.left ,arg1.getY()))
					starty = arg1.getY();
				else
					starty = arg1.getY()<rect.top?rect.top:rect.bottom;
				break;
			case MotionEvent.ACTION_MOVE:
				if(rect.contains(arg1.getX(), rect.top))
					movex = arg1.getX();
				else
					movex = arg1.getX()<rect.left?rect.left:rect.right;
				if(rect.contains(rect.left ,arg1.getY()))
					movey = arg1.getY();
				else
					movey = arg1.getY()<rect.top?rect.top:rect.bottom;
				this.postInvalidate();
				break;
			case MotionEvent.ACTION_UP:
				return false;
		}
		return true;
	}
	@Override
	protected void onDraw(Canvas c)
	{
		if(startx<=movex&&starty<=movey)
			c.drawRect(startx, starty, movex, movey, p);
		else if(startx<=movex&&starty>movey)
			c.drawRect(startx, movey, movex, starty, p);
		else if(startx>movex&&starty<=movey)
			c.drawRect(movex, starty, startx, movey, p);
		else
			c.drawRect(movex, movey, startx, starty, p);
	}
	public FRect getRect()
	{
		FRect rect = new FRect(0,0,0,0);
		rect.left = (startx<=movex?startx:movex)-this.rect.left;
		rect.top = (starty<=movey?starty:movey)-this.rect.top;
		rect.right = (startx>movex?startx:movex)-this.rect.left;
		rect.bottom = (starty>movey?starty:movey)-this.rect.top;
		return rect;
	}
}

class FRect
{
	float left,top,right,bottom;
	FRect(float l,float t,float r,float b)
	{
		left = l;
		top = t;
		right = r;
		bottom = b;
	}
	FRect(Rect r)
	{
		left = r.left;
		top = r.top;
		right = r.right;
		bottom = r.bottom;
	}
	float width()
	{
		return right - left;
	}
	float height()
	{
		return bottom - top;
	}
	Rect getIRect()
	{
		Rect r = new Rect((int)left,(int)top,(int)right,(int)bottom);
		return r;
	}
	boolean contains(float x,float y)
	{
		if(x>=left&&x<=right&&y>=top&&y<=bottom)
			return true;
		else
			return false;
	}
}