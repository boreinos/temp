package com.afilon.mayor.v11.activities;
//////////

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.adapters.ActaAdapter;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.model.ActaEntry;
import com.afilon.mayor.v11.utils.ChallengeHelper.OnApprove;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.model.AppLog;
import com.afilon.mayor.v11.model.Candidate;
import com.afilon.mayor.v11.model.CandidateMarks;
import com.afilon.mayor.v11.model.CrossVoteBundle;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.PreferentialCandidateVotes;
import com.afilon.mayor.v11.model.PreferentialPartyVotes;
import com.afilon.mayor.v11.model.PreferentialVotoBanderas;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import org.apache.commons.lang3.text.WordUtils;



//Removed implements OnTwoButtonDialogFragmentListener DialogToConfirmDuiListener  implements DataResponseCallback
public class SummaryActivity extends AfilonFragmentActivity {

    private static final String CLASS_TAG = "SummaryActivity";
    private Utilities ah;
    private Escrudata escrudata;
    private AppLog applog;
    private VotingCenter vc;
    private List<PreferentialPartyVotes> partyArrayList;
    private String escrudataMapString;
    private DatabaseAdapterParlacen db_adapter;
    private Hashtable<Integer, Integer> partyIndex;
    private ChallengeHelper challengeHelper;
    private int CONTINUE = 1;

    int partynum;

    private View parties;
    //, contents, myView;

    private Button acta_btn, accept_btn, previous_btn;

    // Party Array List Variable Used to Determine Number of Parties
    private ArrayList<Party> partyCount;
    // Coceptos and Party String Array to Lable Buttons
    private String[] partyNames, candidateNames, candidateBand, candidatePrefs, candidatePrefmarks, candidateCruz, candidateCruzmarks, candidateTotal, candidateTotalmarks, empty;
    private ArrayList<ActaEntry> entries;
    private ActaAdapter entry_adapter;
    TextView summaryText, banderasLabel, banderasVotes;

    private LinkedHashMap<String, String> valueMap;
    private ArrayList<CandidateMarks> another;
    private List<PreferentialVotoBanderas> banderasInfo;
    private List<PreferentialCandidateVotes> votesInfo;
    private List<PreferentialCandidateVotes> testInfo;
    private PreferentialVotoBanderas banderasCounts;
    private ArrayList<Candidate> candidates;
    private LinkedHashMap<String, String> conceptosValues;
    private List<PreferentialCandidateVotes> partyValues;
    private ArrayList<CandidateMarks> totalMarks;
    private ArrayList<CrossVoteBundle> another1;
    private ArrayList<CrossVoteBundle> crossvotes;
    private ArrayAdapter<String> adapter;

    View clickSource, touchSource;
    /*
        On create must populate summary window
        with information from selected party,
        party selection menu will be infalted
        in view from party data associated with
        jrv
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Super Class onCreate
        super.onCreate(savedInstanceState);

        // Utilities for Tablet Hardware Information
        ah = new Utilities(this);
        ah.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.summary_layout_singletable);
//        // Set View from Summary Layout XML
//        if(Consts.LOCALE.equals(Consts.HONDURAS)){
//            setContentView(R.layout.summary_layout_singletable);
//        }else {
//            setContentView(R.layout.summary_layout);
//        }

        ah.savePreferences("HowManyBallotSoFar", 0);

        //Catch Unexpected Errors
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(SummaryActivity.this));
        Log.i("CLASS NAME: ", CLASS_TAG);

        //Database Adapter
        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();
        //-----------------------SET UP CHALLENGE HELPER -------------------------------------------
        challengeHelper = new ChallengeHelper(this);
        challengeHelper.setTools(ah,db_adapter);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);
        challengeHelper.addRoutine(CONTINUE,continueRoutine);
        //------------------------------------------------------------------------------------------

        //Array lists for text view, edit view, radio button, and parties lists
        partyCount = new ArrayList<>();
        partyIndex = new Hashtable<>();

        //Text views for voter center, municipal, department, barcode, and JRV
        TextView votecenter_tv = (TextView) findViewById(R.id.header_text); //header for activity
        TextView municipal_tv = (TextView) findViewById(R.id.municipal_value); //municipal value
        TextView departmental_tv = (TextView) findViewById(R.id.department_value); //department value
        TextView barcode_tv = (TextView) findViewById(R.id.barcode_value); //barcode value
        TextView jrv_tv = (TextView) findViewById(R.id.jrv_value); //jrv value
        TextView summaryText = (TextView) findViewById(R.id.summaryHeading);

        // Initialize ListViews
//        final ListView candNames = (ListView) findViewById(R.id.list_table);
//        final ListView candPrefs = (ListView) findViewById(R.id.list_table_prefvotes);
//        final ListView candBand = (ListView) findViewById(R.id.list_table_bandvotes);
//        final ListView candPrefmarks = (ListView) findViewById(R.id.list_table_prefmarks);
//        final ListView candCruz = (ListView) findViewById(R.id.list_table_crossvotes);
//        final ListView candCruzmarks = (ListView) findViewById(R.id.list_table_crossmarks);
//        final ListView candTotal = (ListView) findViewById(R.id.list_table_totalvotes);
//        final ListView candMarcs = (ListView) findViewById(R.id.list_table_totalmarks);
//        final ListView blank1 = (ListView) findViewById(R.id.blank1);
//        final ListView blank2 = (ListView) findViewById(R.id.blank2);
//        final ListView blank3 = (ListView) findViewById(R.id.blank3);

//        candNames.setOnHoverListener(ignoreHover);
//        candPrefs.setOnHoverListener(ignoreHover);
//        candBand.setOnHoverListener(ignoreHover);
//        candPrefmarks.setOnHoverListener(ignoreHover);
//        candCruz.setOnHoverListener(ignoreHover);
//        candCruzmarks.setOnHoverListener(ignoreHover);
//        candTotal.setOnHoverListener(ignoreHover);
//        candMarcs.setOnHoverListener(ignoreHover);
//        blank1.setOnHoverListener(ignoreHover);
//        blank2.setOnHoverListener(ignoreHover);
//        blank3.setOnHoverListener(ignoreHover);




        // Use Bundle to Get/Pass Values Between Activities
        Bundle b = getIntent().getExtras();
        // Populate Local Variables from Bundle
        if (b != null) {
            escrudataMapString = b.getString("escrudataMap");

            ah.savePreferences("escrudataMap", escrudataMapString);
            escrudata = b.getParcelable("com.afilon.tse.escrudata");
            applog = b.getParcelable("com.afilon.assembly.applog");
            vc = b.getParcelable("com.afilon.tse.votingcenter");


            Gson gson = new Gson();
            Type entityType = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
        }

        votecenter_tv.setText(vc.getVoteCenterString());
        municipal_tv.setText(vc.getMunicipioString());
        departmental_tv.setText(vc.getDepartamentoString());
        barcode_tv.setText(ah.loadPreferencesString("barcodeSaved"));
        jrv_tv.setText(vc.getJrvString());
        summaryText.setText(" Please Select Desired Summary Report View ");


        // Get Party List to Count and Determine Number of Parties for
        //  Loading Appropriate Select Party xml
        partyCount = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());
        partynum = partyCount.size();
        banderasInfo = db_adapter.getBanderaVotesPreferential();

        populate();


        // Initialize Prvious Button as Acta Button
        previous_btn = acta_btn;

        // Set OnClickListeners for allways present Accept and Acta Buttons
        // Call Next Activity On Click Accept
        accept_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                challengeHelper.createDuiChallenge(getResources().getString(R.string.continueSummary),CONTINUE);
                  challengeHelper.createDialog(getResources().getString(R.string.continueSummary),CONTINUE);
            }
        });

        acta_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ah.setButtonColorGreen(previous_btn);
                setButtonSelected(acta_btn);
                previous_btn = acta_btn;
                loadSummaryActa();
            }
        });

//        lockScrolling(candNames, candBand, candPrefs, candPrefmarks, candCruz, candCruzmarks, candTotal, candMarcs, blank1, blank2, blank3);
        loadSummaryActa();
        setButtonSelected(acta_btn);

    }

    // Methods

    // Call Next Activity Method
    public void callNextActivity() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("com.afilon.tse.votingcenter", vc);
        bundle.putParcelable("com.afilon.tse.escrudata", escrudata);
        Intent intent = new Intent(SummaryActivity.this, Consts.CHECKLISTACT);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private OnApprove continueRoutine = new OnApprove() {
        @Override
        public void approved() {
            callNextActivity();
        }
    };

    public void populate() {
        // Loading Appropriate View for Party buttons
        LinearLayout selParty = (LinearLayout) findViewById(R.id.select_party);
        parties = getLayoutInflater().inflate(R.layout.summary_party_buttons, null);
        selParty.addView(parties);

        // Button Setup
        acta_btn = (Button) findViewById(R.id.actabtn);
        accept_btn = (Button) findViewById(R.id.accept_btn);
        acta_btn.setText(" ACTA ");
        accept_btn.setText("CONTINUAR");
        ah.buttonNextFocus(accept_btn,R.id.printBtn);
        ah.viewNextFocus(findViewById(R.id.printBtn),R.id.accept_btn);
        ah.setButtonColorGreen(accept_btn);

    }

    public void loadSummaryParty(int ID) {
        /////////////////////////
        // Required Variables ///
        /////////////////////////
        TextView labels = (TextView) findViewById(R.id.TotalMarcas);
        labels.setText("");

        int c;
        Party party = partyCount.get(partyIndex.get(ID));
        //Set text for party name as sub heading
        summaryText = (TextView) findViewById(R.id.summaryHeading);
        summaryText.setText(party.getParty_name().toUpperCase());
        // Fill text as "   Votos :  "
        banderasLabel = (TextView) findViewById(R.id.txt_Banderas);
		//banderasLabel.setText(getResources().getText(R.string.partyTotals));
        banderasLabel.setText("  Marcas : ");
        // Get vote infor for party 1
        banderasCounts = banderasInfo.get(partyIndex.get(ID));
        // Fill text view for total number of party votes
        banderasVotes = (TextView) findViewById(R.id.txt_Banderas_votes);
        banderasVotes.setText(Float.toString(banderasCounts.getParty_preferential_votes() + banderasCounts.getParty_votes() + banderasCounts.getParty_cross_votes()));
        // Fill text views for column labels
//        fillTextviewsParty();

        // for current party, load candidates
        candidates = db_adapter.getParlacenCandidatesArrayList(party.getParty_preferential_election_id());

        // Check if candidates belong to desired party
//        if (candidates.get(0).getPartyName().toUpperCase().equals(partyNames[3].toUpperCase())) {

        // Initate database adapter arrays holding pertinent data
        votesInfo = db_adapter.getPreferentialElectionCandidateVotesForThisParty(party.getParty_preferential_election_id());
        totalMarks = db_adapter.getAllCandidateMarksArrayListByParty(party.getParty_preferential_election_id());
        testInfo = db_adapter.getPreferentialElectionCandidateVotesForThisParty(party.getParty_preferential_election_id());
        another1 = db_adapter.getCrossVoteBundleArrayList(vc.getJrvString());
        crossvotes = db_adapter.getCrossVoteBundleArrayList(vc.getJrvid());

        // Initiate String arrays of required length


        c = candidates.size();
        candidateNames = new String[c];
        candidateBand = new String[c];
        candidatePrefs = new String[c];
        candidateCruz = new String[c];
        candidateCruzmarks = new String[c];
        candidateTotal = new String[c];
        candidatePrefmarks = new String[c];
        candidateTotalmarks = new String[c];
        empty = new String[c];

        // Populate String Arrays
        for (int i = 0; i < c; i++) {
            empty[i] = "";

            // Variable cv for summation of candidates crossvotes
            float[] cv = new float[c];

            // Initialize cv array
            for (int x = 0; x < c; x++) {
                cv[x] = 0;
            }

            // Check all crossvotes for candidate match
            for (int x = 0; x < crossvotes.size(); x++) {
                // increase candiadte crossvote if candidate id matches
                if (crossvotes.get(x).getCandidatePrefElecId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    cv[i] += crossvotes.get(x).getVote();
                }
            }

            // Load data into candidate crossvote string array
//            candidateCruz[i] = String.format("%.3f", cv[i]);
            candidateCruz[i] = String.format("%.0f", cv[i]);

            for (int x = 0; x < totalMarks.size(); x++) {
                if (totalMarks.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    candidateTotalmarks[i] = String.format("%.0f", (float) totalMarks.get(x).getmTotalMarks());
                    candidateTotalmarks[i] = "";
                }

            }

            ArrayList<CandidateMarks> cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "4");
            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    candidatePrefmarks[i] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
                    candidatePrefmarks[i] = "";
                }
            }

            cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "6");
            candidateCruzmarks[i] = "";

            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
//                    candidateCruzmarks[i] =  String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
//                    candidateCruz[i] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
                    candidateCruzmarks[i] = String.format("%.0f", votesInfo.get(i).getCandidate_cross_votes());
                    candidateCruz[i] = candidateCruzmarks[i];
                    candidateCruzmarks[i] = "";
                    break;
                }
                candidateCruzmarks[x] = "";
            }

            candidateNames[i] = candidates.get(i).getCandidate_name();
            float bv = votesInfo.get(i).getCandidate_bandera_votes();
//            candidateBand[i] = String.format("%.3f", bv);
//            candidatePrefs[i] = String.format("%.3f", votesInfo.get(i).getCandidate_preferential_votes());
            candidateBand[i] = String.format("%.0f", bv);
            candidatePrefs[i] = String.format("%.0f", votesInfo.get(i).getCandidate_preferential_votes());

            Float tempVal = votesInfo.get(i).getCandidate_votes();
//            candidateTotal[i] = String.format("%.3f", tempVal);
            candidateTotal[i] = String.format("%.0f", tempVal);

        }
        populateListViews();
        //}
//        if (candidates.get(0).getPartyName().toUpperCase().equals(partyNames[3].toUpperCase())) {
//            populateListViews();
//        }

    }

    //---------------------------------------------------------------------------------------

    // Load Summary View Methods
    public void loadSummaryActa() {
        String[] acta = {""};
        String[] values = {""};
        String[] values1 = {""};
        String[] band = {""};
        String[] pref = {""};
        String[] cruz = {""};
        String[] blank = {""};
        String[] empty = {""};

        fillTextviewsActa();

        partyArrayList = db_adapter.getPartiesPreferentialVotes();

        conceptosValues = db_adapter.getConceptsCountPreferential();
        partyValues = db_adapter.getPreferentialElectionCandidateVotes();

        // For individual party crossvote count
        ArrayList<PreferentialVotoBanderas> partycrossvotes = db_adapter.getParyCrossVotes();

        // For individual party bandera count
        List<PreferentialVotoBanderas> partypref = db_adapter.getBanderaVotesPreferential();

        //Debug:?
//        for (int x = 0 ; x < partyArrayList.size(); x++) {
//            Log.e("party votes", Float.toString(partyArrayList.get(x).getParty_votes()));
//            Log.e("party boletas", partyArrayList.get(x).getParty_boletas());
//            Log.e("Party pref EID", partyArrayList.get(x).getParty_preferential_election_id());
//        }

        // Get total cross vote count by looking at highest boleta number
        ArrayList<CrossVoteBundle> crossvotes = db_adapter.getCrossVoteBundleArrayList(vc.getJrvString());
        int cvnum = 0;
        for (int i = 0; i < crossvotes.size(); i++) {
            if (Integer.parseInt(crossvotes.get(i).getBoletaNo()) > cvnum) {
                cvnum = Integer.parseInt(crossvotes.get(i).getBoletaNo());
            }
        }

        TextView labels = (TextView) findViewById(R.id.Candidate);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Banderas);
//        labels.setText("Voto\nBandera");
        labels.setText("");
        labels = (TextView) findViewById(R.id.bandvotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Preferential);
//        labels.setText("Voto\nPreferential");
        labels.setText("");
        labels = (TextView) findViewById(R.id.PrefVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.PrefMarcas);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Cruzados);
//        labels.setText("Voto\nCruzado");
        labels.setText("");
        labels = (TextView) findViewById(R.id.CruzVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.CruzMarcas);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Total);
        labels.setText("Total");
        labels = (TextView) findViewById(R.id.TotalVotos);
        labels.setText("Papeletas / Votos");
        labels = (TextView) findViewById(R.id.TotalMarcas);
        labels.setText("");


        ArrayList<String> actaLabels = new ArrayList();
        ArrayList<String> valuesLabels = new ArrayList();
        Iterator iterator = conceptosValues.entrySet().iterator();
        while (iterator.hasNext()) {
            String key = "";
            String value="";
            Map.Entry pair = (Map.Entry) iterator.next();

            if (pair.getKey().toString().equals("VOTOS CRUZADOS")) {//TODO MOVE TO STRING FILE
                iterator.remove();
                continue;
            }

            if (pair.getKey().toString().equals("PAPELETAS ENTREGADAS"))//TODO MOVE TO STRING FILE
                key = "Papeletas Entregadas a Votantes";
            else{
                key = pair.getKey().toString();
                key.toLowerCase();
                key = WordUtils.capitalizeFully(key);

            }
            try{
                value = pair.getValue().toString();
            }catch (NullPointerException npe){
                Log.e("SUMMARY ACT",key+" is empty-NULL");
                npe.printStackTrace();
                iterator.remove();
                continue;
            }

            actaLabels.add(key);
            valuesLabels.add(value);
            iterator.remove();
        }
        int numberOfRows = actaLabels.size() + partyCount.size();
        acta = new String[numberOfRows];
        values = new String[numberOfRows];
        int j = partyCount.size();
        for (int i = 0; i < actaLabels.size(); i++) {
            Log.e("actaLabe at i ", "i = " + i + "Acta Label " + actaLabels.get(i));
//            if(i>1){
            if(i>1){
                acta[i+j] = actaLabels.get(i);
                values[i+j] = valuesLabels.get(i);
            }else{
                acta[i] = actaLabels.get(i);
                values[i] = valuesLabels.get(i);
            }
        }
        for(int i =0; i<partyCount.size(); i++){
//            acta[i+2]=partyCount.get(i).getParty_name();
            acta[i+2]=partyCount.get(i).getParty_name();
        }

        entries = new ArrayList<>();

        for(int index=0; index<numberOfRows;index++){
            ActaEntry entry = new ActaEntry(acta[index]);
            entry.setTotalVotes(values[index]);
            entries.add(entry);
        }

        band = new String[numberOfRows];
        pref = new String[numberOfRows];
        cruz = new String[numberOfRows];
        blank = new String[numberOfRows];
        empty = new String[numberOfRows];
        values1 = new String[numberOfRows];

        for (int i = 0; i < numberOfRows; i++) {
            band[i] = "";
            pref[i] = "";
            cruz[i] = "";
            blank[i] = "";
            empty[i] = "";
            values1[i] = "";
        }

        for (int x = 0; x < banderasInfo.size(); x++) {
            for (int i = 0; i < partycrossvotes.size(); i++) {
                if (partycrossvotes.get(i).getParty_preferential_election_id().equals(banderasInfo.get(x).getParty_preferential_election_id())) {
//                    cruz[x + 2] = String.format("%.3f", partycrossvotes.get(i).getParty_cross_votes());
                    cruz[x + 3] = String.format("%.0f", partycrossvotes.get(i).getParty_cross_votes());
                }
            }
        }

        for (int x = 0; x < banderasInfo.size(); x++) {
//            if (cruz[x + 2].equals("")) {
//                cruz[x + 2] = "0";
            if (cruz[x + 3].equals("")) {
                cruz[x + 3] = "0";
            }
        }

//        int k=2; //todo: ASSUMING THERE ARE TWO CONCEPTS PRIOR TO PARTY. DOUBLE CHECK!!
        int k=3;
        for(PreferentialVotoBanderas pvBanderas: partypref){
            Log.e("cross votes",Float.toString(pvBanderas.getParty_cross_votes()));
            Log.e("pref votes",Float.toString(pvBanderas.getParty_preferential_votes()));
            Log.e("party votes",Float.toString(pvBanderas.getParty_votes()));
            Log.e("party totals",Float.toString(pvBanderas.getPartyTotals()));
            Log.e("party boletas",pvBanderas.getParty_boletas());
            band[k]=String.format(Locale.US,"%.0f",pvBanderas.getParty_votes());
            pref[k]=String.format(Locale.US,"%.0f", pvBanderas.getParty_preferential_votes());
//            float total = Float.valueOf(band[k])+Float.valueOf(pref[k])+
//            float total = Float.valueOf(band[k]);
            float total = Float.valueOf(pvBanderas.getParty_boletas());
//            values[k]=String.format(Locale.US,"%.1f",total);
            values[k]=String.format(Locale.US,"%.0f",total);

//            entries.get(k).setTotalMarcas(values[k]);
//            entries.get(k).setTotalVotes("");
//            entries.get(k).setPlanchaMarcas(band[k]);
//            entries.get(k).setCruzadoVotes(cruz[k]);
//            entries.get(k).setParcialVotes(pref[k]);
            k++;
        }

        for(int i = 0; i < actaLabels.size() + partyCount.size() ; i++){
            Log.e("value[i]", i + values[i]);
        }
//        for(int i = 2; i < 8 ; i++){
        for(int i = 0; i < partyCount.size() ; i++){
            values[i+2] = String.format("%.0f",partyArrayList.get(i).getParty_votes());
            entries.get(i+2).setTotalVotes(values[i+2]);
//            values[i+3] = "";
        }
        for(int i = 0; i < actaLabels.size() + partyCount.size() ; i++){
            Log.e("value[i]", i + values[i]);
        }
        populateTable();
//        populateListViewsFromPassed(acta, pref, band, blank, blank, blank, values, empty, empty);
    }

    private void populateTable(){
        final ListView table = (ListView)findViewById(R.id.list_table);
        entry_adapter = new ActaAdapter(this,R.layout.acta_entry,entries);
        table.setAdapter(entry_adapter);

    }

    //Currently Selected Button Colored and Disabled
    private void setButtonSelected(Button btn) {
//        btn.setBackgroundResource(R.drawable.radiobuttongray);
        btn.setBackgroundColor(Color.parseColor("#808080"));
        btn.setEnabled(false);
        btn.setFocusable(false);

    }

    private void lockScrolling(final ListView candNames, final ListView candBand, final ListView candPrefs, final ListView candPrefmarks, final ListView candCruz, final ListView candCruzmarks, final ListView candTotal, final ListView candMarcs, final ListView blank1, final ListView blank2, final ListView blank3) {
        final int offset = 0;
        blank1.setFocusable(false);
        blank2.setFocusable(false);
        blank3.setFocusable(false);
        blank1.setFocusableInTouchMode(false);
        blank2.setFocusableInTouchMode(false);
        blank3.setFocusableInTouchMode(false);
        blank1.setEnabled(false);
        blank2.setEnabled(false);
        blank3.setEnabled(false);

        candNames.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;
//                Log.e("ON TOUCH",event.toString());
//                ah.createCustomToast(event.toString());

                if (v == touchSource) {
                    candBand.dispatchTouchEvent(event);
                    candPrefs.dispatchTouchEvent(event);
                    candPrefmarks.dispatchTouchEvent(event);
                    candCruz.dispatchTouchEvent(event);
                    candCruzmarks.dispatchTouchEvent(event);
                    candTotal.dispatchTouchEvent(event);
                    candMarcs.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }
                return false;
            }
        });

        candNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candNames.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candBand.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruz.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruzmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candTotal.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candMarcs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank1.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank2.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank3.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        candPrefs.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;

                if (v == touchSource) {
                    candBand.dispatchTouchEvent(event);
                    candNames.dispatchTouchEvent(event);
                    candPrefmarks.dispatchTouchEvent(event);
                    candCruz.dispatchTouchEvent(event);
                    candCruzmarks.dispatchTouchEvent(event);
                    candTotal.dispatchTouchEvent(event);
                    candMarcs.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }

                return false;
            }
        });

        candPrefs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candPrefs.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candBand.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candNames.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruz.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruzmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candTotal.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candMarcs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank1.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank2.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank3.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        candBand.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;

                if (v == touchSource) {
                    candPrefs.dispatchTouchEvent(event);
                    candNames.dispatchTouchEvent(event);
                    candPrefmarks.dispatchTouchEvent(event);
                    candCruz.dispatchTouchEvent(event);
                    candCruzmarks.dispatchTouchEvent(event);
                    candTotal.dispatchTouchEvent(event);
                    candMarcs.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }

                return false;
            }
        });

        candBand.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candBand.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candPrefs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candNames.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruz.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruzmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candTotal.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candMarcs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank1.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank2.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank3.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        candPrefmarks.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;

                if (v == touchSource) {
                    candPrefs.dispatchTouchEvent(event);
                    candNames.dispatchTouchEvent(event);
                    candBand.dispatchTouchEvent(event);
                    candCruz.dispatchTouchEvent(event);
                    candCruzmarks.dispatchTouchEvent(event);
                    candTotal.dispatchTouchEvent(event);
                    candMarcs.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }

                return false;
            }
        });

        candPrefmarks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candPrefmarks.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candPrefs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candNames.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candBand.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruz.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruzmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candTotal.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candMarcs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank1.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank2.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank3.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        candCruz.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;

                if (v == touchSource) {
                    candPrefs.dispatchTouchEvent(event);
                    candNames.dispatchTouchEvent(event);
                    candBand.dispatchTouchEvent(event);
                    candPrefmarks.dispatchTouchEvent(event);
                    candCruzmarks.dispatchTouchEvent(event);
                    candTotal.dispatchTouchEvent(event);
                    candMarcs.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }

                return false;
            }
        });

        candCruz.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candCruz.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candPrefs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candNames.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candBand.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruzmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candTotal.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candMarcs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank1.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank2.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank3.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        candCruzmarks.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;

                if (v == touchSource) {
                    candPrefs.dispatchTouchEvent(event);
                    candNames.dispatchTouchEvent(event);
                    candBand.dispatchTouchEvent(event);
                    candPrefmarks.dispatchTouchEvent(event);
                    candCruz.dispatchTouchEvent(event);
                    candTotal.dispatchTouchEvent(event);
                    candMarcs.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }

                return false;
            }
        });

        candCruzmarks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candCruzmarks.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candPrefs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candNames.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candBand.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruz.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candTotal.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candMarcs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank1.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank2.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank3.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        candTotal.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;

                if (v == touchSource) {
                    candPrefs.dispatchTouchEvent(event);
                    candNames.dispatchTouchEvent(event);
                    candBand.dispatchTouchEvent(event);
                    candPrefmarks.dispatchTouchEvent(event);
                    candCruz.dispatchTouchEvent(event);
                    candCruzmarks.dispatchTouchEvent(event);
                    candMarcs.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }

                return false;
            }
        });

        candTotal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candTotal.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candPrefs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candNames.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candBand.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruz.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruzmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candMarcs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank1.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank2.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        blank3.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });


        candMarcs.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSource == null)
                    touchSource = v;

                if (v == touchSource) {
                    candPrefs.dispatchTouchEvent(event);
                    candNames.dispatchTouchEvent(event);
                    candBand.dispatchTouchEvent(event);
                    candPrefmarks.dispatchTouchEvent(event);
                    candCruz.dispatchTouchEvent(event);
                    candCruzmarks.dispatchTouchEvent(event);
                    candTotal.dispatchTouchEvent(event);
                    blank1.dispatchTouchEvent(event);
                    blank2.dispatchTouchEvent(event);
                    blank3.dispatchTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        clickSource = v;
                        touchSource = null;
                    }
                }

                return false;
            }
        });

        candMarcs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent == clickSource) {
                    // Do something with the ListView was clicked
                }
            }
        });

        candMarcs.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view == clickSource) {
                    if (view.getChildAt(0) != null) {
                        candPrefs.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candNames.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candBand.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candPrefmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruz.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candCruzmarks.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                        candTotal.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + offset);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });
    }

    private void populateListViews() {

        final ListView candNames = (ListView) findViewById(R.id.list_table);
        final ListView candPrefs = (ListView) findViewById(R.id.list_table_prefvotes);
        final ListView candBand = (ListView) findViewById(R.id.list_table_bandvotes);
        final ListView candPrefmarks = (ListView) findViewById(R.id.list_table_prefmarks);
        final ListView candCruz = (ListView) findViewById(R.id.list_table_crossvotes);
        final ListView candCruzmarks = (ListView) findViewById(R.id.list_table_crossmarks);
        final ListView candTotal = (ListView) findViewById(R.id.list_table_totalvotes);
        final ListView candMarcs = (ListView) findViewById(R.id.list_table_totalmarks);
        final ListView blank1 = (ListView) findViewById(R.id.blank1);
        final ListView blank2 = (ListView) findViewById(R.id.blank2);
        final ListView blank3 = (ListView) findViewById(R.id.blank3);


        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candNames.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidatePrefs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candPrefs.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateBand) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candBand.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, candidatePrefmarks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candPrefmarks.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateCruz) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candCruz.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, candidateCruzmarks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candCruzmarks.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateTotal) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candTotal.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, candidateTotalmarks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candMarcs.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, empty) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        blank1.setAdapter(adapter);
        blank2.setAdapter(adapter);
        blank3.setAdapter(adapter);
    }

    private void populateListViewsFromPassed(String[] candidateN, String[] candidateP, String[] candidateB, String[] candidatePm, String[] candidateC, String[] candidateCm, String[] candidateT, String[] candidateTm, String[] empty) {
        final ListView candNames = (ListView) findViewById(R.id.list_table);
        final ListView candPrefs = (ListView) findViewById(R.id.list_table_prefvotes);
        final ListView candBand = (ListView) findViewById(R.id.list_table_bandvotes);
        final ListView candPrefmarks = (ListView) findViewById(R.id.list_table_prefmarks);
        final ListView candCruz = (ListView) findViewById(R.id.list_table_crossvotes);
        final ListView candCruzmarks = (ListView) findViewById(R.id.list_table_crossmarks);
        final ListView candTotal = (ListView) findViewById(R.id.list_table_totalvotes);
        final ListView candMarcs = (ListView) findViewById(R.id.list_table_totalmarks);
        final ListView blank1 = (ListView) findViewById(R.id.blank1);
        final ListView blank2 = (ListView) findViewById(R.id.blank2);
        final ListView blank3 = (ListView) findViewById(R.id.blank3);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateN) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candNames.setAdapter(adapter);
        candNames.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateP) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candPrefs.setAdapter(adapter);
        candPrefs.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateB) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candBand.setAdapter(adapter);
        candBand.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, candidatePm) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candPrefmarks.setAdapter(adapter);
        candPrefmarks.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateC) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candCruz.setAdapter(adapter);
        candCruz.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, candidateCm) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candCruzmarks.setAdapter(adapter);
        candCruzmarks.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_row, candidateT) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candTotal.setAdapter(adapter);
        candTotal.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, candidateTm) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        candMarcs.setAdapter(adapter);
        candMarcs.setFocusable(false);

        adapter = new ArrayAdapter<String>(this, R.layout.summary_right_align, empty) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#e2eefb"));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };

        blank1.setAdapter(adapter);
        blank1.setFocusable(false);
        blank2.setAdapter(adapter);
        blank2.setFocusable(false);
        blank3.setAdapter(adapter);
        blank3.setFocusable(false);
    }

    private void fillTextviewsParty() {
        // Fill text views for labels of list view columns
        TextView labels = (TextView) findViewById(R.id.Candidate);
//        SpannableString content = new SpannableString("Candidate");
//        content.setSpan(new UnderlineSpan(),0,content.length(),0);
//        labels.setText(content);
        labels.setText("Candidate");
//        labels.setPaintFlags(labels.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        labels = (TextView) findViewById(R.id.Banderas);
//        SpannableString content = new SpannableString("Bandera___");
        SpannableString content = new SpannableString("Plancha___");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        labels.setText(content);
//        labels.setText("Bandera");
        labels = (TextView) findViewById(R.id.bandvotos);
//        labels.setText("Votos");
        labels.setText("Marcas");
        labels = (TextView) findViewById(R.id.Preferential);
//        content = new SpannableString("___Preferential___");
        content = new SpannableString("_____Parcial______");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        labels.setText(content);
//        labels.setText("Preferential");
        labels = (TextView) findViewById(R.id.PrefVotos);
//        labels.setText("Votos");
        labels.setText("Marcas");
        labels = (TextView) findViewById(R.id.PrefMarcas);
//        labels.setText("Marcas");
        labels.setText("");
        labels = (TextView) findViewById(R.id.Cruzados);
//        content = new SpannableString("____Cruzados_____");
        content = new SpannableString("____Cruzadas_____");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//        content.setSpan(new UnderlineSpan(),0,content.length(),0);
        labels.setText(content);
//        labels.setText("Cruzados");
        labels = (TextView) findViewById(R.id.CruzVotos);
//        labels.setText("Votos");
        labels.setText("Marcas");
        labels = (TextView) findViewById(R.id.CruzMarcas);
//        labels.setText("Marcas");
        labels.setText("");
        labels = (TextView) findViewById(R.id.Total);
        content = new SpannableString("______Total______");
//        content = new SpannableString("Total             ");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        labels.setText(content);
//        labels.setText("Total");
        labels = (TextView) findViewById(R.id.TotalVotos);
//        labels.setText("Votos");
        labels.setText("Marcas");
        labels = (TextView) findViewById(R.id.TotalMarcas);
//        labels.setText("Marcas");
        labels.setText("");
    }

    private void fillTextviewsActa() {
        summaryText = (TextView) findViewById(R.id.summaryHeading);
        summaryText.setText(" ACTA ");

        banderasLabel = (TextView) findViewById(R.id.txt_Banderas);
        banderasLabel.setText("");

        banderasVotes = (TextView) findViewById(R.id.txt_Banderas_votes);
        banderasVotes.setText("");
    }

    View.OnHoverListener ignoreHover = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            touchSource = v;
            clickSource = v;
            return true;
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