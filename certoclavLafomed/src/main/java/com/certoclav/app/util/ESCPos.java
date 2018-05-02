package com.certoclav.app.util;


import java.text.Format;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import android_serialport_api.SerialService;

import com.certoclav.app.AppConstants;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.library.application.ApplicationController;

import static io.fabric.sdk.android.services.network.HttpRequest.append;


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

        String version = "";
        try {
            PackageInfo pInfo;
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (NameNotFoundException e) {
            version = "";
        }

        //start time
        String startTimeString = "";
        try {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startTimeString = formatter.format(protocol.getStartTime());
        } catch (Exception e) {
            startTimeString = "";
        }

        //end time
        String endTimeString = "";
        try {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            endTimeString = formatter.format(protocol.getEndTime());
        } catch (Exception e) {
            endTimeString = "";
        }


        resetToDefault();
        String SPACING = "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n";
        StringBuilder sb = new StringBuilder();
        sb.append("10 ").append("CertoClav Vacuum Pro").append("\n");
        sb.append(SPACING);
        sb.append("15 ").append("Program").append(": ").append(protocol.getProfileName().replace("\u00B0", "")).append("\n");
        sb.append("20 ").append("Program load/details").append(":").append("\n");
        sb.append("   ").append(protocol.getProfileDescription().replace("\u00B0", "")).append("\n");
        sb.append("25 ").append("Date").append(": ").append(startTimeString).append("\n");
        sb.append("30 ").append("Cycle").append(": ").append(protocol.getZyklusNumber()).append("\n");
        sb.append(SPACING);

        if (protocol.getErrorCode() == 0) {
            sb.append("40 ").append("Result").append(": ").append("passed".toUpperCase()).append("\n");
        } else {
            sb.append("40 ").append("Result").append(": ").append("failed".toUpperCase()).append("\n");
            sb.append("41 ").append(AutoclaveMonitor.getInstance().getErrorString(protocol.getErrorCode())).append("\n");
        }
        sb.append(SPACING);

        sb.append("45 ").append("Temperature").append(": ").append(protocol.getSterilisationTemperature()).append("\n");
        sb.append("50 ").append("Pressure").append(": ").append(protocol.getSterilisationPressure()).append("\n");
        sb.append("55 ").append("Time").append(": ").append(protocol.getSterilisationTime()).append("\n");
        sb.append("65 ").append("Start").append(": ").append(startTimeString).append("\n");
        sb.append("70 ").append("End").append(": ").append(endTimeString).append("\n");

        sb.append(SPACING);
        sb.append("80 ").append("SN:").append(Autoclave.getInstance().getController().getSerialnumber()).append(version).append("\n");
        sb.append("85 ").append("User").append(": ").append(protocol.getUserEmail()).append("\n");
        sb.append(SPACING);
        if (prefs.getBoolean(AppConstants.PREFERENCE_KEY_PRINT_ENTITES, true)) {

            sb.append(String.format("%-8s", "t[h:m]"))
                    .append(String.format("%-8s", "P[bar]"))
                    .append(String.format("%-8s", "S[C]"))
                    .append(String.format("%-8s", "M[C]"))
                    .append("\n");
            Long startTime = protocol.getStartTime().getTime();
            int minutesOnLastPrint = -1;
            for (ProtocolEntry entry : protocol.getProtocolEntry()) {
                Long timeDiff = entry.getTimestamp().getTime() - startTime;
                int hours = (int) (timeDiff / (1000 * 60 * 60));
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                if (minutesOnLastPrint != minutes) {
                    minutesOnLastPrint = minutes;
                    String timeStamp = String.format("%02d:%02d", hours, minutes);
                    String pressure = String.format("%03d", Math.round(entry.getPressure()));
                    String temperature = String.format("%05.1f", entry.getTemperature());
                    String mediaTemp = String.format("%05.1f", entry.getMediaTemperature());

                    sb.append(String.format("%-8s", timeStamp))
                            .append(String.format("%-8s", pressure))
                            .append(String.format("%-8s", temperature))
                            .append(String.format("%-8s", mediaTemp))
                            .append("\n");
                }
            }
            sb.append(SPACING);
        }
        sb.append("End");

        printString(sb.toString());

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
     * @param str           String to be encoded in QR.
     * @param  The degree of error correction. (48 <= n <= 51)
     *                      48 = level L / 7% recovery capacity.
     *                      49 = level M / 15% recovery capacity.
     *                      50 = level Q / 25% recovery capacity.
     *                      51 = level H / 30% recovery capacity.
     * @param moduleSize    The size of the QR module (pixel) in dots.
     *                      The QR code will not print if it is too big.
     *                      Try setting this low and experiment in making it larger.
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
