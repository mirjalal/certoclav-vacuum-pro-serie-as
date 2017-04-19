package com.certoclav.library.certocloud;

import android.os.AsyncTask;

public class DeleteTask extends AsyncTask<String, Boolean, Boolean> {

	@Override
	protected Boolean doInBackground(String... params) {
		DeleteUtil deleteUtil = new DeleteUtil();
		int returnval = deleteUtil.deleteToCertocloud(params[0], true);
		
		if(returnval == DeleteUtil.RETURN_OK){
			return true;
		}
		return false;
	}

}
