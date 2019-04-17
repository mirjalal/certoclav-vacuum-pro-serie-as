package com.certoclav.app.button;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.certoclav.app.R;

/**
 * A class that creates a layout arranged to fit in a QuickActionMenu
 * <p>
 * Based on the great work done by Mohd Faruq
 */
public class CheckboxItem extends RelativeLayout {

    /**
     * Creates a new Instance of a QuickActionItem
     *
     * @param context Context to use, usually your Appication or your Activity
     * @param attrs   A collection of attributes, as found associated with a tag in an XML document
     */
    public CheckboxItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {


        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        return drawableState;
    }


    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        ((Switch) findViewById(R.id.switchIsAdmin)).setOnCheckedChangeListener(listener);
    }

    /**
     * Sets a label for the view
     *
     * @parem text The label for this item
     */
    public void setText(String text) {
        ((TextView) findViewById(R.id.checkBoxName)).setText(text);
    }

    public String getText() {
        return ((TextView) findViewById(R.id.checkBoxName)).getText().toString();
    }


    public void setChecked(boolean checked){
        ((Switch)findViewById(R.id.switchIsAdmin)).setChecked(checked);
    }
    public boolean isChecked(){
        return ((Switch)findViewById(R.id.switchIsAdmin)).isChecked();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        findViewById(R.id.checkBoxName).setEnabled(enabled);
        findViewById(R.id.switchIsAdmin).setEnabled(enabled);
    }
}