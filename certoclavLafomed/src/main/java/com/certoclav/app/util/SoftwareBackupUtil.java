package com.certoclav.app.util;

import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class SoftwareBackupUtil {
    private static final String path2Save = "/storage/udisk3/backup/";
    private static List<String> filesListInDir = new ArrayList<>();

    static {
        //noinspection ResultOfMethodCallIgnored
        new File(path2Save).mkdirs();
    }

//    @Nullable
//    public static String importBackup() {
//        try {
//            final File file2Import = new File(path2Save + "backup.zip");
//
//            if (!file2Import.exists())
//                return "\"backup.zip\" file not found in \"backup\" folder!";
//
//            unzip(file2Import, new File("/data/data/com.certoclav.app/"));
//
//            return null;
//        } catch (Exception e) {
//            return "Failed to import backup file!";
//        }
//    }
//
//    private static void unzip(File zipFile, File targetDirectory) throws IOException {
//        ZipInputStream zis = new ZipInputStream(
//                new BufferedInputStream(new FileInputStream(zipFile)));
//        try {
//            ZipEntry ze;
//            int count;
//            byte[] buffer = new byte[8192];
//            while ((ze = zis.getNextEntry()) != null) {
//                File file = new File(targetDirectory, ze.getName());
//                File dir = ze.isDirectory() ? file : file.getParentFile();
//                if (!dir.isDirectory() && !dir.mkdirs())
//                    throw new FileNotFoundException("Failed to ensure directory: " +
//                            dir.getAbsolutePath());
//                if (ze.isDirectory())
//                    continue;
//                FileOutputStream fout = new FileOutputStream(file);
//                try {
//                    while ((count = zis.read(buffer)) != -1)
//                        fout.write(buffer, 0, count);
//                } finally {
//                    fout.close();
//                }
//                // if time should be restored as well
//                long time = ze.getTime();
//                if (time > 0)
//                    file.setLastModified(time);
//            }
//        } finally {
//            zis.close();
//        }
//    }

    /**
     * This method zips the directory.
     *
     * @param dirPath directory to be zipped
     */
    public static boolean getBackup(String dirPath) {
        try {
            File dir = new File(dirPath);

            populateFilesList(dir);
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(path2Save + "backup.zip");
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
}
