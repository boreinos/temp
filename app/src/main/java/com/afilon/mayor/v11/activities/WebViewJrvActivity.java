package com.afilon.mayor.v11.activities;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.ChallengeHelper.OnApprove;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.webservice.WebServiceRestTask;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.model.ApproveEnlaceModel;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.google.gson.Gson;

public class WebViewJrvActivity extends AfilonActivity implements
		DataResponseCallback{

	private WebView webView;
	private String jrvNumber;
	private Button finish_btn;
	private Button approved_btn;
	private Button notapproved_btn;
	private String approvalURL;
	private String provisionalURL;
	private ApproveEnlaceModel approveObject;
	private Gson gson;
	protected Utilities ah;
	private String direct_electionId = "";
	private boolean wasProvisionalAcceptedTriggered = false;
	private boolean btnApproved = false;
	private boolean btnNotApproved = false;
	private final static int USER_APPPROVED = 1;
	private final static int USER_NOT_APPROVED = 2;
	private DatabaseAdapterParlacen db_adapter;
    private ChallengeHelper challengeHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		ah = new Utilities(WebViewJrvActivity.this);
		ah.tabletConfiguration(Build.MODEL,this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_web_view);

		// // // Trap unexpected error
		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
				WebViewJrvActivity.this));


        approvalURL = Consts.PREF_ELECTION_JRVAccept_URL;
        provisionalURL = Consts.PREF_ELECTION_JRV_PROVISIONAL_URL;
        db_adapter = new DatabaseAdapterParlacen(this);
        //-----------------------SET UP DUI CHALLENGE HELPER ---------------------------------------
        challengeHelper = new ChallengeHelper(this);
        challengeHelper.setTools(ah, db_adapter);
        challengeHelper.addCustomKeyBoards(R.id.keyboardview, R.id.keyboardview2);
        challengeHelper.addRoutine(USER_APPPROVED, acceptResults);
        challengeHelper.addRoutine(USER_NOT_APPROVED, rejectResults);
        //----------------------- END SET UP HELPER ------------------------------------------------


		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Escrudata escrudata =  extras.getParcelable("com.afilon.tse.escrudata");
			jrvNumber = escrudata.getJrv();//extras.getString("jrv");
			direct_electionId = escrudata.getActaImageLink();// extras.getString("preferential_election_id");
		}
		ah.saveCurrentScreen(this.getClass(),extras);
		//now update flags from previous screens:
		ah.savePreferences("dsent",false);
		ah.savePreferences("isent",false);

		String provi = ah.loadPreferencesString("provisionalTriggered");
		if(provi != null && !provi.equals("")) {
			wasProvisionalAcceptedTriggered = Boolean.valueOf(provi);
		}

		approveObject = new ApproveEnlaceModel();
		approveObject.setJrv(Integer.parseInt(jrvNumber));
		approveObject.setPreferential_election_id(direct_electionId);

		gson = new Gson();

		webView = (WebView) findViewById(R.id.webView1);
		// webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient());
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		webView.getSettings().setBuiltInZoomControls(true);


		ah.viewNextFocus((View)webView,R.id.approved_btn);

		// String postData = "username=" + Consts.enlaceUser + "&password="
		// + Consts.enlacePassword + "&j_jrv=" + jrvNumber;
		//
		// webView.postUrl(Consts.ENLACE_PREF_URL,
		// EncodingUtils.getBytes(postData, "BASE64"));

		webView.loadUrl(Consts.DIR_VIEW_JRV_ASSAMBLEA_URL
				+ jrvNumber);
		GradientDrawable shape =  new GradientDrawable();
		shape.setCornerRadius( 8 );
		shape.setColor(Color.RED);

//		webView.loadUrl("https://ten.afilon.com/JrvConfirmationAsamblea-war/ViewActaPreferentialDetails?j_jrv="
//				+ jrvNumber);
//
//		webView.loadUrl("https://ten.afilon.com/Asamblea-war/ViewActaPreferentialDetails?jrv="
//				+ jrvNumber);
		//ADD JRV prior to updating.

		finish_btn = (Button) findViewById(R.id.finish_btn);
		ah.setButtonColorGreen(finish_btn);
		finish_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		approved_btn = (Button) findViewById(R.id.approved_btn);
		approved_btn.requestFocus();
		if(wasProvisionalAcceptedTriggered){
			ah.setButtonColorRed(approved_btn);
		} else {
			ah.setButtonColorGreen(approved_btn);

		}

		approved_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
			// when approved button clicked, raise a challenge:
                challengeHelper.createDuiChallenge(getResources().getString(R.string.WebViewYesMessage), USER_APPPROVED);
			}
		});

		notapproved_btn = (Button) findViewById(R.id.notapproved_btn);
		if(wasProvisionalAcceptedTriggered){
			ah.setButtonColorRed(notapproved_btn);
		} else {
			ah.setButtonColorGreen(notapproved_btn);
		}
//		setButtonColorGreen(notapproved_btn);
		notapproved_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
		// when not approved button clicked, raise a challenge: 
                challengeHelper.createDuiChallenge(getResources().getString(R.string.WebViewNoMessage), USER_NOT_APPROVED);
			}
		});

	}

	private void sendApprovalTask(String uri, String jsonString, int task_case) {
		try {

			HttpPost httpPost = new HttpPost(uri);
			httpPost.setHeader("content-type", "application/json");

			HttpEntity entity;

			StringEntity s = new StringEntity(jsonString);
			entity = s;
			httpPost.setEntity(entity);

			WebServiceRestTask task = new WebServiceRestTask(task_case);
			task.setResponseDataCallback(WebViewJrvActivity.this);
			task.execute(httpPost);


			Log.e("WebViewJrvActivity", " sendApprovalTask JSON: " + task_case + jsonString);

		} catch (Exception e) {
			Log.e("WebViewJrvActivity", " sendApprovalTask ERROR: "+ e.getMessage());
		}
	}

	private void sendUpdateProvisionalAcceptTask(String uri, String jsonString, int task_case) {
		try {

			HttpPost httpPost = new HttpPost(uri);
			httpPost.setHeader("content-type", "application/json");

			HttpEntity entity;

			StringEntity s = new StringEntity(jsonString);
			entity = s;
			httpPost.setEntity(entity);

			WebServiceRestTask task = new WebServiceRestTask(task_case);
			task.setResponseDataCallback(WebViewJrvActivity.this);
			task.execute(httpPost);

			Log.e("WebViewJrvActivity", " sendUpdateProvisionalAcceptTask JSON: " + task_case + jsonString);

		} catch (Exception e) {
			Log.e("WebViewJrvActivity", " sendUpdateProvisionalAcceptTask ERROR: "+ e.getMessage());
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_view, menu);
		return true;
	}

	@Override
	public void onRequestDataSuccess(String response) {
		// TODO Auto-generated method stub
		//We force a refresh to the whole enlace web
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		if(btnApproved) {
			webView.loadUrl(Consts.DIR_VIEW_JRV_ASSAMBLEA_URL
					+ jrvNumber);
			ah.setButtonColorRed(approved_btn);
			ah.setButtonColorRed(notapproved_btn);
		}

		if(btnNotApproved) {
			webView.loadUrl(Consts.DIR_VIEW_JRV_ASSAMBLEA_URL
					+ jrvNumber);
			ah.setButtonColorRed(notapproved_btn);
			ah.setButtonColorRed(approved_btn);
		}

        ah.createCustomToast("DATO PROCESADO");


	}

	@Override
	public void onRequestDataError(Exception error) {
		// TODO Auto-generated method stub
        ah.createCustomToast("DATO NO PROCESADO");
	}

	public void approveJRV(){
        ah.createCustomToast("APROBADO");
		approveObject.setProvisional_accepted("1");
		String approvalJson = gson.toJson(approveObject);
		sendUpdateProvisionalAcceptTask(provisionalURL, approvalJson, 14);
		ah.setButtonColorRed(notapproved_btn);
		ah.setButtonColorGreen(finish_btn);
		btnApproved = true;
		ah.savePreferences("provisionalTriggered", "true");
		Intent intent = new Intent(WebViewJrvActivity.this,
				Consts.EXITACT);
		startActivity(intent);
		finish();
	}

	public void notApprovedJRV(){
        ah.createCustomToast("NO APROBADO");
		approveObject.setProvisional_accepted("0");
		String notApprovalJson = gson.toJson(approveObject);
		sendUpdateProvisionalAcceptTask(provisionalURL, notApprovalJson, 16);
		ah.setButtonColorRed(approved_btn);
		ah.setButtonColorGreen(finish_btn);
		btnNotApproved=true;
		ah.savePreferences("provisionalTriggered", "true");
		Intent intent = new Intent(WebViewJrvActivity.this,
				Consts.EXITACT);
		startActivity(intent);
		finish();
	}
	//----------------------------- CHALLENGE ------------------------------------------------------

    private OnApprove acceptResults = new OnApprove() {
        @Override
        public void approved() {
            approveJRV();
        }
    };
    private OnApprove rejectResults = new OnApprove() {
        @Override
        public void approved() {
            notApprovedJRV();
        }
    };

	@Override
	public void onWindowFocusChanged(boolean hasfocus){
		if(!hasfocus){
			findViewById(R.id.button_layout).setVisibility(View.INVISIBLE);
		}else{
			findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
		}

	}

}