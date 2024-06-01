package com.android4dev.navigationview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Behzad on 5/11/2016.
 */
public class addChild extends Activity {
    private SharedPreferences sharedPref;
    String cnic;
    Button bt;
    EditText childName,childDOB,childFormB;
    ProgressDialog pDialog;
    int status=0;
    private PostService jparser = new PostService();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        setContentView(R.layout.add_child);
        cnic = sharedPref.getString("p_cnic", "No name defined");//"No name defined" is the default value
        bt=(Button) findViewById(R.id.register_child_button);
        childName=(EditText) findViewById(R.id.child_name);
        childDOB=(EditText) findViewById(R.id.DOB);
        childFormB=(EditText) findViewById(R.id.FormB);
        //childFormB.setText(cnic);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(isEmpty(childName)){
                    childName.setError("Child's Name required");
                    childName.requestFocus();
                    return;
                }
                if(isEmpty(childDOB)){
                    childDOB.setError("Child's DOB required");
                    childDOB.requestFocus();
                    return;
                }
                if(!isCNICpattern(childFormB)){
                    childFormB.setError("CNIC must be of 13 numeric length");
                    childFormB.requestFocus();
                    return;
                }

                else
                {
                    try {
                        new addTheChild().execute();
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
    private boolean isCNICpattern(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 13;
    }
    private class addTheChild extends AsyncTask<Void, Void, Void> {
        /* Context context;
          private PostData(Context context) {
              this.context = context.getApplicationContext();
          }*/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(addChild.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            List<NameValuePair> child = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            child.add(new BasicNameValuePair("Name", childName.getText().toString()));
            child.add(new BasicNameValuePair("DOB", childDOB.getText().toString()));
            child.add(new BasicNameValuePair("FormB", childFormB.getText().toString()));
            //MyAdditions: because of deprecation
            //ContentValues values=new ContentValues();
            //values.put("username",name);
            //values.put("password",password);
            String url = "http://iir.azurewebsites.net/api/Parents/Children/"+cnic;
            status = jparser.makeHttpRequest(url, "POST", child);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(status==200)
            {Toast.makeText(addChild.this,
                        "Status:"+status+"Child Added Successfully", Toast.LENGTH_LONG).show();
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), childrenMenu.class);
                startActivity(intent_name);
                finish();}


            else{
                Toast.makeText(addChild.this,
                        "Status:"+status+", Child NOT added:Redundant Name:try again", Toast.LENGTH_LONG).show();
            }
            //afterPostStatus.setText(Integer.toString(status));


        }
    }
}