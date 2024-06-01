package com.android4dev.navigationview;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by Behzad on 4/17/2016.
 */
public class statusCheck {
    public int MyReadHttpResponse(String url){
        HttpClient client= new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        int sc=0;
        try {
            HttpResponse response = client.execute(httpget);
            StatusLine sl = response.getStatusLine();
            sc = sl.getStatusCode();
            if (sc==200 || sc==404 || sc==400)
            {
                return sc;
            }
            else
            {
                Log.e("log_tag", "I didn't  get the response!");
                return 0;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sc;
    }
}
