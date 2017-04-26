package com.certoclav.app.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Video;
import com.certoclav.app.menu.VideoFullscreenActivity;




/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 * 
*/
public class VideoAdapter extends ArrayAdapter<Video> {
	private final Context mContext;


	/**
	 * Constructor
	 * 
	 * @param context
	 *            context of calling activity
	 * @param values
	 * {@link List}<{@link Profile}> containing the data to populate the list
	 */
	public VideoAdapter(Context context, List<Video> values) {
 
		super(context, R.layout.menu_fragment_information_video_element, values);
		this.mContext = context;

	}

	/**
	 * Gets a View that displays the data at the specified position in the data
	 * set.The View is inflated it from profile_list_row XML layout file
	 * 
	 * @see Adapter#getView(int, View, ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.menu_fragment_information_video_element, parent, false);
		}


		//set preview image
		GenerateImageTask generateImageTask = new GenerateImageTask();
		generateImageTask.setImageViewAndPath((ImageView)convertView.findViewById(R.id.information_video_element_image), getItem(position).getPath());
		//generateImageTask.execute();
        
        //set description text
        TextView textDescription = (TextView) convertView.findViewById(R.id.information_video_element_text);
        textDescription.setText(getItem(position).getDescription());


        //on click: play video
        convertView.findViewById(R.id.card_view).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, VideoFullscreenActivity.class);
				intent.putExtra(AppConstants.INTENT_EXTRA_VIDEOFULLSCREENACTIVITY_VIDEO_PATH, getItem(position).getPath());
				mContext.startActivity(intent);
				
			}
		});


	
				
				
	


		return convertView;
	}
	
	
	  private class GenerateImageTask extends AsyncTask<View, Void, Bitmap> {
		  
		  private ImageView mImagePrieview = null;
		  private String mPath = null;
		  public void setImageViewAndPath(ImageView imagepreview, String path){
			  mImagePrieview = imagepreview;
			  mPath = path;
			  
		  }
	        @Override
	        protected Bitmap doInBackground(View... params) {

	            
	            return ThumbnailUtils.createVideoThumbnail(mPath,
	            	    MediaStore.Images.Thumbnails.MINI_KIND);
	            
	            
	            
	        }

	        @Override
	        protected void onPostExecute(Bitmap result) {
	        	if(result != null){
	        		mImagePrieview.setImageBitmap(result);
	        	}
	        }

	        @Override
	        protected void onPreExecute() {
	        }

	        @Override
	        protected void onProgressUpdate(Void... values) {
	        }
	    }


}