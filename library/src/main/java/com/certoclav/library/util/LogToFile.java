package com.certoclav.library.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



/**
 * Class used for logging data when tablet is connected to the microcontroller.
 * 
 * @author Iulia Rasinar <iulia.rasinar@nordlogic.com>
 * 
 */
public class LogToFile {

	public final static String LOG_PATH = "sdcard/log.file";
	
	/**
	 * Deletes the log file
	 * @return true if the file was deleted with success  
	 */
	public static boolean deleteLogFile(){
		File file = new File(LOG_PATH);
		return file.delete();
	}

	/**
	 * Writes the text given as parameter in the file {@link LogToFile#LOG_PATH}
	 * .
	 * 
	 * @param text
	 *            the text to be logged
	 */
	public static void appendLog(String text) {
		File logFile = new File(LogToFile.LOG_PATH);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true for setting append file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,true));
			buf.append(text);
			buf.newLine();
			buf.flush();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}