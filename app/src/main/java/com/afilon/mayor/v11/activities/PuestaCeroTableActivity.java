package com.afilon.mayor.v11.activities;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.EmptyListArrayAdapter;
import com.afilon.mayor.v11.model.AppLog;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.google.gson.Gson;

public class PuestaCeroTableActivity extends AfilonListActivity {

	private static final String CLASS_TAG = "PuestaCeroTableActivity";

	private String voteCenterString;
	private String municipioString;
	private String departamentoString;
	private String barcodeString;
	private String jvrString;

	private VotingCenter votingCenter;
	private Button aceptarBtn;
	private String submitterOne;

	private Escrudata escrudata;
	private AppLog applog;
	private DatabaseAdapterParlacen db_adapter;
	private LinkedHashMap<String, String> valuesMap;
	private Activity mActivity;
	private Utilities utilities = new Utilities(this);
	private Button puestaceroBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		utilities.tabletConfiguration(Build.MODEL,this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_empty_table);

		//Catch Unexpected Error:
		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
				PuestaCeroTableActivity.this));

		TextView votecenter_tv = (TextView) findViewById(R.id.vote_center);
		TextView municipio_tv = (TextView) findViewById(R.id.textView13);
		TextView departamento_tv = (TextView) findViewById(R.id.textView15);
		TextView barcode_tv = (TextView) findViewById(R.id.textView23);
		TextView jvr_tv = (TextView) findViewById(R.id.textView25);
		
		

		Bundle b = getIntent().getExtras();
		votingCenter = b.getParcelable("com.afilon.tse.votingcenter");
		escrudata = b.getParcelable("com.afilon.tse.escrudata");
		utilities.saveCurrentScreen(this.getClass(),b);
//		applog = b.getParcelable("com.afilon.assembly.applog");
//		submitterOne = b.getString("submitterOne");

		voteCenterString = votingCenter.getVoteCenterString();
		municipioString = votingCenter.getMunicipioString();
		departamentoString = votingCenter.getDepartamentoString();
        barcodeString = utilities.loadPreferencesString("barcodeSaved");
		jvrString = votingCenter.getJrvString();

		votecenter_tv.setText(voteCenterString);
		municipio_tv.setText(municipioString);
		departamento_tv.setText(departamentoString);
		barcode_tv.setText(barcodeString);
		jvr_tv.setText(jvrString);

		db_adapter = new DatabaseAdapterParlacen(this);
		db_adapter.open();

		// Log.e("Empty table activity PEID",
		// votingCenter.getPref_election_id());

		String[] activeConceptos = db_adapter
				.getConceptosAndParties(votingCenter.getPref_election_id());

		valuesMap = new LinkedHashMap<String, String>();
		for (String string : activeConceptos) {
			valuesMap.put(string.toUpperCase(), "0");
		}

		ArrayList<Party> upperedList = new ArrayList<Party>();

		boolean skip = false;
		for (int i = 0; i < activeConceptos.length; i++) {
			Log.e("activeConceptos",activeConceptos[i].toUpperCase());
			if(Consts.LOCALE.contains("HON")){
//			if(Consts.LOCALE.contains("HON")&&getResources().getString(R.string.voteType).equals("PREFERENTIAL")){

				if(activeConceptos[i].toUpperCase().equals("UTILIZADAS")){
					skip = !skip;
				}
				if (!skip) {
					Party party = new Party();
					if(activeConceptos[i].toUpperCase().equals("CRUZADOS")){
						party.setParty_name("VOTOS");
					}else party.setParty_name(activeConceptos[i].toUpperCase());
					if(i == 0){
						party.setParty_votes(escrudata.getPapeletasTotal());
					}else party.setParty_votes("0");
					upperedList.add(party);
				}
				if (activeConceptos[i].toUpperCase().contains("SOBRANTES")||activeConceptos[i].toUpperCase().contains("VOTANTES")) {
					skip = !skip;
				}

			}else {
				if(Consts.LOCALE.contains("HON")&& i == 0){
					Party party = new Party();
					party.setParty_name(activeConceptos[i].toUpperCase());
					party.setParty_votes(escrudata.getPapeletasTotal());
					upperedList.add(party);
				}else {
					Party party = new Party();
					party.setParty_name(activeConceptos[i].toUpperCase());
					party.setParty_votes("0");
					upperedList.add(party);
				}
			}
		}

		// CARLOS:
		mActivity = this;
		EmptyListArrayAdapter adapter = new EmptyListArrayAdapter(this,
				upperedList);

		View view = getLayoutInflater().inflate(R.layout.empty_text_view, null);
		TextView oneMoreLineToListView = (TextView) view
				.findViewById(R.id.empty_text_view);

		getListView().addFooterView(oneMoreLineToListView);
		setListAdapter(adapter);

        

		aceptarBtn = (Button) findViewById(R.id.aceptar_btn);
		utilities.setButtonColorGreen(aceptarBtn);
		aceptarBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Gson gson = new Gson();
				String list = gson.toJson(valuesMap);
				escrudata.setValuMap(list);
				Bundle b = new Bundle();
				b.putParcelable("com.afilon.tse.votingcenter", votingCenter);
				b.putParcelable("com.afilon.tse.escrudata", escrudata);
//				b.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
//				b.putString("submitterOne", submitterOne);
//				b.putString("escrudataMap", list);

				Intent search = new Intent(PuestaCeroTableActivity.this,
						Consts.CONCEPTOACT);
				search.putExtras(b);
				startActivity(search);
				finish();
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


	public void onBackPressed() {
		Log.d("EmptyTable Activity", "onBackPressed Called");
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fifth, menu);
		return true;
	}

}
