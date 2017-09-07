package com.afilon.mayor.v11.activities;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.model.CrossVoteBundle;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ReclamosActivity extends AfilonActivity {

	private static final String CLASS_TAG = "ReclamosActivity";
	private VotingCenter vc;
	private Escrudata escrudata;
	private DatabaseAdapterParlacen db_adapter;
	private Utilities ah= new Utilities(this);
	private LinkedHashMap<String, String> escrudataMap;
	private ArrayList<Party> partyArrayList;
	private int partyArrayIndex;
	boolean firstTime = true;
	private ArrayList<CrossVoteBundle> CrossVoteB = new ArrayList<CrossVoteBundle>();

	@SuppressLint("LongLogTag")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ah.tabletConfiguration(Build.MODEL,this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_reclamos);

		Log.i("CLASS NAME : ", CLASS_TAG);

		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
				ReclamosActivity.this));

		ah.savePreferences("HowManyBallotSoFar", 0);
		ah.savePreferences("ballotNumber",0);

		TextView votecenter_tv = (TextView) findViewById(R.id.vote_center);
		TextView municipio_tv = (TextView) findViewById(R.id.textView13);
		TextView departamento_tv = (TextView) findViewById(R.id.textView15);
		TextView barcode_tv = (TextView) findViewById(R.id.textView23);
		TextView jvr_tv = (TextView) findViewById(R.id.textView25);

		Bundle b = getIntent().getExtras();
		vc = b.getParcelable("com.afilon.tse.votingcenter");
		escrudata = b.getParcelable("com.afilon.tse.escrudata");
		setButtonColorAmber((Button)findViewById(R.id.aceptar_btn));


		CrossVoteB = null; //= db_adapter.getCrossVoteBundleArrayList(vc.getJrvString());

		votecenter_tv.setText(vc.getVoteCenterString());
		municipio_tv.setText(vc.getMunicipioString());
		departamento_tv.setText(vc.getDepartamentoString());
		barcode_tv.setText(ah.loadPreferencesString("barcodeSaved"));
		jvr_tv.setText(vc.getJrvString());

		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(this));


		String escrudataMapString = ah.loadPreferencesString("escrudataMap");
		ah.savePreferences("currentBallotNumber", 0);

		Gson gson = new Gson();
		Type entityType = new TypeToken<LinkedHashMap<String, String>>() {
		}.getType();

		escrudataMap = gson.fromJson(escrudataMapString, entityType);

		//CARLOS: Testing LinkedHashMap values
		for (Map.Entry<String, String> par : escrudataMap.entrySet()) {
			String partyKey = par.getKey();
			String partyVal = par.getValue();
			Log.e("partyKey escrudataMap = gson.fromJson(escrudataMapString...", partyKey);
			Log.e("partyVal escrudataMap = gson.fromJson(escrudataMapString...", partyVal);
		}

//		if(escrudataMap != null) {
//			Log.e("escrudataMap size", String.valueOf(escrudataMap.size()));
//		} else {
//			Log.e("escrudataMap is null", "true");
//		}

		db_adapter = new DatabaseAdapterParlacen(this);
		db_adapter.open();

		partyArrayList = db_adapter.getParlacenPartiesArrayList(vc
				.getPref_election_id());

		partyArrayIndex = 0;

		Log.e("Print Election ID", vc.getPref_election_id().toString());
//		Log.e("Print Event Locality ID", vc.getEvent1_locality_id().toString());

		escrudata.setActaImageLink(vc.getPref_election_id());

		Set set = escrudataMap.entrySet();
		Iterator i = set.iterator();
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			for (Party party : partyArrayList) {
				if (party.getParty_name().toUpperCase().equals(me.getKey())) {
					party.setParty_votes(me.getValue().toString());
					//MOVED HERE
					if(!(Consts.LOCALE.equals(Consts.HONDURAS) && getResources().getString(R.string.voteType).equals(Consts.DIRECT))){
						long rowId = db_adapter.insertPartiesPreferentialVotes(party, vc.getJrvString());

						Log.e("insertPartiesPreferentialVotes rowID", String.valueOf(rowId));

						Log.e("insertPartiesPreVotes ", party.getParty_name()
								+ "-->" + party.getParty_votes()
								+ " vc.getJrvString " + vc.getJrvString());
					}

				} else {
					Log.e("MATCH DOES NOT EXIST!!!", "true");
				}
				//MOVED UP!
				//db_adapter.insertPartiesPreferentialVotes(party, vc.getJrvString());
				//Log.i("insertPartiesPreVotes ", party.getParty_name() + "-->" + party.getParty_votes());

			}
		}

		//CARLOS:
		for (Party par : partyArrayList) {
			Log.e("Party getPref_election_id", par.getPref_election_id().toString());
			Log.e("Party getId", par.getId().toString());
			Log.e("Party getParty_name", par.getParty_name().toString());
			Log.e("Party getParty_preferential_election_id", par.getParty_preferential_election_id().toString());
			Log.e("Party getParty_votes", par.getParty_votes() != null ? par.getParty_votes().toString() : "NULL");
		}

		db_adapter.close();


		final EditText reclamos = (EditText) findViewById(R.id.reclamos_et);
		reclamos.addTextChangedListener(new CustomTextWatcher(reclamos,(Button) findViewById(R.id.aceptar_btn)));

//				new View.OnKeyListener() {
//			@Override
//			public boolean onKey(View view, int i, KeyEvent keyEvent) {
//				if(firstTime) {
//					Button saveBtn = (Button) findViewById(R.id.aceptar_btn);
//					setButtonColorGreen(saveBtn);
//					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) saveBtn.getLayoutParams();
//					params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//					saveBtn.setLayoutParams(params);
//					firstTime=false;
//				}
//
//				return false;
//			}
//		});

		Button nextBtn = (Button) findViewById(R.id.aceptar_btn);
		nextBtn.setOnClickListener(new OnClickListener() {

			@SuppressLint("LongLogTag")
			public void onClick(View v) {

				escrudata.setReclamos(reclamos.getText().toString());

				if(partyArrayList != null) {
					Log.e("size of partyArrayList ", String.valueOf(partyArrayList.size()));
				} else {
					Log.e("size of partyArrayList is NULL", "true");
				}


				String voteType = getResources().getString(R.string.voteType);
				if (partyArrayList.get(0).getParty_votes().equals("0")) {
					Bundle bndl = new Bundle();
					bndl.putParcelable("com.afilon.tse.votingcenter", vc);
					bndl.putParcelable("com.afilon.tse.escrudata", escrudata);
					bndl.putInt("partyNumber", partyArrayIndex);
					Intent search;

					switch (voteType){
						case "DIRECT":
							search = new Intent(ReclamosActivity.this, Consts.SUMACT);
							search.putExtras(bndl);
							startActivity(search);
							break;
						case "PREFERENTIAL":
							search = new Intent(ReclamosActivity.this, Consts.CANDLISTACT);
							search.putExtras(bndl);
							startActivity(search);
							break;
					}
					finish();


				} else {
					Log.i("nextBtn getParty_votes NOT equals to 0", "");
					Bundle b = new Bundle();
					b.putParcelable("com.afilon.tse.votingcenter", vc);
					b.putParcelable("com.afilon.tse.escrudata", escrudata);
					b.putInt("partyNumber", partyArrayIndex);
					b.putString("currentJrv", vc.getJrvString());
					b.putParcelable("crossVoteBundle", (Parcelable) CrossVoteB);
					Intent search;

					switch (voteType){
						case "DIRECT":
							search = new Intent(ReclamosActivity.this,
									Consts.SUMACT);
							search.putExtras(b);
							startActivity(search);
							break;
						case "PREFERENTIAL":
							if (Consts.LOCALE.contains("HON")){
								search = new Intent(ReclamosActivity.this, Consts.CANDLISTACT);
							}else search = new Intent(ReclamosActivity.this, Consts.VOTETABLEACT);
							search.putExtras(b);
							startActivity(search);
							break;
					}
					ah.savePreferences("NewpartyNumber", partyArrayIndex);

//					Intent search = new Intent(ReclamosActivity.this,
//							CrossedVoteActivity.class);
//					search.putExtras(b);
//					startActivity(search);
					finish();
				}
			}
		});

		nextBtn.performClick();
	}

	public void onBackPressed() {
		Log.d("Reclamos Activity", "onBackPressed Called");
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.third, menu);
		return true;
	}
	private void setButtonColorGreen(Button btn) {
		btn.setBackgroundResource(R.drawable.green_button_selector);
	}

	private void setButtonColorRed(Button btn) {
		btn.setBackgroundResource(R.drawable.red_button_selector);
		btn.setEnabled(false);
	}

	private void setButtonColorAmber(Button btn) {
		btn.setBackgroundResource(R.drawable.amber_button_selector);
	}
	private class CustomTextWatcher implements TextWatcher {
		private EditText mEditText;
		private Button mButton;

		public CustomTextWatcher(EditText e, Button button) {
			mEditText = e;
			mButton = button;
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
			if (s.length() > 2) {
				RelativeLayout.LayoutParams params =(RelativeLayout.LayoutParams) mButton.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);
				params.setMargins(20,150,20,20);
				mButton.setLayoutParams(params);
				setButtonColorGreen(mButton);
			}
		}

		public void afterTextChanged(Editable s) {
		}
	}

}
