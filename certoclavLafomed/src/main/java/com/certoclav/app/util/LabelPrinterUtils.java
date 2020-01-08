package com.certoclav.app.util;

import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LabelPrinterUtils {


    public LabelPrinterUtils() {

    }

    public static void printText(String text, Integer numSets) {
        if (numSets > 5) {
            numSets = 5;
        } else if (numSets < 1) {
            numSets = 1;
        }
        clearCurentBuffer();
        sendInitConfigToBuffer();
        sendTextToBuffer(text, 40, 40);
        printBuffer(numSets);
    }


    public static void printCustomLabel(String line1, String line2, int numsets) {

        clearCurentBuffer();
        sendInitConfigToBuffer();
        sendTextToBuffer(line1, 40, 40);
        sendTextToBuffer(line2, 40, 90);
        sendBarcodeToBuffer(line1, 40, 140);
        printBuffer(1);


    }

    public static void printLabel(Date dateExpiration, String serialAutoclave, int cycleNumber, int numSets, boolean passed) {
        try {
            if (numSets > 5) {
                numSets = 5;
            } else if (numSets < 1) {
                numSets = 1;
            }
            clearCurentBuffer();
            sendInitConfigToBuffer();
            if (passed) {
                sendTextToBuffer(AppController.getContext().getString(R.string.result_passes), 56, 8);
            } else {
                sendTextToBuffer(AppController.getContext().getString(R.string.result_failed), 56, 8);
            }
            sendBarcodeToBuffer(serialAutoclave + "." + cycleNumber, 56, 15 * 8); //text,x,y
            Format formatter = new SimpleDateFormat("dd-MM-yyyy");
            String expDate = formatter.format(dateExpiration);
            sendTextToBuffer("Exp: " + expDate, 40, 9 * 8);
            printBuffer(numSets);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static void clearCurentBuffer() {
        //CLS clears the image Buffer
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("CLS");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");

    }


    public static void sendInitConfigToBuffer() {

        //Labelsize in mm
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("SIZE 56 mm,25 mm");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
        //Vertical distance between two Labels in mm
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("GAP 5 mm,0 mm");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
        //Printing Speed in inch per second
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("SPEED 2");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");

        //Level of Darkness between 0 and 15 (15 specifies the darkest Level)
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("DENSITY 8");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");


        //Printout direction
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("DIRECTION 1, 0");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
    }


    /**
     * @param numSets specifies how many set of labels will be printed
     */
    public static void printBuffer(int numSets) {
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
        //This command prints the label format stored in the image buffer
        // Syntax is Print m
        // m specifies how many set of labels will be printed
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("PRINT " + numSets);
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
    }

    // Coordinates are in dots 8dots = 1mm
    public static void sendCertoclavLogoToBuffer(int xCoordinate, int yCoordinate) {

        String x = Integer.toString(xCoordinate);
        String y = Integer.toString(yCoordinate);
        //This command is used to print BMP format image.
        //Syntax is:
        //PUTBMP X, Y, filename
        //The BMP File has to be stored in the internal storage of the Labelprinter.
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("BITMAP " + x + "," + y + ",27,48,0,");
        byte[] bitmap = hexStringToByteArray("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF45FFFFFFFFFFFFFFFFFFFFFFFFFFFF82FFFFFFFFFFFFFFFFFFFFF8003FFFFFFFFFFFFFFFFFFFFFFFFFFC001FFFFFFFFFFFFFFFFFFFE0000FFFFFFFFFFFFFFFFFFFFFFFFFF00007FFFFFFFFFFFFFFFFFFC00007FFFFFFFFFFFFFFFFFFFFFFFFC00003FFFFFFFFFFFFFFFFFF000003FFFFFFFFFFFFFFFFFFFFFFFF800001FFFFFFFFFFFFFFFFFE000001FFFFFFFFFFFFFFFFFFFFFFFF000000FFFFFFFFFFFFFFFFFE000000FFFFFFFFFFFFFFFFFFFFFFFF0000007FFFFFFFFFFFFFFFFC00FE00FFFFFFFFFFFFFFFFFFFFFFFE007E007FFFFFFFFFFFFFFFF803FF007FFFFFFFFFFFFFFFFFFFFFFC01FF803FFFFFFFFFFFFFFFF807FF807FFFFFFFFFFFFFFFFFFFFFFC03FFC03FFFFFFFFFFFFFFFF807FFE07C0003E007FE0000FF80FFF807FFF07E7FFFFC7F8FFFC7F00FFFFFF80001C0007C00007E001FF807FFFFFC3FFFF83F87FF83F00FFFFFF80001800038000078000FF80FFFFFF83FFFF03F83FF87F01FFFFFF8000380001C0000700C07F00FFFFFF83FFFF01F83FF87E01FFFFFF82007810C1E1820E07F03F00FFFFFF83FFFE01FC3FF07E01FFFFFF83FFF81FE0FF83FE0FFC1F00FFFFFF83FFFE00FC1FF0FE01FFFFFF83FFF81FE0FF83FC1FFC1F00FFFFFF83FFFE10FC1FF0FE01FFFFFF83FFF81FE0FF83FC1FFE0F00FFFFFF83FFFC30FE1FE0FE01FFFFFF83FFF81FE0FF83FC3FFE0F00FFFFFF83FFFC387E0FE1FE01FFFFFF83FFF81FE0FF83F83FFF0F00FFFFFF83FFF8387E0FE1FE01FFFFFF83FFF81FE1FF83F83FFF0700FFFFFF83FFF8787F0FC1FE01FFFFFF8000F81F81FF83F83FFF0700FFFFFF83FFF87C3F0FC3FE01FFFFFF8000780001FF83F83FFF0700FFFFFF83FFF07C3F07C3FE01FFFFFF8000F80003FF83F83FFF0700FFFFFF83FFF0FC1F8783FF01FFFF078201F8000FFF83F83FFF0780FFFF8383FFF0FE1F8787FF00FFFC0383FFF8120FFF83F83FFF07807FFE0183FFE0841F8387FF00FFFC0383FFF81F07FF83F83FFF0F807FFE0383FFE0000FC387FF807FF80783FFF81F03FF83F83FFE0FC03FFC0383FFC0000FC30FFF803FF80783FFF81F83FF83FC1FFE0FC01FFC0383FFC0000FC10FFFC01FF00783FFF81FC1FF83FC1FFC1FE00FF80783FFC3FF07E00FFFC007800F83FFF81FC0FF83FE0FFC1FE003C00783FF83FF87E01FFFE000001F8200081FE0FF83FE07F83FF000000F830187FF83E01FFFF000003F8000001FE07F83FF01407FF800001F800007FF83F01FFFF800007F8000001FF07F83FF8000FFFC00003F800007FFC3F03FFFFC0000FF8000003FF83F83FFC001FFFE00007FC0000FFFC1F03FFFFF0001FFC000007FFC7FC7FFF807FFFF8000FFE0001FFFE3FC7FFFFFE01FFFFFFFFFFFFFFFFFFFFFFFFFFFF007FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendBytes(bitmap);


    }


    // Coordinates are in dots 8dots = 1mm
    public static void sendBarcodeToBuffer(String text, int xCoordinate, int yCoordinate) {

        String x = Integer.toString(xCoordinate);
        String y = Integer.toString(yCoordinate);


        //size of barcode. can be between 48 and 110
        String s = "70";


        //Barcode should also be printed as plaintext, otherwise readable = '0'
        String readable = "0";


        // The Syntax to Store a Barcode in the image Buffer is:
        // BARCODE X, Y, code type, height, human readable, rotation, narrow, wide,code
        //  X and Y specify the x-coordinate,y-coordinate coordinate of the Text on the label
        // Coordinates are in dots 8dots = 1mm
        // code type = 128   for Code128 Barcodes
        // human readable=1 plaintext will be printed below the Barcode
        // human readable=0 no plaintext will be printed
        // rotation clockwise in 90 degree steps
        // narrow = width of narrow element
        // wide = width of wide element
        // code = Text that will be printed as a Barcode
        // BARCODE 10,10,128M,48,1,0,2,2,!104!096ABCD!101EFGH


        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("BARCODE ");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(new String(x));
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(new String(y));

        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",\"128\",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(new String(s));
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(readable); //isReadable "0" or "1"
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",0,2,2,\"");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(new String(text));
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\"");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");

    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    // Coordinates are in dots 8dots = 1mm
    protected static void sendTextToBuffer(String text, int xCoordinate, int yCoordinate) {
        //Labelsize in mm
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("SIZE 56 mm,25 mm");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
        //Vertical distance between two Labels in mm
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("GAP 5 mm,0 mm");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
        // Printing Speed in inch per second
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("SPEED 2");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");
        //Level of Darkness between 0 and 15 (15 specifies the darkest Level)
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("DENSITY 15");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");


        String x = Integer.toString(xCoordinate);
        String y = Integer.toString(yCoordinate);


        //textsize s= '1' => small, s=='2' => middle, s=='3' => big

        String s = "1";

        // Syntax for Text is:
        // TEXT X, Y, font, rotation, x-multiplication, y-multiplication, content
        //  X and Y specify the x-coordinate,y-coordinate coordinate of the Text on the label
        // Coordinates are in dots 8dots = 1mm
        //x-multiplication = Horizontal multiplication with factors between 1 and 10
        //y-multiplication = Vertical multiplication with factors between 1 and 10
        //content is the Text that will be printed on the label
        //TEXT 20,40,"4",0,1,1\
        //TEXT x,y,"fontName",rotation,x-multiplication,y-multiplication,text

        //If font �0� is used, the font width and font height is
        //stretchable by x-multiplication and y-multiplication
        //parameter. It is expressed by pt (point). 1 point=1/72inch.

        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("TEXT ");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(x); //x-coordinate 1 point = 1/72inch
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(y); //y-coordinate 1 point = 1/72inch
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\"4\""); //font name
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("0"); //rotation in degree
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(s); //scale: x-multiplication of true-type font
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(s); //scale: y-multiplication of true-type font
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(",");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\"");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage(new String(text));
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\"");
        Autoclave.getInstance().getSerialsServiceLabelPrinter().sendMessage("\r\n");


    }


}

			
			
		
		
