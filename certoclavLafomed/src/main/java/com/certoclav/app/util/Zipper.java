package com.certoclav.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Zipper {
    private static final String path2Save = "/storage/udisk3/backup/";
    private static List<String> filesListInDir = new ArrayList<>();

    static {
        //noinspection ResultOfMethodCallIgnored
        new File(path2Save).mkdirs();
    }

    /**
     * This method zips the directory.
     *
     * @param dirPath directory to be zipped
     */
    public static boolean zipDirectory(String dirPath) {
        try {
            File dir = new File(dirPath);

            populateFilesList(dir);
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(path2Save + getFormattedTimestamp() + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (String filePath : filesListInDir) {
                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length() + 1));
                zos.putNextEntry(ze);
                //read the file and write to ZipOutputStream
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0)
                    zos.write(buffer, 0, len);
                zos.closeEntry();

                fis.close();
            }
            zos.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void populateFilesList(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) filesListInDir.add(file.getAbsolutePath());
            else populateFilesList(file);
        }
    }

    private static String getFormattedTimestamp() {
        return new SimpleDateFormat("dd.MM.yyyy_kk-mm-ss-SSS", Locale.ROOT).format(new Date(System.currentTimeMillis()));
    }
}
