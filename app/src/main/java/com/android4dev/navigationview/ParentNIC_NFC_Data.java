package com.android4dev.navigationview;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ParentNIC_NFC_Data extends AppCompatActivity {
    private SharedPreferences sharedPref;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private statusCheck sta= new statusCheck();
    int status=0;
     private TextView mTextView = null;
     private NfcAdapter mNfcAdapter = null;
    TextView parent_cnic;
    TextView child_id;
    TextView child_name;
    TextView Exp_vac;
    Button btn;
    ProgressDialog pDialog;
    private String w_ID;
    GPSTracker gps;
    Double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_nfc_layout);
        parent_cnic=(TextView) findViewById(R.id.parents_cnic);
        child_id=(TextView) findViewById(R.id.child_id_tv);
        child_name=(TextView) findViewById(R.id.child_name_tv);
        Exp_vac=(TextView)findViewById(R.id.next_vaccination);
        btn=(Button) findViewById(R.id.vaacinate_using_NFC);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ParentNIC_NFC_Data.this);
        w_ID=sharedPref.getString("w_id", "No name defined");

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            parent_cnic.setText("NFC is disabled.");
        } else {
            parent_cnic.setText("NFC is enabled");
        }

        handleIntent(getIntent());


        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                sharedPref.edit().putString("p_cnic", parent_cnic.getText().toString()).apply();
                sharedPref.edit().putString("child_id", child_id.getText().toString()).apply();
                sharedPref.edit().putString("child_name",child_name.getText().toString()).apply();
                sharedPref.edit().putString("Expected_Vac",Exp_vac.getText().toString()).apply();
                sharedPref.edit().putInt("use_nfc", 1).apply();
                new CheckStatus().execute();

            }
        });
    }




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
            intent_name.setClass(ParentNIC_NFC_Data.this, loginActivity.class);
            startActivity(intent_name);
            finish();
        }
        if(id == R.id.action_settings){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            //Toast.makeText(getApplicationContext(), "Use Mobile Settings", Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.sendText){
            gps = new GPSTracker(ParentNIC_NFC_Data.this);
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
    @Override
    protected void onResume() {
        super.onResume();

        /*
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /*
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }
    /*@Override
    public void onBackPressed() {

    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        /*
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }


    /*
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    /*
     * @param activity The corresponding {@linkBaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }


        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //mTextView.setText("Read content: "+ result);
                child_id.setText(result.substring(result.indexOf('>')+1,result.indexOf(':')));
                child_name.setText(result.substring(result.indexOf('<')+1,result.indexOf('>')));
                parent_cnic.setText(result.substring(0,result.indexOf('<')));
                Exp_vac.setText(result.substring(result.indexOf(':')+1,result.length()));

            }
        }


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
            pDialog = new ProgressDialog(ParentNIC_NFC_Data.this);
            pDialog.setMessage("Fetching data from API to verify CNIC");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance


            status = sta.MyReadHttpResponse("http://iir.azurewebsites.net/api/Parents/Children/"+parent_cnic.getText().toString());
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(status==200){
                Toast.makeText(ParentNIC_NFC_Data.this,
                        "CNIC exist in DB", Toast.LENGTH_LONG).show();
                Intent intent_name = new Intent();
                intent_name.setClass(ParentNIC_NFC_Data.this, verifyFP.class);
                startActivity(intent_name);
                finish();
            }



            else{
                Toast.makeText(ParentNIC_NFC_Data.this,
                        "CNIC doesnot exist, This RFID is forged", Toast.LENGTH_LONG).show();
            }
            //afterPostStatus.setText(Integer.toString(status));


        }
    }
}

