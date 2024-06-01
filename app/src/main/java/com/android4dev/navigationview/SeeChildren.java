package com.android4dev.navigationview;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Behzad on 5/11/2016.
 */
public class SeeChildren extends ListActivity {
    private ProgressDialog pDialog;
    private SharedPreferences sharedPref;
    private String cnic;
    String id;
    // URL to get contacts JSON


    // JSON Node names
    private static final String TAG_ID = "ChildId";
    private static final String TAG_NAME = "Name";
    private static final String TAG_DOB="DOB";
    //private static final String TAG_PARENTS = "Parents";

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> childList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.children_list);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        cnic = sharedPref.getString("p_cnic", "No name defined");

        childList = new ArrayList<HashMap<String, String>>();
        ListView lv = getListView();
        ActionBar ab=getActionBar();
        ab.setTitle("Registered Children");
        ab.setSubtitle("CNIC:" + cnic);
        getActionBar().setIcon(R.drawable.actbar_icon);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        // Listview on item click listener
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String name = ((TextView) view.findViewById(R.id.ChildName))
                        .getText().toString();
                String iden = ((TextView) view.findViewById(R.id.ID))
                        .getText().toString();
                String DOB = ((TextView) view.findViewById(R.id.DOB))
                        .getText().toString();
                //parent contain id
                // Starting single contact activity
                /*Character DOBorg_0= DOB.charAt(0);
                Character DBorg_1=DOB.charAt(1);
                Character DOBorg_2= DOB.charAt(2);
                Character DBorg_3=DOB.charAt(3);
                Character DOBorg_4= DOB.charAt(4);
                Character DBorg_5=DOB.charAt(5);
                Character DOBorg_6= DOB.charAt(6);
                Character DBorg_7=DOB.charAt(7);
                Character DOBorg_8= DOB.charAt(8);
                Character DBorg_9=DOB.charAt(9);*/
                //String d=DOB.substring(0,DOB.indexOf("T")-1);

                Intent in = new Intent(getApplicationContext(),
                        VaccinateChildActivity.class);
                in.putExtra(TAG_NAME, name);
                //in.putExtra(TAG_PARENTS, parents);
                in.putExtra(TAG_ID, iden);
                startActivity(in);
                sharedPref.edit().putString("child_id",iden).apply();
                sharedPref.edit().putString("child_name",name).apply();
                sharedPref.edit().putString("child_dob",DOB).apply();

            }
        });

        // Calling async task to get json
        new GetContacts().execute();
        /*if(contacts.length()==0)
        {
            for (int i=0; i < 2; i++)
            {
                Toast.makeText(this, "No children registered", Toast.LENGTH_LONG).show();
            }
        }*/
    }
    /*public void myMethod() throws IOException {
        URL url = new URL("https://wikipedia.org/");
    }*/
    /**
     * Async task class to get json by making HTTP call
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.see_children_menu, menu);
        return true;
    }
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(SeeChildren.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            //ServiceHandlerNew sh = new ServiceHandlerNew();
            ServiceHandler sh=new ServiceHandler();

            // Making a request to url and getting response
            //String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            String url = "http://iir.azurewebsites.net/api/Parents/Children/"+cnic;
            //URL myURL = new URL(url);
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            //String jsonStr = sh.makeHttpRequest(url, "GET");
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);
                    //JSONArray contacts=new JSONArray(jsonStr);
                    contacts=new JSONArray(jsonStr);
                    Log.d("Response3: ", "> " + contacts);

                    // Getting JSON Array node
                    //contacts = jsonObj.getJSONArray(TAG_CONTACTS);
                    //contacts = jsonObj.getJSONArray("");
                    //contacts = new JSONArray(jsonStr);
                    //Log.d("Response2: ", "> " + contacts);
                    // looping through All Contacts
                    if(contacts.length()==0){ runOnUiThread(new Runnable() {
                        public void run() {
                            for (int j=0; j < 2; j++){
                            Toast.makeText(SeeChildren.this, "No children registered", Toast.LENGTH_SHORT).show();}
                        }
                    });}
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        String DOB=c.getString(TAG_DOB);
                        //String parents = c.getString(TAG_PARENTS);
                        //String address = c.getString(TAG_ADDRESS);
                        //String gender = c.getString(TAG_GENDER);

                        // Phone node is JSON Object
                            /*JSONObject phone = c.getJSONObject(TAG_PHONE);
                            String mobile = phone.getString(TAG_PHONE_MOBILE);
                            String home = phone.getString(TAG_PHONE_HOME);
                            String office = phone.getString(TAG_PHONE_OFFICE);*/

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_NAME, name);
                        String newDOB=DOB.substring(0,DOB.indexOf("T"));
                        //String newNewDOB=new StringBuilder(newDOB).reverse().toString();
                        contact.put(TAG_DOB,"DOB:"+newDOB);
                       // contact.put(TAG_PARENTS, parents);
                            /*contact.put(TAG_PHONE_MOBILE, mobile);*/

                        // adding contact to contact list
                        childList.add(contact);
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
            for(int j=0;j<3;j++)
            {Toast.makeText(SeeChildren.this, "Child id here"+id, Toast.LENGTH_SHORT).show();}

            /**
             * Updating parsed JSON data into ListView
             * */
           /* ListAdapter adapter = new SimpleAdapter(
                    SeeChildren.this, childList,
                    R.layout.list_item, new String[] { TAG_NAME, TAG_PARENTS,
                    TAG_ID }, new int[] { R.id.name,
                    R.id.ID, R.id.parents });*/
            ListAdapter adapter = new SimpleAdapter(
                    SeeChildren.this, childList,
                    R.layout.check_list, new String[] { TAG_NAME,
                    TAG_ID ,TAG_DOB}, new int[] { R.id.ChildName, R.id.ID ,R.id.DOB});
            setListAdapter(adapter);
        }

    }

}
