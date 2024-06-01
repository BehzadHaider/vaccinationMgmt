package com.android4dev.navigationview;

/*
 * Copyright (C) 2013 SecuGen Corporation
 *
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;


public class verifyFP extends Activity implements View.OnClickListener, Runnable, SGFingerPresentEvent {

    private static final String TAG = "SecuGen USB";
    private Button mButtonRegister;
    private Button mButtonMatch;
    private TextView mTextViewResult;
    private PendingIntent mPermissionIntent;
    private ImageView mImageViewFingerprint;
    private ImageView mImageViewRegister;
    private ImageView mImageViewVerify;
    private byte[] mRegisterImage;
    private byte[] mVerifyImage;
    private byte[] mRegisterTemplate;
    private byte[] mVerifyTemplate;
    private int[] mMaxTemplateSize;
    private int mImageWidth;
    private int mImageHeight;
    private int[] grayBuffer;
    private Bitmap grayBitmap;
    private IntentFilter filter; //2014-04-11
    private SGAutoOnEventNotifier autoOn;
    private boolean mLed;
    String cnic;
    ProgressDialog pDialog;
    private SharedPreferences sharedPref;
    int nfc;
    int u=0;
    String SendString;
    private Button Fetch;
    String FetchedString;
    JSONArray contacts = null;


    private JSGFPLib sgfplib;


    //RILEY
    //This broadcast receiver is necessary to get user permissions to access the attached USB device
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //DEBUG Log.d(TAG,"Enter mUsbReceiver.onReceive()");
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //DEBUG Log.d(TAG, "Vendor ID : " + device.getVendorId() + "\n");
                            //DEBUG Log.d(TAG, "Product ID: " + device.getProductId() + "\n");
                            //debugMessage("Vendor ID : " + device.getVendorId() + "\n");
                            //debugMessage("Product ID: " + device.getProductId() + "\n");
                        }
                        else
                            Log.e(TAG, "mUsbReceiver.onReceive() Device is null");
                    }
                    else
                        Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device " + device);
                }
            }
        }
    };

    //RILEY
    //This message handler is used to access local resources not
    //accessible by SGFingerPresentCallback() because it is called by
    //a separate thread.
    public Handler fingerDetectedHandler = new Handler(){
        // @Override
        public void handleMessage(Message msg) {
            //Handle the message
            CaptureFingerPrint();

        }
    };

    public void EnableControls(){
        this.mButtonRegister.setClickable(true);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.white));
        this.mButtonMatch.setClickable(true);
        this.mButtonMatch.setTextColor(getResources().getColor(android.R.color.white));
    }

    public void DisableControls(){
        this.mButtonRegister.setClickable(false);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.black));
        this.mButtonMatch.setClickable(false);
        this.mButtonMatch.setTextColor(getResources().getColor(android.R.color.black));
    }


    //RILEY
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //service.setDevicePackage( usbDevice, YOUR_APP_PACKAGE_NAMESPACE, ai.uid );
        setContentView(R.layout.verify_fp_layout_new);
        Fetch= (Button) findViewById(R.id.fetch_rec) ;
        mButtonRegister = (Button)findViewById(R.id.buttonRegister);
        mButtonRegister.setOnClickListener(this);
        mButtonMatch = (Button)findViewById(R.id.buttonMatch);
        mButtonMatch.setOnClickListener(this);
        mImageViewFingerprint = (ImageView)findViewById(R.id.imageViewFingerprint);
        mImageViewRegister = (ImageView)findViewById(R.id.imageViewRegister);
        mImageViewVerify = (ImageView)findViewById(R.id.imageViewVerify);
        mTextViewResult=(TextView)findViewById(R.id.textViewResult);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        cnic = sharedPref.getString("p_cnic", "No name defined");
        nfc=sharedPref.getInt("use_nfc", 0);
        grayBuffer = new int[JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES* JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES];
        for (int i=0; i<grayBuffer.length; ++i)
            grayBuffer[i] = Color.GRAY;
        grayBitmap = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES, Bitmap.Config.ARGB_8888);
        grayBitmap.setPixels(grayBuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES);
        mImageViewFingerprint.setImageBitmap(grayBitmap);

        int[] sintbuffer = new int[(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2)*(JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2)];
        for (int i=0; i<sintbuffer.length; ++i)
            sintbuffer[i] = Color.GRAY;
        Bitmap sb = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2, Bitmap.Config.ARGB_8888);
        sb.setPixels(sintbuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES / 2);
        mImageViewRegister.setImageBitmap(grayBitmap);
        mImageViewVerify.setImageBitmap(grayBitmap);

        mMaxTemplateSize = new int[1];

        //USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        sgfplib = new JSGFPLib((UsbManager)getSystemService(Context.USB_SERVICE));


        //debugMessage("jnisgfplib version: " + sgfplib.Version() + "\n");
        mLed = false;
        autoOn = new SGAutoOnEventNotifier(sgfplib, this);
        Fetch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {
                    //new addFingerPrint().execute();
                    // new UploadImages().execute();
                    if(isOnline()){new GetFP().execute();}
                    else{Toast.makeText(getApplicationContext(), "Please connect to INTERNET and retry", Toast.LENGTH_SHORT).show(); }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please Retry!", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        autoOn.stop();
        EnableControls();
        sgfplib.CloseDevice();
        unregisterReceiver(mUsbReceiver);
        mRegisterImage = null;
        mVerifyImage = null;
        mRegisterTemplate = null;
        mVerifyTemplate = null;
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        mImageViewRegister.setImageBitmap(grayBitmap);
        mImageViewVerify.setImageBitmap(grayBitmap);
        super.onPause();
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume()");
        super.onResume();
        registerReceiver(mUsbReceiver, filter);
        long error = sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
                dlgAlert.setMessage("The attached fingerprint device is not supported on Android");
            else
                dlgAlert.setMessage("Fingerprint device initialization failed!");
            dlgAlert.setTitle("SecuGen Fingerprint SDK");
            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int whichButton){
                            finish();
                            return;
                        }
                    }
            );
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        }
        else {
            UsbDevice usbDevice = sgfplib.GetUsbDevice();
            if (usbDevice == null){
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage("SDU04P or SDU03P fingerprint sensor not found!");
                dlgAlert.setTitle("SecuGen Fingerprint SDK");
                dlgAlert.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton){
                                finish();
                                return;
                            }
                        }
                );
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            }
            else {
                sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
                error = sgfplib.OpenDevice(0);
                //debugMessage("OpenDevice() ret: " + error + "\n");
                SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
                error = sgfplib.GetDeviceInfo(deviceInfo);
                //debugMessage("GetDeviceInfo() ret: " + error + "\n");
                mImageWidth = deviceInfo.imageWidth;
                mImageHeight= deviceInfo.imageHeight;
                //debugMessage("Image width: " + mImageWidth + "\n");
                //debugMessage("Image height: " + mImageHeight + "\n");
                //debugMessage("Serial Number: " + new String(deviceInfo.deviceSN()) + "\n");
                sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
                sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
                //debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");
                //Toast.makeText(JSGDActivity.this,"This is mMaxTemplateSize"+mMaxTemplateSize[0], Toast.LENGTH_LONG).show(); //results|400
                mRegisterTemplate = new byte[mMaxTemplateSize[0]];
                mVerifyTemplate = new byte[mMaxTemplateSize[0]];
                //boolean smartCaptureEnabled = this.mToggleButtonSmartCapture.isChecked();
                //for(int h=0;h<20;h++) {
                //	Toast.makeText(JSGDActivity.this, "Height of Image::" + deviceInfo.imageHeight + "Width of image::" + deviceInfo.imageWidth, Toast.LENGTH_LONG).show();
                //} results|Height 400 and width 300
                sgfplib.WriteData((byte)5, (byte)1);
                autoOn.start();;

                //Thread thread = new Thread(this);
                //thread.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        sgfplib.CloseDevice();
        mRegisterImage = null;
        mVerifyImage = null;
        mRegisterTemplate = null;
        mVerifyTemplate = null;
        sgfplib.Close();
        super.onDestroy();
    }

    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer, int width, int height)
    {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
            Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //Bitmap bm contains the fingerprint img
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }


    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer)
    {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
            Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        //Bitmap bm contains the fingerprint img
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }



    public void DumpFile(String fileName, byte[] buffer)
    {
        //Uncomment section below to dump images and templates to SD card
    	/*
        try {
            File myFile = new File("/sdcard/Download/" + fileName);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            fOut.write(buffer,0,buffer.length);
            fOut.close();
        } catch (Exception e) {
            debugMessage("Exception when writing file" + fileName);
        }
       */
    }

    public void SGFingerPresentCallback (){
        autoOn.stop();
        fingerDetectedHandler.sendMessage(new Message());
    }

    public void CaptureFingerPrint(){
        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
//        this.mCheckBoxMatched.setChecked(false);
        byte[] buffer = new byte[mImageWidth*mImageHeight];
        dwTimeStart = System.currentTimeMillis();
        long result = sgfplib.GetImageEx(buffer, 10000,50);
        DumpFile("capture.raw", buffer);


        mImageViewFingerprint.setImageBitmap(this.toGrayscale(buffer));
        buffer = null;
    }
    public void onClick(View v) {
        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
        sgfplib.WriteData((byte) 5, (byte) 1);
        sgfplib.WriteData((byte) 0, (byte) 1);//this is the flag set when we find check button pressed
        autoOn.start();
        if (v == this.mButtonRegister) {
            //DEBUG Log.d(TAG, "Clicked REGISTER");
            //debugMessage("Clicked REGISTER\n");
            if (mRegisterImage != null)
                mRegisterImage = null;
            mRegisterImage = new byte[mImageWidth*mImageHeight];

            //this.mCheckBoxMatched.setChecked(false);
            ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
            dwTimeStart = System.currentTimeMillis();
            long result = sgfplib.GetImage(mRegisterImage);
            DumpFile("register.raw", mRegisterImage);
            /*dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");*/
            mImageViewFingerprint.setImageBitmap(this.toGrayscale(mRegisterImage));
            SendString = Base64.encodeToString(mRegisterImage, Base64.DEFAULT);
            //SendString = Base64.encodeToString(mRegisterImage,Base64.URL_SAFE);
            Log.d("StringToSend: ", "> " + SendString);
            //ba1.length();
            //SendString.substring(5,10)
            // for(int h=0;h<20;h++) {
            //Toast.makeText(registerFP.this, "This is String::"+ SendString, Toast.LENGTH_LONG).show();
            //}
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.SetTemplateFormat(SecuGen.FDxSDKPro.SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
            dwTimeEnd = System.currentTimeMillis();
            //dwTimeElapsed = dwTimeEnd-dwTimeStart;
            //debugMessage("SetTemplateFormat(SG400) ret:" +  result + " [" + dwTimeElapsed + "ms]\n");
            SGFingerInfo fpInfo = new SGFingerInfo();
            for (int i=0; i< mRegisterTemplate.length; ++i)
                mRegisterTemplate[i] = 0;
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);
            DumpFile("register.min", mRegisterTemplate);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            //debugMessage("CreateTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
            mImageViewRegister.setImageBitmap(this.toGrayscale(mRegisterImage));
            mTextViewResult.setText("Click Verify");
            mImageViewRegister.buildDrawingCache();
            Bitmap bmap = mImageViewRegister.getDrawingCache();

            byteBuf = null;
            //mRegisterImage = null;
            fpInfo = null;

        }
        if (v == this.mButtonMatch) {
            //DEBUG Log.d(TAG, "Clicked MATCH");     //this button is named as verify:: we will get new image and match with already present mRegisterTemplate
            if (mVerifyImage != null)
                mVerifyImage = null;
            mVerifyImage = new byte[mImageWidth*mImageHeight];
            //long result = sgfplib.GetImage(mVerifyImage);
            //DumpFile("verify.raw", mVerifyImage);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            mVerifyImage = Base64.decode(FetchedString, Base64.DEFAULT);
           // debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
            mImageViewFingerprint.setImageBitmap(this.toGrayscale(mVerifyImage));
            mImageViewVerify.setImageBitmap(this.toGrayscale(mVerifyImage));
            dwTimeStart = System.currentTimeMillis();
            long result = sgfplib.SetTemplateFormat(SecuGen.FDxSDKPro.SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            //debugMessage("SetTemplateFormat(SG400) ret:" +  result + " [" + dwTimeElapsed + "ms]\n");
            SGFingerInfo fpInfo = new SGFingerInfo();
            for (int i=0; i< mVerifyTemplate.length; ++i)
                mVerifyTemplate[i] = 0;
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.CreateTemplate(fpInfo, mVerifyImage, mVerifyTemplate);
            DumpFile("verify.min", mVerifyTemplate);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            //debugMessage("CreateTemplate() ret:" + result+ " [" + dwTimeElapsed + "ms]\n");
            boolean[] matched = new boolean[1];
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.MatchTemplate(mRegisterTemplate, mVerifyTemplate, SGFDxSecurityLevel.SL_NORMAL, matched);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            //debugMessage("MatchTemplate() ret:" + result+ " [" + dwTimeElapsed + "ms]\n");
            if (matched[0]) {
                if (nfc == 0) {
                    Intent intent_name = new Intent();
                    intent_name.setClass(verifyFP.this, childrenMenu.class);
                    startActivity(intent_name);
                    finish();
                }
                if (nfc == 1) {
                    Intent intent_name = new Intent();
                    intent_name.setClass(verifyFP.this, VaccinateChildActivity.class);
                    startActivity(intent_name);
                    finish();
                }
            }
            else {
                mTextViewResult.setText("NOT MATCHED!!");
                Toast.makeText(getApplicationContext(), "Please Retry!", Toast.LENGTH_SHORT).show();
                //debugMessage("NOT MATCHED!!\n");
            }
            mVerifyImage = null;
            fpInfo = null;
            matched = null;
        }
    }


    public void run() {

        Log.d(TAG, "Enter run()");
        //ByteBuffer buffer = ByteBuffer.allocate(1);
        //UsbRequest request = new UsbRequest();
        //request.initialize(mSGUsbInterface.getConnection(), mEndpointBulk);
        //byte status = -1;
        while (true) {}
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) verifyFP.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private class GetFP extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(verifyFP.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandlerNew sh = new ServiceHandlerNew();
            String url = "http://iir.azurewebsites.net/api/Parents/Prints/"+cnic;
            Log.d("Before Fetch",url);
            String jsonStr = sh.makeHttpRequest(url, "GET");
            Log.d("JSON", jsonStr);
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    contacts=new JSONArray(jsonStr);
                    JSONObject c = contacts.getJSONObject(0);
                    if(contacts.length()!=0){
                        Log.d("Response3: ", "> " + contacts);
                        FetchedString=c.getString("Finger");
                        //String id = c.getString(TAG_ID);
                        //name = c.getString(TAG_NAME);
                    }
                    else{
                        Toast.makeText(verifyFP.this,
                                "Some problem while fetching FP", Toast.LENGTH_LONG).show();
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
            mVerifyImage = new byte[mImageWidth*mImageHeight];
            //long result = sgfplib.GetImage(mVerifyImage);
            //DumpFile("verify.raw", mVerifyImage);
            mVerifyImage = Base64.decode(FetchedString, Base64.DEFAULT);
            // debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
            mImageViewFingerprint.setImageBitmap(this.toGrayscale(mVerifyImage));
            mImageViewVerify.setImageBitmap(this.toGrayscale(mVerifyImage));

                Toast.makeText(verifyFP.this,
                        "Finger print fetched successfully", Toast.LENGTH_LONG).show();

                //Intent intent_name = new Intent();

                //intent_name.setClass(getApplicationContext(), MainActivity.class);
                //startActivity(intent_name);
                //finish();


        }
        public Bitmap toGrayscale(byte[] mImageBuffer)
        {
            byte[] Bits = new byte[mImageBuffer.length * 4];
            for (int i = 0; i < mImageBuffer.length; i++) {
                Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
                Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
            }

            Bitmap bmpGrayscale = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
            //Bitmap bm contains the fingerprint img
            bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
            return bmpGrayscale;
        }


    }




}