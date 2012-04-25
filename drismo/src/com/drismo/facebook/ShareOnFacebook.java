package com.drismo.facebook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.model.Config;

import java.io.*;
import java.net.MalformedURLException;

// Tutorial and code from: http://integratingstuff.com/2010/10/14/integrating-facebook-into-an-android-application/
// Note: Some minor changes has been done, due to outdated code and customizing.

/**
 * This activity uses the Android Facebook SDK to share a link to Facebook via a Facebook application.<br />
 * To specify string to share, use <code>putExtra("facebookMessage", "this is the message");</code>
 */
public class ShareOnFacebook extends Activity {

	private static final String APP_ID = "324306136397";     // DriSMo Facebook Application ID
	private static final String[] PERMISSIONS = new String[] {"publish_stream"};

	private static final String TOKEN = "access_token";
    private static final String EXPIRES = "expires_in";
    private static final String KEY = "facebook-credentials";

	private Facebook facebook;
	private String messageToPost;
    private boolean shareImage = false;

    /**
     * Storing Facebook login data to our (hidden) application preferences.
     * @param facebook A Facebook object, from the facebook-android-sdk.
     * @return True if it was stored successfully. False otherwise.
     */
	public boolean saveCredentials(Facebook facebook) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
        editor.putString(TOKEN, facebook.getAccessToken());
        editor.putLong(EXPIRES, facebook.getAccessExpires());
        return editor.commit();
    }

    /**
     * Get the user's current Facebook login information, from our application preferences.
     * @param facebook A Facebook object, from the facebook-android-sdk.
     * @return True if the user don't have to log in again. False if no valid session exists.
     */
    public boolean restoreCredentials(Facebook facebook) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(KEY, Context.MODE_PRIVATE);
        facebook.setAccessToken(sharedPreferences.getString(TOKEN, null));
        facebook.setAccessExpires(sharedPreferences.getLong(EXPIRES, 0));
        return facebook.isSessionValid();
    }

    /**
     * Lock the screen orientation, manage received trip data and prompt the user to share.
     * @param savedInstanceState Not beeing used, as <code>onSaveInstanceState(Bundle)</code> is not implemented.
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}

        switch (this.getResources().getConfiguration().orientation)
        {
        case Configuration.ORIENTATION_PORTRAIT:
          this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
          break;
        case Configuration.ORIENTATION_LANDSCAPE:
          this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
          break;
        default:
          this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
          break;
        }

		facebook = new Facebook(APP_ID);
		restoreCredentials(facebook);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.facebook_dialog);

        shareImage = getIntent().getBooleanExtra("shareTripImage", false);

		String facebookMessage = getIntent().getStringExtra("facebookMessage");
		if (facebookMessage == null){
			facebookMessage = getString(R.string.defaultFbShare);
		}
		messageToPost = facebookMessage;
	}

    /**
     * Nothing to be done. Closing the Facebook sharing dialog.
     * @param button button
     */
	public void doNotShare(View button){
		finish();
	}

    /**
     * Share the trip. Log in first if necessary.
     * @param button button
     */
	public void share(View button){
		if (!facebook.isSessionValid()) {
			loginAndPostToWall();
		}
		else {
			postToWall(messageToPost);
		}
	}

    /**
     * Log on to Facebook.
     */
	public void loginAndPostToWall(){
		 facebook.authorize(this, PERMISSIONS, Facebook.FORCE_DIALOG_AUTH, new LoginDialogListener());
	}

    /**
     * Post the trip summary to the Facebook wall.
     * Attach a picture or link based on where in the app the sharing is done from.
     * @param message The text summary of the trip.
     */
	public void postToWall(String message){
		Bundle parameters = new Bundle();

        parameters.putString("message", message);

        if(shareImage) {
            parameters.putByteArray("picture", getIntent().getByteArrayExtra("tripImage"));
            new UploadToFacebook().execute(parameters);
        }
        else {
            //facebook.dialog(this, "stream.publish", parameters, new WallPostDialogListener());
            parameters.putString("link", "https://market.android.com/details?id=com.drismo");
            new UploadToFacebook().execute(parameters);
        }
	}

    /**
     * Listen to events from the <code>Facebook.DialogListener</code>, and give the user relevant feedback.
     */
	class LoginDialogListener implements Facebook.DialogListener {
	    public void onComplete(Bundle values) {
	    	saveCredentials(facebook);
	    	if (messageToPost != null) postToWall(messageToPost);
	    }
	    public void onFacebookError(FacebookError error) {
	    	showToast(getString(R.string.fbAuthFailed));
	        finish();
	    }
	    public void onError(DialogError error) {
	    	showToast(getString(R.string.fbAuthFailed));
	        finish();
	    }
	    public void onCancel() {
	    	showToast(getString(R.string.fbAuthCancelled));
	        finish();
	    }
	}

    /**
     * Listen to events from the <code>Facebook.DialogListener</code>, and give the user relevant feedback.
     */
	class WallPostDialogListener implements Facebook.DialogListener {
		public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                showToast(getString(R.string.fbMessagePosted));
            } else {
                showToast(getString(R.string.wallPostCancelled));
            }
            finish();
        }
		public void onFacebookError(FacebookError e) {
			showToast(getString(R.string.wallPostFailed));
			e.printStackTrace();
			finish();
		}
		public void onError(DialogError e) {
			showToast(getString(R.string.wallPostFailed));
			e.printStackTrace();
			finish();
		}
		public void onCancel() {
			showToast(getString(R.string.wallPostCancelled));
			finish();
		}
    }

    /**
     * Post a short <code>Toast</code>.
     * @param message The message to show.
     */
	private void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

    /**
     * Upload the content to Facebook, and finish all Facebook dialogs when finished.
     */
    private class UploadToFacebook extends AsyncTask<Bundle, Void, Boolean>{
        ProgressDialog uploadProgressDialog;

        /**
         * Create the <code>ProgressDialog</code>.
         */
        protected void onPreExecute() {
            uploadProgressDialog = new ProgressDialog(ShareOnFacebook.this);
            uploadProgressDialog.setCancelable(false);
            uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            uploadProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.loading));
            uploadProgressDialog.setMessage(getString(R.string.uploading));
            uploadProgressDialog.show();
        }

        /**
         * Send a request to Facebook, to append the trip summary to either a picture or a link.
         * @param params A <code>Bundle</code> with the summary, and either the link or the picture to share with it.
         * @return The response from the Facebook server. <code>null</code> if no response.
         */
        @Override
        protected Boolean doInBackground(Bundle... params) {
            String response = null;

            try {
                if(shareImage) response = facebook.request("me/photos", params[0], "POST");
                else           response = facebook.request("me/links",  params[0], "POST");

            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
//                showToast(fileNotFoundException.getMessage());
            } catch (MalformedURLException malformedURLException) {
                malformedURLException.printStackTrace();
//                showToast(malformedURLException.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
//                showToast(ioException.getMessage());
            }

            return (response != null);
        }

        /**
         * Dismiss<code>ProgressDialog</code>, and give the user feedback about the upload.
         */
        @Override
        protected void onPostExecute(Boolean uploadSuccess) {
            uploadProgressDialog.dismiss();
            finish();

            if(uploadSuccess)
                 Toast.makeText(ShareOnFacebook.this, getString(R.string.hasBeenShared),
                 Toast.LENGTH_SHORT).show();
            else Toast.makeText(ShareOnFacebook.this, getString(R.string.problemUpload),
                 Toast.LENGTH_SHORT).show();
        }
    }
}