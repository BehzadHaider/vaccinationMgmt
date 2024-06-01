package com.android4dev.navigationview;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.gsm.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    GPSTracker gps;
    public Double latitude;
    public Double longitude;
    public String w_ID;
    private SharedPreferences sharedPref;
    private PostService jparser = new PostService();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vaccination System");
        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        // Toast.makeText(getApplicationContext(),"Please Enter Parent Detail",Toast.LENGTH_SHORT).show();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        w_ID = sharedPref.getString("w_id", "No name defined");
        firstPage fragment3 = new firstPage();
        android.support.v4.app.FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.frame, fragment3).addToBackStack(null);
        fragmentTransaction1.commit();

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {


                    //Replacing the main content with addParentActivity Which is our Inbox View;
                    case R.id.add_parent:
                        Toast.makeText(getApplicationContext(), "Please Enter Parent Detail", Toast.LENGTH_SHORT).show();
                        addParentActivity fragment = new addParentActivity();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fragment).addToBackStack(null);
                        fragmentTransaction.commit();
                        return true;

                    // For rest of the options we just show a toast on click

                    case R.id.vacc_schedule:
                        Toast.makeText(getApplicationContext(), "Vaccinations Information", Toast.LENGTH_SHORT).show();
                        ShowSchedule fragment2 = new ShowSchedule();
                        android.support.v4.app.FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction2.replace(R.id.frame, fragment2).addToBackStack(null);
                        fragmentTransaction2.commit();
                        return true;
                    case R.id.vaccinate:
                        Toast.makeText(getApplicationContext(), "Please Enter parent's CNIC", Toast.LENGTH_SHORT).show();
                        verifyParentCNIC fragment3 = new verifyParentCNIC();
                        android.support.v4.app.FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction1.replace(R.id.frame, fragment3).addToBackStack(null);
                        fragmentTransaction1.commit();
                        return true;
                    case R.id.showCalender:
                        Toast.makeText(getApplicationContext(), "Opening Calender", Toast.LENGTH_SHORT).show();
                        showCalender fragment4 = new showCalender();
                        android.support.v4.app.FragmentTransaction fragmentTransaction4 = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction4.replace(R.id.frame, fragment4).addToBackStack(null);
                        fragmentTransaction4.commit();
                        return true;
                    case R.id.ChildNFC:
                        Intent intent_name = new Intent();
                        intent_name.setClass(MainActivity.this, ViewWritten.class);
                        startActivity(intent_name);
                        Toast.makeText(getApplicationContext(), "All Mail Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.trash:
                        Toast.makeText(getApplicationContext(), "Trash Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.spam:

                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Spam Selected", Toast.LENGTH_SHORT).show();
                        firstPage fragment_1 = new firstPage();
                        android.support.v4.app.FragmentTransaction fragmentTransaction7 = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction7.replace(R.id.frame, fragment_1).addToBackStack(null);
                        fragmentTransaction7.commit();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
        }
        if (id == R.id.log_out) {
            Intent intent_name = new Intent();
            intent_name.setClass(MainActivity.this, loginActivity.class);
            startActivity(intent_name);
            finish();
        }
        if (id == R.id.sendText) {
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
    void Repeat(){
        Date cDate = new Date();
        String fDate;
        final int status;
        fDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(cDate);
        gps = new GPSTracker(MainActivity.this);
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Sent to Web:"+status, Toast.LENGTH_SHORT).show();
                    }
                });
                //Toast.makeText(getApplicationContext(), "Emergency Text sent", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "This is the error while sending on web:"+status, Toast.LENGTH_SHORT).show();
                }
            });
        }
        try{
            SmsManager.getDefault().sendTextMessage(number, null, messageToSend, null,null);
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Emergency Text Sent", Toast.LENGTH_SHORT).show();
                }
            });
            //Toast.makeText(getApplicationContext(), "Emergency Text sent", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){ MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, "Emergency Text Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
        }
    }
    /*private class SendMessage extends AsyncTask<Void, Void, Void> {
        /* Context context;
          private PostData(Context context) {
              this.context = context.getApplicationContext();
          }


        @Override
        protected Void doInBackground(Void... arg0) {
            gps = new GPSTracker(MainActivity.this);
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Emergency Text Sent", Toast.LENGTH_SHORT).show();
                    }
                });
                //Toast.makeText(getApplicationContext(), "Emergency Text sent", Toast.LENGTH_SHORT).show();
            }
            catch(Exception e){ MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Emergency Text Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            });
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


            //afterPostStatus.setText(Integer.toString(status));


        }
    }*/
    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            //super.onBackPressed();
            //additional code
            Intent intent_name = new Intent();
            intent_name.setClass(MainActivity.this, MainActivity.class);
            //intent_name.setClass(MainActivity.this, loginActivity.class);
            startActivity(intent_name);
        } else {
            getFragmentManager().popBackStack();
        }

    }

    void RepeatTask() { /*
        gps = new GPSTracker(MainActivity.this);
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
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Emergency Text Sent", Toast.LENGTH_SHORT).show();
                }
            });
            //Toast.makeText(getApplicationContext(), "Emergency Text sent", Toast.LENGTH_SHORT).show();
            }
        catch(Exception e){ MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, "Emergency Text Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
         }*/


    }
}

