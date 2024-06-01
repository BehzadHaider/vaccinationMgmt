package com.android4dev.navigationview;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.gsm.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Behzad on 5/10/2016.
 */
public class childrenMenu extends AppCompatActivity {
    Button bt;
    Button bt2;
    private String w_ID;
    GPSTracker gps;
    Double latitude,longitude;
    private SharedPreferences sharedPref;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.children_menu);
        bt=(Button) findViewById(R.id.add_child);
        bt2=(Button) findViewById(R.id.see_children);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        w_ID=sharedPref.getString("w_id", "No name defined");
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), addChild.class);
                startActivity(intent_name);


            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), SeeChildren.class);
                startActivity(intent_name);



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
            intent_name.setClass(childrenMenu.this, loginActivity.class);
            startActivity(intent_name);
            finish();
        }
        if(id == R.id.action_settings){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            //Toast.makeText(getApplicationContext(), "Use Mobile Settings", Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.sendText){
            gps = new GPSTracker(childrenMenu.this);
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
}
