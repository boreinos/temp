package com.afilon.mayor.v11.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class JrvActivity extends AfilonActivity implements DataResponseCallback,TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener, DialogToConfirmDui.DialogToConfirmDuiListener {

	private static final int BAR_CODE_SCAN = 333;
	private static final String CLASS_TAG = "JrvActivity";

	private DatabaseAdapterParlacen db_adapter;
	private TextView barcode_tv;
	private EditText barcode_et;

	private String jrvString;
	private EditText jrvNmb_et;
	private CustomKeyboard mCustomKeyboard;
	private Utilities ah;
	private String submitterOne;
	private String dui;
	private String organization;
	private VotingCenter vc;
	private String isBarcodeMatch;

	private Button loginBtn;
	private String jrvNumber;
	private Button adminBtn;
	private Button scanBarCodeBtn;
	private DialogToConfirmDui dialogToConfirmDui;
	private String passThisJRV;
	private Button continuarBtn;
	private TwoButtonDialogFragment twoBtnDialogFragment;
	private final static int USER_APPPROVED = 1;
	private final static int USER_NOT_APPROVED = 2;
	private final static int SCAN=4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ah = new Utilities(this);
		ah.tabletConfiguration(Build.MODEL,this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_jrv);
		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
				JrvActivity.this));

		//--------- filter listeners ---------------------------------------------------
		CommonListeners listenerHandler = new CommonListeners();
		View.OnKeyListener altkeys = listenerHandler.getAltKeysListener();
		View.OnLongClickListener longClickListener = listenerHandler.getMouseListener();
		//--------- end filter listeners ------------------------------------------------


		vc = new VotingCenter();

		String userName = ah.loadPreferencesString("username");

		Intent i = getIntent();
		dui = i.getStringExtra("dui");
		organization = i.getStringExtra("organization");
		jrvNumber = i.getStringExtra("jrvNumber");
		submitterOne = dui;

//		passThisJRV = String.valueOf(jrvNumber);
//		Log.i(">>> DEBUG " + CLASS_TAG, "passThisJRV " + passThisJRV);

		//CARLOS: 2016-11-04
		//Paint in red CONTINUAR Bnt
//		continuarBtn = (Button) findViewById(R.id.continuarBtn);
//		setButtonColorRed(continuarBtn);

		ImageView rightImageView = (ImageView) findViewById(R.id.imageView2);
		TextView topTextView = (TextView) findViewById(R.id.textview_one);

		barcode_et = (EditText) findViewById(R.id.scan_barcode_tv);
		barcode_et.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
		barcode_et.setOnLongClickListener(longClickListener);
		barcode_et.setFocusable(true);

		barcode_tv = (TextView) findViewById(R.id.textView32);
		mCustomKeyboard = new CustomKeyboard(this, R.id.keyboardview,
				R.xml.tenhexkbd);

//		mCustomKeyboard.registerEditText(R.id.textView43);
		mCustomKeyboard.registerEditText(R.id.scan_barcode_tv);

		isBarcodeMatch = "false"; //CARLOS: Why Slava hard-coded this line as 'TRUE" ?

		db_adapter = new DatabaseAdapterParlacen(JrvActivity.this);
		db_adapter.open();

		vc = db_adapter.getNewJrv(jrvNumber);

		// barcodeString = vc.getBarcodeString();
		jrvNumber = ("00000" + jrvNumber).substring(jrvNumber.length());

		TextView municipio_tv = (TextView) findViewById(R.id.textView22);
		TextView departamento_tv = (TextView) findViewById(R.id.textView72);
		TextView jrv_tv = (TextView) findViewById(R.id.textView35);
		TextView vote_center_tv = (TextView) findViewById(R.id.textView62);

		jrv_tv.setText(jrvNumber);
		departamento_tv.setText(vc.getDepartamentoString());
		municipio_tv.setText(vc.getMunicipioString());
		vote_center_tv.setText(vc.getVoteCenterString());
		// barcode_tv.setText(vc.getPref_election_id());

		barcode_et.setFocusableInTouchMode(true);
		barcode_et.requestFocus();
		barcode_et.addTextChangedListener(tw1);

		loginBtn = (Button) findViewById(R.id.login_btn);

		ah.setButtonColorRed(loginBtn);

		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (barcode_et.getText().toString().equals("")) {
					ah.createCustomToast("INGRESAR", "CODIGO DE BARRA");
				} else if(barcode_et.getText().toString().equals(ah.loadPreferencesString("KeyLog"))) {
					ah.savePreferences("startTime", ah.getCurrentDateTime());
					ah.savePreferences("barcodeSaved", barcode_et.getText().toString());
					Bundle b = new Bundle();
					b.putParcelable("com.afilon.tse.votingcenter", vc);
//					b.putString("submitterOne", submitterOne);
					b.putParcelable("com.afilon.tse.escrudata",new Escrudata(vc.getJrvString()));


//					ah.savePreferences("PassThisJRV", passThisJRV);

					Intent search = new Intent(JrvActivity.this,
							Consts.ROLLACT);//PapeletasActivity.class);
					search.putExtras(b);
					startActivity(search);
					finish();
				}else {
					ah.createCustomLongToast(
							"El codigo de barra",
							" no coincide con el numero de "+getResources().getString(R.string.JRV),
							3000);
				}
			}
		});

		scanBarCodeBtn = (Button) findViewById(R.id.scan_barcode_btn);
		ah.setButtonColorGreen(scanBarCodeBtn);
		scanBarCodeBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				//startScanBarcodeActivity();
				createDialog(getResources().getString(R.string.challengeScan),SCAN);

				// ah.createCustomToast(
				// "Este JRV ya fue procesado, si esto es un error,",
				// " por favor comuniquese con su supervisor");
			}
		});

		adminBtn = (Button) findViewById(R.id.admin_btn);
		ah.setButtonColorRed(adminBtn);
		adminBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				createDialogToConfirmDui("Ingrese su "+ getResources().getString(R.string.dui), 1);
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (db_adapter != null) {
			db_adapter.close();
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {

		super.onPause();
		if (db_adapter != null) {
			db_adapter.close();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		barcode_et.setSelection(barcode_et.getText().length());
	}

//	 public void handleDecode(Result rawResult, Bitmap barcode)
//	    {
//	    	Toast.makeText(this.getApplicationContext(), "Scanned code " + rawResult.getText(), Toast.LENGTH_LONG);
//	    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
		if(result!=null){
			if(result.getContents()==null){
				Toast.makeText(this,"CODIGO DE BARRA NO FUE LEIDO",Toast.LENGTH_SHORT).show();
			}else {
				String scannedBarcode = result.getContents();
				barcode_et.setText(scannedBarcode);
				barcode_tv.setText(scannedBarcode);
				vc.setBarcodeString(scannedBarcode);
			}
		}else{
			super.onActivityResult(requestCode,resultCode,data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.jrv, menu);
		return true;
	}

	protected void startZXingScannerActivity() {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.setPrompt(getResources().getString(R.string.scanMessage));
		integrator.setCaptureActivity(Consts.CAPTUREACT);
		integrator.initiateScan();

	}

	protected void startScanBarcodeActivity() {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.setPackage("com.google.zxing.client.android");
		intent.putExtra("SCAN_MODE", "ONE_D_MODE");
		startActivityForResult(intent, BAR_CODE_SCAN);
	}

	@Override
	public void onBackPressed() {
		Log.d("JRV Activity", "onBackPressed Called");
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
		// NOTE Trap the back key: when the CustomKeyboard is still visible hide
		// it, only when it is invisible, finish activity
/*		if (mCustomKeyboard.isCustomKeyboardVisible()) {
			mCustomKeyboard.hideCustomKeyboard();
		} else {

			Log.d("JRV Activity", "onBackPressed Called");
			Intent setIntent = new Intent(Intent.ACTION_MAIN);
			setIntent.addCategory(Intent.CATEGORY_HOME);
			setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(setIntent);
		}*/
	}

	public void createDialogToConfirmDui(String msg, int yesIndex) {
		FragmentManager fm = getFragmentManager();
		dialogToConfirmDui = new DialogToConfirmDui();
		dialogToConfirmDui.setOnButtonsClickedListenerOne(this);
		dialogToConfirmDui.setCustomKeyboard(mCustomKeyboard);
		Bundle bndl = new Bundle();
		bndl.putString("yesButtonText", "Continuar");
		bndl.putInt("yesIndex", yesIndex);
		bndl.putString("noButtonText", "No");
		bndl.putString("question", msg);
		bndl.putString("invisible", "invisible");
		dialogToConfirmDui.setArguments(bndl);
		dialogToConfirmDui.show(fm, "new triage dialog");
	}

	@Override
	public void onRequestDataSuccess(String response) {
		// TODO Auto-generated method stub
		isBarcodeMatch = response.substring(0,
				response.length() - 1);
		// Log.e("Jrv Activity jrv", response);
	}

	@Override
	public void onRequestDataError(Exception error) {
		// TODO Auto-generated method stub
		Log.e("Jrv Activity jrv", error.getMessage());
	}

	@Override
	public void onYesButtonDialogToConfirmDuiClicked(String duiNumber) {

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		Bundle bc = new Bundle();

		Intent intent = new Intent(JrvActivity.this,
				Consts.LOGINACT);
		intent.putExtras(bc);
		startActivity(intent);
		finish();

	}

	@Override
	public void onNoButtonDialogToConfirmDuiClicked() {
		// TODO Auto-generated method stub

	}

	TextWatcher tw1 = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
		}

		@SuppressLint("LongLogTag")
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (barcode_et.getText().length() > 0){
				ah.setButtonColorRed((Button)findViewById(R.id.scan_barcode_btn));
			} else {
				ah.setButtonColorGreen((Button)findViewById(R.id.scan_barcode_btn));
				ah.editTextNextFocus(barcode_et,R.id.scan_barcode_btn);
			}

			if (barcode_et.getText().length() > 12) {
				ah.setButtonColorGreen(loginBtn);
				ah.editTextNextFocus(barcode_et,loginBtn.getId());
			} else {
				ah.setButtonColorRed(loginBtn);
			}

			if((barcode_et.getText().length() < 12) && (barcode_et.getText().length() > 0)){
				ah.editTextNextFocus(barcode_et,barcode_et.getId());
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			Log.d("tw1 afterTextChanged", "x");

		}
	};

	//----------------------------- CHALLENGE ------------------------------------------------------

	public void createDialog(String msg, int yesIndex) {
		FragmentManager fm = getFragmentManager();
		twoBtnDialogFragment = new TwoButtonDialogFragment();
		twoBtnDialogFragment.setOnButtonsClickedListenerOne(this);
		Bundle bndl = new Bundle();
		bndl.putString("yesButtonText", "Si");
		bndl.putInt("yesIndex", yesIndex);
		bndl.putString("noButtonText", "No");
		bndl.putString("question", msg);
		bndl.putString("invisible", "visible");
		twoBtnDialogFragment.setArguments(bndl);
		twoBtnDialogFragment.show(fm, "new triage dialog");
	}

	@Override
	public void onYesButtonForTwoButtonDialogClicked(int caseWhichLaunchedFragment) {
		/** caseWhichLaunchedFragment is the yestIndex assigned when create dialog was created*/
		//todo start the scan activity
		startZXingScannerActivity();

	}

	@Override
	public void onNoButtonForTwoButtonDialogClickedX() {
		/** in the challenge fragment, exit the fragment */
		twoBtnDialogFragment.dismiss();
	}
	//------------------------------ END CHALLANGE -------------------------------------------------
}
