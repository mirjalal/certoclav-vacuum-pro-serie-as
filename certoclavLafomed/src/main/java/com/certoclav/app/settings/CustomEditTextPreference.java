package com.certoclav.app.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.util.AutoclaveModelManager;

import es.dmoral.toasty.Toasty;

public class CustomEditTextPreference extends EditTextPreference {
    public CustomEditTextPreference(Context context) {
        super(context);
    }


    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super((Context) null);
        throw new RuntimeException("Stub!");
    }


    @Override
    protected void onAddEditTextToDialogView(View dialogView, final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editText == null) return;
                String inputString = editable.toString();
                if (!inputString.contains("."))
                    editText.setText(inputString + ".0");
            }
        });
        super.onAddEditTextToDialogView(dialogView, editText);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        int id = Integer.valueOf(getKey().replace("preferences_autoclave_parameter_", ""));
        Pair<Float, Float> range = AutoclaveModelManager.getInstance().getParamterRange(id);
        if (range != null) {
            boolean isInRange = true;
            try {
                if (getEditText().getText().toString().endsWith(".") || getEditText().getText().toString().startsWith(y"."))
                    throw new Exception();
                float value = Float.valueOf(getEditText().getText().toString());
                isInRange = value >= range.first && value <= range.second;
            } catch (Exception e) {
                isInRange = false;
            }
            if (!isInRange) {
                Toasty.error(getContext(), getContext().getString(R.string.parameter_range, range.first, range.second),
                        Toast.LENGTH_SHORT, true).show();
            }
            super.onDialogClosed(isInRange);
        }
    }
}
