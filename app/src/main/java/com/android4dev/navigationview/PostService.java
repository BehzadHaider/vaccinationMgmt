package com.android4dev.navigationview;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class PostService {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    int sc;

    // constructor
    public PostService() {

    }

    // function get json from url
    // by making HTTP POST or GET mehtod
    public int makeHttpRequest(String url, String method,
                                      List<NameValuePair> params) {

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 6000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 6000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        // Making HTTP request
        try {

            // check for request method
            if(method == "POST"){
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

                HttpPost httpPost = new HttpPost(url);

                httpPost.setEntity(new UrlEncodedFormEntity(params));//set parameters for query
                Log.d("Vicky", "Data, Behzad Check FYP = " + params);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                StatusLine sl = httpResponse.getStatusLine();
                sc = sl.getStatusCode();
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();		// get response from php


            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }




        // return JSON String
        return sc;

    }
//    public boolean isConnected(){
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//            if (networkInfo != null && networkInfo.isConnected())
//                return true;
//            else
//                return false;
//    }

}