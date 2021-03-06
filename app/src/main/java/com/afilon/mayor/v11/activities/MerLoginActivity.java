package com.afilon.mayor.v11.activities;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.webservice.WebServiceActaImageTask.SendImageResponseCallback;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.model.Connectivity;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.LogConfirmation;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.webservice.WebServiceActaImageTask;
import com.afilon.mayor.v11.webservice.WebServiceRestTask;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import android.provider.Settings;
import android.provider.Settings.System;

import java.util.ArrayList;

public class MerLoginActivity extends AfilonActivity implements DataResponseCallback{

    private static final String CLASS_TAG = "LoginActivity";
    private Button mLoginOne;
    private EditText mDuiOne;
    private String TYPE="MYR";

    private int duiSwitchCase;

    protected Utilities ah;

    protected String organizationString;
    private Button mTestNetworkOne;

    private DatabaseAdapterParlacen db_adapter;
    protected String duiTwoString="";
    protected String duiOneString="";
    protected String duiThreeString="";
    protected String validation="";

    private EditText enterJrv;
    protected String jrvNumber="";
    private Button mContinuarBtn;
    private CustomKeyboard customKeyboard;
    private RelativeLayout loginParent;
    private VotingCenter vc;

    private String mPassThisJrv;
    private boolean loginRequest=true;

    private boolean isLoginPresidentLoggedIn = false;
    private boolean isLoginSecretaryLoggedIn = false;
    private boolean isLoginVocalLoggedIn = false;
    private boolean wasClosed = false;
    private boolean validationCheck = false;

    private boolean permissionC, permissionD, permissionP;

    TelephonyManager tm;
    WifiManager wm;
    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        //--------- filter listeners ---------------------------------------------------
        CommonListeners listenerHandler = new CommonListeners();
        View.OnLongClickListener longClickListener = listenerHandler.getMouseListener();
        //--------- end filter listeners ------------------------------------------------

        tm = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
        wm = (WifiManager)getApplicationContext().getSystemService(this.WIFI_SERVICE);

        ah = new Utilities(MerLoginActivity.this);
        ah.tabletConfiguration(Build.MODEL,this);
        Log.e("Built Model Number ", Build.MODEL);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.mer_activity_login);
        //Catch Unexpected Error:
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(MerLoginActivity.this));

        // CARLOS: Clean up database for all data stored for testing ***********
        // that was not sent out through the web services
        // 2014-08-22 12:45
        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();
        db_adapter.deletePreferentialVotoBanderas();
        db_adapter.deletePartiesPreferentialVotes();
        db_adapter.deleteAllPreferentialCandidateVotes();
        db_adapter.deleteAllLogin();
        db_adapter.deleteAllCandidateCrossVote();
        db_adapter.deleteTemp();
        db_adapter.deleteMERMembers();
        db_adapter.deleteAllCandidateMarks();
        db_adapter.deleteActaAttendees();
        db_adapter.deleteConceptCount();
        Log.e("LOGIN","---------------------------------------------------------------");
//		String tstemp = db_adapter.testTemp();

        //db_adapter.deleteAllCandidateMarks();
        /** DEBUGGIN !!!!*/
/*		ArrayList<CandidateMarks> cms =db_adapter.getCandidateMarksArrayList("290","4");
		for(CandidateMarks cm: cms){
			Log.e("Candidate ID", cm.getmCandidateId());
			Log.e("Marcas  ", Integer.toString(cm.getmTotalMarks()));
		}*/
        Log.e("LOGIN","---------------------------------------------------------------");
        db_adapter.close();

        // ************** End of Cleaning Database *************************
        ah.removePreferences("provisionalTriggered"); //This is created in WebViewJrvActivity
        ah.removePreferences("sendDataBtnTriggered");
        ah.removePreferences("sendImageBtnTriggered");

        vc = new VotingCenter();

        loginParent = (RelativeLayout) findViewById(R.id.loginParent);

        mContinuarBtn = (Button) findViewById(R.id.continuar_btn);
        //setButtonColorGreen(mContinuarBtn);

        //CARLOS: 2016-11-01
//		mContinuarBtn.setEnabled(false);
        ah.setButtonColorRed(mContinuarBtn);

        mLoginOne = (Button) findViewById(R.id.loginBtnOne);
        ah.setButtonColorRed(mLoginOne);

        mTestNetworkOne = (Button) findViewById(R.id.testnetworkBtnOne);
        ah.setButtonColorGreen(mTestNetworkOne);
        //------------------------------------------------------------------------------------------
        //register custom keyboard:
        customKeyboard = new CustomKeyboard(this, R.id.keyboardview, R.xml.tenhexkbd);
        customKeyboard.registerEditText(R.id.enter_jrv);
        customKeyboard.registerEditText(R.id.duiOne);

        enterJrv = (EditText) findViewById(R.id.enter_jrv);
        enterJrv.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
        enterJrv.setOnLongClickListener(longClickListener);
        //CARLOS: 2016-11-01
        enterJrv.addTextChangedListener(tw1);
        enterJrv.requestFocus();

        mDuiOne = (EditText) findViewById(R.id.duiOne);
        mDuiOne.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
        mDuiOne.setOnLongClickListener(longClickListener);
        mDuiOne.addTextChangedListener(new CustomTextWatcher(mDuiOne, mLoginOne));
        mDuiOne.setOnTouchListener(listenerHandler.getDismissListener());
        ah.enableEditText(mDuiOne,false);

//        enterJrv.setBackgroundResource(R.drawable.cbselected);
        enterJrv.setBackgroundResource(R.drawable.etbordergreen);
//        enterJrv.setHintTextColor(Color.WHITE);
//        enterJrv.setTextColor(Color.WHITE);
        mDuiOne.setBackgroundColor(Color.TRANSPARENT);

        mContinuarBtn.setOnClickListener(continuarBtnListerner());
        mLoginOne.setOnClickListener(loginOneBtnListerner());

        loginParent.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){

                    mDuiOne.requestFocus();
                    customKeyboard.showCustomKeyboard(mDuiOne);
                    Log.e(">>> DEBUG Enabling custom Keyboard", "");
                    // Do what you want
                    return true;
                }
                return false;
            }
        });

        mTestNetworkOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isNetworkFast = Connectivity
                        .isConnectedFast(MerLoginActivity.this);
                String connectionSpeed = Connectivity.getConnectionSpeed(MerLoginActivity.this);
                if (isNetworkFast) {
                    ah.createCustomToast("Ancho de Banda Actual ",
                            connectionSpeed);
                } else {
                    ah.createCustomToast("Ancho de Banda Actual ",
                            connectionSpeed);
                }

//				challengeHelper.signaturePad("132", "Inigo Montoya","Avenger", "794613852");

            }
        });

        ah.buttonNextFocus(mTestNetworkOne,enterJrv.getId());
        enterJrv.requestFocus();

        permissionD = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED);
        permissionP = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED);
        if (!permissionD){
            ah.createCustomToast("Storage Permissions Not Granted \n Please Enable Storage Permissions");
        }

        if (!permissionP){
            ah.createCustomToast("Phone Permissions Not Granted \n Please Enable Phone Permissions");
        }

//        abraKadabra();
//		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//			//ask for authorisation
//			//Manifest.permission.CAMERA
//			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//					Manifest.permission.READ_PHONE_STATE)) {
//				showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_CAMERA);
//			}
//			else
//				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
//		}
//		else{
//			try {
//				//releasing camera if it's already in use
//				releaseCamera();
//				camera = Camera.open(camId);
//			}catch (Exception e)
//			{
//				e.printStackTrace();
//			}}   /////// put your else condition in braces

    }

    @Override
    public void onResume(){
        super.onResume();
        if(wasClosed) {
            ah.createCustomToast("Application Was Minimized \n Please Re-enter Information");
        }
        wasClosed = true;
        permissionD = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED);
        permissionP = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED);
        clearEntries();
    }

    private View.OnClickListener loginOneBtnListerner(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                organizationString = "Afilon";
                duiOneString = mDuiOne.getText().toString();
                duiSwitchCase = 1;
                if (!organizationString.equals("") && !duiOneString.equals("")) {
                    try {
                        int ws_task_number = 0;
//                        String url = Consts.BASE_URI + Consts.PATH_CONFIRM + jrvNumber + "&" + duiOneString +"&"+"4000-3000-20002"+"&"+"4000-3000-20003";
                        String url = Consts.MER_LOGIN_PATH + jrvNumber + "&" + duiOneString + "&" + getResources().getString(R.string.electype);
                        Log.e("URL : " , url);
                        HttpGet searchRequest = new HttpGet(url);
                        WebServiceRestTask task = new WebServiceRestTask(ws_task_number);
                        task.setResponseDataCallback(MerLoginActivity.this);
                        task.execute(searchRequest);
                    } catch (Exception e) {
                        Log.e("DUI REST ERROR: ", e.getMessage());
                    }
//                    ah.enableEditText(mDuiOne,false);
//                    ah.setButtonColorRed(mLoginOne);
                    findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                } else {
                    ah.createCustomToast("Ingresar "+getResources().getString(R.string.dui)+" de Usuario","");
                }
            }
        };
    }

    //CARLOS: 2016-11-01 CONTINUAR BUTTON
    TextWatcher tw1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (enterJrv.getText().length() > 0) {
                ah.setButtonColorGreen(mContinuarBtn);
            } else {
                ah.setButtonColorRed(mContinuarBtn);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw1 afterTextChanged", "x");

        }
    };

    private View.OnClickListener continuarBtnListerner(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int jrvInt = -1;
                jrvNumber = enterJrv.getText().toString();

                mPassThisJrv = String.valueOf(jrvNumber);
                ah.savePreferences("PassThisJRV", mPassThisJrv);

                duiSwitchCase = 0;
                if (!permissionD || !permissionP) {
                    ah.createCustomToast("Storage/Phone Permissions Not Granted \n Please Enable Camera/Storage/Phone Permissions");
                } else {
                    jrvInt = ah.parseInt(jrvNumber, 0);
                    jrvInt--;
                    if (jrvInt >= 0 && jrvInt <= 104249) {
                        ah.setButtonColorRed(mContinuarBtn);
                        ah.enableEditText(enterJrv,false);
                        ah.enableEditText(mDuiOne, true);

                        validateTablet();
                    }else ah.createCustomToast(getResources().getString(R.string.JRV)+" DEBE SER MAYOR QUE ZERO", "Y MENOR QUE 10066");
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void onBackPressed() {
//		Log.d("LoginActivity", "onBackPressed Called");
//		Intent setIntent = new Intent(Intent.ACTION_MAIN);
//		setIntent.addCategory(Intent.CATEGORY_HOME);
//		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(setIntent);
    }

    @Override
    public void onRequestDataSuccess(String response) {

        if(validationCheck){
            validationCheck = false;
            response = response.substring(0, response.length() - 1);

            validationConfirmation(response);
        }else {
            // TODO Auto-generated method stub
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
            // Process the response data (here we just display it)
            Log.d("REST OK :", response);
            // mResult.setText(response); //Display the string from the Web Service

            response = response.substring(0, response.length() - 1);

            // if the request was made from log in:
            if (loginRequest)
                logInConfirmation(response);
            else {
                Log.e("Response" , "after get members");
                loadAndSaveUsers(response);
            }
        }
    }

    private void validationConfirmation(String response){
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("validationNumber", validationNumber.class);
        validationNumber validID = new validationNumber();
        validID = (validationNumber) xstream.fromXML(response);

        //Check returned JRV and ESN match sent
        if(validID.getJrv().equals(jrvNumber) && validID.getESN().equals(validation)){
            //Do good stuff here
//            alakazam();
            ah.enableEditText(mDuiOne,true);
            ah.buttonNextFocus(mTestNetworkOne,mDuiOne.getId());
            mDuiOne.requestFocus();
            ah.enableEditText(enterJrv,false);
            ah.setButtonColorRed(mContinuarBtn);
//            mDuiOne.setBackgroundResource(R.drawable.cbselected);
            mDuiOne.setBackgroundResource(R.drawable.etbordergreen);
//            mDuiOne.setHintTextColor(Color.WHITE);
//            mDuiOne.setTextColor(Color.WHITE);
//            enterJrv.setBackgroundColor(Color.TRANSPARENT);
            enterJrv.setBackgroundResource(R.drawable.etborderred);
//            enterJrv.setTextColor(Color.BLACK);
        }else {
//			ah.createCustomToast("Incorrect Device For " + getResources().getString(R.string.JRV));
            ah.createCustomToast("No esta habilitado para entrar");
            ah.setButtonColorGreen(mContinuarBtn);
            ah.enableEditText(enterJrv,true);
        }
    }

    private void logInConfirmation(String response){
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("LogConfimation", LogConfirmation.class);
        LogConfirmation logConfirmation = new LogConfirmation();
        logConfirmation = (LogConfirmation) xstream.fromXML(response);
//        if(logConfirmation.getJrv().equals("inuse")){
//            ah.createCustomToast("Datos Incorrectos");
//            clearEntries();
//        }else
        if(logConfirmation.getDUI1().equals(duiOneString)){
//            && logConfirmation.getDUI2().equals("4000-3000-20002")){
            loginRequest = false;
            //log in confirm move on save barcode
            ah.savePreferences("duiNumber", duiOneString);
            ah.savePreferences("KeyLog",logConfirmation.getKey());
//			Intent i = new Intent(LoginActivity.this, JrvActivity.class);
            //-------------------------------------------------------------------------------------
            //create the table to check the login
            db_adapter.open();
            db_adapter.insertDui(jrvNumber,duiOneString, "00000000-0");
            db_adapter.close();
            //-------------------------------------------------------------------------------------
            fetchMesaJuntaMembers();
//			i.putExtra("dui", duiOneString);
//			i.putExtra("organization", "Afilon");
//			i.putExtra("jrvNumber", jrvNumber);
            ah.createCustomToast("LOGIN EXITOSO.");

//			startActivity(i);
//			finish();
        }else{
            //todo clear all entries
            ah.createCustomToast("Datos Incorrectos");
            clearEntries();
        }
    }

    private void loadAndSaveUsers(String response){
        JSONArray jsonArray;
        db_adapter.open();
        try {
            jsonArray = new JSONArray(response);
            for (int i=0; i<jsonArray.length(); i++){
                JSONObject member = jsonArray.getJSONObject(i);
                db_adapter.insertMERMembers(member.getString("DUI"),member.getString("Name"),
                        member.getString("Title"),"","", member.getString("Proprietario"),jrvNumber);
                Log.e("Name : " , member.getString("Name"));
                Log.e("DUI : " , member.getString("DUI"));
//                db_adapter.insertMERMemberswParty(member.getString("DUI"),member.getString("Name"), member.getString("Title"),"","", member.getString("Proprietario"),jrvNumber, member.getString("Party"));
            }
            db_adapter.close();
        }catch (JSONException je){
            db_adapter.close();
            je.printStackTrace();
        }

        db_adapter.open();
        vc = db_adapter.getNewJrv(jrvNumber);
        db_adapter.close();
        //-------------------------------------------------------------------------------------
//		goToNextActivity();
        Bundle b = new Bundle();
        b.putParcelable("com.afilon.tse.votingcenter", vc);
        b.putParcelable("com.afilon.tse.escrudata",new Escrudata(vc.getJrvString()));

        Intent i = new Intent(MerLoginActivity.this, MerManagerActivity.class);
        i.putExtra("dui", duiOneString);
        i.putExtra("organization", "Afilon");
        i.putExtra(getResources().getString(R.string.jrvNumber), jrvNumber);
        i.putExtras(b);
        ah.savePreferences(getResources().getString(R.string.jrvNumber),jrvNumber);
        ah.createCustomToast("LOGIN EXITOSO.");
        startActivity(i);
        finish();


    }

    private void fetchMesaJuntaMembers(){
        //METHODS TO FETCH DATA:
        JSONObject json = new JSONObject();
        try {
            Log.e("HERE WE GET, ", "MEMBERS");
            json.put("JRV",jrvNumber);
            String fetch = json.toString();
            int ws_task_number = 0;
            String url = Consts.GETMEMBERSELSA;
            HttpPost membersRequest = new HttpPost(url);
            membersRequest.setHeader("content-type","application/json");
            StringEntity entity = new StringEntity(fetch);
            membersRequest.setEntity(entity);
            WebServiceRestTask task = new WebServiceRestTask(ws_task_number);
            task.setResponseDataCallback(MerLoginActivity.this);
            task.execute(membersRequest);
        } catch (Exception e) {
            Log.e("DUI REST ERROR: ", e.getMessage());
        }
    }

    @Override
    public void onRequestDataError(Exception error) {
        if(validationCheck){
            ah.createCustomToast("OnRquestDataError");
            ah.setButtonColorGreen(mContinuarBtn);
            ah.enableEditText(enterJrv,true);
        }else {
            // Process the response data (here we just display it)
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
            Log.d("REST onRequestError :", error.getMessage());
            // mResult.setText(error.getMessage());
            ah.createCustomToast("Error de Conneccion," + "por favor consulte con su supervisor.");
            clearEntries();
        }
    }
    private void clearEntries() {
//		enterJrv.setText("");
        mDuiOne.setText("");
//		ah.enableEditText(enterJrv,true);
        ah.buttonNextFocus(mTestNetworkOne, mDuiOne.getId());
        ah.enableEditText(mDuiOne, true);
        mDuiOne.setFocusable(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST FUNCTION FOR SENDING SIGNATURE IMAGES //////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void sendOneSig(String filename, String dui, String title) {
        WebServiceActaImageTask uploadActaImageTaskOne;

        if (ah.isOnline(MerLoginActivity.this)) {
            uploadActaImageTaskOne = new WebServiceActaImageTask();
//				public void postSig(Context context, String serviceUrl, String fileName, String dui, String title)
            uploadActaImageTaskOne.postSig(MerLoginActivity.this, Consts.PREF_ELECTION_IMAGE_URL+"/Signatures", filename, dui, title, jrvNumber);
        } else {
            ah.createCustomToast("No hay connecion", "accesible.");
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // END TEST FUNCTION FOR SENDING SIGNATURE IMAGES //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private class CustomTextWatcher implements TextWatcher {
        private EditText mEditText;
        private Button mButton;
        int realCount =0;
        boolean replace=false, replace2 = false;

        public CustomTextWatcher(EditText e, Button button) {
            mEditText = e;
            mButton = button;
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (Consts.LOCALE.contains("ELSA")) {
                if (s.length() == 9 && s.charAt(8) != '-') {
                    replace = true;
                }

                if (s.length() > 9) {
                    Log.e("CHAR", String.valueOf(s.charAt(8)));
                    if ((s.charAt(8) == '-')) {
                        ah.setButtonColorGreen(mButton);
                        getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        ah.enableEditText(mEditText, true);
                        ah.buttonNextFocus(mTestNetworkOne, mEditText.getId());
                        ah.setButtonColorGreen(mButton);
                        Log.e("LOG IN", "RIGHT FORMAT");
                    }

                } else {
                    ah.setButtonColorRed(mButton);
                }
            } else if (Consts.LOCALE.contains("HON")){
                if(s.length()==5 && s.charAt(4) !='-'){
                    replace =true;
                }
                if(s.length()==10 && s.charAt(9)!='-'){
                    replace2=true;
                }

                if (s.length() > 14) {
                    Log.e("CHAR",String.valueOf(s.charAt(8)));
//				if((s.charAt(8)=='-')){
                    ah.setButtonColorGreen(mButton);
                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    ah.enableEditText(mEditText, true);
                    ah.buttonNextFocus(mTestNetworkOne, mEditText.getId());
                    ah.setButtonColorGreen(mButton);
                    Log.e("LOG IN", "RIGHT FORMAT");
//				}

                }else{
                    ah.setButtonColorRed(mButton);
                }
            }
        }

        public void afterTextChanged(Editable s) {
            if (Consts.LOCALE.contains("ELSA")) {
                if (replace && realCount == 0) {
                    realCount++;
                    char d = s.charAt(8);
                    s.replace(8, 9, "-" + d);
                    replace = false;
                }
                realCount = 0;
            } else if (Consts.LOCALE.contains("HON")){
                if(replace && realCount==0){
                    realCount++;
                    char d = s.charAt(4);
                    s.replace(4,5,"-"+d);
                    replace=false;
                }else if(replace2 && realCount==0){
                    realCount++;
                    char d = s.charAt(9);
                    s.replace(9,10,"-"+d);
                    replace2=false;
                }
                realCount=0;
            }
        }
    }

    private void abraKadabra(){
        findViewById(R.id.duiOne).setVisibility(View.GONE);
        findViewById(R.id.loginBtnOne).setVisibility(View.GONE);
        findViewById(R.id.testnetworkBtnOne).setVisibility(View.VISIBLE);
        findViewById(R.id.textInfo).setVisibility(View.GONE);
    }

    private void alakazam(){
        findViewById(R.id.duiOne).setVisibility(View.VISIBLE);
        findViewById(R.id.loginBtnOne).setVisibility(View.VISIBLE);
        findViewById(R.id.testnetworkBtnOne).setVisibility(View.VISIBLE);
        findViewById(R.id.textInfo).setVisibility(View.VISIBLE);
    }

//	public void keyControl(){
//		onKeyDown(KeyEvent.KEYCODE_CTRL_LEFT);
//	}

    //TODO Add Send data function for serial number to be placed into Continuar button listener
    private void validateTablet(){
        int ws_task_number = 0; //TODO check this number
        validationCheck = true;

        if( tm.getDeviceId() != null){
            validation = tm.getDeviceId();
        }else validation = Build.SERIAL;
//		ah.createCustomToast("Validation Number : " + validation);
        String url = Consts.VALIDATETABLETELSA + jrvNumber + "&" + validation; //TODO ensure proper URL
        Log.e("URL for tablet valid ", url);
        HttpGet searchRequest = new HttpGet(url);
        WebServiceRestTask task = new WebServiceRestTask(ws_task_number);
        task.setResponseDataCallback(MerLoginActivity.this); //TODO check proper data response callback

        task.execute(searchRequest);
    }
}