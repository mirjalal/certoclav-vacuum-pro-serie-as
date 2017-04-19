package com.certoclav.library.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.certoclav.library.R;



public class UpdateUtils {

	public static final Integer FILETYPE_VIDEO = 1;
	private Context context;
	public static final int SOURCE_USB = 1;
	public static final int SOURCE_FOLDER_DOWNLOAD= 2;
	public static final int SOURCE_SDCARD = 3;
	public final static String FILENAME_UPDATE = "update.zip";
	
		
public UpdateUtils(Context context) {
		this.context = context;
		

        
	}



//pass source from where the update should be installed
//SOURCE_USB or SOURCE_FOLDER_DOWNLOAD
public boolean installUpdateZip(int source){
	
try{


	if(source == SOURCE_SDCARD){
		  try{
			  
			  File rootSD = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/extsd/");
	
			    File dirSD = new File( rootSD.getAbsolutePath() );
			    File[] usbFiles = dirSD.listFiles();
			    for( File usbFile : usbFiles ) {
			        if( usbFile.isFile()  ) {
			        	if(usbFile.getName().equals(FILENAME_UPDATE)){
			        		String sourceFile = usbFile.getAbsolutePath();
			        		String destinationFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + usbFile.getName();
			        		Boolean success = copyFile( sourceFile, destinationFile);
			        		if(success == false){
			        			Log.e("UpdateUtils", "unable to copy file from SDcard to downlaod folder");
			        			return false;
			        		}
			        		break;
			        	}
			        }
			    }

			    
			    } catch (Exception e) {
			    	return false;
			    }
	}
   
   
	if(source == SOURCE_USB){
		  try{
			  
 
			  
			  File rootUSB = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/udisk/");
	
			    File dirUSB = new File( rootUSB.getAbsolutePath() );
			    File[] usbFiles = dirUSB.listFiles();
			    for( File usbFile : usbFiles ) {
			        if( usbFile.isFile()  ) {
			        	if(usbFile.getName().equals(FILENAME_UPDATE)){
			        		String sourceFile = usbFile.getAbsolutePath();
			        		String destinationFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + usbFile.getName();
			        		Boolean success = copyFile( sourceFile, destinationFile);
			        		if(success == false){
			        			Log.e("UpdateUtils", "unable to copy file from USB flash drive to downlaod folder");
			        			return false;
			        		}
			        		break;
			        	}
			        }
			    }
			    
			    } catch (Exception e) {
			    	return false;
			    }
	}
	
	
	//delete old files except the recently created update.zip
    File[] files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
    for(File file : files){
    	if(file.getName().equals("update.zip")){
    		//do nothing
    	}else{
    		//delete file
    	   file.delete();
    	}
    }
    
	
	    boolean success = ExtractZipFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + FILENAME_UPDATE, 
	    		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
	    
	    if(success == false){
	    	Log.e("UpdateUtils", "unable to unzip files");
	    	return false;
	    }
	    
		//get a list of apps which are stored at /sdcard/Download/
		files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
		
		
		if(files != null){
			List<File> filesSorted = new ArrayList<File>();
			//resorting files -> putting apk file to the end.
			for(File file : files){
				Log.e("UpdateUtils", "filename found: " + file.getName());
				if(file.getName().contains("CertoclavAndroid")){
					filesSorted.add(file);
				}else{
					filesSorted.add(0, file);
				}
			}
			for(File file : filesSorted){
				Log.e("UpdateUtils", "files sorted: " + file.getName());
			}
			
			for(int i = 0; i< filesSorted.size(); i++){

				if(filesSorted.get(i).getName().endsWith("apk")){
					 String apkFilePath = filesSorted.get(i).getAbsolutePath(); //For example...
					 PackageManager pm = context.getPackageManager();
					 Log.e("Updateutils", "vor getpackagearchiveinfo");
					 PackageInfo    pi = pm.getPackageArchiveInfo(apkFilePath, 0);
					 if(pi != null){
						 Log.e("Updateutils", "nach getpackagearchiveinfo");
						 // the secret are these two lines....
						 pi.applicationInfo.sourceDir       = apkFilePath;
						 pi.applicationInfo.publicSourceDir = apkFilePath;
						 String   AppName = (String)pi.applicationInfo.loadLabel(pm);

						 int versionCode = pi.versionCode;
						 
						 Log.e("DownloadUtils", "found app: " + AppName + " " + " with versioncode: "+ versionCode + " in download directory");
			
						
						    try {
						        PackageInfo currentPi = context.getPackageManager().getPackageInfo(pi.packageName, 0);        
	
						        int currentVersionCode = currentPi.versionCode;
						        
						        if(currentVersionCode < versionCode){
						        	Log.e("DownloadUtils", AppName+ " update found");
						        	Intent intent = new Intent(Intent.ACTION_VIEW);
							    	intent.setDataAndType(Uri.fromFile(filesSorted.get(i)), "application/vnd.android.package-archive");
							    	context.startActivity(intent);
						        	
						        }else{
						        	try{
						        		Toast.makeText(context, context.getString(R.string.application_is_up_to_date), Toast.LENGTH_LONG).show();
						        	}catch(Exception e){
						        		
						        	}
						        	Log.e("DownloadUtils", AppName + " up to date. current version is " + currentVersionCode);
						        }
						
						    } catch (NameNotFoundException e) {
						    	Intent intent = new Intent(Intent.ACTION_VIEW);
						    	intent.setDataAndType(Uri.fromFile(filesSorted.get(i)), "application/vnd.android.package-archive");
						    	context.startActivity(intent);
						 
					        		

						      }
						    }
					}else if(filesSorted.get(i).getName().endsWith("mp4")){
						    Log.e("DownloadUtils", "found mp4 media file " + filesSorted.get(i).getName() );
						    
				    }
		
		
					    
				}
			}//end for(installing files)
			
}catch(Exception e){
	return false;
}
return true;
}



/*
* Copyright 2010 Srikanth Reddy Lingala  
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License. 
* You may obtain a copy of the License at 
* 
* http://www.apache.org/licenses/LICENSE-2.0 
* 
* Unless required by applicable law or agreed to in writing, 
* software distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License. 
*/



/**
 * Demonstrates extraction of a single file from the zip file
 * 
 * @author Srikanth Reddy Lingala
 */


	
	public boolean ExtractZipFile(String source, String dest) {
		
	
		Log.e("udpateustils inzip", "source: " + source);
		Log.e("udpateustils inzip", "dest: " + dest);
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile(source);
			
			// Extracts all files to the path specified
			zipFile.extractAll(dest);

		} catch (ZipException e) {
			return false;
		}
		return true;
		
	}
	
	

    private boolean copyFile(String source, String dest) {
        // TODO Auto-generated method stub

        try
        {
        	Log.e("UpdateUtils", "copyFile() from: " + source+ " to: " + dest);
            OutputStream myoutput = new FileOutputStream(dest);
            InputStream myinput = new FileInputStream(source);
            byte[] buffer = new byte[1024];
            int length;
            while((length=myinput.read(buffer))>0)
            {
                myoutput.write(buffer, 0, length);
            }
            myoutput.flush();

            myoutput.close();

            myinput.close();

        }
        catch(Exception e)
        {
        	return false;
        }
        return true;

    }
}


			
			
			
			
		
		
