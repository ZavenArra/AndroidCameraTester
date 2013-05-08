package org.calflora.experimental;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.cameratester.*;

public class CapturePhotoActivity extends Activity implements SurfaceHolder.Callback
{
	public static String PHOTO_FILE_NAME = "PHOTO_FILE_NAME";
	
	public static String getPhotoDirectory(Context context)
	{
		//return Environment.getExternalStorageDirectory().getPath() +"/cbo-up";
		return context.getExternalFilesDir(null).getPath() +"/cbo-up";
	}
	//private String PIC_DATA_PATH; // = "/sdcard/whatsinvasive";
	String fname;
	String TAG = "CapPhoto";

	Camera mCamera;
	boolean mPreviewRunning = false;

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	    
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); 

		//Toast.makeText(this, "Take photo by pressing camera button or center directional pad.  After you are done capturing photos, press back key.", Toast.LENGTH_LONG).show(); 

		window.setFormat(PixelFormat.TRANSLUCENT);

		setContentView(R.layout.activity_photo);
		mSurfaceView = (SurfaceView)findViewById(R.id.surface);
		Button button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (mCamera != null)
					mCamera.autoFocus(mAutoFocusCallback);
			}
		});

		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		//Deprecated in 3.0
		//	mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
	}


	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera c) {          
			Date now = new Date();
			long nowLong = now.getTime() / 9000; 
			fname = getPhotoDirectory(CapturePhotoActivity.this)+"/"+nowLong+".jpg";

			String why = "";
			try {

				File ld = new File(getPhotoDirectory(CapturePhotoActivity.this));
				if (ld.exists()) {
					if (!ld.isDirectory()){
						CapturePhotoActivity.this.finish();
					}
				} else {
					ld.mkdir();
				}

				Log.d(TAG, "open output stream "+fname +" : " +data.length);

				OutputStream os = new FileOutputStream(fname);
				os.write(data,0,data.length);
				os.close();
			} catch (FileNotFoundException ex0) {
				why = ex0.toString();
				ex0.printStackTrace();
			} catch (IOException ex1) {
				why = ex1.toString();
				ex1.printStackTrace();
			}

			Log.d(TAG, "capture returns "+fname);
			File file = new File(fname); // keep this!
			Log.d(TAG, "exists? " +file.exists());
			if (!file.exists())
				Toast.makeText(getBaseContext(), "capture: file does not exist " + fname +" " +why, Toast.LENGTH_LONG);

			else {

				// setResult is used to send result back to the Activity that started this one.
				Intent intent = new Intent();
				intent.putExtra(CapturePhotoActivity.PHOTO_FILE_NAME, fname);
				//intent.putExtra("Tag", CapturePhoto.this.getIntent().getStringExtra("Tag"));
				//intent.putExtra("amount", CapturePhoto.this.getIntent().getStringExtra("amount")));
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			CapturePhotoActivity.this.setResult(Activity.RESULT_CANCELED);
			CapturePhotoActivity.this.finish();
			return false;
			//return super.onKeyDown(keyCode, event);
		}

		if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) 
		{
			mCamera.autoFocus(mAutoFocusCallback);
			return true;
		}

		return false;
	}

	Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback (){

		public void onAutoFocus(boolean success, Camera camera) {
			if (mCamera != null)
			{
				mCamera.takePicture(null, null, mPictureCallback);
			}
		}

	};

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		// XXX stopPreview() will crash if preview is not running
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		// if this throws, use default:
		try
		{
			Camera.Parameters p = mCamera.getParameters();
			
			p.setPreviewSize(w, h);
			Log.d(TAG, "surface: get sizes:");


			List<Camera.Size> list = p.getSupportedPictureSizes();
			Camera.Size cs;
			Camera.Size best = null;
			if (list.size() > 0)
			{
				for (int i = 0; i < list.size(); i++)
				{
					cs = list.get(i);
					//Log.d(TAG, i+ " camera size: w: " +cs.width +" h: " + cs.height);


					//if (cs.width >= 1200 && cs.width <= 1024 && cs.height > 380 && cs.height <= 768)
					if (cs.height <= 1400) {
						if( best != null ){
							if(best.height > cs.height){
								continue;
							}
						}
					
						best = cs;
					
					}
					
				}
			}
			if (best != null)
			{
				Log.d(TAG, " USE size: " +best.width +" " + best.height);
				p.setPictureSize(best.width, best.height);
			}
			//p.setPictureSize(640, 480); // width height
			//p.setPictureSize(213, 350);

			Log.d(TAG, "surface: setParameters:");
			
			//This is meant to fix the rotation issues
	        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	        
			if(display.getRotation() == Surface.ROTATION_0)
	        {
	            p.setPreviewSize(h, w);                           
	            mCamera.setDisplayOrientation(90);
	        }

	        if(display.getRotation() == Surface.ROTATION_90)
	        {
	            p.setPreviewSize(w, h);                           
	        }

	        if(display.getRotation() == Surface.ROTATION_180)
	        {
	            p.setPreviewSize(h, w);               
	        }

	        if(display.getRotation() == Surface.ROTATION_270)
	        {
	            p.setPreviewSize(w, h);
	            mCamera.setDisplayOrientation(180);
	        }
	        

			
			mCamera.setParameters(p);
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
			Log.d(TAG, "cannot set camera params! ex: " + ex);
		}

		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "surface: startPreview:");
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		mCamera = Camera.open();
		if(mCamera==null) { // No backfacing camera
			mCamera = Camera.open(0);			
		}

		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException ex) {
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
			Log.d(TAG, "surfaceCreated ex " + ex);
		}	
	}
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
		mCamera = null;
	}
}
