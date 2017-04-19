package com.certoclav.library.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadUtils {

	Context mContext;
	private List<Long> downloadReferences;
	private DownloadManager downloadManager;
	private int numberOfDownloads;
	private int counterDownload;
    ProgressDialog barProgressDialog;

	
	public DownloadUtils(Context context) {
		this.mContext = context;
		downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		
		  //set filter to only when download is complete and register broadcast receiver
		  IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		  context.registerReceiver(downloadReceiver, filter);
		  counterDownload = 0;
		  numberOfDownloads = 0;
		  
	}

	
			
			
			
			
			
			
			
			
		
			

		 
	
	
	/*
	 * 
	 * dirType the directory type to pass to Environment.getExternalStoragePublicDirectory(String). For example Environment.DOWNLOAD_DIRECTORY
	 */
	public void Download(List<String> urls){
		//String dirType = Environment.DIRECTORY_DOWNLOADS;
		//urls.clear();
		//urls.add("https://www.basicthinking.de/blog/wp-content/uploads/2013/06/see-how-your-google-results-measure-up-with-google-grader-video-6b8bbb4b41.jpg");
		
		  counterDownload = 0; 
		  numberOfDownloads = urls.size();
		downloadReferences = new ArrayList<Long>();
		
        barProgressDialog = new ProgressDialog(mContext);
   	 
        barProgressDialog.setTitle("DOWNLOAD UPDATES");
        barProgressDialog.setMessage("Downloading update.zip");
        barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(100);
        barProgressDialog.setCancelable(true);
        barProgressDialog.setCanceledOnTouchOutside(false);
        barProgressDialog.show();
        
	if(isDownloadManagerAvailable()){

		for(String url : urls){
		
	        String filepathArray[] = url.split("/");
	        String nameOfFile = filepathArray[filepathArray.length-1];
	        Log.e("DownloadUtils", url);
	        Log.e("DownloadUtils", "download file.. filename: " +  nameOfFile);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription(nameOfFile);
			
			
		
			
			// in order for this if to run, you must use the android 3.2 to compile your app
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//	request.allowScanningByMediaScanner();
			//	request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			
			File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
			if (!folder.exists()) {
			    folder.mkdir();
			    Log.e("DownloadUtils", "created download folder");
			}else{
				 Log.e("DownloadUtils", "download folder already exists -> no need to mkdir");
			}
			
			
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameOfFile);
			File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"+ nameOfFile);
			Log.e("DownloadUtils", "looking if file exists: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"+ nameOfFile);
			if (f.exists()) { 
				Log.e("DownloadUtils","file exists "  + nameOfFile + "deleting");
			    boolean fileWasDeleted = f.delete();
			    Log.e("DownloadUtils", "file was deleteted: "  + fileWasDeleted);
			}else{
				Log.e("DownloadUtils", f.getPath().toString() + " does not exist -> no need to delete it");
			}

			// get download service and enqueue file
			
			final long downloadReference = downloadManager.enqueue(request);
			downloadReferences.add(downloadReference);
			
			
		
		
		       new Thread(new Runnable() {

		            @Override
		            public void run() {

		                boolean downloading = true;

		                while (downloading) {

		                    DownloadManager.Query q = new DownloadManager.Query();
		                    q.setFilterById(downloadReference);

		                    Cursor cursor = downloadManager.query(q);
		                    cursor.moveToFirst();
		                    int bytes_downloaded = cursor.getInt(cursor
		                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
		                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

		                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
		                        downloading = false;
		                    }

		                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
		                        downloading = false;
		                    }
		                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
  	
		                    Log.e("DownloadUtils", "progress: " + (int) dl_progress);
		                    Log.e("download status message", statusMessage(cursor));
		                    barProgressDialog.setProgress(dl_progress);
		                   
		                    cursor.close();
		                    try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		                }

		            }
		        }).start();
		
		
		}
	}
}
	
	
	private String statusMessage(Cursor c) {
	    String msg = "???";

	    switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
	    case DownloadManager.STATUS_FAILED:
	        msg = "Download failed!";
	        //barProgressDialog.setMessage("Downlaod failed");
	        barProgressDialog.dismiss();
	        //Toast.makeText(mContext, "Download failed", Toast.LENGTH_LONG);
	        break;

	    case DownloadManager.STATUS_PAUSED:
	        msg = "Download paused!";
	        //barProgressDialog.setMessage("Downlaod paused");
	        break;

	    case DownloadManager.STATUS_PENDING:
	        msg = "Download pending!";
	        //barProgressDialog.setMessage("Downlaod pending");
	        break;

	    case DownloadManager.STATUS_RUNNING:
	        msg = "Download in progress!";
	        //barProgressDialog.setMessage("Downlaoding update.zip");
	        break;

	    case DownloadManager.STATUS_SUCCESSFUL:
	        msg = "Download complete!";
	        break;

	    default:
	        msg = "Download is nowhere in sight";
	        break;
	    }

	    return (msg);
	}
	
	
	/**
	 * @param mContext used to check the device version and DownloadManager information
	 * @return true if the download manager is available
	 */
	private boolean isDownloadManagerAvailable() {
	    try {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	            return false;
	        }
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	    	Log.e("DownloadUtils","exception downloadmanager" + e.toString());
	        return false;
	    }
	}
	
	

	
   


    
	 private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		 
		  @Override
		  public void onReceive(Context context, Intent intent) {
			  
			    Bundle extras = intent.getExtras();
			    DownloadManager.Query q = new DownloadManager.Query();
			    Long downloaded_id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);

				    q.setFilterById(downloaded_id);
				    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				    Cursor c = manager.query(q);
				    if (c.moveToFirst()) {
				        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
				        if (status == DownloadManager.STATUS_SUCCESSFUL) {
				           
				        	
							  barProgressDialog.dismiss();
							  counterDownload = counterDownload +1;
							  if(counterDownload == numberOfDownloads){
								  UpdateUtils updateutils = new UpdateUtils(mContext);
								  updateutils.installUpdateZip(UpdateUtils.SOURCE_FOLDER_DOWNLOAD);
					
								   Toast.makeText(mContext,"all downloads done", Toast.LENGTH_SHORT).show();  
								  
							  }
							  
							  
				        }else{
				        	Toast.makeText(mContext,"Download failed", Toast.LENGTH_SHORT).show(); 
				        	barProgressDialog.dismiss();
				        }
				    }
				    c.close();

		  }
		 }; 
		 
		 

}
