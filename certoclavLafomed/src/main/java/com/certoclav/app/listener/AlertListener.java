package com.certoclav.app.listener;

import java.util.ArrayList;

import com.certoclav.app.model.Error;



public interface AlertListener {
 void onWarnListChange(ArrayList<Error> errorList);
}
