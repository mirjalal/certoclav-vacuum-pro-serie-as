package com.certoclav.app.menu;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.util.LabelPrinterUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by Michael on 12/6/2016.
 */

public class MenuLabelPrinterActivity extends CertoclavSuperActivity {

    private CertoclavNavigationbarClean navigationbar = null;


    private Button buttonPrint = null;
    private ImageView Barcode = null;
    private EditText editText1 = null;
    private EditText editText2 = null;


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_main_label_editor);


        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.showButtonBack();
        navigationbar.setHeadText(getString(R.string.label_designer));

        Barcode = (ImageView) findViewById(R.id.imageViewBarcode);
        editText1 = (EditText) findViewById(R.id.dialog_label_text_edittext_line1);
        editText2 = (EditText) findViewById(R.id.dialog_label_text_edittext_line2);

        //Create an initial Barcode
        com.google.zxing.Writer writer = new QRCodeWriter();
        String finaldata = Uri.encode("test", "utf-8");
        try {
            //BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE, 150, 150);
            BitMatrix bm = new Code128Writer().encode("123456789", BarcodeFormat.CODE_128, 350, 120, null);


            Bitmap barcodeBitmap = Bitmap.createBitmap(350, 120, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < 350; i++) {//width
                for (int j = 0; j < 120; j++) {//height
                    barcodeBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }
            Barcode.setImageBitmap(barcodeBitmap);
        } catch (Exception e) {

        }


        buttonPrint = (Button) findViewById(R.id.menu_main_label_button_print);
        buttonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    LabelPrinterUtils.printCustomLabel(editText1.getText().toString(), editText2.getText().toString(), 1);
                    Toast.makeText(MenuLabelPrinterActivity.this, R.string.label_printed_com2,Toast.LENGTH_LONG);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        editText1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                com.google.zxing.Writer writer = new QRCodeWriter();

                String finaldata = Uri.encode(String.valueOf(editText1.getText()), "utf-8");


                try {
                    //BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE, 150, 150);
                    BitMatrix bm = new Code128Writer().encode(finaldata, BarcodeFormat.CODE_128, 350, 120, null);

                    Bitmap barcodeBitmap = Bitmap.createBitmap(350, 120, Bitmap.Config.ARGB_8888);

                    for (int i = 0; i < 350; i++) {//width
                        for (int j = 0; j < 120; j++) {//height
                            barcodeBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    //Bitmap barcodeBitmap = encodeAsBitmap("test", BarcodeFormat.QR_CODE, 150, 150);

                    Barcode.setImageBitmap(barcodeBitmap);


                } catch (Exception e) {

                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }

        });


    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();

    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();


    }


}
