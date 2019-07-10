package com.certoclav.app.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.Log;
import com.certoclav.library.application.ApplicationController;
import com.j256.ormlite.dao.ForeignCollection;

import java.text.Format;
import java.text.SimpleDateFormat;

import android_serialport_api.SerialService;


public class ESCPos {


    public Boolean debug = false; //debug switch

    public final static String VERSION = "##version##";
    private SerialService printer = null;
    private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());


    public ESCPos() {
        printer = Autoclave.getInstance().getSerialsServiceProtocolPrinter();

    }


    public void printProtocol(Protocol protocol, Context context) {
        //get version
        //Print line
        i = 5;
        String version = "";
        try {
            PackageInfo pInfo;
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = " (" + pInfo.versionName + ")";
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            version = "";
        }

        //start time
        String startTimeString = "";
        try {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startTimeString = formatter.format(protocol.getStartTime());
        } catch (Exception e) {
            e.printStackTrace();
            startTimeString = "";
        }

        //end time
        String endTimeString = "";
        try {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            endTimeString = formatter.format(protocol.getEndTime());
        } catch (Exception e) {
            e.printStackTrace();
            endTimeString = "";
        }


        resetToDefault();
        String SPACING = "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n";
        StringBuilder sb = new StringBuilder();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        //GLP Header
        if (isPrefEnabled(R.string.preferences_print_header, R.bool.preferences_print_header)
                && prefs.getString("preferences_glp_header", "").length() > 0) {
            sb.append(getPrintLine()).append(prefs.getString("preferences_glp_header", "") + "\n");
        }
        //Autoclave Name
        if (isPrefEnabled(R.string.preferences_print_autoclave_name, R.bool.preferences_print_autoclave_name))
            sb.append(getPrintLine()).append(context.getString(R.string.glp_autoclave_name)).append(" ").append(
                    prefs.getString(context.getString(R.string.preferences_glp_autoclave_name),
                            context.getString(R.string.preferences_print_autoclave_name_value)) + "\n");

        //Serial Number
        if (isPrefEnabled(R.string.preferences_print_autoclave_serial_number, R.bool.preferences_print_autoclave_serial_number))
            sb.append(getPrintLine()).append(context.getString(R.string.glp_autoclave_serial_number)).append(" ").append(
                    Autoclave.getInstance().getController().getSavetyKey()).append(version).append("\n");

        //Project Name
        if (isPrefEnabled(R.string.preferences_print_project_name, R.bool.preferences_print_project_name))
            sb.append(getPrintLine()).append(context.getString(R.string.glp_project_name)).append(" ").append(
                    prefs.getString(context.getString(R.string.preferences_glp_project_name),
                            "") + "\n");


        sb.append(SPACING);

        //Program Name
        if (isPrefEnabled(R.string.preferences_print_program_name, R.bool.preferences_print_program_name)) {
            sb.append(getPrintLine()).append(context.getString(R.string.glp_program_name)).append(" ")
                    .append(protocol.getProfileName().replace("\u00B0", "")).append("\n");
        }
        //Program description
        if (isPrefEnabled(R.string.preferences_print_program_desc, R.bool.preferences_print_program_desc)) {
            sb.append(getPrintLine()).append(context.getString(R.string.print_program_desc)).append(":").append("\n");
            sb.append("   ").append(protocol.getProfileDescription().replace("\u00B0", "")
                    .replaceAll("\u2103", "C")
                    .replaceAll("\u2109", "F")
                    .replace("\n", "\n   ")).append("\n");
            sb.append(getPrintLine()).append(context.getString(R.string.date)).append(": ")
                    .append(startTimeString).append("\n");
            sb.append(getPrintLine()).append(context.getString(R.string.cycle)).append(": ")
                    .append(protocol.getZyklusNumber()).append("\n");
            sb.append(SPACING);
        }

        if (isPrefEnabled(R.string.preferences_print_program_result, R.bool.preferences_print_program_result)) {
            if (protocol.getErrorCode() == 0) {
                sb.append(getPrintLine()).append(context.getString(R.string.result)).append(": ").append(context.getString(R.string.passed).toUpperCase()).append("\n");
            } else {
                sb.append(getPrintLine()).append(context.getString(R.string.result)).append(": ").append(context.getString(R.string.failed).toUpperCase()).append("\n");
                sb.append(getPrintLine()).append(AutoclaveMonitor.getInstance().getErrorString(protocol.getErrorCode())).append("\n");
            }
            sb.append(SPACING);
            sb.append(getPrintLine()).append(context.getString(R.string.glp_temperature)).append(": ")
                    .append(Helper.getInstance().celsiusToCurrentUnit(protocol.getSterilisationTemperature())).append("\n");
            sb.append(getPrintLine()).append(context.getString(R.string.glp_pressure)).append(": ").append(protocol.getSterilisationPressure()).append("\n");
            sb.append(getPrintLine()).append(context.getString(R.string.glp_time)).append(": ").append(protocol.getSterilisationTime()).append("\n");
            sb.append(getPrintLine()).append(context.getString(R.string.glp_start)).append(": ").append(startTimeString).append("\n");
            sb.append(getPrintLine()).append("End").append(": ").append(endTimeString).append("\n");
            sb.append(SPACING);
        }

        if (isPrefEnabled(R.string.preferences_print_user_name, R.bool.preferences_print_user_name)) {
            sb.append(getPrintLine()).append(context.getString(R.string.email)).append(": ").append(protocol.getUserEmail()).append("\n");
            sb.append(SPACING);
        }
        if (isPrefEnabled(R.string.preferences_print_program_data_points, R.bool.preferences_print_program_data_points)) {

            boolean showMediaTemp = false;
            boolean showMedia2Temp = false;

            ForeignCollection<ProtocolEntry> protocolEntries = protocol.getProtocolEntry();

            for (ProtocolEntry protocolEntry : protocolEntries) {
                showMediaTemp = protocolEntry.getMediaTemperature() > -100;
                showMedia2Temp = protocolEntry.getMediaTemperature2() > -100;
                break;
            }

            sb.append(getPrintLine()).append(context.getString(R.string.glp_data_points)).append("\n");
            sb.append(String.format("%-8s", "t[h:m]"))
                    .append(String.format("%-8s", "P[bar]"))
                    .append(String.format("%-8s", "S[" + AutoclaveModelManager.getInstance().getTemperatureUnit() + "]"))
                    .append(showMediaTemp ? String.format("%-8s", "M[" + AutoclaveModelManager.getInstance().getTemperatureUnit() + "]") : "")
                    .append(showMedia2Temp ? String.format("%-8s", "M2[" + AutoclaveModelManager.getInstance().getTemperatureUnit() + "]") : "")
                    .append("\n");
            Long startTime = protocol.getStartTime().getTime();
            int minutesOnLastPrint = -1;
            for (ProtocolEntry entry : protocolEntries) {
                Long timeDiff = entry.getTimestamp().getTime() - startTime;
                int hours = (int) (timeDiff / (1000 * 60 * 60));
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                if (minutesOnLastPrint != minutes) {
                    minutesOnLastPrint = minutes;
                    String timeStamp = String.format("%02d:%02d", hours, minutes);
                    String pressure = String.format("%04.2f", entry.getPressure());
                    String temperature = String.format("%05.1f", Helper.getInstance().celsiusToCurrentUnit(entry.getTemperature()));
                    String mediaTemp = String.format("%05.1f", Helper.getInstance().celsiusToCurrentUnit(entry.getMediaTemperature()));
                    String mediaTemp2 = String.format("%05.1f", Helper.getInstance().celsiusToCurrentUnit(entry.getMediaTemperature2()));

                    sb.append(String.format("%-8s", timeStamp))
                            .append(String.format("%-8s", pressure))
                            .append(String.format("%-8s", temperature))
                            .append(showMediaTemp ? String.format("%-8s", mediaTemp) : "")
                            .append(showMedia2Temp ? String.format("%-8s", mediaTemp2) : "")
                            .append("\n");
                }
            }
            sb.append(SPACING);
        }

//        sb.append(context.getString(R.string.glp_end)).append("\n");

        //Printed Date
        if (isPrefEnabled(R.string.preferences_print_date, R.bool.preferences_print_date))
            sb.append(getPrintLine()).append(context.getString(R.string.glp_date)).append("\t").append(startTimeString).append("\n");

        if (isPrefEnabled(R.string.preferences_print_signature, R.bool.preferences_print_signature)) {
            sb.append("\n");
            sb.append(getPrintLine()).append(context.getString(R.string.signature) + ":    _______________" + "\n");
            sb.append("\n");
            sb.append(getPrintLine()).append(context.getString(R.string.verified_by) + ":  _______________" + "\n");
        }


        android.util.Log.e("printed", sb.toString());
        printString(sb.toString());

    }

    private boolean isPrefEnabled(int resId, int defaultResId) {
        return prefs.getBoolean(AppController.getContext().getString(resId), AppController.getContext().getResources().getBoolean(defaultResId));
    }

    int i = 5;

    private String getPrintLine() {
        i += 5;
        return i + " ";
    }

    public void sayHello() {


        printer.write((byte) (byte) 0x1B);
        printer.sendMessage("@");
        printer.sendMessage("Hello World");
        printer.write((byte) 0x1B);
        printer.sendMessage("d");
        printer.write((byte) 6);

        System.out.println("Hello World");
    }

    /**
     * return the version of the library.
     *
     * @return String
     */
    public static String version() {
        return VERSION;
    }

    /**
     * reusable init esc code
     */
    public void escInit() {
        printer.write((byte) 0x1B);
        printer.sendMessage("@");
    }

    /**
     * resets all printer settings to default
     */
    public void resetToDefault() {
        setInverse(false);
        setBold(false);
        setUnderline(0);
        setJustification(0);
    }

    /**
     * @param str String to print
     */
    public void printString(String str) {
        //escInit();
        printer.sendMessage(str);
        printer.write((byte) 0xA);
    }

    public void storeString(String str) {
        printer.sendMessage(str);
    }

    public void storeChar(int hex) {
        printer.write((byte) hex);
    }

    public void printStorage() {
        printer.write((byte) 0xA);
    }

    /**
     * Prints n lines of blank paper.
     */
    public void feed(int feed) {
        //escInit();
        printer.write((byte) 0x1B);
        printer.sendMessage("d");
        printer.write((byte) feed);
    }

    /**
     * Prints a string and outputs n lines of blank paper.
     */

    public void printAndFeed(String str, int feed) {
        //escInit();
        printer.sendMessage(str);
        //output extra paper
        printer.write((byte) 0x1B);
        printer.sendMessage("d");
        printer.write((byte) feed);
    }

    /**
     * Sets bold
     */
    public void setBold(Boolean bool) {
        printer.write((byte) 0x1B);
        printer.sendMessage("E");
        printer.write((byte) (int) (bool ? 1 : 0));
    }

    /**
     * Sets white on black printing
     */
    public void setInverse(Boolean bool) {
        printer.write((byte) 0x1D);
        printer.sendMessage("B");
        printer.write((byte) (int) (bool ? 1 : 0));
    }

    /**
     * Sets underline and weight
     *
     * @param val 0 = no underline.
     *            1 = single weight underline.
     *            2 = double weight underline.
     */

    public void setUnderline(int val) {
        printer.write((byte) 0x1B);
        printer.sendMessage("-");
        printer.write((byte) val);
    }


    /**
     * Sets left, center, right justification
     *
     * @param val 0 = left justify.
     *            1 = center justify.
     *            2 = right justify.
     */

    public void setJustification(int val) {
        printer.write((byte) 0x1B);
        printer.sendMessage("a");
        printer.write((byte) val);
    }

    /**
     * Encode and print QR code
     *
     * @param str        String to be encoded in QR.
     * @param The        degree of error correction. (48 <= n <= 51)
     *                   48 = level L / 7% recovery capacity.
     *                   49 = level M / 15% recovery capacity.
     *                   50 = level Q / 25% recovery capacity.
     *                   51 = level H / 30% recovery capacity.
     * @param moduleSize The size of the QR module (pixel) in dots.
     *                   The QR code will not print if it is too big.
     *                   Try setting this low and experiment in making it larger.
     */
    public void printQR(String str, int errCorrect, int moduleSize) {
        //save data function 80
        printer.write((byte) 0x1D);//init
        printer.sendMessage("(k");//adjust height of barcode
        printer.write((byte) (str.length() + 3)); //pl
        printer.write((byte) 0); //ph
        printer.write((byte) 49); //cn
        printer.write((byte) 80); //fn
        printer.write((byte) 48); //
        printer.sendMessage(str);

        //error correction function 69
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 3); //pl
        printer.write((byte) 0); //ph
        printer.write((byte) 49); //cn
        printer.write((byte) 69); //fn
        printer.write((byte) errCorrect); //48<= n <= 51

        //size function 67
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 3);
        printer.write((byte) 0);
        printer.write((byte) 49);
        printer.write((byte) 67);
        printer.write((byte) moduleSize);//1<= n <= 16

        //print function 81
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 3); //pl
        printer.write((byte) 0); //ph
        printer.write((byte) 49); //cn
        printer.write((byte) 81); //fn
        printer.write((byte) 48); //m
    }

    /**
     * Encode and print barcode
     *
     * @param code String to be encoded in the barcode.
     *             Different barcodes have different requirements on the length
     *             of data that can be encoded.
     * @param type Specify the type of barcode
     *             65 = UPC-A.
     *             66 = UPC-E.
     *             67 = JAN13(EAN).
     *             68 = JAN8(EAN).
     *             69 = CODE39.
     *             70 = ITF.
     *             71 = CODABAR.
     *             72 = CODE93.
     *             73 = CODE128.
     * @param h    height of the barcode in points (1 <= n <= 255)
     * @param w    width of module (2 <= n <=6).
     *             Barcode will not print if this value is too large.
     * @param font Set font of HRI characters
     *             0 = font A
     *             1 = font B
     * @param pos  set position of HRI characters
     *             0 = not printed.
     *             1 = Above barcode.
     *             2 = Below barcode.
     *             3 = Both above and below barcode.
     */
    public void printBarcode(String code, int type, int h, int w, int font, int pos) {

        //need to test for errors in length of code
        //also control for input type=0-6

        //GS H = HRI position
        printer.write((byte) 0x1D);
        printer.sendMessage("H");
        printer.write((byte) pos); //0=no print, 1=above, 2=below, 3=above & below

        //GS f = set barcode characters
        printer.write((byte) 0x1D);
        printer.sendMessage("f");
        printer.write((byte) font);

        //GS h = sets barcode height
        printer.write((byte) 0x1D);
        printer.sendMessage("h");
        printer.write((byte) h);

        //GS w = sets barcode width
        printer.write((byte) 0x1D);
        printer.sendMessage("w");
        printer.write((byte) w);//module = 1-6

        //GS k
        printer.write((byte) 0x1D); //GS
        printer.sendMessage("k"); //k
        printer.write((byte) type);//m = barcode type 0-6
        printer.write((byte) code.length()); //length of encoded string
        printer.sendMessage(code);//d1-dk
        printer.write((byte) 0);//print barcode
    }

    /**
     * Encode and print PDF 417 barcode
     *
     * @param code  String to be encoded in the barcode.
     *              Different barcodes have different requirements on the length
     *              of data that can be encoded.
     * @param type  Specify the type of barcode
     *              0 - Standard PDF417
     *              1 - Standard PDF417
     * @param h     Height of the vertical module in dots 2 <= n <= 8.
     * @param w     Height of the horizontal module in dots 1 <= n <= 4.
     * @param cols  Number of columns 0 <= n <= 30.
     * @param rows  Number of rows 0 (automatic), 3 <= n <= 90.
     * @param error set error correction level 48 <= n <= 56 (0 - 8).
     */
    public void printPSDCode(String code, int type, int h, int w, int cols, int rows, int error) {

        //print function 82
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) code.length()); //pl Code length
        printer.write((byte) 0); //ph
        printer.write((byte) 48); //cn
        printer.write((byte) 80); //fn
        printer.write((byte) 48); //m
        printer.sendMessage(code); //data to be encoded


        //function 65 specifies the number of columns
        printer.write((byte) 0x1D);//init
        printer.sendMessage("(k");//adjust height of barcode
        printer.write((byte) 3); //pl
        printer.write((byte) 0); //pH
        printer.write((byte) 48); //cn
        printer.write((byte) 65); //fn
        printer.write((byte) cols);

        //function 66 number of rows
        printer.write((byte) 0x1D);//init
        printer.sendMessage("(k");//adjust height of barcode
        printer.write((byte) 3); //pl
        printer.write((byte) 0); //pH
        printer.write((byte) 48); //cn
        printer.write((byte) 66); //fn
        printer.write((byte) rows); //num rows

        //module width function 67
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 3);//pL
        printer.write((byte) 0);//pH
        printer.write((byte) 48);//cn
        printer.write((byte) 67);//fn
        printer.write((byte) w);//size of module 1<= n <= 4

        //module height fx 68
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 3);//pL
        printer.write((byte) 0);//pH
        printer.write((byte) 48);//cn
        printer.write((byte) 68);//fn
        printer.write((byte) h);//size of module 2 <= n <= 8

        //error correction function 69
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 4);//pL
        printer.write((byte) 0);//pH
        printer.write((byte) 48);//cn
        printer.write((byte) 69);//fn
        printer.write((byte) 48);//m
        printer.write((byte) error);//error correction

        //choose pdf417 type function 70
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 3);//pL
        printer.write((byte) 0);//pH
        printer.write((byte) 48);//cn
        printer.write((byte) 70);//fn
        printer.write((byte) type);//set mode of pdf 0 or 1

        //print function 81
        printer.write((byte) 0x1D);
        printer.sendMessage("(k");
        printer.write((byte) 3); //pl
        printer.write((byte) 0); //ph
        printer.write((byte) 48); //cn
        printer.write((byte) 81); //fn
        printer.write((byte) 48); //m

    }


    /**
     * Store custom character
     * input array of column bytes
     *
     * @param columnArray Array of bytes (0-255). Ideally not longer than 24 bytes.
     * @param mode        0 - 8-dot single-density.
     *                    1 - 8-dot double-density.
     *                    32 - 24-dot single density.
     *                    33 - 24-dot double density.
     */
    public void storeCustomChar(int[] columnArray, int mode) {

        //function GS*
        printer.write((byte) 0x1B);
        printer.sendMessage("*");
        printer.write((byte) mode);
        printer.write((mode == 0 || mode == 1) ? (byte) columnArray.length : (byte) (columnArray.length / 3));//number of cols
        printer.write((byte) 0);
        for (int i = 0; i < columnArray.length; i++) {
            printer.write((byte) columnArray[i]);
        }

    }

    /**
     * Store custom character
     * input array of column bytes.	NOT WORKING
     *
     * @param spacing Integer representing Vertical motion of unit in inches. 0-255
     */
    public void setLineSpacing(int spacing) {

        //function ESC 3
        printer.write((byte) 0x1B);
        printer.sendMessage("3");
        printer.write((byte) spacing);

    }

    public void cut() {
        printer.write((byte) 0x1D);
        printer.sendMessage("V");
        printer.write((byte) 48);
        printer.write((byte) 0);
    }

    public void feedAndCut(int feed) {

        feed(feed);
        cut();
    }

    public void beep() {
        printer.write((byte) 0x1B);
        printer.sendMessage("(A");
        printer.write((byte) 4);
        printer.write((byte) 0);
        printer.write((byte) 48);
        printer.write((byte) 55);
        printer.write((byte) 3);
        printer.write((byte) 15);
    }


    /**
     * Print a sample sheet
     */
    public void printSampler() {
        //print samples of all functions here
        resetToDefault();
        escInit();
        storeChar(178);
        storeChar(177);
        storeChar(176);

        storeString("Hello World");
        printStorage();


        printString("printString();");
        setBold(true);
        printString("setBold(true)");
        setBold(false);
        setUnderline(1);
        printString("setUnderline(1)");
        setUnderline(2);
        printString("setUnderline(2)");
        setUnderline(0);
        setInverse(true);
        printString("setInverse(true)");
        setInverse(false);
        setJustification(0);
        printString("setJustification(0)\n//left - default");
        setJustification(1);
        printString("setJustification(1)\n//center");
        setJustification(2);
        printString("setJustification(2)\n//right");
        setJustification(1);
        printQR("http://www.josephbergen.com", 51, 8);
        printAndFeed("\n##name## ##version##\nby Joseph Bergen\nwww.josephbergen.com", 4);
        resetToDefault();
    }
}
