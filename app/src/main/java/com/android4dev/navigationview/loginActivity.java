package com.android4dev.navigationview;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Behzad on 4/15/2016.
 */
public class loginActivity extends Activity {
    Button bt;
    String url = "http://iir.azurewebsites.net/api/login/";
    final String TAG_ID="WorkerId";
    final String TAG_NAME="Name";
    EditText login_username;
    EditText login_password;
    private ProgressDialog pDialog;
    private UserLoginTask mAuthTask = null;
    Activity par = this;
    public SharedPreferences sharedPref;
    int u=0;
    JSONArray contacts = null;
    String id="",name="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        login_username=(EditText) findViewById(R.id.login_username_input);
        login_password=(EditText) findViewById(R.id.login_password_input);
        bt = (Button) findViewById(R.id.parse_login_button) ;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        sharedPref.edit().putString("p_cnic","0" ).apply();


        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(isEmpty(login_username)){
                    login_username.setError("Username Required");
                    login_username.requestFocus();
                    return;
                }
                if(isEmpty(login_password)){
                    login_password.setError("Enter Password");
                    login_password.requestFocus();
                    return;
                }
                if(!isPasswordValid(gS(login_password))){
                    login_password.setError("Minimum Length Should be 5");
                    login_password.requestFocus();
                    return;
                }
                else
                {
                String url = "http://iir.azurewebsites.net/api/login/"+login_username.getText().toString()+"/"+login_password.getText().toString();
                    try {
                        attemptLogin(url);
                        new GetWorker().execute();

                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(), "Please Retry!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private boolean isEmpty(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 0;
    }
    private boolean isPasswordValid(String password) {
        // TODO: Replace this with your own logic
        return password.length() > 4;
    }
    private String gS(EditText et){
        return et.getText().toString();
    }
    public void attemptLogin(String address) {
        if (mAuthTask != null) {
            return;
        }

        // Store values at the time of the login attempt.


        // Check for a valid password, if the user entered one.
       try{
                mAuthTask = new UserLoginTask(address);
                mAuthTask.execute((Void) null);
            }catch(Exception e){
                Toast.makeText(loginActivity.this, "Check Network Connection", Toast.LENGTH_SHORT).show();}
        }



    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String url;
        private boolean RETURN_VALUE = false;

        UserLoginTask(String url) {
            this.url=url;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(loginActivity.this);
            pDialog.setMessage("Logging In ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
           // pDialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... args) {

            try{
            LoginAuthentication st=new LoginAuthentication();
                Log.d("tag",url);
            u = st.MyReadHttpResponse(url);

            } catch (Exception e) {

                e.printStackTrace();
            }
            return RETURN_VALUE;
        }
        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            pDialog.dismiss();

            Log.d("tag-u val", Integer.toString(u));
            if(isOnline()) {
                if (u == 200) {

                    Toast.makeText(loginActivity.this,
                            "Login Successful user:" + login_username.getText().toString(), Toast.LENGTH_LONG).show();


                } else if (u == 404) {
                    Toast.makeText(loginActivity.this,
                            "Username not found", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(loginActivity.this,
                            "Incorrect password entered", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(loginActivity.this,
                        "Please connect to Internet", Toast.LENGTH_LONG).show();
            }

        }

        }
    private class GetWorker extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(loginActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandlerNew sh = new ServiceHandlerNew();
            //ServiceHandler sh=new ServiceHandler();
            //URL myURL = new URL(url);
            // Making a request to url and getting response
            //String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            String url = "http://iir.azurewebsites.net/api/login/"+login_username.getText().toString()+"/"+login_password.getText().toString();
            Log.d("Before Fetch",url);
            String jsonStr = sh.makeHttpRequest(url, "GET");
            Log.d("JSON", jsonStr);
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    contacts=new JSONArray(jsonStr);
                    JSONObject c = contacts.getJSONObject(0);
                    if(contacts.length()!=0){
                    Log.d("Response3: ", "> " + contacts);
                        id=c.getString(TAG_ID);
                        //String id = c.getString(TAG_ID);
                        name = c.getString(TAG_NAME);
                    sharedPref.edit().putString("w_id",id ).apply();
                    sharedPref.edit().putString("w_name",name ).apply();
                        if(contacts.length()==0){ runOnUiThread(new Runnable() {
                            public void run() {
                                for (int j=0; j < 2; j++){
                                    Toast.makeText(loginActivity.this,
                                            "id:"+id+"name:"+name, Toast.LENGTH_SHORT).show();}
                            }
                        });}
                   }
                    else{
                        Toast.makeText(loginActivity.this,
                                "Worker with this ID and Pass doesnot exist", Toast.LENGTH_LONG).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            if(u==200){
                Intent intent_name = new Intent();
                //intent_name.setClass(getApplicationContext(), checkActivity.class);
                intent_name.setClass(getApplicationContext(), MainActivity.class);
                startActivity(intent_name);
                finish();
            }

        }

    }
}
