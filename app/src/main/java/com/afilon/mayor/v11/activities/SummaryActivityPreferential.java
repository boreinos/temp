package com.afilon.mayor.v11.activities;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.adapters.ActaAdapter;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.model.ActaEntry;
import com.afilon.mayor.v11.model.ActaSumaryReport;
import com.afilon.mayor.v11.utils.ChallengeHelper.OnApprove;
import com.afilon.mayor.v11.model.AppLog;
import com.afilon.mayor.v11.model.Candidate;
import com.afilon.mayor.v11.model.CandidateMarks;
import com.afilon.mayor.v11.model.CrossVoteBundle;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.PreferentialCandidateVotes;
import com.afilon.mayor.v11.model.PreferentialVotoBanderas;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.universalprintservice.PrintServiceXP;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import org.apache.commons.lang3.text.WordUtils;

import static com.afilon.mayor.v11.utils.Consts.LOCALE;

/**
 * Created by BReinosa on 4/14/2017.
 */
public class SummaryActivityPreferential extends FragmentActivity {
    private static final String CLASS_TAG = "SummaryActivity";
    private boolean isAccepted;
    private boolean isDebugMode = false;
    private Utilities ah;
    private Escrudata escrudata;
    private AppLog applog;
    private VotingCenter vc;
    private String escrudataMapString;
    private DatabaseAdapterParlacen db_adapter;
    private Hashtable<Integer, Integer> partyIndex;
    private ChallengeHelper challengeHelper;
    private int CONTINUE = 1;

    private List<String> partiesName = new ArrayList<>();
    private List<String> partiesTotalMarcas = new ArrayList<>();
    private List<String> partiesPlanchaMarcas = new ArrayList<>();
    private List<String> partiesParcialVotes = new ArrayList<>();
    private List<String> partiesCruzadoVotes = new ArrayList<>();
    private List<String> partiesOther = new ArrayList<>();
    private String partyList[] = null;

    private ActaSumaryReport actaSumaryReport =
            new ActaSumaryReport("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                    partiesName, partiesTotalMarcas, partiesPlanchaMarcas, partiesParcialVotes,
                    partiesCruzadoVotes, partiesOther);

    int partynum;

    private View parties;
    //, contents, myView;

    private Button acta_btn, accept_btn, previous_btn;

    // Party Array List Variable Used to Determine Number of Parties
    private ArrayList<Party> partyCount;
    // Coceptos and Party String Array to Lable Buttons
    private String[] partyNames, candidateNames, candidateBand, candidatePrefs, candidatePrefmarks, candidateCruz, candidateCruzmarks, candidateTotal, candidateTotalmarks, empty;
    private ArrayList<ActaEntry> entries;
    TextView summaryText, banderasLabel, banderasVotes;

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
    private ActaAdapter entry_adapter;
    private int lineNumber = 0;
    private ArrayList<ActaEntry> partyEntries;

    View clickSource, touchSource;
    /*
        On create must populate summary window
        with information from selected party,
        party selection menu will be infalted
        in view from party data associated with
        jrv
     */

    public void doPrint(View view) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = this.getString(R.string.app_name) + " Document";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        printManager.print(jobName, new PrintAdapter(this, Consts.LOCALE, actaSumaryReport),
                null); //
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Super Class onCreate
        super.onCreate(savedInstanceState);

        // Utilities for Tablet Hardware Information
        ah = new Utilities(this);
        ah.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set View from Summary Layout XML
//        if(Consts.LOCALE.equals(Consts.HONDURAS)){
//            setContentView(R.layout.summary_layout_singletable);
//        }else {
//            setContentView(R.layout.summary_layout);
//        }

        setContentView(R.layout.summary_layout_singletable);
        //Catch Unexpected Errors
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(SummaryActivityPreferential.this));
        Log.i("CLASS NAME: ", CLASS_TAG);

        //Database Adapter
        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();
        //-----------------------SET UP CHALLENGE HELPER -------------------------------------------
        challengeHelper = new ChallengeHelper(this);
        challengeHelper.setTools(ah,db_adapter);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);
        challengeHelper.addRoutine(CONTINUE,continueRoutine);

        //Array lists for text view, edit view, radio button, and parties lists
        partyCount = new ArrayList<Party>();
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
//
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
//
//        // just hide the blank views:
//        blank1.setVisibility(View.GONE);
//        blank2.setVisibility(View.GONE);
//        blank3.setVisibility(View.GONE);

        // Use Bundle to Get/Pass Values Between Activities
        Bundle b = getIntent().getExtras();
        // Populate Local Variables from Bundle
        if (b != null) {
//            escrudataMapString = b.getString("escrudataMap");

//            ah.savePreferences("escrudataMap", escrudataMapString);
            escrudata = b.getParcelable("com.afilon.tse.escrudata");
//            applog = b.getParcelable("com.afilon.mayor.applog");
            vc = b.getParcelable("com.afilon.tse.votingcenter");

            Gson gson = new Gson();
            Type entityType = new TypeToken<LinkedHashMap<String, String>>() {
            }.getType();
        }

        ah.saveCurrentScreen(this.getClass(),b);

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

        // Array for Party Names to Lable Party Select Buttons
        partyNames = db_adapter.getConceptosAndParties(vc.getPref_election_id());

        // banderasInfo variable of type List<PreferentialVotoBanderas>
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
                if(LOCALE.contains("HON")){
                    loadSummaryActaHon();
                }else loadSummaryActa();
            }
        });

//        lockScrolling(candNames, candBand, candPrefs, candPrefmarks, candCruz, candCruzmarks, candTotal, candMarcs, blank1, blank2, blank3);
        if(LOCALE.contains("HON")){
            loadSummaryActaHon();
        }else loadSummaryActa();
        setButtonSelected(acta_btn);

    }

    // Methods

    // Call Next Activity Method
    public void callNextActivity() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("com.afilon.tse.votingcenter", vc);
        bundle.putParcelable("com.afilon.tse.escrudata", escrudata);
//        bundle.putParcelable("com.afilon.mayor.applog", applog);
        Intent intent = new Intent(SummaryActivityPreferential.this, Consts.CHECKLISTACT);
//        Intent intent = new Intent(SummaryActivityPreferential.this, ReclamosActivity.class);
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

        ah.setButtonColorGreen(accept_btn);
        //allow arrow buttons
        findViewById(R.id.leftArrow).setVisibility(View.VISIBLE);
        findViewById(R.id.rightArrow).setVisibility(View.VISIBLE);
        findViewById(R.id.leftArrow).setOnClickListener(leftArrow());
        findViewById(R.id.rightArrow).setOnClickListener(rightArrow());
        findViewById(R.id.sv_party_btns).setOnTouchListener(scrollListener());


        int i = 0;
        for (Party party : partyCount) {
            int viewID = party.getParty_preferential_election_id().hashCode();
            Button party_btn = (Button) getLayoutInflater().inflate(R.layout.styled_button, null);
            LayoutParams layoutParams = new LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getResources().getDisplayMetrics()), ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            layoutParams.setMargins(margin, 0, margin, 0);
            party_btn.setLayoutParams(layoutParams);
            party_btn.setId(viewID);
            partyIndex.put(viewID, i);
            party_btn.setText(party.getParty_name().toUpperCase());
            ah.setButtonColorGreen(party_btn);
            party_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    ah.setButtonColorGreen(previous_btn);
                    setButtonSelected((Button) view);
                    previous_btn = (Button) view;
                    if(LOCALE.contains("HON")){
                        loadSummaryPartyHon(view.getId());
                    }else loadSummaryParty(view.getId());
                }
            });
            ((LinearLayout) findViewById(R.id.linear_layout_partybtns)).addView(party_btn);


            i++;
        }

        //findViewById(R.id.leftArrow).setEnabled(false);
        setButtonSelected((Button)findViewById(R.id.leftArrow));
        findViewById(R.id.leftArrow).setPadding(3,0,3,0);
        if (partyCount.size() > 4) {
            ah.setButtonColorGreen((Button)findViewById(R.id.rightArrow));
            findViewById(R.id.rightArrow).setPadding(3,0,3,0);
        } else {
            setButtonSelected((Button)findViewById(R.id.rightArrow));
            findViewById(R.id.rightArrow).setPadding(3,0,3,0);
        }


    }

    public void loadSummaryPartyHon(int ID) {
        /////////////////////////
        // Required Variables ///
        /////////////////////////

        int c;
        Party party = partyCount.get(partyIndex.get(ID));
        //Set text for party name as sub heading
        summaryText = (TextView) findViewById(R.id.summaryHeading);
        summaryText.setText(party.getParty_name().toUpperCase());
        // Fill text as "   Votos :  "
        banderasLabel = (TextView) findViewById(R.id.txt_Banderas);
        if(LOCALE.contains("HON")){
            banderasLabel.setText("  Marcas : ");
        }else {
            banderasLabel.setText("  Votos : ");
        }
        // Get vote infor for party 1
        banderasCounts = banderasInfo.get(partyIndex.get(ID));
        // Fill text view for total number of party votes
        banderasVotes = (TextView) findViewById(R.id.txt_Banderas_votes);
        if(LOCALE.contains("HON")){
            banderasVotes.setText("");
        }else banderasVotes.setText(String.format("%.5f",banderasCounts.getParty_preferential_votes() + banderasCounts.getParty_votes() + banderasCounts.getParty_cross_votes()));
        // Fill text views for column labels
        fillTextviewsParty();

        // for current party, load candidates
        if(!db_adapter.isOpen()) db_adapter.open();
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
        entries = new ArrayList<>();
        for(Candidate candidate: candidates){
            ActaEntry entry = new ActaEntry(candidate.getCandidate_name());
            entries.add(entry);
        }

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
            candidateCruz[i] = String.format("%.5f", cv[i]);

//            for (int x = 0; x < totalMarks.size(); x++) {
//                if (totalMarks.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
//                    candidateTotalmarks[i] = String.format("%.0f", (float) totalMarks.get(x).getmTotalMarks());
//                }
//
//            }

            ArrayList<CandidateMarks> cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "4");
            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    candidatePrefmarks[i] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
                }
            }

            cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "6");
            candidateCruzmarks[i] = "0";


            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    candidateCruzmarks[i] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
//                    candidateCruzmarks[i] = String.format("%d", db_adapter.getCandMark(candidates.get(i).getCandidatePreferentialElectionID(),"6"));
                    break;
                }
            }

//            candidateCruzmarks[i] = String.format("%d", db_adapter.getCandMark(candidates.get(i).getCandidatePreferentialElectionID(),"6"));
            candidateNames[i] = candidates.get(i).getCandidate_name();

            candidateBand[i] = String.format("%d", db_adapter.getCandMark(candidates.get(i).getCandidatePreferentialElectionID(),"5"));


            int total = Integer.valueOf(candidateBand[i])+Integer.valueOf(candidatePrefmarks[i])+Integer.valueOf(candidateCruzmarks[i]);
            candidateTotalmarks[i] = String.format(Locale.US, "%d", total); ;

            entries.get(i).setCruzadoMarcas(candidateCruzmarks[i]);
            entries.get(i).setPlanchaMarcas(candidateBand[i]);
            entries.get(i).setTotalMarcas(candidateTotalmarks[i]);
            entries.get(i).setParcialMarcas(candidatePrefmarks[i]);

            candidatePrefs[i] = "";
            candidateTotal[i] = "";
            candidateCruz[i] = "";
        }

        int totes = 0;
        for (int x = 0; x < candidateTotalmarks.length; x++) {
            totes += Integer.parseInt(candidateTotalmarks[x]);
        }
        banderasVotes.setText(String.format(Locale.US, "%d", totes));
        populateTable();
//        populateListViews();
        //}
//        if (candidates.get(0).getPartyName().toUpperCase().equals(partyNames[3].toUpperCase())) {
//            populateListViews();
//        }
    }

    public void loadSummaryParty(int ID) {
        /////////////////////////
        // Required Variables ///
        /////////////////////////

        int c;
        Party party = partyCount.get(partyIndex.get(ID));
        //Set text for party name as sub heading
        summaryText = (TextView) findViewById(R.id.summaryHeading);
        summaryText.setText(party.getParty_name().toUpperCase());
        // Fill text as "   Votos :  "
        banderasLabel = (TextView) findViewById(R.id.txt_Banderas);
        if(LOCALE.contains("HON")){
            banderasLabel.setText("  Marcas : ");
        }else {
            banderasLabel.setText("  Votos : ");
        }
        // Get vote infor for party 1
        banderasCounts = banderasInfo.get(partyIndex.get(ID));
        // Fill text view for total number of party votes
        banderasVotes = (TextView) findViewById(R.id.txt_Banderas_votes);
        if(LOCALE.contains("HON")){
            banderasVotes.setText("");
        }else banderasVotes.setText(String.format("%.5f",banderasCounts.getParty_preferential_votes() + banderasCounts.getParty_votes() + banderasCounts.getParty_cross_votes()));
        // Fill text views for column labels
        fillTextviewsParty();

        // for current party, load candidates
        if(!db_adapter.isOpen()) db_adapter.open();
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
        entries = new ArrayList<>();
        for(Candidate candidate: candidates){
            ActaEntry entry = new ActaEntry(candidate.getCandidate_name());
            entries.add(entry);
        }




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
            candidateCruz[i] = String.format("%.5f", cv[i]);
            int totes = 0;

            for (int x = 0; x < totalMarks.size(); x++) {
                if (totalMarks.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    candidateTotalmarks[i] = String.format("%.0f", (float) totalMarks.get(x).getmTotalMarks());
                }
            }

            ArrayList<CandidateMarks> cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "4");
            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    candidatePrefmarks[i] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
                }
            }

            cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "6");
            candidateCruzmarks[i] = "0";

            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(i).getCandidatePreferentialElectionID())) {
                    candidateCruzmarks[i] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
                    break;
                }
            }

            candidateNames[i] = candidates.get(i).getCandidate_name();
            float bv = votesInfo.get(i).getCandidate_bandera_votes();
            Float tempVal = votesInfo.get(i).getCandidate_votes();


            if(LOCALE.contains("HON")) {
//                candidateBand[i] = String.format("%.1f", partyValues.get(i).getCandidate_bandera_votes() * c);  //This needs to be number of Bandeera Marks
//                Log.e("getCandBandVotes", String.format("%.1f", partyValues.get(i).getCandidate_votes()));
//                candidateBand[i] = String.format("%.0f", partyValues.get(i).getCandidate_votes()*c);
                for(int x =0; x < candidates.size(); x++){
                    Log.i("XXXXXXXXXXXXXXXXXXXXXXX","XXXXXXXXXXXXXXXXXXXXXX");
                    Log.e("candidates prefElecId", candidates.get(x).getCandidatePreferentialElectionID());
                    Log.e("party      prefElecId", party.getParty_preferential_election_id());
                    Log.e("cand get band marks", String.format("%d", candidates.get(x).getBanderaMarks()));
                    Log.i("XXXXXXXXXXXXXXXXXXXXXXX","XXXXXXXXXXXXXXXXXXXXXX");
                    if(candidates.get(x).getCandidatePreferentialElectionID().contains(party.getParty_preferential_election_id())){
                        candidateBand[i] = String.format("%d", candidates.get(x).getBanderaMarks());
                    }
                }
                candidatePrefs[i] = "";
                candidateTotal[i] = "";
                candidateCruz[i] = "";
            } else {
                candidateBand[i] = String.format("%.5f", bv);
                candidatePrefs[i] = String.format("%.5f", votesInfo.get(i).getCandidate_preferential_votes());
                candidateTotal[i] = String.format("%.5f", tempVal);
            }
            entries.get(i).setPlanchaMarcas(candidateBand[i]);//bandera
            entries.get(i).setParcialMarcas(candidatePrefmarks[i]);// preferential
            entries.get(i).setCruzadoMarcas(candidateCruzmarks[i]);//cross marks
            entries.get(i).setTotalMarcas(candidateTotalmarks[i]);
//            entries.get(i).setPlanchaVotes();
            entries.get(i).setParcialVotes(candidatePrefs[i]);
            entries.get(i).setCruzadoVotes(candidateCruz[i]);
            entries.get(i).setTotalVotes(candidateTotal[i]);

        }
        populateTable();
//        populateListViews();
        //}
//        if (candidates.get(0).getPartyName().toUpperCase().equals(partyNames[3].toUpperCase())) {
//            populateListViews();
//        }

    }

    //---------------------------------------------------------------------------------------
    // Load Summary View Methods
    @SuppressLint("LongLogTag")
    public void loadSummaryActa() {
        String[] acta = {""};
        String[] values = {""};
        String[] band = {""};
        String[] pref = {""};
        String[] cruz = {""};
        String[] blank = {""};
        String[] empty = {""};
        String voto = "";
        boolean isHon = false;
        if(LOCALE.contains("HON")){
            voto = "Marca";
            isHon = true;
        } else voto = "Voto";

        fillTextviewsActa();

        conceptosValues = db_adapter.getConceptsCountPreferential();
        partyValues = db_adapter.getPreferentialElectionCandidateVotes();

        // For individual party crossvote count
        ArrayList<PreferentialVotoBanderas> partycrossvotes = db_adapter.getParyCrossVotes();

        // For individual party bandera count
        List<PreferentialVotoBanderas> partypref = db_adapter.getBanderaVotesPreferential();

        //Debug:?
//        for (int i = 0; i < partypref.size(); i++) {
//            Log.i("Value count ", "i = " + i);
//            Log.e("get ", " party cross votes =   " + partypref.get(i).getParty_cross_votes());
//            Log.e("get ", " party votes       =   " + partypref.get(i).getParty_votes());
//            Log.e("get ", " party pref votes  =   " + partypref.get(i).getParty_preferential_votes());
//            Log.e("get ", " party boletas     =   " + partypref.get(i).getParty_boletas());
//            Log.e("get ", " part band pref id =   " + partypref.get(i).getBandera_preferential_election_id());
//            Log.e("get ", " party pref elec id=   " + partypref.get(i).getParty_preferential_election_id());
//            banderasCounts = banderasInfo.get(i);
//            Log.e("get ", " bandinfo pref id  =   " + banderasCounts.getBandera_preferential_election_id());
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
        labels.setText(voto+"\n"+getResources().getString(R.string.flagvote));
        labels = (TextView) findViewById(R.id.bandvotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Preferential);
        labels.setText(voto+"\n"+getResources().getString(R.string.prefvote));
        labels = (TextView) findViewById(R.id.PrefVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.PrefMarcas);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Cruzados);
        labels.setText(voto+"\nCruzado");
        labels = (TextView) findViewById(R.id.CruzVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.CruzMarcas);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Total);
        labels.setText("Total");
        labels = (TextView) findViewById(R.id.TotalVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.TotalMarcas);
        labels.setText("");

        //Debug:?
//        Log.e("Value of", "conceptosValues(SOBRANTES)    " + conceptosValues.get("SOBRANTES"));
//        Log.e("Value of", "conceptosValues(Inutilizadas)    " + conceptosValues.get("INUTILIZADAS"));
//        Log.e("Value of", "conceptosValues(VOTOTS CRUZADOS)    " + conceptosValues.get("VOTOS CRUZADOS"));
//        Log.e("Value of", "conceptosValues(IMPUGNADOS)    " + conceptosValues.get("IMPUGNADOS"));
//        Log.e("Value of", "conceptosValues(NULOS)    " + conceptosValues.get("NULOS"));
//        Log.e("Value of", "conceptosValues(ABSTENCIONES)    " + conceptosValues.get("ABSTENCIONES"));
//        Log.e("Value of", "conceptosValues(TOTAL PAPELETAS ESCRUTADAS)    " + conceptosValues.get("TOTAL PAPELETAS ESCRUTADAS"));
//        Log.e("Value of", "conceptosValues(PAPELETAS FALTANTES)    " + conceptosValues.get("PAPELETAS FALTANTES"));
//        Log.e("Value of", "conceptosValues(PAPELETAS ENTREGADAS)    " + conceptosValues.get("PAPELETAS ENTREGADAS"));
//        Log.e("Value of", "conceptosValues(PAPELETAS RECIBIDAS)    " + conceptosValues.get("PAPELETAS RECIBIDAS"));


        ArrayList<String> actaLabels = new ArrayList();
        ArrayList<String> valuesLabels = new ArrayList();
        Iterator iterator = conceptosValues.entrySet().iterator();
        while (iterator.hasNext()) {
            String key = "";
            String value="";
            Map.Entry pair = (Map.Entry) iterator.next();
            if (pair.getKey().toString().equals("VOTOS CRUZADOS")||pair.getKey().toString().equals("PAPELETAS RECIBIDAS")||pair.getKey().toString().equals("PREFERENTIAL ELECTION ID")) {//TODO MOVE TO STRING FILE
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
        int numberOfRows =actaLabels.size()+partyCount.size();
        acta = new String[numberOfRows];
        values = new String[numberOfRows];
        int j = partyCount.size();
        for (int i = 0; i < actaLabels.size(); i++) {
            if(i>1){
                acta[i+j] = actaLabels.get(i);
                values[i+j] = valuesLabels.get(i);
            }else{
                acta[i] = actaLabels.get(i);
                values[i] = valuesLabels.get(i);
            }
        }
        for(int i =0; i<partyCount.size(); i++){
            acta[i + 2] = partyCount.get(i).getParty_name();
//            acta[i] = partyCount.get(i).getParty_name();
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

        for (int i = 0; i < numberOfRows; i++) {
            band[i] = "";
            pref[i] = "";
            cruz[i] = "";
            blank[i] = "";
            empty[i] = "";
        }

        for (int x = 0; x < banderasInfo.size(); x++) {
            for (int i = 0; i < partycrossvotes.size(); i++) {
                if (partycrossvotes.get(i).getParty_preferential_election_id().equals(banderasInfo.get(x).getParty_preferential_election_id())) {
                    if(isHon){
                        cruz[x + 2] = String.format(Locale.US, "%.0f", partycrossvotes.get(i).getParty_cross_votes());
                    }else {
                        cruz[x + 2] = String.format(Locale.US, "%.5f", partycrossvotes.get(i).getParty_cross_votes());
                    }
                }
            }
        }

        for (int x = 0; x < banderasInfo.size(); x++) {
            if (cruz[x + 2].equals("")) {
                cruz[x + 2] = "0";
            }
        }

        int k=2; //todo: ASSUMING THERE ARE TWO CONCEPTS PRIOR TO PARTY. DOUBLE CHECK!!

        for(PreferentialVotoBanderas pvBanderas: partypref){

            band[k] = String.format(Locale.US, "%.5f", pvBanderas.getParty_votes());
            pref[k] = String.format(Locale.US, "%.5f", pvBanderas.getParty_preferential_votes());

            float total = Float.valueOf(band[k])+Float.valueOf(pref[k])+Float.valueOf(cruz[k]);
            values[k] = String.format(Locale.US, "%.5f", total);
            entries.get(k).setTotalVotes(values[k]);
            entries.get(k).setTotalMarcas("");
            entries.get(k).setPlanchaMarcas(band[k]);
            entries.get(k).setCruzadoVotes(cruz[k]);
            entries.get(k).setParcialVotes(pref[k]);

            k++;
        }

        //CARLOS: Populating Report for Acta ELSA

        int counter = 0;
        int iPartyQty = partyCount.size();
//        String partyList[] = new String[iPartyQty];
        partyList = new String[iPartyQty];
        List<String[]> pName = new ArrayList<>();
        for (Party p : partyCount) {
            partyList[counter] = p.getParty_name();
            counter++;
        }
        pName.add(partyList);

        partyEntries = new ArrayList<>();


        for (ActaEntry ae : entries) {

            if (ae.getEntryName().equals("Sobrantes")) {
                actaSumaryReport.setConceptA(ae.getTotalVotes());
            }
            if (ae.getEntryName().equals("Inutilizadas")) {
                actaSumaryReport.setConceptB(ae.getTotalVotes());
            }
            if (ae.getEntryName().equals("Impugnados")) {
                actaSumaryReport.setConceptC(ae.getTotalVotes());
            }
            if (ae.getEntryName().equals("Abstenciones")) {
                actaSumaryReport.setConceptD(ae.getTotalVotes());
            }
            if (ae.getEntryName().equals("Total Papeletas Escrutadas")) {
                actaSumaryReport.setConceptE(ae.getTotalVotes());
            }
            if (ae.getEntryName().equals("Papeletas Faltantes")) {
                actaSumaryReport.setConceptF(ae.getTotalVotes());
            }
            if (ae.getEntryName().equals("Papeletas Entregadas a Votantes")) {
                actaSumaryReport.setConceptG(ae.getTotalVotes());
            }
            if (ae.getEntryName().equals("Nulos")) {
                actaSumaryReport.setConceptH(ae.getTotalVotes());
            }


            Log.e("CC:entries ae.getEntryName()", ae.getEntryName() != null ? ae.getEntryName() : "Empty");
            Log.e("CC:entries ae.getTotalVotes()", ae.getTotalVotes() != null ? ae.getTotalVotes() : "Empty");
            Log.e("CC:entries ae.getPlanchaVotes()", ae.getPlanchaVotes() != null ? ae.getPlanchaVotes() : "Empty");
            Log.e("CC:entries ae.getParcialVotes()", ae.getParcialVotes() != null ? ae.getParcialVotes() : "empty");
            Log.e("CC:entries ae.getCruzadoVotes()", ae.getCruzadoVotes() != null ? ae.getCruzadoVotes() : "Empty");
            Log.e("CC:entries ae.getTotalMarcas()", ae.getTotalMarcas() != null ? ae.getTotalMarcas() : "Empty");
            Log.e("CC:entries ae.getPlanchaMarcas()", ae.getPlanchaMarcas() != null ? ae.getPlanchaMarcas() : "Empty");
            Log.e("CC:entries ae.getParcialMarcas()", ae.getParcialMarcas() != null ? ae.getParcialMarcas() : "Empty");
            Log.e("CC:entries ae.getCruzadoMarcas()", ae.getCruzadoMarcas() != null ? ae.getCruzadoMarcas() : "Empty");

        }
        populateTable();
//        populateListViewsFromPassed(acta, pref, band, blank, cruz, blank, values, blank, empty);
    }

    public void loadSummaryActaHon() {
        String[] acta = {""};
        String[] values = {""};
        String[] band = {""};
        String[] pref = {""};
        String[] cruz = {""};
        String[] blank = {""};
        String[] empty = {""};
        String voto = "";

        voto = "Marca";


        fillTextviewsActa();

        conceptosValues = db_adapter.getConceptsCountPreferential();
        partyValues = db_adapter.getPreferentialElectionCandidateVotes();

        // For individual party crossvote count
        ArrayList<PreferentialVotoBanderas> partycrossvotes = db_adapter.getParyCrossVotes();

        // For individual party bandera count
        List<PreferentialVotoBanderas> partypref = db_adapter.getBanderaVotesPreferential();

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
        labels.setText(voto+"\n"+getResources().getString(R.string.flagvote));
        labels = (TextView) findViewById(R.id.bandvotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Preferential);
        labels.setText(voto+"\n"+getResources().getString(R.string.prefvote));
        labels = (TextView) findViewById(R.id.PrefVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.PrefMarcas);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Cruzados);
        labels.setText(voto+"\nCruzada");
        labels = (TextView) findViewById(R.id.CruzVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.CruzMarcas);
        labels.setText("");
        labels = (TextView) findViewById(R.id.Total);
        labels.setText("Total");
        labels = (TextView) findViewById(R.id.TotalVotos);
        labels.setText("");
        labels = (TextView) findViewById(R.id.TotalMarcas);
        labels.setText("");

        ArrayList<String> actaLabels = new ArrayList();
        ArrayList<String> valuesLabels = new ArrayList();
        Iterator iterator = conceptosValues.entrySet().iterator();
        while (iterator.hasNext()) {
            String key = "";
            String value="";
            Map.Entry pair = (Map.Entry) iterator.next();
            if (pair.getKey().toString().equals("VOTOS CRUZADOS")||pair.getKey().toString().equals("PAPELETAS RECIBIDAS")) {//TODO MOVE TO STRING FILE
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
        int numberOfRows =actaLabels.size()+partyCount.size();
        acta = new String[numberOfRows];
        values = new String[numberOfRows];
        int j = partyCount.size();
        for (int i = 0; i < actaLabels.size(); i++) {
            if(i>1){
                acta[i+j] = actaLabels.get(i);
                values[i+j] = valuesLabels.get(i);
            }else{
                acta[i] = actaLabels.get(i);
                values[i] = valuesLabels.get(i);
            }
        }
        for(int i =0; i<partyCount.size(); i++){
            acta[i + 2] = partyCount.get(i).getParty_name();
//            acta[i] = partyCount.get(i).getParty_name();
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

        for (int i = 0; i < numberOfRows; i++) {
            band[i] = "";
            pref[i] = "";
            cruz[i] = "";
            blank[i] = "";
            empty[i] = "";
        }

        for (int x = 0; x < banderasInfo.size(); x++) {
            for (int i = 0; i < partycrossvotes.size(); i++) {
                if (partycrossvotes.get(i).getParty_preferential_election_id().equals(banderasInfo.get(x).getParty_preferential_election_id())) {
                    cruz[x + 2] = String.format(Locale.US, "%.0f", partycrossvotes.get(i).getParty_cross_votes());
                }
            }
        }

        for (int x = 0; x < banderasInfo.size(); x++) {
            if (cruz[x + 2].equals("")) {
                cruz[x + 2] = "0";
            }
        }

        int k=2; //todo: ASSUMING THERE ARE TWO CONCEPTS PRIOR TO PARTY. DOUBLE CHECK!!
        float prefmark = 0, plancha = 0;
        if (!db_adapter.isOpen()) db_adapter.open();
//        for(PreferentialVotoBanderas pvBanderas: partypref){
        for(Party party : partyCount) {
//                candidates = db_adapter.getParlacenCandidatesArrayList(party.getParty_preferential_election_id());
//                ArrayList<CandidateMarks> cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "4");

//                for(int x = 0; x < candidates.size(); x++){
//                    Log.e("cvm party id", cvm.get(x).getmPartyId());
//                    Log.e("pvBanderas pref elec id", pvBanderas.getParty_preferential_election_id());
//                    if(cvm.get(x).getmPartyId().contains(pvBanderas.getParty_preferential_election_id())) {
//                        Log.e("getmtotalmarks", String.format("%d",cvm.get(x).getmTotalMarks()));
//                        prefmark = prefmark + (float) cvm.get(x).getmTotalMarks();
//                    }
//                    if(partyValues.get(x).getParty_preferential_election_id().contains(pvBanderas.getParty_preferential_election_id())){
////                        && partyValues.get(x).getParty_preferential_election_id().contains(pvBanderas.getParty_preferential_election_id())  party.getParty_preferential_election_id()
//                        plancha = plancha + partyValues.get(x).getCandidate_votes();
//                    }
//                }
//                plancha = plancha - prefmark;
            Log.e("Party prefelec ID", party.getParty_preferential_election_id());

            band[k] = String.format(Locale.US, "%d", db_adapter.getPartyMark(party.getParty_preferential_election_id(), "5"));
            pref[k] = String.format(Locale.US, "%d", db_adapter.getPartyMark(party.getParty_preferential_election_id(), "4"));
//                plancha = 0;
//            }

//            prefmark = Float.valueOf(cruz[k]) - Float.valueOf(pref[k]) - Float.valueOf(band[k]);
            cruz[k] = String.format("%d",db_adapter.getPartyMark(party.getParty_preferential_election_id(), "6"));
            int total = Integer.valueOf(band[k])+Integer.valueOf(pref[k])+Integer.valueOf(cruz[k]);
            values[k] = String.format(Locale.US, "%d", total);

            entries.get(k).setTotalMarcas(values[k]);
            entries.get(k).setTotalVotes("");
            entries.get(k).setPlanchaMarcas(band[k]);
            entries.get(k).setCruzadoVotes(cruz[k]);
            entries.get(k).setParcialVotes(pref[k]);

//            prefmark = 0;
//            plancha = 0;
            k++;
        }

        //CARLOS: Populating Report for Acta

        int counter = 0;
        int iPartyQty = partyCount.size();
//        String partyList[] = new String[iPartyQty];
        partyList = new String[iPartyQty];
        List<String[]> pName = new ArrayList<>();
        for (Party p : partyCount) {
            partyList[counter] = p.getParty_name();
            counter++;
        }
        pName.add(partyList);

        partyEntries = new ArrayList<>();


        for (ActaEntry ae : entries) {

            if(ae.getEntryName().equals("Sobrantes")) { actaSumaryReport.setConceptA(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("Utilizadas")) { actaSumaryReport.setConceptB(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("Ciudadanos")) { actaSumaryReport.setConceptC(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("Mer")) { actaSumaryReport.setConceptD(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("Total Votantes")) { actaSumaryReport.setConceptE(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("Votos Validos")) { actaSumaryReport.setConceptF(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("En Blanco")) { actaSumaryReport.setConceptG(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("Nulos")) { actaSumaryReport.setConceptH(ae.getTotalVotes()); }
            if(ae.getEntryName().equals("Gran Total")) { actaSumaryReport.setConceptI(ae.getTotalVotes()); }

//
        }

        populateTable();
//        populateListViewsFromPassed(acta, pref, band, blank, cruz, blank, values, blank, empty);
    }

    private int getTotalMarcas(){
        int marks = 0;
        int c = 0;

        //Iterate through each party
        for (int party = 0; party < partyCount.size(); party++) {

            Party pty = partyCount.get(party);
            if(!db_adapter.isOpen()) db_adapter.open();
            candidates = db_adapter.getParlacenCandidatesArrayList(pty.getParty_preferential_election_id());

            c = candidates.size();
            candidateBand = new String[c];
            candidatePrefmarks = new String[c];
            candidateCruzmarks = new String[c];

            candidateBand[party] = String.format("%d", db_adapter.getCandMark(candidates.get(party).getCandidatePreferentialElectionID(),"5"));

            ArrayList<CandidateMarks> cvm = db_adapter.getCandidateMarksArrayListByParty(pty.getParty_preferential_election_id(), "4");
            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(party).getCandidatePreferentialElectionID())) {
                    candidatePrefmarks[party] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
                }
            }

            cvm = db_adapter.getCandidateMarksArrayListByParty(pty.getParty_preferential_election_id(), "6");
            candidateCruzmarks[party] = "0";

            for (int x = 0; x < cvm.size(); x++) {
                if (cvm.get(x).getmCandidateId().equals(candidates.get(party).getCandidatePreferentialElectionID())) {
                    candidateCruzmarks[party] = String.format("%.0f", (float) cvm.get(x).getmTotalMarks());
                    break;
                }
            }

            //Iterate through each candidate
            for(int cand = 0; cand < c; cand++) {
                marks += Integer.valueOf(candidateBand[cand])+Integer.valueOf(candidatePrefmarks[cand])+Integer.valueOf(candidateCruzmarks[cand]);
            }
        }

        return marks;
    }

    private OnClickListener leftArrow() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                //((HorizontalScrollView)findViewById(R.id.sv_party_btns)).arrowScroll(HorizontalScrollView.LAYOUT_DIRECTION_LTR);
                ((HorizontalScrollView) findViewById(R.id.sv_party_btns)).smoothScrollBy(-140, 0);
                int left = ((HorizontalScrollView) findViewById(R.id.sv_party_btns)).getMaxScrollAmount();
                int current = (findViewById(R.id.sv_party_btns)).getScrollX();

                if (current < 10) {
                    setButtonSelected((Button)v);
                    v.setPadding(3,0,3,0);
                }
                if (current >= left - 15) {
                    setButtonSelected((Button)findViewById(R.id.rightArrow));
                    findViewById(R.id.rightArrow).setPadding(3,0,3,0);

                } else {
                    ah.setButtonColorGreen((Button)findViewById(R.id.rightArrow));
                    findViewById(R.id.rightArrow).setPadding(3,0,3,0);
                }
                Log.e("LEFT", String.valueOf(left) + ", " + String.valueOf(current));
            }
        };
    }

    private OnClickListener rightArrow() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                //((HorizontalScrollView)findViewById(R.id.sv_party_btns)).arrowScroll(HorizontalScrollView.LAYOUT_DIRECTION_RTL);
                ((HorizontalScrollView) findViewById(R.id.sv_party_btns)).smoothScrollBy(140, 0);
                int right = ((HorizontalScrollView) findViewById(R.id.sv_party_btns)).getMaxScrollAmount();
                int current = findViewById(R.id.sv_party_btns).getScrollX();
                Log.e("RIGHT", String.valueOf(right) + ", " + String.valueOf(current));
                if (current < 10) {
                    setButtonSelected((Button) findViewById(R.id.leftArrow));
                    findViewById(R.id.leftArrow).setPadding(3,0,3,0);
                } else {
                    ah.setButtonColorGreen((Button) findViewById(R.id.leftArrow));
                    findViewById(R.id.leftArrow).setPadding(3,0, 3,0);
                }
                if (current >= right - 15) {
                    setButtonSelected((Button) v);
                }
            }
        };
    }

    private OnTouchListener scrollListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int scrollx;
                int max;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        scrollx = v.getScrollX();
                        max = ((HorizontalScrollView) v).getMaxScrollAmount();
                        Log.e("!!!SCROLL!!!", String.valueOf(max) + ", " + String.valueOf(scrollx));
                        if (scrollx < 10) {
                            setButtonSelected((Button) findViewById(R.id.leftArrow));
                            findViewById(R.id.leftArrow).setPadding(3, 0, 3, 0);
                        } else
                            ah.setButtonColorGreen((Button) findViewById(R.id.leftArrow));
                        findViewById(R.id.leftArrow).setPadding(3, 0, 3, 0);
                        if (scrollx >= max - 32) {
                            setButtonSelected((Button) findViewById(R.id.rightArrow));
                        } else {
                            ah.setButtonColorGreen((Button) findViewById(R.id.rightArrow));
                            findViewById(R.id.rightArrow).setPadding(3, 0, 3, 0);
                        }
                        break;
                    default:
                        break;

                }
                return false;
            }
        };
    }

    //Currently Selected Button Colored and Disabled
    private void setButtonSelected(Button btn) {
//        btn.setBackgroundResource(R.drawable.radiobuttongray);
        btn.setBackgroundColor(Color.parseColor("#808080"));
        btn.setEnabled(false);
        btn.setFocusable(false);

    }
    private void populateTable(){
        final ListView table = (ListView)findViewById(R.id.list_table);
        entry_adapter = new ActaAdapter(this,R.layout.acta_entry,entries);
        table.setAdapter(entry_adapter);

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
//        candBand.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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

        adapter = new ArrayAdapter<String>(this, R.layout.summary_layout_center, candidateBand) {
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
//        candBand.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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
        String votos, votos1, marcas;
        if(LOCALE.contains("HON")){
            votos = "";
            votos1 = "Marcas";
            marcas = "Marcas";
        }else {
            votos = "Votos";
            votos1 = "Votos";
            marcas = "Marcas";
        }
        TextView labels = (TextView) findViewById(R.id.Candidate);
//        SpannableString content = new SpannableString("Candidate");
//        content.setSpan(new UnderlineSpan(),0,content.length(),0);
//        labels.setText(content);
        labels.setText("Candidate");
//        labels.setPaintFlags(labels.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        labels = (TextView) findViewById(R.id.Banderas);
        SpannableString content = new SpannableString("   "+getResources().getString(R.string.flagvote)+"   ");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        labels.setText(content);
//        labels.setText("Bandera");
        labels = (TextView) findViewById(R.id.bandvotos);
        labels.setText(votos1);
        labels = (TextView) findViewById(R.id.Preferential);
        content = new SpannableString("___"+getResources().getString(R.string.prefvote)+"___");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        labels.setText(content);
//        labels.setText("Preferential");
        labels = (TextView) findViewById(R.id.PrefVotos);
        labels.setText(votos);
        labels = (TextView) findViewById(R.id.PrefMarcas);
        labels.setText(marcas);
        labels = (TextView) findViewById(R.id.Cruzados);
        if(LOCALE.contains("HON")){
            content = new SpannableString("____Cruzadas_____");
        }else content = new SpannableString("____Cruzados_____");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//        content.setSpan(new UnderlineSpan(),0,content.length(),0);
        labels.setText(content);
//        labels.setText("Cruzados");
        labels = (TextView) findViewById(R.id.CruzVotos);
        labels.setText(votos);
        labels = (TextView) findViewById(R.id.CruzMarcas);
        labels.setText(marcas);
        labels = (TextView) findViewById(R.id.Total);
        content = new SpannableString("______Total______");
//        content = new SpannableString("Total             ");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        labels.setText(content);
//        labels.setText("Total");
        labels = (TextView) findViewById(R.id.TotalVotos);
        labels.setText(votos);
        labels = (TextView) findViewById(R.id.TotalMarcas);
        labels.setText(marcas);
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
    private void setArrowButtonGray(Button btn){
        btn.setPadding(3,0,3,0);
    }
    private void setArrowButtonGreen(Button btn){
        btn.setPadding(5,5,5,5);
    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus){
        if(!hasfocus){
            findViewById(R.id.button_layout).setVisibility(View.INVISIBLE);
        }else{
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
        }

    }


    //CARLOS: 2017-08-23
    //PRINTING WIFI CAPABILITY
//    public class PrintAdapter extends PrintDocumentAdapter {
      public class PrintAdapter extends PrintServiceXP {
        private Context context;
        private String electionType = Consts.LOCALE;
        private ActaSumaryReport asm;
        private int pageHeight;
        private int pageWidth;
        public PdfDocument myPdfDocument;
        public int totalpages = 2;


        public PrintAdapter(Context context, Object key, Object value) {
            super(context, key, value);
            this.electionType = key.toString();
            this.asm = (ActaSumaryReport) value;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle metadata) {

            myPdfDocument = new PrintedPdfDocument(context, newAttributes);

            pageHeight =
                    newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
            pageWidth =
                    newAttributes.getMediaSize().getWidthMils() / 1000 * 72;

            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            if (totalpages > 0) {
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                        .Builder("print_output.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(totalpages);

                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            } else {
                callback.onLayoutFailed("Page count is zero.");
            }
        }



        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal cancellationSignal,
                            final WriteResultCallback callback) {

            for (int i = 0; i < totalpages; i++) {
                if (pageInRange(pageRanges, i)) {
                    PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth,
                            pageHeight, i).create();

                    PdfDocument.Page page =
                            myPdfDocument.startPage(newPage);

                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        myPdfDocument.close();
                        myPdfDocument = null;
                        return;
                    }
                    drawPage(page, i);
                    myPdfDocument.finishPage(page);
                }
            }

            try {
                myPdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                myPdfDocument.close();
                myPdfDocument = null;
            }

            callback.onWriteFinished(pageRanges);

        }

        private boolean pageInRange(PageRange[] pageRanges, int page) {
            for (int i = 0; i < pageRanges.length; i++) {
                if ((page >= pageRanges[i].getStart()) &&
                        (page <= pageRanges[i].getEnd()))
                    return true;
            }
            return false;
        }


        private void drawPage(PdfDocument.Page page,
                              int pagenumber) {
            Canvas canvas = page.getCanvas();

            pagenumber++; // Make sure page numbers start at 1

            int titleBaseLine = 72;
            int leftMargin = 54;
            String SpacingColumn = "                    ";
            lineNumber = 110;
            int np = 0;
            String datet = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            canvas.drawText(
                    "TSE - CONGRESO ",
                    leftMargin,
                    titleBaseLine,
                    paint);

            paint.setTextSize(14);
            canvas.drawText("ACTA DE CIERRE ", leftMargin, titleBaseLine + 35, paint);
            canvas.drawText(datet, leftMargin + 200, titleBaseLine + 35, paint);


            PdfDocument.PageInfo pageInfo = page.getInfo();

            paint.setColor(Color.BLACK);
            paint.setTextSize(12);
            canvas.drawText("CONCEPTO          TOTAL          Marca/Plancha          Marca/Parcial          Marca/Cruzada",
                    leftMargin, titleBaseLine + (35 * 2), paint);
            canvas.drawText("======================================================================================",
                    leftMargin, titleBaseLine + (90), paint);

            canvas.drawText("Sobrantes", leftMargin, titleBaseLine + (lineNumber), paint);
            canvas.drawText(this.asm.getConceptA(), leftMargin + 100, titleBaseLine + (lineNumber), paint);

            if(this.electionType.contains("HON")) {
                canvas.drawText("Utilizadas", leftMargin, titleBaseLine + (lineNumber+20), paint);
            } else {
                canvas.drawText("Inutilizadas", leftMargin, titleBaseLine + (lineNumber+20), paint);
            }
            canvas.drawText(this.asm.getConceptB(), leftMargin + 100, titleBaseLine + (lineNumber+20), paint);

            lineNumber = lineNumber + 20;
            for (ActaEntry ae : entries) {
                if(findList(partyList, ae.getEntryName())) {
                    canvas.drawText(ae.getEntryName(), leftMargin, titleBaseLine + (lineNumber), paint);
                    canvas.drawText(ae.getTotalMarcas(), leftMargin + 100, titleBaseLine + (lineNumber), paint);
                    canvas.drawText(ae.getPlanchaMarcas(), leftMargin + 200, titleBaseLine + (lineNumber), paint);
                    canvas.drawText(ae.getParcialVotes(), leftMargin + 300, titleBaseLine + (lineNumber), paint);
                    canvas.drawText(ae.getCruzadoVotes(), leftMargin + 400, titleBaseLine + (lineNumber), paint);
                    np = lineNumber;
                }
                lineNumber+=20;
            }

            if(this.electionType.contains("HON")){
                canvas.drawText("Ciudadanos", leftMargin, titleBaseLine + (np + 20), paint);
                canvas.drawText(this.asm.getConceptC(), leftMargin + 100, titleBaseLine + (np + 20), paint);
                canvas.drawText("MER", leftMargin, titleBaseLine + (np + 40), paint);
                canvas.drawText(this.asm.getConceptD(), leftMargin + 100, titleBaseLine + (np + 40), paint);
                canvas.drawText("Total Votantes", leftMargin, titleBaseLine + (np + 60), paint);
                canvas.drawText(this.asm.getConceptE(), leftMargin + 100, titleBaseLine + (np + 60), paint);
                canvas.drawText("Votos Validos", leftMargin, titleBaseLine + (np + 80), paint);
                canvas.drawText(this.asm.getConceptF(), leftMargin + 100, titleBaseLine + (np + 80), paint);
                canvas.drawText("Blanco", leftMargin, titleBaseLine + (np + 100), paint);
                canvas.drawText(this.asm.getConceptG(), leftMargin + 100, titleBaseLine + (np + 100), paint);
                canvas.drawText("Nulos", leftMargin, titleBaseLine + (np + 120), paint);
                canvas.drawText(this.asm.getConceptH(), leftMargin + 100, titleBaseLine + (np + 120), paint);
                canvas.drawText("Gran Total", leftMargin, titleBaseLine + (np + 140), paint);
                canvas.drawText(this.asm.getConceptI(), leftMargin + 100, titleBaseLine + (np + 140), paint);
            } else {
                canvas.drawText("Impugnados", leftMargin, titleBaseLine + (np + 20), paint);
                canvas.drawText(this.asm.getConceptC(), leftMargin + 100, titleBaseLine + (np + 20), paint);
                canvas.drawText("Abstenciones", leftMargin, titleBaseLine + (np + 40), paint);
                canvas.drawText(this.asm.getConceptD(), leftMargin + 100, titleBaseLine + (np + 40), paint);
                canvas.drawText("Escrutadas", leftMargin, titleBaseLine + (np + 60), paint);
                canvas.drawText(this.asm.getConceptE(), leftMargin + 100, titleBaseLine + (np + 60), paint);
                canvas.drawText("Faltantes", leftMargin, titleBaseLine + (np + 80), paint);
                canvas.drawText(this.asm.getConceptF(), leftMargin + 100, titleBaseLine + (np + 80), paint);
                canvas.drawText("Entregadas", leftMargin, titleBaseLine + (np + 100), paint);
                canvas.drawText(this.asm.getConceptG(), leftMargin + 100, titleBaseLine + (np + 100), paint);
                canvas.drawText("Nulos", leftMargin, titleBaseLine + (np + 120), paint);
                canvas.drawText(this.asm.getConceptH(), leftMargin + 100, titleBaseLine + (np + 120), paint);


            }


        }

    }

    public static boolean findList(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

}


//    public void loadSummaryActaHon() {
//        String[] acta = {""};
//        String[] values = {""};
//        String[] band = {""};
//        String[] pref = {""};
//        String[] cruz = {""};
//        String[] blank = {""};
//        String[] empty = {""};
//        String voto = "";
//
//        voto = "Marca";
//
//
//        fillTextviewsActa();
//
//        conceptosValues = db_adapter.getConceptsCountPreferential();
//        partyValues = db_adapter.getPreferentialElectionCandidateVotes();
//
//        // For individual party crossvote count
//        ArrayList<PreferentialVotoBanderas> partycrossvotes = db_adapter.getParyCrossVotes();
//
//        // For individual party bandera count
//        List<PreferentialVotoBanderas> partypref = db_adapter.getBanderaVotesPreferential();
//
//        //Debug:?
////        for (int i = 0; i < partypref.size(); i++) {
////            Log.i("Value count ", "i = " + i);
////            Log.e("get ", " party cross votes =   " + partypref.get(i).getParty_cross_votes());
////            Log.e("get ", " party votes       =   " + partypref.get(i).getParty_votes());
////            Log.e("get ", " party pref votes  =   " + partypref.get(i).getParty_preferential_votes());
////            Log.e("get ", " party boletas     =   " + partypref.get(i).getParty_boletas());
////            Log.e("get ", " part band pref id =   " + partypref.get(i).getBandera_preferential_election_id());
////            Log.e("get ", " party pref elec id=   " + partypref.get(i).getParty_preferential_election_id());
////            banderasCounts = banderasInfo.get(i);
////            Log.e("get ", " bandinfo pref id  =   " + banderasCounts.getBandera_preferential_election_id());
////        }
//
//        // Get total cross vote count by looking at highest boleta number
//        ArrayList<CrossVoteBundle> crossvotes = db_adapter.getCrossVoteBundleArrayList(vc.getJrvString());
//        int cvnum = 0;
//        for (int i = 0; i < crossvotes.size(); i++) {
//            if (Integer.parseInt(crossvotes.get(i).getBoletaNo()) > cvnum) {
//                cvnum = Integer.parseInt(crossvotes.get(i).getBoletaNo());
//            }
//        }
//
//        TextView labels = (TextView) findViewById(R.id.Candidate);
//        labels.setText("");
//        labels = (TextView) findViewById(R.id.Banderas);
//        labels.setText(voto+"\n"+getResources().getString(R.string.flagvote));
//        labels = (TextView) findViewById(R.id.bandvotos);
//        labels.setText("");
//        labels = (TextView) findViewById(R.id.Preferential);
//        labels.setText(voto+"\n"+getResources().getString(R.string.prefvote));
//        labels = (TextView) findViewById(R.id.PrefVotos);
//        labels.setText("");
//        labels = (TextView) findViewById(R.id.PrefMarcas);
//        labels.setText("");
//        labels = (TextView) findViewById(R.id.Cruzados);
//        labels.setText(voto+"\nCruzado");
//        labels = (TextView) findViewById(R.id.CruzVotos);
//        labels.setText("");
//        labels = (TextView) findViewById(R.id.CruzMarcas);
//        labels.setText("");
//        labels = (TextView) findViewById(R.id.Total);
//        labels.setText("Total");
//        labels = (TextView) findViewById(R.id.TotalVotos);
//        labels.setText("");
//        labels = (TextView) findViewById(R.id.TotalMarcas);
//        labels.setText("");
//
//        //Debug:?
////        Log.e("Value of", "conceptosValues(SOBRANTES)    " + conceptosValues.get("SOBRANTES"));
////        Log.e("Value of", "conceptosValues(Inutilizadas)    " + conceptosValues.get("INUTILIZADAS"));
////        Log.e("Value of", "conceptosValues(VOTOTS CRUZADOS)    " + conceptosValues.get("VOTOS CRUZADOS"));
////        Log.e("Value of", "conceptosValues(IMPUGNADOS)    " + conceptosValues.get("IMPUGNADOS"));
////        Log.e("Value of", "conceptosValues(NULOS)    " + conceptosValues.get("NULOS"));
////        Log.e("Value of", "conceptosValues(ABSTENCIONES)    " + conceptosValues.get("ABSTENCIONES"));
////        Log.e("Value of", "conceptosValues(TOTAL PAPELETAS ESCRUTADAS)    " + conceptosValues.get("TOTAL PAPELETAS ESCRUTADAS"));
////        Log.e("Value of", "conceptosValues(PAPELETAS FALTANTES)    " + conceptosValues.get("PAPELETAS FALTANTES"));
////        Log.e("Value of", "conceptosValues(PAPELETAS ENTREGADAS)    " + conceptosValues.get("PAPELETAS ENTREGADAS"));
////        Log.e("Value of", "conceptosValues(PAPELETAS RECIBIDAS)    " + conceptosValues.get("PAPELETAS RECIBIDAS"));
//
//
//        ArrayList<String> actaLabels = new ArrayList();
//        ArrayList<String> valuesLabels = new ArrayList();
//        Iterator iterator = conceptosValues.entrySet().iterator();
//        while (iterator.hasNext()) {
//            String key = "";
//            String value="";
//            Map.Entry pair = (Map.Entry) iterator.next();
//            if (pair.getKey().toString().equals("VOTOS CRUZADOS")||pair.getKey().toString().equals("PAPELETAS RECIBIDAS")) {//TODO MOVE TO STRING FILE
//                iterator.remove();
//                continue;
//            }
//
//            if (pair.getKey().toString().equals("PAPELETAS ENTREGADAS"))//TODO MOVE TO STRING FILE
//                key = "Papeletas Entregadas a Votantes";
//            else{
//                key = pair.getKey().toString();
//                key.toLowerCase();
//                key = WordUtils.capitalizeFully(key);
//
//            }
//            try{
//                value = pair.getValue().toString();
//            }catch (NullPointerException npe){
//                Log.e("SUMMARY ACT",key+" is empty-NULL");
//                npe.printStackTrace();
//                iterator.remove();
//                continue;
//            }
//
//            actaLabels.add(key);
//            valuesLabels.add(value);
//            iterator.remove();
//        }
//        int numberOfRows =actaLabels.size()+partyCount.size();
//        acta = new String[numberOfRows];
//        values = new String[numberOfRows];
//        int j = partyCount.size();
//        for (int i = 0; i < actaLabels.size(); i++) {
//            if(i>1){
//                acta[i+j] = actaLabels.get(i);
//                values[i+j] = valuesLabels.get(i);
//            }else{
//                acta[i] = actaLabels.get(i);
//                values[i] = valuesLabels.get(i);
//            }
//        }
//        for(int i =0; i<partyCount.size(); i++){
//            acta[i + 2] = partyCount.get(i).getParty_name();
////            acta[i] = partyCount.get(i).getParty_name();
//        }
//
//        band = new String[numberOfRows];
//        pref = new String[numberOfRows];
//        cruz = new String[numberOfRows];
//        blank = new String[numberOfRows];
//        empty = new String[numberOfRows];
//
//        for (int i = 0; i < numberOfRows; i++) {
//            band[i] = "";
//            pref[i] = "";
//            cruz[i] = "";
//            blank[i] = "";
//            empty[i] = "";
//        }
//
//        for (int x = 0; x < banderasInfo.size(); x++) {
//            for (int i = 0; i < partycrossvotes.size(); i++) {
//                if (partycrossvotes.get(i).getParty_preferential_election_id().equals(banderasInfo.get(x).getParty_preferential_election_id())) {
//                        cruz[x + 2] = String.format(Locale.US, "%.0f", partycrossvotes.get(i).getParty_cross_votes());
//                }
//
//                if((k > 1) && (k < partyCount.size() - 1)){
//                    values[k] = "";
//                }else values[k] = String.format(Locale.US, "%.0f", total);
//
//        }
//
//        for (int x = 0; x < banderasInfo.size(); x++) {
//            if (cruz[x + 2].equals("")) {
//                cruz[x + 2] = "0";
//            }
//        }
//
//        int k=2, kk = 2; //todo: ASSUMING THERE ARE TWO CONCEPTS PRIOR TO PARTY. DOUBLE CHECK!!
//        float prefmark = 0;
//        if (!db_adapter.isOpen()) db_adapter.open();
//        for(PreferentialVotoBanderas pvBanderas: partypref){
//                for(Party party : partyCount) {
//                    candidates = db_adapter.getParlacenCandidatesArrayList(party.getParty_preferential_election_id());
//                    ArrayList<CandidateMarks> cvm = db_adapter.getCandidateMarksArrayListByParty(party.getParty_preferential_election_id(), "4");
//
//                    for(int x = 0; x < candidates.size(); x++){
//                        Log.e("getmtotalmarks", String.format("%.3f", cvm.get(x).getmTotalMarks()));
//                        prefmark = prefmark + (float) cvm.get(x).getmTotalMarks();
//                    }
//
//                    band[k] = String.format(Locale.US, "%.0f", pvBanderas.getParty_votes());
//                    pref[k] = String.format(Locale.US, "%.0f", prefmark);
//                }
//
//
//            float total = Float.valueOf(band[k])+Float.valueOf(pref[k])+Float.valueOf(cruz[k]);
//            values[k] = String.format(Locale.US, "%.0f", total);
//            k++;
//        }
//
//
//        populateListViewsFromPassed(acta, pref, band, blank, cruz, blank, values, blank, empty);
//    }
//
