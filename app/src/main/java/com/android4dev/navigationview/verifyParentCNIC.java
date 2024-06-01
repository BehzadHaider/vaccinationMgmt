package com.android4dev.navigationview;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Behzad on 5/10/2016.
 */
public class verifyParentCNIC extends Fragment {
    EditText CNIC;
    private SharedPreferences sharedPref;
    private statusCheck sta= new statusCheck();
    int status=0;
    public int value=0;
    Button bt,bt2;
    ProgressDialog pDialog;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vaccinate_layout,container,false);
        return v;
    }
    public void onViewCreated (View view, Bundle savedInstanceState){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CNIC=(EditText) view.findViewById(R.id.cnic_num);
        bt=(Button) view.findViewById(R.id.cnic_enterbtn);
        bt2=(Button) view.findViewById(R.id.see_nfc);
        //see_nfc
        bt2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent_name = new Intent();
                intent_name.setClass(getActivity(), ParentNIC_NFC_Data.class);
                startActivity(intent_name);
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                if(!isCNICpattern(CNIC)){
                    CNIC.setError("CNIC must be of 13 numeric length");
                    CNIC.requestFocus();
                    return;
                }
                else{
                    new CheckStatus().execute();
                   /* statusCheck st=new statusCheck();
                    Log.d("String","http://iir.azurewebsites.net/api/Parents/Children/"+CNIC.getText().toString() );
                    value = st.MyReadHttpResponse("http://iir.azurewebsites.net/api/Parents/Children/"+CNIC.getText().toString());

                    if(value==404){
                        Toast.makeText(getActivity(),
                                "CNIC number doesnot exist", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getActivity(),



                    }*/
                    }
            }
        });
    }
    private boolean isCNICpattern(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 13;
    }
    private class CheckStatus extends AsyncTask<Void, Void, Void> {
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


            status = sta.MyReadHttpResponse("http://iir.azurewebsites.net/api/Parents/Children/"+CNIC.getText().toString());
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(status==200){
                Toast.makeText(getActivity(),
                        "CNIC exist in DB", Toast.LENGTH_LONG).show();
            Intent intent_name = new Intent();
            //intent_name.setClass(getActivity(), childrenMenu.class);
            intent_name.setClass(getActivity(), verifyFP.class);
            startActivity(intent_name);
            sharedPref.edit().putString("p_cnic", CNIC.getText().toString()).apply();
            sharedPref.edit().putInt("use_nfc",0).apply();
            }



            else{
                Toast.makeText(getActivity(),
                        "CNIC doesnot exist", Toast.LENGTH_LONG).show();
            }
            //afterPostStatus.setText(Integer.toString(status));


        }
    }
}


