package com.certoclav.library.util;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

	public FileUtils() {
	}

	public String getVideoDirectory(){
		File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		storageDirectory.mkdirs(); //create directory, if it does not exist yet
		return storageDirectory.getAbsolutePath();
	}

	public List<File> getVideosFromDownloadDirectory(){
		
		ArrayList<File> videoFiles = new ArrayList<File>();
		
		try {
			File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			storageDirectory.mkdirs(); //create directory, if it does not exist yet
			File[] files = storageDirectory.listFiles();
			if(files != null){
			    for(File file : files){
			    	if(file.getName().contains(".mp4")){
			    		videoFiles.add(file);
			    	}
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return videoFiles;
	}

	
}
