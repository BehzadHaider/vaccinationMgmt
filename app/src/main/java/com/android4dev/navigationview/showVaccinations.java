package com.android4dev.navigationview;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Behzad on 5/21/2016.
 */
public class showVaccinations extends ListActivity {
    private ProgressDialog pDialog;
    //private SharedPreferences sharedPref;
    private String Child_ID;
    private String Child_name;
    private String p_cnic,w_ID;
    GPSTracker gps;
    Double latitude,longitude;
    private static final String TAG_ID = "VaccineId";
    private SharedPreferences sharedPref;
    //private static final String TAG_NAME = "Name";
   // private static final String TAG_PARENTS = "Parents";

    // contacts JSONArray
    JSONArray vaccinations = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> childVaccineList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vaccination_list);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        Child_ID = sharedPref.getString("child_id", "No name defined");
        Child_name=sharedPref.getString("child_name", "No name defined");
        p_cnic=sharedPref.getString("p_cnic","No name defined");
        w_ID=sharedPref.getString("w_id", "No name defined");

        childVaccineList = new ArrayList<HashMap<String, String>>();
        ActionBar ab=getActionBar();
        ab.setTitle("Vaccination History of:"+Child_name);
        ab.setSubtitle("Parent's CNIC:" + p_cnic);
        getActionBar().setIcon(R.drawable.actbar_icon);
        //ListView lv = getListView();
        // Calling async task to get json
        new GetContacts().execute();
    }
     /* Async task class to get json by making HTTP call
     * */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         //getMenuInflater().inflate(R.menu.menu_main, menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_main, menu);
         return true;
     }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.log_out) {
            Intent intent_name = new Intent();
            intent_name.setClass(showVaccinations.this, loginActivity.class);
            startActivity(intent_name);
            finish();
        }
        if(id == R.id.action_settings){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            //Toast.makeText(getApplicationContext(), "Use Mobile Settings", Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.sendText){
            gps = new GPSTracker(showVaccinations.this);
            // check if GPS enabled
            if(gps.canGetLocation()){

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                // \n is for new line
                // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            }else{
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }
            String messageToSend = "My worker id is"+w_ID+"and location is:(lat,long):"+Double.toString(latitude)+","+Double.toString(longitude)+"  I need help";
            String number = "+923455269676";
            try{
                SmsManager.getDefault().sendTextMessage(number, null, messageToSend, null,null);
                Toast.makeText(getApplicationContext(), "Emergency Text sent", Toast.LENGTH_SHORT).show();}
            catch(Exception e){ Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();}
        }

        return super.onOptionsItemSelected(item);
    }
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(showVaccinations.this);
            pDialog.setMessage("Please wait..."+Integer.valueOf(Child_ID));
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandlerNew sh = new ServiceHandlerNew();

            // Making a request to url and getting response
            //String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            String url = "http://iir.azurewebsites.net/api/Children/Vaccinations/"+Child_ID;
            //URL myURL = new URL(url);
            //String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            String jsonStr = sh.makeHttpRequest(url, "GET");
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);
                    //JSONArray contacts=new JSONArray(jsonStr);
                    vaccinations=new JSONArray(jsonStr);
                    Log.d("Response3: ", "> " + vaccinations);

                    // Getting JSON Array node
                    //contacts = jsonObj.getJSONArray(TAG_CONTACTS);
                    //contacts = jsonObj.getJSONArray("");
                    //contacts = new JSONArray(jsonStr);
                    //Log.d("Response2: ", "> " + contacts);
                    // looping through All Contacts
                    if(vaccinations.length()==0){ runOnUiThread(new Runnable() {
                        public void run() {
                            for (int j=0; j < 2; j++){
                                Toast.makeText(showVaccinations.this, "No Previous Vaccinations", Toast.LENGTH_SHORT).show();
                                }
                        }
                    });}
                    for (int i = 0; i < vaccinations.length(); i++) {
                        JSONObject c = vaccinations.getJSONObject(i);
                        String str;
                        String id = c.getString(TAG_ID);
                        String date=c.getString("VaccinationDate");
                        String newDate=date.substring(0,date.indexOf("T"));
                        //String address = c.getString(TAG_ADDRESS);
                        //String gender = c.getString(TAG_GENDER);
                        String e;
                        // Phone node is JSON Object
                            /*JSONObject phone = c.getJSONObject(TAG_PHONE);
                            String mobile = phone.getString(TAG_PHONE_MOBILE);
                            String home = phone.getString(TAG_PHONE_HOME);
                            String office = phone.getString(TAG_PHONE_OFFICE);*/

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();
                        switch(id){
                            case "1": str="BCG  OPV"; break;
                            case "2": str="DTP + HepB + Hib  OPV  PCV"; break;
                            case "3": str="DTP + HepB + Hib  OPV  PCV   ROTAVIRUS-10 weeks"; break;
                            case "4": str="DTP + HepB + Hib  OPV  PCV   ROTAVIRUS-14 weeks"; break;
                            case "5": str="Measles"; break;
                            case "6": str="Hepatits A and Chicken Pox"; break;
                            case "7": str="MMR and PCV"; break;
                            case "8": str="DTaP + Hib OPV Hepatitis A"; break;
                            case "9": str="Typhoid"; break;
                            case "10": str="DTaP Chicken Pox OPV   "; break;
                            case "11": str="MMR"; break;
                            case "12": str="TT MMR"; break;
                            default:
                                str="LOL"; break;
                                //Log.e("", "no case");

                        }
                        contact.put("VaccineId", String.valueOf(i + 1)+"-"+str);
                        contact.put("VaccineDate","Date:"+newDate);
                            /*contact.put(TAG_PHONE_MOBILE, mobile);*/

                        // adding contact to contact list
                        childVaccineList.add(contact);
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
            ListAdapter adapter = new SimpleAdapter(
                    showVaccinations.this, childVaccineList,
                    R.layout.check_list1, new String[] {
                    TAG_ID,"VaccineDate"}, new int[] { R.id.VaccineName,R.id.VaccineDate });

            setListAdapter(adapter);
        }

    }
}
