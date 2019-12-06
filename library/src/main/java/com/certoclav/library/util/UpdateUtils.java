package com.certoclav.library.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.certoclav.library.R;
import com.certoclav.library.certocloud.PostSoftwareUpdateService;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class UpdateUtils {

    public static final Integer FILETYPE_VIDEO = 1;
    private final String rootFolder;
    private Context context;
    public static final int SOURCE_USB = 1;
    public static final int SOURCE_FOLDER_DOWNLOAD = 2;
    public static final int SOURCE_SDCARD = 3;
    public final static String FILENAME_UPDATE = "update.zip";
    public static final String[] SD_FOLDERS = new String[]{"extsd", "sdcard1"};


    public UpdateUtils(Context context) {
        this.context = context;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Do something for lollipop and above versions
            rootFolder = "/storage";
        } else {
            rootFolder = android.os.Environment.getExternalStorageDirectory().getPath();
        }


    }


    public File getExternalSDCard() { //ckeck external sd card storage

        //functions provided in API level 11 very limited. See android.os.Environment documentation
        //because its not possible to check external sd card located at /sdcard/extsd/ - try to write to this location

        try {
            for (String folder : SD_FOLDERS) {
                File root = new File(rootFolder + "/" + folder + "/");
                if (root.exists()) {
                    return root;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;

    }

    //pass source from where the update should be installed
//SOURCE_USB or SOURCE_FOLDER_DOWNLOAD
    public void installUpdateZip(int source, final String deviceKey, final String softVersion) {


        new AsyncTask<Integer, Integer, Integer>() {
            final static int RESULT_UP_TO_DATE = 1, RESULT_FAILED_NO_FILE = 2,
                    RESULT_INSTALL = 3, PROGRESS_COPIED = 4, RESULT_SOMETHING_WENT_WRONG = 5,
                    RESULT_WRONG_FILE = 6, PROGRESS_EXTRACTING = 7, PROGRESS_COPYING = 8;
            private SweetAlertDialog dialogLocal;
            private Uri apkUri;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialogLocal = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
                dialogLocal.setCancelable(false);
                dialogLocal.setTitleText(context.getString(R.string.checking));
                dialogLocal.show();
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                switch (result) {
                    case RESULT_FAILED_NO_FILE:
                        dialogLocal.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        dialogLocal.setTitleText(context.getString(R.string.no_update_file));
                        break;
                    case RESULT_SOMETHING_WENT_WRONG:
                        dialogLocal.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        dialogLocal.setTitleText(context.getString(R.string.something_went_wrong));
                        break;
                    case RESULT_UP_TO_DATE:
                        dialogLocal.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        dialogLocal.setTitleText(context.getString(R.string.application_is_up_to_date));
                        // upload to cloud
                        PostSoftwareUpdateService service = new PostSoftwareUpdateService();
                        service.postDeviceData(deviceKey, softVersion);
                        break;
                    case RESULT_WRONG_FILE:
                        dialogLocal.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        dialogLocal.setTitleText(context.getString(R.string.no_update_file));
                        break;
                    case RESULT_INSTALL:
                        dialogLocal.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        dialogLocal.setTitleText(context.getString(R.string.new_update_exists));
                        dialogLocal.setConfirmText(context.getString(R.string.install));
                        dialogLocal.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                context.startActivity(intent);
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        });
                        break;

                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                switch (values[0]) {
                    case PROGRESS_COPYING:
                        dialogLocal.setTitleText(context.getString(R.string.copying));
                        break;
                    case PROGRESS_COPIED:
                        dialogLocal.setTitleText(context.getString(R.string.extracting));
                        break;
                    case PROGRESS_EXTRACTING:
                        dialogLocal.setTitleText(context.getString(R.string.checking));
                        break;
                }
            }

            @Override
            protected Integer doInBackground(Integer... params) {
                boolean copied = false;
                int source = params[0];
                try {

                    if (source == SOURCE_SDCARD) {
                        try {

                            File rootSD = getExternalSDCard();

                            File dirSD = new File(rootSD.getAbsolutePath());
                            File[] usbFiles = dirSD.listFiles();
                            for (File usbFile : usbFiles) {
                                if (usbFile.isFile()) {
                                    if (usbFile.getName().equals(FILENAME_UPDATE)) {
                                        String sourceFile = usbFile.getAbsolutePath();
                                        String destinationFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + usbFile.getName();
                                        copied = copyFile(sourceFile, destinationFile);
                                        publishProgress(PROGRESS_COPYING);
                                        if (!copied) {
                                            return RESULT_SOMETHING_WENT_WRONG;
                                        }
                                        break;
                                    }
                                }
                            }


                        } catch (Exception e) {
                            return RESULT_SOMETHING_WENT_WRONG;
                        }
                    }


                    if (source == SOURCE_USB) {
                        try {
                            File[] udisks = new File(rootFolder).listFiles();
                            for (File f : udisks) {
                                if (f.getName().startsWith("udisk")) {
                                    File dirUSB = new File(f.getAbsolutePath());
                                    File[] usbFiles = dirUSB.listFiles();
                                    for (File usbFile : usbFiles) {
                                        if (usbFile.isFile()) {
                                            if (usbFile.getName().equals(FILENAME_UPDATE)) {
                                                String sourceFile = usbFile.getAbsolutePath();
                                                String destinationFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + usbFile.getName();
                                                copied = copyFile(sourceFile, destinationFile);
                                                publishProgress(PROGRESS_COPYING);
                                                if (!copied) {
                                                    return RESULT_SOMETHING_WENT_WRONG;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            return RESULT_SOMETHING_WENT_WRONG;
                        }
                    }
                    if(source == SOURCE_FOLDER_DOWNLOAD){
                        copied = true;
                    }
                    if (!copied)
                        return RESULT_FAILED_NO_FILE;

                    //delete old files except the recently created update.zip
                    File[] files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
                    for (File file : files) {
                        if (!file.getName().equals("update.zip"))
                            file.delete();
                    }

                    publishProgress(PROGRESS_COPIED);


                    if (!ExtractZipFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + FILENAME_UPDATE,
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())) {
                        Log.e("UpdateUtils", "unable to unzip files");
                        return RESULT_WRONG_FILE;
                    }

                    publishProgress(PROGRESS_EXTRACTING);

                    //get a list of apps which are stored at /sdcard/Download/
                    files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();

                    if (files != null) {
                        List<File> filesSorted = new ArrayList<File>();
                        //resorting files -> putting apk file to the end.
                        for (File file : files) {
                            Log.e("UpdateUtils", "filename found: " + file.getName());
                            if (file.getName().contains("CertoclavAndroid")) {
                                filesSorted.add(file);
                            } else {
                                filesSorted.add(0, file);
                            }
                        }

                        for (int i = 0; i < filesSorted.size(); i++) {

                            if (filesSorted.get(i).getName().endsWith("apk")) {
                                String apkFilePath = filesSorted.get(i).getAbsolutePath(); //For example...
                                PackageManager pm = context.getPackageManager();
                                PackageInfo pi = pm.getPackageArchiveInfo(apkFilePath, 0);
                                if (pi != null) {

                                    // the secret are these two lines....
                                    pi.applicationInfo.sourceDir = apkFilePath;
                                    pi.applicationInfo.publicSourceDir = apkFilePath;
                                    int versionCode = pi.versionCode;
                                    PackageInfo currentPi = context.getPackageManager().getPackageInfo(pi.packageName, 0);
                                    if (currentPi.versionCode < versionCode) {
                                        apkUri = Uri.fromFile(filesSorted.get(i));
                                        return RESULT_INSTALL;
                                    } else {
                                        return RESULT_UP_TO_DATE;
                                    }

                                }
                            } else if (filesSorted.get(i).getName().endsWith("mp4")) {
                                Log.e("DownloadUtils", "found mp4 media file " + filesSorted.get(i).getName());

                            }


                        }
                    }//end for(installing files)

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    return RESULT_SOMETHING_WENT_WRONG;
                }
                return RESULT_UP_TO_DATE;
            }
        }.execute(source);

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
        try {
            Log.e("UpdateUtils", "copyFile() from: " + source + " to: " + dest);
            OutputStream myoutput = new FileOutputStream(dest);
            InputStream myinput = new FileInputStream(source);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myinput.read(buffer)) > 0) {
                myoutput.write(buffer, 0, length);
            }
            myoutput.flush();

            myoutput.close();

            myinput.close();

        } catch (Exception e) {
            return false;
        }
        return true;

    }
}


			
			
			
			
		
		
