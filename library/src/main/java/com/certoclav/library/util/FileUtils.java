package com.certoclav.library.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

public class FileUtils {

	public FileUtils() {
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
