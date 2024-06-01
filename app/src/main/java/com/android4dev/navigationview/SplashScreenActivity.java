package com.android4dev.navigationview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class SplashScreenActivity extends Activity {

	
	SharedPreferences sharedPref ;
	private static final String TAG_SUCCESS = "success";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		if(savedInstanceState==null){
			Log.d("start", "new instance");
			sharedPref.edit().clear().commit();
		}
		Thread logoTimer = new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {

					sleep(2500);
					Intent i= new Intent(getApplicationContext(),loginActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					startActivity(i);
					finish(); // Call once you redirect to another activity

				} catch (InterruptedException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				finally{
					finish();
				}
			}
		};
		logoTimer.start();
		checkcon();
		//newactivity();

	}

	void checkcon(){
		if(isOnline()){
			Toast.makeText(getApplicationContext(), "Internet Connection Successful", Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getApplicationContext(), "Error No Internet Connection", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	public boolean isOnline() {
		ConnectivityManager cm =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}



}






