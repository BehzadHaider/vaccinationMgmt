/*
 * Copyright (C) 2013 SecuGen Corporation
 *
 */

package com.android4dev.navigationview;

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
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;

public class registerFP extends Activity implements View.OnClickListener, Runnable, SGFingerPresentEvent {

    private static final String TAG = "SecuGen USB";
    private Button mCapture;
    private Button mButtonRegister;
    //private TextView mTextViewResult;
    private PendingIntent mPermissionIntent;
    private ImageView mImageViewFingerprint;
    private ImageView mImageViewRegister;
    private byte[] mRegisterImage;
    private byte[] mRegisterTemplate;
    private int[] mMaxTemplateSize;
    private int mImageWidth;
    private int mImageHeight;
    private int[] grayBuffer;
    private Bitmap grayBitmap;
    private IntentFilter filter; //2014-04-11
    private SGAutoOnEventNotifier autoOn;
    private boolean mLed;
    String cnic;
    private boolean mAutoOnEnabled;
    private int nCaptureModeN;
    private PostService jparser = new PostService();
    ProgressDialog pDialog;
    int status=0;
    String SendString;
    private Button SendButton;
    String result;
    SharedPreferences sharedPref;
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
        //this.mButtonRegister.setClickable(true);
        //this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.white));
    }

    public void DisableControls(){
        this.mCapture.setClickable(false);
        this.mCapture.setTextColor(getResources().getColor(android.R.color.black));
        this.mButtonRegister.setClickable(false);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.black));
    }


    //RILEY
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //service.setDevicePackage( usbDevice, YOUR_APP_PACKAGE_NAMESPACE, ai.uid );
        setContentView(R.layout.register_fp_layout_new);
        SendButton= (Button) findViewById(R.id.sendbt) ;
        //mCapture = (Button)findViewById(R.id.buttonCapture);
        //mCapture.setOnClickListener(this);
        mButtonRegister = (Button)findViewById(R.id.buttonRegister);
        mButtonRegister.setOnClickListener(this);
        mImageViewFingerprint = (ImageView)findViewById(R.id.imageViewFingerprint);
        mImageViewRegister = (ImageView)findViewById(R.id.imageViewRegister);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        cnic = sharedPref.getString("p_cnic", "No name defined");
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
        sb.setPixels(sintbuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2);
        mImageViewRegister.setImageBitmap(grayBitmap);
        //mImageViewVerify.setImageBitmap(grayBitmap);

        mMaxTemplateSize = new int[1];

        //USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        sgfplib = new JSGFPLib((UsbManager)getSystemService(Context.USB_SERVICE));


        //debugMessage("jnisgfplib version: " + sgfplib.Version() + "\n");
        mLed = false;
        mAutoOnEnabled = false;
        autoOn = new SGAutoOnEventNotifier(sgfplib, this);
        nCaptureModeN = 0;
        SendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {
                    if(isOnline()){
                    new addFingerPrint().execute();}
                    else{
                        Toast.makeText(getApplicationContext(), "Please connect to INTERNET and retry", Toast.LENGTH_SHORT).show();
                    }
                   // new UploadImages().execute();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please Retry!", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) registerFP.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        autoOn.stop();
        EnableControls();
        sgfplib.CloseDevice();
        unregisterReceiver(mUsbReceiver);
        mRegisterImage = null;
        mRegisterTemplate = null;
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        mImageViewRegister.setImageBitmap(grayBitmap);
        //mImageViewVerify.setImageBitmap(grayBitmap);
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
                //
                //
                //
                //debugMessage("Serial Number: " + new String(deviceInfo.deviceSN()) + "\n");
                sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
                sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
                //debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");
                //Toast.makeText(JSGDActivity.this,"This is mMaxTemplateSize"+mMaxTemplateSize[0], Toast.LENGTH_LONG).show(); //results|400
                mRegisterTemplate = new byte[mMaxTemplateSize[0]];
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
        String NFIQString;
        NFIQString = "";
        DumpFile("capture.raw", buffer);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        //debugMessage("getImageEx(10000,50) ret:" + result + " [" + dwTimeElapsed + "ms]" + NFIQString + "\n"); //result is 0  //NFIQ=3
       // mTextViewResult.setText("getImageEx(10000,50) ret: " + result + " [" + dwTimeElapsed + "ms] " + NFIQString + "\n");
        //String str=GetMimeType(registerFP.this,buffer);


        mImageViewFingerprint.setImageBitmap(this.toGrayscale(buffer));
        buffer = null;
    }
    public void onClick(View v) {
        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
        sgfplib.WriteData((byte) 5, (byte) 1);
        sgfplib.WriteData((byte)0, (byte)1);
        mAutoOnEnabled = true;      //this is the flag set when we find check button pressed
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
            mImageViewFingerprint.setImageBitmap(this.toGrayscale(mRegisterImage));
            SendString = Base64.encodeToString(mRegisterImage,Base64.DEFAULT);
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
            //mTextViewResult.setText("Click Verify");
            mImageViewRegister.buildDrawingCache();
            Bitmap bmap = mImageViewRegister.getDrawingCache();

            byteBuf = null;
            //mRegisterImage = null;
            fpInfo = null;

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

    private class addFingerPrint extends AsyncTask<Void, Void, Void> {
        /* Context context;
          private PostData(Context context) {
              this.context = context.getApplicationContext();
          }*/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(registerFP.this);
            pDialog.setMessage("Uploading Finger Print");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            List<NameValuePair> FP = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            Log.d("Vicky", "Data, String =Length" + String.valueOf(SendString.length()));
            Log.d("Vicky", "Data, String = " + SendString.toString());
            FP.add(new BasicNameValuePair("Finger", SendString.toString()));
            int charCount = 0;
            char temp;

            for( int i = 0; i < SendString.length( ); i++ )
            {
                temp = SendString.charAt( i );

                if( temp != ' ')
                    charCount++;
            }
            Log.d("Vicky", "Total Number of Characters " + charCount);
           // FP.add(new BasicNameValuePair("Finger","wohoooo" ));
            //Log.d("StringNum 2 ", "> " + SendString);
            //FP.add(new BasicNameValuePair("Finger",SendString));
            String url = "http://iir.azurewebsites.net/api/Parents/Prints/"+cnic;
            status = jparser.makeHttpRequest(url, "POST", FP);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(status==200) {
                Toast.makeText(registerFP.this,
                        "Status:" + status + "FP added successfully", Toast.LENGTH_LONG).show();
                sharedPref.edit().putInt("use_nfc", 0).apply();
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), addChild.class);
                startActivity(intent_name);
                finish();
            }
            else{
                Toast.makeText(registerFP.this,
                        "Status:"+status+", Fp not added", Toast.LENGTH_LONG).show();
            }
            //afterPostStatus.setText(Integer.toString(status));


        }
    }




}