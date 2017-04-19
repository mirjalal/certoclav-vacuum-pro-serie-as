package com.certoclav.library.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import android.util.Log;


public class ExportUtils {


		
		
		
		/** Method to check whether external sdcard available and writable. This is adapted from
		   http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */

		 public boolean checkExternalSDCard(){ //ckeck external sd card storage
			
			 //functions provided in API level 11 very limited. See android.os.Environment documentation
			 //because its not possible to check external sd card located at /sdcard/extsd/ - try to write to this location
		 
		  try{
			  File root = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/extsd/");
			    Log.e("Export Utils", "\nExternal file system root: "+root);

			    File dir = new File (root.getAbsolutePath());
			    dir.mkdirs();
			    File file = new File(dir, "log.txt"); // for example protocol123.txt
			    
			   
			        FileOutputStream f = new FileOutputStream(file);
			        PrintWriter pw = new PrintWriter(f);
			        pw.print("Folder last modified: " + Calendar.getInstance().getTime().toString());
			        pw.flush();
			        pw.close();
			        f.close();
			    } catch (Exception e) {
			        return false;
			    }
		  
		 
		
		  return true;
		  
		 }
		 
		
		
		
	/** Method to check whether external media available and writable. This is adapted from
	   http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */

	 public boolean checkExternalMedia(){ //ckeck external usb storage
		

		 
	  try{
		  File root = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/udisk/");
		    Log.e("Export Utils", "\nExternal file system root: "+root);

		    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

		    File dir = new File (root.getAbsolutePath() + "/Certoclav_protocols");
		    dir.delete();
		    dir.mkdir();
		    File file = new File(dir, "log.txt"); // for example protocol123.txt
		    
		   
		        FileOutputStream f = new FileOutputStream(file);
		        PrintWriter pw = new PrintWriter(f);
		        pw.print("Folder last modified: " + Calendar.getInstance().getTime().toString());
		        pw.flush();
		        pw.close();
		        f.close();
		    } catch (Exception e) {
		        return false;
		    }
	  
	 
	
	  return true;
	 }
	/** Method to write ascii text characters to file on SD card. Note that you must add a 
	   WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
	   a FileNotFound Exception because you won't have write permission. */

	public boolean writeToExtUsbFile(String subfolder, String filename, String filetype, String data){

	    // Find the root of the external storage.
	    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

	    File root = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/udisk/");
	    Log.e("Export Utils", "\nExternal file system root: "+root);

	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

	    File dir = new File (root.getAbsolutePath() + "/Certoclav protocols");
	    dir.mkdirs();
	    File file = new File(dir, filename + "." + filetype); // for example protocol123.txt

	    try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        pw.print(data);
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        Log.e("ExportUtils", "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	        return false;
	    } catch (IOException e) {
	        e.printStackTrace();
	        Log.e("Export Utils","io exception: " +  e.toString());
	        return false;
	    }   
	    Log.e("Export Utils","\n\nFile written to "+file);
	    return true;
	}

	

	
	
	public boolean writeToExtSDFile(String subfolder, String filename, String filetype, String data){

	    // Find the root of the external storage.
	    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

	    File root = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/extsd/");
	    Log.e("Export Utils", "\nExternal file system root: "+root);

	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

	    File dir = new File (root.getAbsolutePath() + "/Certoclav protocols");
	    dir.mkdirs();
	    File file = new File(dir, filename + "." + filetype); // for example protocol123.txt

	    try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        pw.print(data);
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        Log.e("ExportUtils", "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	        return false;
	    } catch (IOException e) {
	        e.printStackTrace();
	        Log.e("Export Utils","io exception: " +  e.toString());
	        return false;
	    }   
	    Log.e("Export Utils","\n\nFile written to "+file);
	    return true;
	}

	
}
