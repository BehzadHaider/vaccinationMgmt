package com.android4dev.navigationview;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

/**
 * Created by Behzad on 5/15/2016.
 */
public class checkActivity extends Activity {
    TextView name, id;
    private SharedPreferences sharedPref;
    String name_pref,id_pref;

    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.to_check);
        name=(TextView)findViewById(R.id.textView1);
        id=(TextView) findViewById(R.id.textView2);
        name_pref = sharedPref.getString("w_name", "No name defined");
        id_pref = sharedPref.getString("w_id", "No name defined");
        name.setText(name_pref);
        id.setText(id_pref);

    }
}
