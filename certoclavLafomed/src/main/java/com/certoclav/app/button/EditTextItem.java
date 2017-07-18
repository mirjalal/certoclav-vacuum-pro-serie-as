package com.certoclav.app.button;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.certoclav.app.R;

/**
 * A class that creates a layout arranged to fit in a QuickActionMenu
 * 
 * Based on the great work done by Mohd Faruq
 *
 */
public class EditTextItem extends RelativeLayout  {
   
private boolean hasValidString = false;
	

    public boolean hasValidString() {
	return hasValidString;
}



	public void setHasValidString(boolean hasValidString) {
		this.hasValidString = hasValidString;
	}

	public EditText getEditTextView(){
		return (EditText)findViewById(R.id.step_by_step_element_edit);
	}


	private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    /**
     * Creates a new Instance of a QuickActionItem
     * 
     * @param context Context to use, usually your Appication or your Activity
     * @param attrs A collection of attributes, as found associated with a tag in an XML document
     */
    public EditTextItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        

    	
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        return drawableState;
    }




    
    /**
     * Sets the icon for the view
     * 
     * @param drawable The icon for this item 
     */

     
    public void setEditTextInputtype(int type){
    	((EditText)findViewById(R.id.step_by_step_element_edit)).setInputType(type);
    }
    
  public void addTextChangedListner(TextWatcher watcher){
	  ((EditText)findViewById(R.id.step_by_step_element_edit)).addTextChangedListener( watcher);
  }
    
 //   public void setBackgroundRes(int resid) {
  //  	((EditTextItem)findViewById(R.id.quickaction_item_base)).setBackgroundResource(resid);
   // }
    
  
    
    /**
     * Sets a label for the view
     * 
     * @parem text The label for this item
     */
    public void setText(String text) {
    	((EditText)findViewById(R.id.step_by_step_element_edit)).setText(text);
    }
    
    public String getText() {
    	return ((EditText)findViewById(R.id.step_by_step_element_edit)).getText().toString();
    }
    
    public void setHint(String hint) {
    	((EditText)findViewById(R.id.step_by_step_element_edit)).setHint(hint);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ((EditText)findViewById(R.id.step_by_step_element_edit)).setEnabled(enabled);
    }
}