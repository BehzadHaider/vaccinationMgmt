package com.android4dev.navigationview;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Behzad on 3/1/2016.
 */
public class VaccinateChildActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private SharedPreferences sharedPref;
    Double latitude,longitude;
    GPSTracker gps;
    int pos;
    int status,nfc;
    String item;
    String str="False";
    CheckBox cb;
    TextView name, parents_txt, id_txt,date,spin_check,worker_id,nextVaccinetxt;
    EditText vaccination_id;
    Button bt;
    Button bt2;
    private static final String TAG_ID = "ChildId";
    private static final String TAG_NAME = "Name";
    private static final String TAG_PARENTS = "Parents";
    String cnic,fDate,w_ID;
    String url="http://iir.azurewebsites.net/api/Vaccinations";
    ProgressDialog pDialog;
    String currentDateTimeString, content;
    private View v;
    private PostService jparser = new PostService();
    private ArrayAdapter<String> dataAdapter;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yy/MM/dd HH:mm");
    private NFCManager nfcMger;
    private NdefMessage message = null;
    Tag currentTag;
    private ProgressDialog dialog;

//email=id   //phone=parents
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcMger = new NFCManager(this);
        //id/contact_name_liearlayout
        v = findViewById(R.id.contact_name_liearlayout);
        setContentView(R.layout.show_info);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        name = (TextView) findViewById(R.id.t_name);
        parents_txt = (TextView) findViewById(R.id.parents_cnic);
        spin_check=(TextView) findViewById(R.id.contact_email);
        id_txt = (TextView) findViewById(R.id.t_phone);
        cb=(CheckBox) findViewById(R.id.checkbox);
        nextVaccinetxt=(TextView) findViewById(R.id.nextVaccine);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        cnic = sharedPref.getString("p_cnic", "No name defined");
        w_ID=sharedPref.getString("w_id", "No name defined");//"w_id" //
        nfc=sharedPref.getInt("use_nfc", 0);
        worker_id=(TextView) findViewById(R.id.workerID_et);
        //vaccination_id=(EditText) findViewById(R.id.vaccine_id_et);
        // on_time=(EditText)findViewById(R.id.onTime_et);
        date=(TextView)findViewById(R.id.contact_number0);
        bt=(Button) findViewById(R.id.Vaccine_update);
        bt2=(Button) findViewById(R.id.NFC_update);
        Bundle extras=getIntent().getExtras();
        //final String tagName = (String) extras.get("Name");
        final String Child_Name = sharedPref.getString("child_name","No name defined");
        String child_id=sharedPref.getString("child_id", "No name defined");
        nextVaccinetxt.setText("");
        if(nfc==1)
        {
            String FutureVaccination=sharedPref.getString("Expected_Vac", "No name defined");
            nextVaccinetxt.setText("Vaccination Due::"+FutureVaccination);
        }
        //String id = (String) extras.get(TAG_ID);
        //String Parents = (String) extras.get(TAG_PARENTS);
         getSupportActionBar().setTitle("Options to vaccinate");
        //ab.setTitle("My Title");
        //ab.setSubtitle("sub-title");
        //currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        //currentDateTimeString = DateFormat.getDateTimeInstance().format();
       // printStandardDate
        //VaccinateChildActivity.this.getActionBar().setTitle("new title");
        Date cDate = new Date();
        //fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(cDate);
        fDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(cDate);
        //date.setText(fDate);
        /////////**********************GPS ***********************************************///////
        gps = new GPSTracker(VaccinateChildActivity.this);
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

        //////////////*****************Spinner********************////////////////////////////
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("BCG OPV");
        categories.add("DTP+HepB+Hib OPV PCV");
        categories.add("DTP+HepB+Hib OPV PCV ROTVIRUS-10weeks");
        categories.add("DTP+HepB+Hib OPV PCV ROTVIRUS-14weeks");
        categories.add("Measles");
        categories.add("Hepatits A and Chicken Pox");
        categories.add("MMR and PCV");
        categories.add("DTaP+Hib OPV Hepatitis A");
        categories.add("Typhoid");
        categories.add("DTaP Chicken Pox OPV");
        categories.add("MMR");
        categories.add("TT MMR");

        // Creating adapter for spinner
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);
        dataAdapter.notifyDataSetChanged();
        ///////////////////////*************Spinner Ends********************/////////////////////////

        name.setText(Child_Name);
        parents_txt.setText(cnic);
        id_txt.setText(child_id);
        worker_id.setText(w_ID);

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    str = "True";
                    // perform logic
                }
                if (!isChecked) {
                    str = "False";
                }

            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                NfcManager manager = (NfcManager) VaccinateChildActivity.this.getSystemService(Context.NFC_SERVICE);
                NfcAdapter adapter = manager.getDefaultAdapter();
                //parent.getItemAtPosition(position).toString()
                content = cnic+"<"+Child_Name+">"+id_txt.getText().toString()+":"+item;
                message =  nfcMger.createTextMessage(content);
                if (adapter != null && adapter.isEnabled()) {
                    if (message != null) {
                        dialog = new ProgressDialog(VaccinateChildActivity.this);
                        dialog.setMessage("Tag NFC Tag please" + content);
                        dialog.show();

                    }


                }
                if (adapter !=null && !adapter.isEnabled()){
                    Toast.makeText(VaccinateChildActivity.this,
                            "Enable NFC from Settings", Toast.LENGTH_LONG).show();
                }



            }
        });
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                    if (isOnline()) {
                        new PostData().execute();
                    } else {
                        Toast.makeText(VaccinateChildActivity.this,
                                "Internet Connection not available", Toast.LENGTH_LONG).show();
                    }





            }
        });


    }
    @Override
    protected void onResume() {
        super.onResume();

        try {
            nfcMger.verifyNFC();
            //nfcMger.enableDispatch();

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[] {};
            String[][] techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        }
        catch(NFCManager.NFCNotSupported nfcnsup) {
            Snackbar.make(v, "NFC not supported", Snackbar.LENGTH_LONG).show();
        }
        catch(NFCManager.NFCNotEnabled nfcnEn) {
            Snackbar.make(v, "NFC Not enabled", Snackbar.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        nfcMger.disableDispatch();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        // It is the time to write the tag
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (message != null) {
            nfcMger.writeTag(currentTag, message);
            dialog.dismiss();
          //  Snackbar.make(v, "Tag written", Snackbar.LENGTH_LONG).show();
            Toast.makeText(VaccinateChildActivity.this,
                    "Tag Written Successfully, Now please click Vaccinate button", Toast.LENGTH_LONG).show();
        }
        else {
            // Handle intent

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vaccinate_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
            intent_name.setClass(VaccinateChildActivity.this, loginActivity.class);
            startActivity(intent_name);
            finish();
        }
        if(id == R.id.action_settings){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            //Toast.makeText(getApplicationContext(), "Use Mobile Settings", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.action_vaccinationHistory){
            Intent intent_name = new Intent();
            intent_name.setClass(VaccinateChildActivity.this, showVaccinations.class);
            startActivity(intent_name);
        }
        if(id==R.id.sendText){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyWakelockTag");
            wakeLock.acquire();
            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    //Called each time when 1000 milliseconds (1 second) (the period parameter)
                    //new SendMessage().execute();
                    Repeat();
                    /*PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
                    wl.acquire();*/
                    //Looper.loop();
                } },0,120000);
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        if(position<11){
        item = parent.getItemAtPosition(position+1).toString();}
        else{
            item="All Vaccinations Completed";
        };
        //tv.setText(String.valueOf(position));
        pos=position+1;
       // date.setText(position);
        //spin_check.setText(Integer.toString(position));
        //Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private boolean timeCheck(EditText myeditText) {
        if(myeditText.getText().toString().equals("True") || myeditText.getText().toString().equals("False"))
        { return false;}
        else {return true;}
    }
    void Repeat(){
        Date cDate = new Date();
        String fDate;
        final int status;
        fDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(cDate);
        gps = new GPSTracker(VaccinateChildActivity.this);
        // check if GPS enabled
        if(gps.canGetLocation()){

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }else{
            gps.showSettingsAlert();
        }
        String messageToSend = "My worker id is"+w_ID+"and location is:(lat,long):"+Double.toString(latitude)+","+Double.toString(longitude)+"  I need help,time is"+fDate;
        String number = "+923455269676";
        List<NameValuePair> EmText = new ArrayList<NameValuePair>();
        String url="http://iir.azurewebsites.net/api/Workers/"+w_ID+"/AddLocation";
        EmText.add(new BasicNameValuePair("Coordinates"," Latitude: "+Double.toString(latitude)+" Longitude: "+Double.toString(longitude)));
        EmText.add(new BasicNameValuePair("DateAndTime", fDate));
        status = jparser.makeHttpRequest(url, "POST", EmText);
        if(status==200)
        {
            {
                VaccinateChildActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(VaccinateChildActivity.this, "Sent to Web:"+status, Toast.LENGTH_SHORT).show();
                    }
                });
                //Toast.makeText(getApplicationContext(), "Emergency Text sent", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            VaccinateChildActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(VaccinateChildActivity.this, "This is the error while sending on web:"+status, Toast.LENGTH_SHORT).show();
                }
            });
        }
        try{
            SmsManager.getDefault().sendTextMessage(number, null, messageToSend, null,null);
            VaccinateChildActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(VaccinateChildActivity.this, "Emergency Text Sent", Toast.LENGTH_SHORT).show();
                }
            });
            //Toast.makeText(getApplicationContext(), "Emergency Text sent", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){ VaccinateChildActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(VaccinateChildActivity.this, "Emergency Text Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) VaccinateChildActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    private class PostData extends AsyncTask<Void, Void, Void> {
        /* Context context;
          private PostData(Context context) {
              this.context = context.getApplicationContext();
          }*/

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(VaccinateChildActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            List<NameValuePair> parent = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            parent.add(new BasicNameValuePair("VaccinationDate", fDate));
            parent.add(new BasicNameValuePair("WorkerId",w_ID ));
            parent.add(new BasicNameValuePair("ChildId", id_txt.getText().toString()));
            //parent.add(new BasicNameValuePair("VaccineId", vaccination_id.getText().toString()));
            parent.add(new BasicNameValuePair("VaccineId", String.valueOf(pos)));//String.valueOf(position)
            //parent.add(new BasicNameValuePair("OnTime", on_time.getText().toString()));
            parent.add(new BasicNameValuePair("OnTime", str));
            parent.add(new BasicNameValuePair("Location"," Latitude: "+Double.toString(latitude)+" Longitude: "+Double.toString(longitude)));

            //MyAdditions: because of deprecation
            //ContentValues values=new ContentValues();
            //values.put("username",name);
            //values.put("password",password);

            status = jparser.makeHttpRequest(url, "POST", parent);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(status==201){
                Toast.makeText(VaccinateChildActivity.this,
                        "Status:" + status + "Vaccination added Successfully", Toast.LENGTH_LONG).show();
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), MainActivity.class);
                startActivity(intent_name);
                finish();
                }

            else{
                Toast.makeText(VaccinateChildActivity.this,
                        "Status:"+status+", Vaccination not added,try again", Toast.LENGTH_LONG).show();
            }
            //afterPostStatus.setText(Integer.toString(status));


        }
    }

}