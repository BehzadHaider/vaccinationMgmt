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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 04-06-2015.
 */
public class addParentActivity extends Fragment {
    int status=0;
    private PostService jparser = new PostService();
    private static String login_url = "http://iir.azurewebsites.net/api/Parents/";
    EditText name;
    String Gender;
    EditText CNIC;
    EditText phoneNum;
    Button bt;
    TextView afterPostStatus;
    ProgressDialog pDialog;
    private SharedPreferences sharedPref;
    CheckBox checMale,checFemale;
    //FragmentManager fragManager = myContext.getFragmentManager();
    //public FragmentManager getSupportFragmentManager (get)
    private FragmentActivity myContext;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_parent,container,false);
        v.isFocusable();
        return v;
    }
    public void onViewCreated (View view, Bundle savedInstanceState){
        name=(EditText)view.findViewById(R.id.parent_name);
        //Gender=(EditText)view.findViewById(R.id.parent_gender);
        CNIC=(EditText)view.findViewById(R.id.parent_CNIC);
        phoneNum=(EditText)view.findViewById(R.id.parent_phoneNum);
        bt=(Button)view.findViewById(R.id.register_Parent_button);
        checMale=(CheckBox) view.findViewById(R.id.checkbox1);
        checFemale=(CheckBox) view.findViewById(R.id.checkbox2);
        //((MainActivity) getActivity()).setOnBackPressedListener(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        checMale.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(checMale.isChecked()){
                    checMale.setChecked(true);
                    checFemale.setEnabled(false);
                    Gender = "M";
                }else{
                    checMale.setChecked(false);
                    checFemale.setEnabled(true);
                }
            }
        });
        checFemale.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(checFemale.isChecked()){
                    checFemale.setChecked(true);
                    checMale.setEnabled(false);
                    Gender= "F";
                }else{

                    checFemale.setChecked(false);
                    checMale.setEnabled(true);
                }
            }
        });

    bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(isEmpty(name)){
                    name.setError("Parent name Required");
                    name.requestFocus();
                    return;
                }
               /* if(genderCheck(Gender)|| isEmpty(Gender)){
                    Gender.setError("Enter F or M");
                    Gender.requestFocus();
                    return;
                }*/

                if(!isCNICpattern(CNIC)){
                    CNIC.setError("CNIC must be of 13 numeric length");
                    CNIC.requestFocus();
                    return;
                }
                if(isEmpty(phoneNum)){
                    phoneNum.setError("Phone Number Required");
                    phoneNum.requestFocus();
                    return;
                }
                if(!checFemale.isChecked() && !checMale.isChecked())
                {
                    Toast.makeText(getActivity(),
                            "Select Gender to proceed ", Toast.LENGTH_LONG).show();
                    return;
                }
                else{

                if(isOnline()){
                new PostData().execute();}
                else{
                    Toast.makeText(getActivity(),
                            "Internet Connection not available", Toast.LENGTH_LONG).show();
                }
                }
            }
        });

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //String cameback="CameBack";
                firstPage fragment3 = new firstPage();
                android.support.v4.app.FragmentTransaction fragmentTransaction1 = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.frame, fragment3);
                fragmentTransaction1.commit();}
                return true;
        }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /*@Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }*/
    private boolean isEmpty(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 0;
    }

    private boolean isCNICpattern(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 13;
    }
    private boolean genderCheck(EditText myeditText) {
        if(myeditText.getText().toString().equals("M") || myeditText.getText().toString().equals("F"))
        { return false;}
        else {return true;}
    }

    private class PostData extends AsyncTask<Void, Void, Void> {
        /* Context context;
          private PostData(Context context) {
              this.context = context.getApplicationContext();
          }*/

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            List<NameValuePair> parent = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            parent.add(new BasicNameValuePair("Name", name.getText().toString()));
            parent.add(new BasicNameValuePair("Gender", Gender));
            parent.add(new BasicNameValuePair("NIC", CNIC.getText().toString()));
            parent.add(new BasicNameValuePair("PhoneNo", phoneNum.getText().toString()));
            //MyAdditions: because of deprecation
            //ContentValues values=new ContentValues();
            //values.put("username",name);
            //values.put("password",password);

            status = jparser.makeHttpRequest(login_url, "POST", parent);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(status==201){
            Toast.makeText(getActivity(),
                    "Status:"+status+"Parent Added Successfully", Toast.LENGTH_LONG).show();
            sharedPref.edit().putString("p_cnic", CNIC.getText().toString()).apply();
            sharedPref.edit().putInt("use_nfc", 0).apply();
            Intent intent_name = new Intent();
            intent_name.setClass(getActivity(), registerFP.class);
            startActivity(intent_name);}

            else{
                Toast.makeText(getActivity(),
                        "Status:"+status+", Parent NOT added", Toast.LENGTH_LONG).show();
            }
            //afterPostStatus.setText(Integer.toString(status));


        }
    }
   /* @Override
    public void onBackPressed() {
        FragmentManager fragManager = myContext.getSupportFragmentManager();
        if(fragManager.getBackStackEntryCount() != 0) {
            fragManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }*/

}
