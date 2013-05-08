package com.example.cameratester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.calflora.experimental.CapturePhotoActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_FILE_URI = 100;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_CONTENT_RESOLVER = 101;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_HOMEGROWN = 102;


	private Uri fileUri;
	private ImageView imageView;
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	//Content Resolver
    private Uri mPhotoUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		imageView = (ImageView) findViewById(R.id.imageView1);
		
		
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// Create Intent to take a picture and return control to the calling application
				// This is the strategy detailed by google at 
				// http://developer.android.com/guide/topics/media/camera.html
				// This method doesn't seem to be reliable
			    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
			    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

			    // start the image capture Intent
			    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_FILE_URI);				
			}
		}
		);
		
		button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// Alternative strategy of generating a content URI using the MediaStore
				// This way seems to work very reliably
				
				mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_CONTENT_RESOLVER);				
			}
		}
		);
		
		
		button = (Button) findViewById(R.id.button3);
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// Just use our own camera implementation
				// This works reliably, but doesn't always choose the best resolution for the camera
				// It also misses some other features
				// The capture photo activity could be improved to work just as well as the onboard camera activity
				
				Intent intent = new Intent("org.calflora.experimental.action.CAPTUREPHOTO");
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_HOMEGROWN);  
				
			}
		}
		);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	imageView.setImageBitmap(null);

	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_FILE_URI) {
	        if (resultCode == RESULT_OK) {
	            // Image captured and saved to fileUri specified in the Intent
	        	
        		// Docs at http://developer.android.com/guide/topics/media/camera.html
        		// tell us that data should not be null, and getData() should be a fileUri
	        	
	        	if(data != null){
	        	
	            Toast.makeText(this, "Image saved to:\n" +
	                     data.getData(), Toast.LENGTH_LONG).show();
	            Uri fileUri = data.getData();
	            if(fileUri == null){
	            	 Toast.makeText(this, "fileUri is NULL", Toast.LENGTH_LONG).show();
	            } else {
	                try {
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
						imageView.setImageBitmap(bitmap);
					} catch (FileNotFoundException e) {
		            	 Toast.makeText(this, "Failed to get bitmap from URI", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					} catch (IOException e) {
		            	 Toast.makeText(this, "Failed to get bitmap from URI", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
	            
	            }
	        	} else {

	        		Toast.makeText(this, "Intent data is NULL", Toast.LENGTH_LONG).show();
	        	}
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            // Image capture failed, advise user
	        }
	        
	        
	        
	        
	    } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_CONTENT_RESOLVER ) {
	    	if (resultCode == RESULT_OK) {
	    			// Image saved to a generated MediaStore.Images.Media.EXTERNAL_CONTENT_URI
	    		   String[] projection = {
	                        MediaStore.MediaColumns._ID,
	                        MediaStore.Images.ImageColumns.ORIENTATION,
	                        MediaStore.Images.Media.DATA
	                };
	                Cursor c = getContentResolver().query(mPhotoUri, projection, null, null, null);
	                c.moveToFirst();
	                String photoFileName = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
	                
	                Bitmap bitmap = BitmapFactory.decodeFile(photoFileName);
					imageView.setImageBitmap(bitmap);

	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            // Image capture failed, advise user
	        }

	    } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_HOMEGROWN ) {
	    	if (resultCode == RESULT_OK) {
	    		Bundle extras =  data.getExtras();
				if(extras == null){
					Toast.makeText(this, "Error Capturing Images", Toast.LENGTH_LONG ).show();
				} else {

					String photoFileName = extras.getString(CapturePhotoActivity.PHOTO_FILE_NAME);		
					Bitmap bitmap = BitmapFactory.decodeFile(photoFileName);
					imageView.setImageBitmap(bitmap);
				}

	    	} else if (resultCode == RESULT_CANCELED) {
	    		// User cancelled the image capture
	    	} else {
	    		// Image capture failed, advise user
	    	}


	    }
	}
}
