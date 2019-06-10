package com.certoclav.app.listener;

import com.certoclav.app.model.Error;

import java.util.ArrayList;



public interface AlertListener {
 void onWarnListChange(ArrayList<Error> errorList, ArrayList<Error> warningList);
}
