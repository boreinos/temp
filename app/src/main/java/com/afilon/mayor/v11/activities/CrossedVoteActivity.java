package com.afilon.mayor.v11.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.adapters.CrossVoteAdapter;
import com.afilon.mayor.v11.adapters.FlagAdapter;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.BallotDialog;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.FourButtonFragment;
import com.afilon.mayor.v11.fragments.ThreeButtonFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogEditTextFragment4Boletas;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.fragments.WheelFragment;
import com.afilon.mayor.v11.fragments.WheelFragment.OnWheelDialogListener;
import com.afilon.mayor.v11.interfaces.OnGridListener;
import com.afilon.mayor.v11.layout.FixedGridLayoutManager;
import com.afilon.mayor.v11.model.Ballot;
import com.afilon.mayor.v11.model.Candidate;
import com.afilon.mayor.v11.model.CandidateCrossVote;
import com.afilon.mayor.v11.model.CrossVoteBundle;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.ContextHandler;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.widgets.SpaceItemDecoration;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class CrossedVoteActivity extends AfilonActivity implements OnTwoButtonDialogFragmentListener {
//        OnWheelDialogListener{
    //------------------------------------------------------------------------------------------------

    private static final String CLASS_TAG = "CrossVoteActivity";


    private CrossVoteAdapter adapter;
    private VotingCenter vc;
    private Escrudata escrudata;
    private FlagAdapter flagsAdapter;
    //-- need views to be global:
    private RecyclerView gridView, gridPartyFlags;
    private ArrayList<Party> partyArrayList;
    private ArrayList<String> partyListIds;
    private List<Candidate> candidateCrossVoteTemp;
    private ArrayList<CandidateCrossVote> candidateCrossVotes = new ArrayList<CandidateCrossVote>();
    private DatabaseAdapterParlacen db_adapter;
    private LinkedHashMap valuesMap;

    private int partiesQty;
    private int biggestPartyElements = 0;
    private int smallestParty = 0;
    private int HowManyBallotsToIterate = 0;
    private Ballot.BallotType validBallot, nullBallot, emptyBallot, impugnadoBallot;
    private String currentJrv = "";
    private Utilities util;
    private int currentBallotNumber = 0;
    private Ballot ballot;
    private Button seguienteBtn;
    private Button aceptarBtn, invalidBtn, descartarBtn;
    private TextView crossVoteMarksDetail;
    private TextView crossVoteTotalDetail;
    private TextView crossVoteNoDetail;
    private boolean mAceptarAllowed = false;
    private static final String STARTBALLOT = "INGRESAR\nMARCAS";
    private static final String ACCEPT = "ACEPTAR";
    private static final String CONFIRM_ENTRY = "VERIFICAR";
    private static final String REJECT = "RECHAZAR";
    private static final String REENTER = "REINGRESAR\nMARCAS";
    private static final String CORRECT_MISTAKES = "CORREGIR";
    private static final String RESTART = "REINICIAR\nPAPELETA";
    private static final String NEXT = "PROXIMA\nPAPELETA";
    private static final String SAVE = "GUARDAR";
    private static final String ADD  = "Añadir\nPAPELETA"; //= ContextHandler.getElectionContext().getResources().getString(R.string.add_ballots);//
    private static final String DROP = "Descartar\nRestantes";
    private static final String INVALID="Papeleta\nInvalida";
    private static final String RE_INVALID="Re-invalido";
    private static final String CANCEL="Cancelar",
            NULO="Nulo",
            EMPTY="Blanco",
            IMPUGNADO="Impugnado";
    private static final int DROP_MESSAGE = 2;
    private static final int ACCEPT_MESSAGE = 1;
    private CustomKeyboard customKeyboard;
    private ChallengeHelper challengeHelper;
    private static final int ADD_BALLOT = 1;
    private static final int DROP_BALLOT = 2;
    private static final int SELECT_NULO = 3;
    private static final int SELECT_EMPTY = 4;
    private static final int SELECT_IMPUGNADO=5;
    private SubMenu voteMenu;
    private String currentBtnSelection;
    private String nextLabel = "ERROR!";
    View touchSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = new Utilities(this);
        util.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Consts.LOCALE.contains("ELSA")) {
            setContentView(R.layout.activity_cross_vote_es);
        } else {
            setContentView(R.layout.activity_cross_vote);
        }
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(CrossedVoteActivity.this));
        db_adapter = new DatabaseAdapterParlacen(this);
        //-----------------------------------------------------------------------------------------

        challengeHelper = new ChallengeHelper(this);
        // REGISTER ROUTINES TO CHALLENGE HELPER:
        challengeHelper.addRoutine(ADD_BALLOT, add_ballots);
        challengeHelper.addRoutine(DROP_BALLOT, drop_ballots);
        challengeHelper.addRoutine(SELECT_NULO,select_nulo);
        challengeHelper.addRoutine(SELECT_EMPTY,select_blank);
        challengeHelper.addRoutine(SELECT_IMPUGNADO,select_impugnado);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);// FOR DUI CONFIRMATION ONLY
        challengeHelper.setTools(util, db_adapter); //TOOLS ARE FOR DUI CONFIRMATION ONLY

        //---------------------------------------------------------------------------
        currentBallotNumber = util.loadPreferences("HowManyBallotSoFar") + 1;
        HowManyBallotsToIterate = util.loadPreferences(Consts.VOTO_CRUZADO);

        // ---------------------  HEADER SECTION   ----------------------------------------
        TextView crossVoteNo = (TextView) findViewById(R.id.crossvote_no_tv);
        crossVoteNoDetail = (TextView) findViewById(R.id.crossvote_no_detail_tv);
        TextView crossVoteMarks = (TextView) findViewById(R.id.crossvote_marks_tv);
        crossVoteMarksDetail = (TextView) findViewById(R.id.crossvote_marks_detail_tv);
        TextView crossVoteTotal = (TextView) findViewById(R.id.crossvote_total_tv);
        crossVoteTotalDetail = (TextView) findViewById(R.id.crossvote_total_detail_tv);

        findViewById(R.id.entered_marcas).setVisibility(View.GONE);
        String totalBallots = "/  " + String.valueOf(HowManyBallotsToIterate);
        ((TextView) findViewById(R.id.total_ballots)).setText(totalBallots);

        // set up invalid vote:
        invalidBtn =(Button)findViewById(R.id.invalid_btn);
        invalidBtn.setOnClickListener(getInvalidBallot());
        aceptarBtn = (Button) findViewById(R.id.aceptar_btn);
        seguienteBtn = (Button) findViewById(R.id.siguiente_btn);
        descartarBtn = (Button) findViewById(R.id.descartar_btn);
        // determine button text and color:
        setButtonsToStart();
        //---------------------------------------------------------------------------------------

        partyListIds = new ArrayList<>();
        crossVoteNo.setText("PAPELETA No ");
        crossVoteTotal.setText("VOTO X MARCA ");
        initiateHeaders();

        db_adapter.open();
        Bundle b = getIntent().getExtras();
        vc = b.getParcelable("com.afilon.tse.votingcenter");
        escrudata = b.getParcelable("com.afilon.tse.escrudata");
        currentJrv = vc.getJRV();

        util.saveCurrentScreen(this.getClass(),b);

        //Get Parties
        partyArrayList = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());
        getTotalCandidatesList(partyArrayList);

        for (int i = 0; i < partyArrayList.size(); i++) {
            //Save Image Drawable Resource ID:
            String name = partyArrayList.get(i).getParty_name();
            partyArrayList.get(i).createPartyVotes();
            partyArrayList.get(i).setPartyDrawableId(getResources().getIdentifier(name.toLowerCase(), "drawable", getApplicationContext().getPackageName()));
        }
        partiesQty = partyArrayList.size();

        //Find out the party with less candidates
        int smallestPartyElement = biggestPartyElements;
        for (int t = 0; t < partiesQty; t++) {

            String partyPreferentialElectionId = partyArrayList.get(t).getParty_preferential_election_id();
            candidateCrossVoteTemp = db_adapter.getParlacenCandidatesArrayList(partyPreferentialElectionId);
            int currentSize = candidateCrossVoteTemp.size();

            //Get the smallest party detected
            if (currentSize <= smallestPartyElement) {
                smallestPartyElement = currentSize;
                smallestParty = t;
            }
        }
        final FixedGridLayoutManager flagLayoutManager = new FixedGridLayoutManager();
        final FixedGridLayoutManager gridLayoutManager = new FixedGridLayoutManager();
        gridPartyFlags = (RecyclerView) findViewById(R.id.PartyFlagsFragment);
        gridView = (RecyclerView) findViewById(R.id.gridviewFragment);
        SpaceItemDecoration spacing;

        if (Consts.LOCALE.equals(Consts.ELSALVADOR)) {
            // EL SALVADOR SORTING:
            sortCandidatesGridView();
            flagLayoutManager.setTotalColumnCount(partiesQty);
            gridLayoutManager.setTotalColumnCount(partiesQty);
            boolean includeEdge = false;
            if (partiesQty < 10) {
                spacing = new SpaceItemDecoration(partiesQty, getSpacingES(partiesQty), includeEdge);
                gridView.addItemDecoration(spacing);
                gridPartyFlags.addItemDecoration(spacing);
            }
        } else if(Consts.LOCALE.equals(Consts.HONDURAS)) {
            // HONDURAS SORTING
            sortCandidatesGridHonduras();
            flagLayoutManager.setTotalColumnCount(1);
            gridLayoutManager.setTotalColumnCount(biggestPartyElements);
        }
        //------------------------------------------------------------------------------------------
        gridView.setLayoutManager(gridLayoutManager);
        gridPartyFlags.setLayoutManager(flagLayoutManager);
        flagsAdapter = new FlagAdapter(getApplicationContext(), partyArrayList, GridListener);
        adapter = new CrossVoteAdapter(getApplicationContext(), candidateCrossVotes, biggestPartyElements, GridListener);

        findViewById(R.id.parentLayout).setOnTouchListener(clearTouchSource());
        ballot = new Ballot(biggestPartyElements);
        ballot.setPartyArrayList(partyArrayList);
        ballot.setLocation(currentJrv, vc.getPreferential_election2_id());
        //------------------------------------------------------------------------------------------
        Gson gson = new Gson();
        String map = escrudata.getValueMap();
        valuesMap = gson.fromJson(map,LinkedHashMap.class);
        // get menu view:
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        voteMenu = navigationView.getMenu().findItem(R.id.ballot_menu).getSubMenu();
        // initialize ballot count:
        if(Consts.LOCALE.equals(Consts.HONDURAS)){
            validBallot = ballot.createType(Ballot.BallotType.VALID_VOTE);
            nullBallot = ballot.createType(Ballot.BallotType.NULL_VOTE);
            emptyBallot = ballot.createType(Ballot.BallotType.EMPTY_VOTE);
            validBallot.setCount(util.parseInt((String)valuesMap.get("VOTOS VALIDOS"),0));// load them from escrudata?
            nullBallot.setCount(util.parseInt((String)valuesMap.get("NULOS"),0));
            emptyBallot.setCount(util.parseInt((String)valuesMap.get("EN BLANCO"),0));
            updateVoteMenu(validBallot.getCount(),nullBallot.getCount(), emptyBallot.getCount());
        }else if (Consts.LOCALE.equals(Consts.ELSALVADOR)){
            nullBallot = ballot.createType(Ballot.BallotType.NULL_VOTE);
            emptyBallot = ballot.createType(Ballot.BallotType.EMPTY_VOTE);
            impugnadoBallot = ballot.createType(Ballot.BallotType.IMPUGNADO_VOTE);
            nullBallot.setCount(util.parseInt((String)valuesMap.get("NULOS"),0));
            emptyBallot.setCount(util.parseInt((String)valuesMap.get("ABSTENCIONES"),0));
            impugnadoBallot.setCount(util.parseInt((String)valuesMap.get("IMPUGNADOS"),0));
            updateVoteMenuES(nullBallot.getCount(),emptyBallot.getCount(),impugnadoBallot.getCount());
        }
        // todo: LEFT HERE KIND OFF

        gridView.setAdapter(adapter);
        gridPartyFlags.setAdapter(flagsAdapter);
        gridView.setOnScrollListener(gridScrollListener());
        gridView.setOnTouchListener(gridTouchListener());
        gridPartyFlags.setOnScrollListener(flagScrollListener());
        gridPartyFlags.setOnTouchListener(gridTouchListener());
        aceptarBtn.setOnClickListener(getAceptar());

        seguienteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String label = ((Button) v).getText().toString();
                setButtonColorGreen(invalidBtn);
                // ----------------------------------------------------------
                setButtonColorRed(seguienteBtn);
                seguienteBtn.setText(NEXT);
                switch (label) {
                    case NEXT:
                        if (currentBallotNumber < HowManyBallotsToIterate) {
                            nextBallot();
                        } else {
                            nextActivity();
                        }
                        break;
                    case RESTART:
                        animateClearBallot();
                        reInitiateBallot();
                        break;
                }
            }
        });
        descartarBtn.setOnClickListener(getAddDescartar());
        if (currentBallotNumber >= HowManyBallotsToIterate) {
            setButtonColorRed(invalidBtn);
            setButtonColorRed(aceptarBtn);
            setButtonColorGreen(seguienteBtn);
        }
    }

    private void updateVoteMenu(int valid,int nullv, int emptyv){
        int total = valid + nullv + emptyv;
        String totalS=String.valueOf(total);
        String validBallots = String.valueOf(valid);
        String nullBallots = String.valueOf(nullv);
        String emptyBallots = String.valueOf(emptyv);

        // set El Salvador counts invisible:
//        voteMenu.findItem(R.id.nav_impugnado).getActionView().setVisibility(View.GONE);
        // update views:
        ((TextView)voteMenu.findItem(R.id.nav_valid).getActionView())
                .setText(validBallots);

        ((TextView)voteMenu.findItem(R.id.nav_null).getActionView())
                .setText(nullBallots);

        ((TextView)voteMenu.findItem(R.id.nav_blank).getActionView())
                .setText(emptyBallots);

        ((TextView)voteMenu.findItem(R.id.nav_grantotal).getActionView())
                .setText(totalS);
    }

    private void updateVoteMenuES(int nullv, int emptyv, int impugnadov){
        String nullBallots = String.valueOf(nullv);
        String emptyBallots = String.valueOf(emptyv);
        String impugandoBallots = String.valueOf(impugnadov);

        // hide Honduras counts:
//        voteMenu.findItem(R.id.nav_valid).getActionView().setVisibility(View.GONE);
//        voteMenu.findItem(R.id.nav_grantotal).getActionView().setVisibility(View.GONE);
        //update views:
        ((TextView)voteMenu.findItem(R.id.nav_null).getActionView()).setText(nullBallots);
        ((TextView)voteMenu.findItem(R.id.nav_blank).getActionView()).setText(emptyBallots);
        ((TextView)voteMenu.findItem(R.id.nav_impugnado).getActionView()).setText(impugandoBallots);

    }

    private void updateImpugnadoInMenu(int impugnado){
        String impugnadoS = String.valueOf(impugnado);
        ((TextView)voteMenu.findItem(R.id.nav_impugnado).getActionView()).setText(impugnadoS);
    }

    private void updateValidInMenu(int valid){
        TextView grantotal = (TextView)voteMenu.findItem(R.id.nav_grantotal).getActionView();
        String totalS = (String)grantotal.getText();
        int total = util.parseInt(totalS,0)+1;
        totalS = String.valueOf(total);
        String validS = String.valueOf(valid);
        ((TextView)voteMenu.findItem(R.id.nav_valid).getActionView()).setText(validS);
        grantotal.setText(totalS);
    }

    private void updateNullInMenu(int nullv){
        if(Consts.LOCALE.equals(Consts.HONDURAS)){
            TextView grantotal = (TextView)voteMenu.findItem(R.id.nav_grantotal).getActionView();
            String totalS = (String)grantotal.getText();
            int total = util.parseInt(totalS,0)+1;
            totalS = String.valueOf(total);
            grantotal.setText(totalS);
        }


        String nullS = String.valueOf(nullv);
        ((TextView)voteMenu.findItem(R.id.nav_null).getActionView()).setText(nullS);
    }

    private void updateBlankInMenu(int emptyv){
        if(Consts.LOCALE.equals(Consts.HONDURAS)){
            TextView grantotal = (TextView)voteMenu.findItem(R.id.nav_grantotal).getActionView();
            String totalS = (String)grantotal.getText();
            int total = util.parseInt(totalS,0)+1;
            totalS = String.valueOf(total);
            grantotal.setText(totalS);
        }
        String emptyS = String.valueOf(emptyv);
        ((TextView)voteMenu.findItem(R.id.nav_blank).getActionView()).setText(emptyS);
    }

    private ChallengeHelper.OnApprove add_ballots = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() { createDialogEditText("INGRESAR CANTIDAD DE PAPELETAS ADICIONALES", -1);}
    };

    private ChallengeHelper.OnApprove drop_ballots = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            discardBallots();
        }
    };

    private ChallengeHelper.OnApprove select_nulo = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            markBallotNulo();
        }
    };

    private ChallengeHelper.OnApprove select_blank = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            markBallotAbstained();
        }
    };

    private ChallengeHelper.OnApprove select_impugnado = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {markBallotImpugnado();
        }
    };

    private RecyclerView.OnScrollListener gridScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.equals(touchSource)) {
                    return;
                }
                gridPartyFlags.scrollBy(dx, dy);
            }
        };
    }

    private RecyclerView.OnScrollListener flagScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.equals(touchSource)) {
                    return;
                }
                gridView.scrollBy(dx, dy);
            }
        };
    }

    RecyclerView.OnItemTouchListener listener = new RecyclerView.SimpleOnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
          return !(gridPartyFlags.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && gridView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE);
//            if (gridPartyFlags.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && gridView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
//                return false;
//            }
//            return true;
        }
    };

    private View.OnTouchListener clearTouchSource() { // assign to the parent of recylce views
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.e("FRAGMET LISTENR", "TOUCH SOURCE IS NULL NOW");
                    touchSource = null;
                    gridPartyFlags.removeOnItemTouchListener(listener);
                    gridView.removeOnItemTouchListener(listener);
                }
                return false;
            }
        };
    }

    public View.OnTouchListener gridTouchListener() { // assign to the recycle views
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP && ((RecyclerView) v).getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.e("RECYCLER LISTENR", "TOUCH SOURCE IS NULL NOW");
                    touchSource = null;
                    gridPartyFlags.removeOnItemTouchListener(listener);
                    gridView.removeOnItemTouchListener(listener);
                    return false;
                }
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    Log.e("RECYCLER LISTENR", "TOUCH SOURCE IS NULL NOW");
//                    touchSource = null;
//                    gridPartyFlags.removeOnItemTouchListener(listener);
//                    gridView.removeOnItemTouchListener(listener);
//                    return false;
//                }

                if (touchSource == null) {
                    touchSource = v;
                    if (v.equals(gridView)) {
                        gridPartyFlags.addOnItemTouchListener(listener);//disable gridparty flags
                    } else {
                        //gridflag
                        gridView.addOnItemTouchListener(listener);//disable gridparty flags
                    }
                    Log.e("RECYCLER LSTNER", "TOUCH SOURCE TAKEN");
                }
                return false;
            }
        };
    }

    private void initiateHeaders() {
        crossVoteNoDetail.setText(String.valueOf(currentBallotNumber));
        crossVoteMarksDetail.setText(String.valueOf(0));
        crossVoteTotalDetail.setText(String.valueOf(0));
    }

    public ArrayList<Candidate> getTotalCandidatesList(ArrayList<Party> partyArrayList) {
        String partyPrefElecId = "";
        List<Candidate> candidates;
        ArrayList<Candidate> candidateTemp = new ArrayList<Candidate>();
        for (int l = 0; l < partyArrayList.size(); l++) {

            partyPrefElecId = partyArrayList.get(l).getParty_preferential_election_id();
            partyListIds.add(partyPrefElecId);

            candidates = db_adapter.getParlacenCandidatesArrayList(partyPrefElecId);

            //Get the biggest candidate list
            if (biggestPartyElements < candidates.size()) {
                biggestPartyElements = candidates.size();
            }
            for (Candidate ca : candidates) {
                candidateTemp.add(new Candidate(
                        ca.getCandidate_name(),
                        ca.getCandidateID(),
                        ca.getPartyName(),
                        ca.getPartyPreferentialElectionID(),
                        ca.getCandidate_order()));
            }
        }
        return candidateTemp;
    }

    private void sortCandidatesGridView() {
        //Sort Candidates to match the required sorting by gridview:
        for (int i = 0; i < partiesQty; i++) {

            String partyElectionId = partyArrayList.get(i).getParty_preferential_election_id();
            candidateCrossVoteTemp = db_adapter.getParlacenCandidatesArrayList(partyElectionId);
            partyArrayList.get(i).setCandidateTotal(candidateCrossVoteTemp.size());
            int y = 0;
            for (Candidate candidate : candidateCrossVoteTemp) {

                candidate.setCandidate_image(String.valueOf(getResources().getIdentifier("pic" +
                        candidate.getCandidatePreferentialElectionID().toLowerCase(), "drawable", getApplicationContext().getPackageName())));

                CandidateCrossVote crossVote = new CandidateCrossVote(y, candidate.getCandidate_name(),
                        candidate.getCandidateID(), Integer.valueOf(candidate.getCandidate_image()),
                        candidate.getPartyName(), candidate.getCandidatePreferentialElectionID(),
                        false, 0f);
                crossVote.setPartyElectionId(candidate.getPartyPreferentialElectionID());

                candidateCrossVotes.add(crossVote);
                y++;
            }

            if (i == smallestParty) {
                //Now let's add the empty elements in order to complete the max candidate list
                int emptyElementsToAdd = (biggestPartyElements - y);
                for (int m = 0; m < emptyElementsToAdd; m++) {
                    //Insert blank Candidate
                    CandidateCrossVote emptyCandidate = new CandidateCrossVote(y, "",
                            "",
                            getResources().getIdentifier("blankavatar", "drawable", getApplicationContext().getPackageName()), "",
                            "", false, 0f);
                    emptyCandidate.setPartyElectionId(partyElectionId);
                    candidateCrossVotes.add(emptyCandidate);
                    y++;
                }
            }
            //todo: note- for honduras we can skip sorting:
            //Sort Candidates based on index number :
            //balance the tree:
            Collections.sort(candidateCrossVotes, new Comparator<CandidateCrossVote>() {
                @Override
                public int compare(CandidateCrossVote s1, CandidateCrossVote s2) {
                    return Integer.valueOf(s1.getCandidateSortIndex()).compareTo(s2.getCandidateSortIndex());
                }
            });
        }
    }

    private void sortCandidatesGridHonduras() {
        //Sort Candidates to match the required sorting by gridview:
        for (int i = 0; i < partiesQty; i++) {

            String partyElectionId = partyArrayList.get(i).getParty_preferential_election_id();
            candidateCrossVoteTemp = db_adapter.getParlacenCandidatesArrayList(partyElectionId);
            partyArrayList.get(i).setCandidateTotal(candidateCrossVoteTemp.size());
            int y = 0;
            for (Candidate candidate : candidateCrossVoteTemp) {

                candidate.setCandidate_image(String.valueOf(getResources().getIdentifier("pic" +
                                candidate.getCandidatePreferentialElectionID().toLowerCase(), "drawable",
                        getApplicationContext().getPackageName())));

                CandidateCrossVote crossVote = new CandidateCrossVote(y, candidate.getCandidate_name(),
                        candidate.getCandidateID(), Integer.valueOf(candidate.getCandidate_image()),
                        candidate.getPartyName(), candidate.getCandidatePreferentialElectionID(),
                        false, 0f);
                crossVote.setPartyElectionId(candidate.getPartyPreferentialElectionID());

                candidateCrossVotes.add(crossVote);
                y++;
            }

            if (i == smallestParty) {
                //Now let's add the empty elements in order to complete the max candidate list
                int emptyElementsToAdd = (biggestPartyElements - y);
                for (int m = 0; m < emptyElementsToAdd; m++) {
                    //Insert blank Candidate
                    CandidateCrossVote emptyCandidate = new CandidateCrossVote(y, "",
                            "",
                            getResources().getIdentifier("blankavatar", "drawable", getApplicationContext().getPackageName()), "",
                            "", false, 0f);
                    emptyCandidate.setPartyElectionId(partyElectionId);
                    candidateCrossVotes.add(emptyCandidate);
                    y++;
                }
            }
        }
    }

    public void dumpCrossVoteToDb() {
        keepDbOpen();
        ArrayList<CrossVoteBundle> mCrossVoteBundle = ballot.buildVoteBundle();

        int TotalMarks = mCrossVoteBundle.size();

        if (Consts.LOCALE.contains("HON")) {
            int electionType = ballot.getMarkType(partyListIds);
            String markType = String.valueOf(electionType);
            for (CrossVoteBundle mCVB : mCrossVoteBundle) {

                db_adapter.insertCandidateCrossVote(
                        mCVB.getJrv(),
                        mCVB.getPrefElecId(),
                        mCVB.getPartyPrefElecId(),
                        mCVB.getCandidatePrefElecId(),
                        mCVB.getVote(),
//                    1,
                        mCVB.getBoletaNo());
                db_adapter.insertMarks(
                        mCVB.getJrv(),
                        mCVB.getPrefElecId(),
                        mCVB.getCandidatePrefElecId(),
                        mCVB.getPartyPrefElecId(),
                        markType,
                        "1"
                );
            }
            // db insertion:
            includePartyVoteBreakDown(electionType);
            int validBallots = validBallot.addToCount();
            String validCount = String.valueOf(validBallots);
            valuesMap.put("VOTOS VALIDOS",validCount);
            updateValidInMenu(validBallots);
           // updatePersistBallotCount();
        } else if(Consts.LOCALE.equals(Consts.ELSALVADOR)){
            //todo: go one by one:
            int electionType = ballot.getMarkTypeES();
            String markType = String.valueOf(electionType);
            if(electionType!=Ballot.NULO){
                for (CrossVoteBundle mCVB : mCrossVoteBundle) {

                    db_adapter.insertCandidateCrossVote(
                            mCVB.getJrv(),
                            mCVB.getPrefElecId(),
                            mCVB.getPartyPrefElecId(),
                            mCVB.getCandidatePrefElecId(),
                            mCVB.getVote(),
//                    1,
                            mCVB.getBoletaNo());
                    db_adapter.insertMarks(
                            mCVB.getJrv(),
                            mCVB.getPrefElecId(),
                            mCVB.getCandidatePrefElecId(),
                            mCVB.getPartyPrefElecId(),
                            markType,
                            "1"
                    );
                }
                // db insertion:
                includePartyVoteBreakDown(electionType);
            }else{
                markBallotNulo();
            }
        }
        updatePersistBallotCount(); // good.
        String strCurrentMarkValue = ballot.voteMultiplier();
        crossVoteTotalDetail.setText(strCurrentMarkValue);
        crossVoteMarksDetail.setText(String.valueOf(TotalMarks));
        Log.e(CLASS_TAG, "TotalMarks after saving to DB " + String.valueOf(TotalMarks));
    }

    private void updatePersistBallotCount(){
        Gson gson = new Gson();
        String jsonValueMap = gson.toJson(valuesMap);
        escrudata.setValuMap(jsonValueMap);
        util.saveCurrentScreen(this.getClass(),prepareBundle());
        //todo: save update on database.

    }

    private void keepDbOpen(){
        if(!db_adapter.isOpen())db_adapter.open();
    }

    private void includePartyVoteBreakDown(int electionType) {
        switch (electionType) {
            case Ballot.PREF_MARK:
                ballot.addPreferentialPartyVote();
                break;
            case Ballot.PLAN_MARK:
                ballot.addPlanchaPartyVote();
                break;
            case Ballot.CROS_MARK:
                ballot.addCrossParyVote();
                break;
        }
        updatePartyVotes(ballot.getPartyArrayList());
    }

    private void updatePartyVotes(ArrayList<Party> partylist) {
        for (int i = 0; i < partylist.size(); i++) {
            db_adapter.updatePartyVotes(partylist.get(i).getVoteBreakdown(), partylist.get(i).getParty_preferential_election_id());
        }
    }

    public void allowAcceptar(boolean allowed) {
        mAceptarAllowed = allowed;
//        if (mAceptarAllowed && ballot.verifyMarks()) {
        if (mAceptarAllowed) {
            setButtonColorGreen(aceptarBtn);
            aceptarBtn.setText(SAVE);
//            crossVoteMarksDetail.setTextColor(getResources().getColor(R.color.green));

        } else {
            setButtonColorRed(aceptarBtn);
//            crossVoteMarksDetail.setTextColor(getResources().getColor(R.color.red));
        }
    }

    public void disableChildsOnTouch(ViewGroup viewGroup) {
        int cnt = viewGroup.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                disableChildsOnTouch((ViewGroup) v);
            } else {
                v.setOnTouchListener(null);
                v.setOnClickListener(null);
            }
        }
    }

    private View.OnClickListener getAceptar() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String label = ((Button) v).getText().toString();
                switch (label) {
                    case STARTBALLOT:
                        //todo: remove from here:
                        if(Consts.LOCALE.equals(Consts.HONDURAS)){
                            setButtonColorRed(invalidBtn);
                        }
                        //-----------------------
                        firstEntry();
                        nextLabel = REENTER;
//                        requestMarcasCount();
                        break;
                    case ACCEPT:
                        noIndex = ACCEPT_MESSAGE;
                        createDialog("¿DESEA ACEPTAR LOS RESULTADOS?", ACCEPT_MESSAGE);
                        break;
                    case CONFIRM_ENTRY:
                        confirmBallotEntry();
                        break;
                    case "RECHAZAR":
                        reInitiateBallot();
                        break;
                    case REENTER:
                        reEnterBallot();
                        nextLabel = CONFIRM_ENTRY;
                        break;
                    case CORRECT_MISTAKES:
                        corregirProcedure(v);
//                        firstEntry();
                        break;
                    case SAVE:
                        changeText(v);
                        break;
                }
            }
        };
    }

    private View.OnClickListener getAddDescartar() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String label = ((Button) v).getText().toString();
                switch (label) {
                    case DROP:
                        int remaining = HowManyBallotsToIterate - currentBallotNumber;
                        String discardMessage = getResources().getString(R.string.dropBallotMessage);
                        challengeHelper.createDuiChallenge(String.format(discardMessage, remaining), DROP_BALLOT); //"¿DESEA DESCARTAR LAS BOLETAS RESTANTES? "
//                        noIndex = DROP_MESSAGE;
//                        createDialog("¿DESEA  DESCARTAR DE BOLETA " + currentBallotNumber+"?", DROP_MESSAGE);
                        break;
                    case ADD:
                        challengeHelper.createDuiChallenge(getResources().getString(R.string.addBallotMessabe), ADD_BALLOT); //"¿DESEA AÑADIR BOLETAS?"
//                        createDialogEditText("INGRESAR CANTIDAD DE BOLETAS ADICIONALES",-1);
                        break;
                }
            }
        };

    }

    private View.OnClickListener getInvalidBallot(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Consts.LOCALE.equals(Consts.HONDURAS)){
                    challengeHelper.createThreeButtonMenu(CANCEL,NULO,EMPTY,menuListener);
                }else if(Consts.LOCALE.equals(Consts.ELSALVADOR)){
                    challengeHelper.createFourButtonMenu(CANCEL,NULO,EMPTY,IMPUGNADO,toastMenuListener);
                }
            }
        };
    }

    private void corregirProcedure(View v) {
        flagsAdapter.setGreenWarning(true);
        adapter.setShowWarning(false);
//        adapter.notifyDataSetChanged();
//        flagsAdapter.notifyDataSetChanged();
        ((Button) v).setText(STARTBALLOT);
    }

    private void changeText(View view) {
//        setButtonColorRed((Button)view);
        animateSaveBallot();
        ((Button) view).setText(nextLabel);
        adapter.attachListener(false);
        flagsAdapter.attachListener(false);
        flagsAdapter.ignoreTouch(true);//todo: needed?

    }

    public void createDialog(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        TwoButtonDialogFragment twoBtnDialogFragment = new TwoButtonDialogFragment();
        twoBtnDialogFragment.setOnButtonsClickedListenerOne(this);
        Bundle bndl = new Bundle();
        switch (yesIndex) {
            // accept button case
            case ACCEPT_MESSAGE:
                bndl.putString("yesButtonText", "Aceptar Papeleta");
                bndl.putString("noButtonText", "Reiniciar Papeleta");
                break;
            case DROP_MESSAGE:
                bndl.putString("yesButtonText", "Descartar Papeletas");
                bndl.putString("noButtonText", "Cancelar");
                break;
            default:
                break;

        }
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("question", msg);
        bndl.putString("invisible", "visible");
        twoBtnDialogFragment.setArguments(bndl);
        twoBtnDialogFragment.show(fm, "new two button dialog");
    }

    private void valuesAccepted() {
        setButtonColorGreen(seguienteBtn);
        setButtonColorRed(aceptarBtn);
        seguienteBtn.requestFocus();
        mAceptarAllowed = false;
        dumpCrossVoteToDb();
//        currentBallotNumber++; // we are done with this ballot, next.
        util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
        // stop adapter from attaching listeners:
        adapter.attachListener(false);
        adapter.allMatch();
        flagsAdapter.allMatch();
        flagsAdapter.ignoreTouch(true);
        flagsAdapter.attachListener(false);
        flagsAdapter.notifyDataSetChanged(); // just to update scroll
        //disable touch event after saving into db
        disableChildsOnTouch(gridView);
        disableChildsOnTouch(gridPartyFlags);
        Log.i("CROSSVOTE", "RESULTS WERE ACCEPTED");
        //change the name
        seguienteBtn.setText(NEXT);

    }

    private void valuesRejected() {
        setButtonColorRed(seguienteBtn);
        setButtonColorRed(aceptarBtn);
        aceptarBtn.setText(STARTBALLOT);// TODO: fixme if reject then startballot but must clear values first in both the ballot and the textview
        seguienteBtn.setText(NEXT);
//        seguienteBtn.setEnabled(false);
        animateClearBallot();
        reInitiateBallot();
    }

    private void nextBallot() {
        setButtonColorRed(seguienteBtn);
        setButtonColorRed((Button) findViewById(R.id.descartar_btn));
        currentBallotNumber++;
        animateNewBallot();
        reInitiateBallot();
        initiateHeaders();
    }

    private void nextActivity() {
        util.savePreferences("HowManyBallotSoFar", currentBallotNumber); // now super important // no longer important

        if(Consts.LOCALE.equals(Consts.HONDURAS)){
            TextView grantotal = (TextView)voteMenu.findItem(R.id.nav_grantotal).getActionView();
            String totalS = (String)grantotal.getText();
            valuesMap.put("GRAN TOTAL",totalS);
        }
        //update concepts to db:
        if(!db_adapter.isOpen())db_adapter.open();
        db_adapter.updateConceptsCount(valuesMap,vc.getJRV()); // super important now also

        Intent search = new Intent(this, CrossVoteSummaryActivity.class);
        Bundle b = prepareBundle();
        search.putExtras(b);
        startActivity(search);
        finish();

    }

    private Bundle prepareBundle(){
        Bundle b = new Bundle();
        escrudata.setPageTitle(Consts.CROSS_VOTE_SUMMARY);
        b.putParcelable("com.afilon.tse.votingcenter", vc);
        b.putParcelable("com.afilon.tse.escrudata", escrudata);
        return b;
    }

    private void reEnterBallot() {
        ballot.setFirstEntry(false);
        ballot.setupNextEntry();
        clearScreen();
//        aceptarBtn.setText(SAVE);
//        aceptarBtn.setText(CONFIRM_ENTRY);
        adapter.attachListener(true); // unlock grid.
        flagsAdapter.attachListener(true);
        flagsAdapter.ignoreTouch(false);
        HashMap<String, CandidateCrossVote> withMarks = ballot.getSecondMarks();
        HashMap<String, Party> pwithMarks = ballot.getPartySecondMarks();
        adapter.ballotFull(false);
        adapter.setResId(R.drawable.red_x);
        flagsAdapter.setResId(R.drawable.red_x);
        adapter.candidatesWithPreviousMarks(withMarks);
        flagsAdapter.partiesWithPreviousMarks(pwithMarks);
        allowAcceptar(ballot.isReady(partyListIds));
        if (withMarks == null || withMarks.size() == 0) {
            return;
        }
        ballot.updateSecondEntryVotes();
//        ballot.updatePartyList(false);
        updateHeaders();
        flagsAdapter.updatePartyData(ballot.getPartyArrayList());
//        flagsAdapter.updatePartyData(partyArrayList);
//        adapter.notifyDataSetChanged(); maybe?
    }

    private void confirmBallotEntry() {

        // prior to confirming remove any warnings
        flagsAdapter.setGreenWarning(false);
        adapter.setShowWarning(true);
        animateSaveBallot();
        adapter.allMatch();
        //lock grid:
        adapter.attachListener(false);
        flagsAdapter.attachListener(false);
        flagsAdapter.allMatch();
        // enable mis matches
        adapter.setResId(R.drawable.match_x);
        flagsAdapter.setResId(R.drawable.match_x);
        adapter.unLockMisMatches(ballot.confirmBallotEntries());// todo: rename, it doesn't unlock mismatches. it identifyMismatches()
        flagsAdapter.unLockMisMatches(ballot.confirmPartySelection());
        adapter.ballotFull(ballot.isBallotFull());
        adapter.setReviewMode(true); // now in review mode.

        flagsAdapter.setReviewMode(true);
        flagsAdapter.ignoreTouch(true);
        flagsAdapter.updatePartyData(ballot.getPartyArrayList());
//        flagsAdapter.updatePartyData(partyArrayList);
        updateHeaders();

        if (ballot.hasMismatches()) {
            setButtonColorAmber(seguienteBtn);
            seguienteBtn.setText(RESTART);
            findViewById(R.id.mismatch_icon).setVisibility(View.VISIBLE);
            aceptarBtn.setText(CORRECT_MISTAKES);
        } else {
            aceptarBtn.setText(ACCEPT);
            seguienteBtn.setText(NEXT);
            setButtonColorRed(seguienteBtn);
            findViewById(R.id.mismatch_icon).setVisibility(View.INVISIBLE);
        }
        setButtonColorGreen(aceptarBtn);
//        adapter.notifyDataSetChanged();
//        flagsAdapter.notifyDataSetChanged();

    }

    private void reInitiateBallot() {
//        animateClearBallot();
        aceptarBtn.setText(STARTBALLOT);// todo: fixme change to startballot
        setButtonColorGreen(aceptarBtn);
        clearBallot();

//        ballot.setFirstEntry(true); // this will be done after marks are entered.
        // reset party adapter:
        flagsAdapter.ignoreTouch(true);
        adapter.setReviewMode(false);
        flagsAdapter.setReviewMode(false);
//        adapter.setResId(R.drawable.blue_x); again, it will occur after marks are entered.
        adapter.attachListener(false);
        flagsAdapter.attachListener(false);
        flagsAdapter.updatePartyData(ballot.getPartyArrayList());


    }

    private void animateNewBallot() {
        // Fade in top title
//        TextView logo1 = (TextView) findViewById(R.id.tse_logo);
        final View parent = findViewById(R.id.candidate_layout);
        Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.slide_ballot_out);
//        final Animation slideOut = AnimationUtils.loadAnimation(this,R.anim.slide_ballot_out);
        parent.startAnimation(fade1);


//        logo1.startAnimation(fade1);

        // Transition to Main Menu when bottom title finishes animating
        fade1.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                slideBallotIn();
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

    }

    private void slideBallotIn() {
        View parent = findViewById(R.id.candidate_layout);
        Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.slide_ballot_in);
        parent.startAnimation(fade1);
    }

    private void animateClearBallot() {
        View parent = findViewById(R.id.candidate_layout);
        Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.reset_ballot);
        parent.startAnimation(fade1);

    }

    private void animateSaveBallot() {
        View parent = findViewById(R.id.candidate_layout);
        Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.save_ballot);
        parent.startAnimation(fade1);
    }

    private void clearBallot() {
//        ballot.resetBallot(); //clear vote data from party and candidates
        ballot.newBallot(currentBallotNumber);
        adapter.attachListener(true); // unlock
        flagsAdapter.attachListener(true);
        adapter.ballotFull(false);
        clearScreen();
        ((TextView) findViewById(R.id.entered_marcas)).setText("");
        findViewById(R.id.mismatch_icon).setVisibility(View.INVISIBLE);
//        crossVoteMarksDetail.setTextColor(getResources().getColor(R.color.red));

//        clearHeaders();
    }

    private void clearHeaders() {
        crossVoteTotalDetail.setText("0");
        crossVoteMarksDetail.setText("0");
    }

    private void clearScreen() {
        adapter.removeCandidateMarks(); //update  candidate grid
        flagsAdapter.removePartyMarks();
        flagsAdapter.updatePartyData(ballot.getPartyArrayList()); //update party grid
//        flagsAdapter.updatePartyData(partyArrayList); //update party grid
        clearHeaders();
    }

    private void enableDescartar() {
        setButtonColorAmber((Button) findViewById(R.id.descartar_btn));
        if (currentBallotNumber < HowManyBallotsToIterate) {
            ((Button) findViewById(R.id.descartar_btn)).setText(DROP);
        } else {
            ((Button) findViewById(R.id.descartar_btn)).setText(ADD);
        }

    }

    //----------------------------------------------------------------------------------------------

    private void discardBallots() {
        keepDbOpen();
        util.savePreferences(Consts.VOTO_CRUZADO, currentBallotNumber); // try this
//        util.savePreferences(Consts.VOTO_CRUZADO,--currentBallotNumber); // save for errors
        if (Consts.LOCALE.contains("ELSA")) {
            //todo: add the functions to update EL Salvador
            db_adapter.updatePartyCrossVotes(currentBallotNumber, vc.getPreferential_election2_id()); // save for partyvote

        } else {
            db_adapter.updateConceptCount("VotosValidos", String.valueOf(currentBallotNumber), currentJrv);
            escrudata.setVotosValidos(String.valueOf(currentBallotNumber));
        }


        nextActivity();
    }

    private void addBallots(int ballots) {
        keepDbOpen();
        int totalBallots = currentBallotNumber + ballots;
        util.savePreferences(Consts.VOTO_CRUZADO, totalBallots); // save for errors
        HowManyBallotsToIterate = totalBallots;
        String textBallots = String.valueOf(HowManyBallotsToIterate);

        String displayBallots = "/  " + textBallots;
        ((TextView) findViewById(R.id.total_ballots)).setText(displayBallots);
        ((Button) findViewById(R.id.descartar_btn)).setText(DROP);

        if (Consts.LOCALE.contains("ELSA")) {
            //todo: add the function to update El Salvador
            db_adapter.updatePartyCrossVotes(totalBallots, vc.getPreferential_election2_id());

        }
//        else {
            //function for honduras to update concepts for Votos Validos, no longer needed
//            db_adapter.updateConceptCount("VotosValidos", textBallots, currentJrv);
//            escrudata.setVotosValidos(textBallots);
//        }
        nextBallot();

    }

    //--------------------------- COLLECT MARCAS CHALLENGE -----------------------------------------

    TwoButtonDialogEditTextFragment4Boletas editTextFragment;

    public void createDialogEditText(String msg, int routineID) {
        editTextFragment = new TwoButtonDialogEditTextFragment4Boletas();
        FragmentManager fm = getFragmentManager();
        editTextFragment.setOnButtonsClickedListenerOne(ballotListener);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", routineID);
        bndl.putString("noButtonText", "Cancelar");
        bndl.putString("question", msg);
        bndl.putString("invisible", "invisible");
        editTextFragment.setArguments(bndl);
        editTextFragment.show(fm, "new triage dialog");

    }

    private void firstEntry() {
        allowAcceptar(ballot.isReady(partyListIds));
//        aceptarBtn.setText(SAVE);
//        enableDescartar();
//        aceptarBtn.setText(REENTER);
//        setButtonColorRed(aceptarBtn);
        adapter.attachListener(true); // unlock grid.
        flagsAdapter.attachListener(true);
        adapter.setResId(R.drawable.blue_x);
        flagsAdapter.setResId(R.drawable.blue_x);
        ballot.setFirstEntry(true);
        adapter.candidatesWithPreviousMarks(ballot.getFirstMarks());
        flagsAdapter.partiesWithPreviousMarks(ballot.getpartiesFirstmarks());
        flagsAdapter.ignoreTouch(false); //should be able to touch from here until confirm is selected
//        adapter.notifyDataSetChanged(); //maybe?
        flagsAdapter.notifyDataSetChanged();
    }

    //Coloring Buttons
    private void setButtonColorGreen(Button btn) {
        util.setButtonColorGreen(btn);
        int valueInPx = (int) getResources().getDimension(R.dimen.btnSmallPadding);
//        btn.setPadding(valueInPx,valueInPx,valueInPx,valueInPx);
    }

    private void setButtonColorRed(Button btn) {
        util.setButtonColorRed(btn);
        int valueInPx = (int) getResources().getDimension(R.dimen.btnSmallPadding);
//        btn.setPadding(valueInPx, valueInPx, valueInPx, valueInPx);
    }

    private void setButtonColorAmber(Button btn) {
        util.setButtonColorAmber(btn);
        int valueInPx = (int) getResources().getDimension(R.dimen.btnSmallPadding);
//        btn.setPadding(valueInPx, valueInPx, valueInPx, valueInPx);
    }
    // HONDURAS METHODS:
    private void selectedCandidatesFromES(String partyElectionID) {
        keepDbOpen();
        ArrayList<Candidate> candidates = db_adapter.getParlacenCandidatesArrayList(partyElectionID);
        for (Candidate candidate : candidates) {
            candidate.setCandidate_image(String.valueOf(getResources().getIdentifier("pic" +
                            candidate.getCandidatePreferentialElectionID().toLowerCase(), "drawable",
                    getApplicationContext().getPackageName())));
        }
//        ballot.markCandidatesFrom(candidates);
        ballot.addCandidatesFrom(candidates);
        adapter.ballotFull(ballot.isBallotFull());
        updateHeaders();
        // update party grid:
        flagsAdapter.updatePartyData(ballot.getPartyArrayList()); // set parties as marked ia ir needed?
    }

    private void deselectCandidateFromES(String partyElectionID){
        keepDbOpen();
        ArrayList<Candidate> candidates = db_adapter.getParlacenCandidatesArrayList(partyElectionID);
        ballot.removeCandidatesFrom(candidates);
        adapter.ballotFull(ballot.isBallotFull());
        updateHeaders();
        flagsAdapter.updatePartyData(ballot.getPartyArrayList());
    }



    //----------------------------------------------------------------------------------------------
    // HONDURAS METHODS:
    private void selectedCandidatesFrom(String partyElectionID) {
        keepDbOpen();
        clearScreen();
        if (ballot.isBallotFull() && ballot.partyHasVotes(partyElectionID)) {
            //clear ballot
            ballot.clearCandidates();

        } else {
            ArrayList<Candidate> candidates = db_adapter.getParlacenCandidatesArrayList(partyElectionID);
            for (Candidate candidate : candidates) {
                candidate.setCandidate_image(String.valueOf(getResources().getIdentifier("pic" +
                                candidate.getCandidatePreferentialElectionID().toLowerCase(), "drawable",
                        getApplicationContext().getPackageName())));
            }
            ballot.markCandidatesFrom(candidates);
            // update data in candidate grid and grid itself:
            adapter.markCandidatesFrom(partyElectionID);
        }

        adapter.ballotFull(ballot.isBallotFull());
        updateHeaders();
        // update party grid:
        flagsAdapter.updatePartyData(ballot.getPartyArrayList());
    }

    public void updateHeaders() {
        boolean ready = ballot.isReady(partyListIds);
        allowAcceptar(ready);
        String totalMarks = ballot.getSize();
        String strCurrentMarkValue = ballot.voteMultiplier();
        crossVoteTotalDetail.setText(strCurrentMarkValue);
        crossVoteMarksDetail.setText(totalMarks);
    }

    private int getSpacingES(int columns) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int parent = displayMetrics.widthPixels; //parent with in px
        int someDpValue = 120;
        int itemWidth = Math.round(someDpValue * getResources().getDisplayMetrics().density);
        float space = ((float) (parent - (itemWidth * columns))) / ((float) (columns + 1));
        return (int) space;
    }


    //--------------------------  CALLBACK LISTENERS ------------------------------------
    private OnGridListener GridListener = new OnGridListener() {
        @Override
        public void onCandidateGridEvent(CandidateCrossVote candidate) {
            if(Consts.LOCALE.equals(Consts.HONDURAS)){
                //candidate Logic:
                if (candidate.isMarked()) {
                    //candidate was selected:
                    ballot.addCandidate(candidate);
                    adapter.ballotFull(ballot.isBallotFull()); // todo fix for second iteration

                } else {
                    //likely candidate was de-selected: possibility of ballot full and not marked
                    ballot.removeCandidate(candidate);
                    adapter.ballotFull(ballot.isBallotFull()); //todo fix for second iteration
                }
                flagsAdapter.updatePartyData(ballot.getPartyArrayList());
                updateHeaders();
            }else if(Consts.LOCALE.equals(Consts.ELSALVADOR)){
                //candidate Logic:
                if (candidate.isMarked()) {
                    //candidate was selected:
                    ballot.addCandidate(candidate);
                    adapter.ballotFull(ballot.isBallotFull()); // todo fix for second iteration

                } else {
                    //likely candidate was de-selected: possibility of ballot full and not marked
                    ballot.removeCandidateES(candidate);
                    adapter.ballotFull(ballot.isBallotFull()); //todo fix for second iteration
                }
                flagsAdapter.updatePartyData(ballot.getPartyArrayList());
                updateHeaders();
            }

        }

        @Override
        public void onPartyGridEvent(Party party) {
            //Todo: party Logic:
            if (Consts.LOCALE.equals(Consts.HONDURAS)) {
                selectedCandidatesFrom(party.getParty_preferential_election_id());
            } else if (Consts.LOCALE.equals(Consts.ELSALVADOR)){
                if(party.isMarked()){
                    ballot.addParty(party);
                    selectedCandidatesFromES(party.getParty_preferential_election_id());
                }else{
                    ballot.removeParty(party);
                    deselectCandidateFromES(party.getParty_preferential_election_id());
                }
                // assign votes, no marks are assigned
                // vote becomes a bandera vote, this is ok.
                // add to party mark, track party mark, wtf is party mark?
            }
        }
    };

    TwoButtonDialogEditTextFragment4Boletas.OnTwoButtonBoletaDialogFragmentListener ballotListener =
            new TwoButtonDialogEditTextFragment4Boletas.OnTwoButtonBoletaDialogFragmentListener() {
                @Override
                public void onYesButtonBoletaDialogClicked(String numberOfPics) {
//                    editTextFragment.dismiss();
//                    hideSoftKeyBoard();


                    int ballots = util.parseInt(numberOfPics, -1);
                    if (ballots == -1) {
                        //reject values
                        util.createCustomToast("El numero de papeletas ingresado no es valido");
                        return;
                    } else if (ballots == 0) {
                        util.createCustomToast("El numero de papeletas debe ser mayor que zero");
                        return;
                    }
                    addBallots(ballots);
                }

                @Override
                public void onNoButtonBoletaDialogClicked() {

                }
            };


    @Override
    public void onYesButtonForTwoButtonDialogClicked(int yesIdnex) {
        switch (yesIdnex) {
            // accept button case
            case ACCEPT_MESSAGE:
                valuesAccepted();
                enableDescartar();
                break;
            case DROP_MESSAGE:
                //not running through here anymore.
//                discardBallots();
                break;
            default:
                break;
        }
    }

    private int noIndex = -1;

    @Override
    public void onNoButtonForTwoButtonDialogClickedX() {
        switch (noIndex) {// lame boris
            // accept button case
            case ACCEPT_MESSAGE:
                valuesRejected();
                break;
            case DROP_MESSAGE:
//                discardBallots();
                break;
            default:
                break;
        }
        noIndex = -1; //reset
    }

    //---------------------------------- HIDE BUTTONS ------------------------------------
    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        if (!hasfocus) {
            aceptarBtn.setVisibility(View.INVISIBLE);
            seguienteBtn.setVisibility(View.INVISIBLE);

            if(Consts.LOCALE.equals(Consts.HONDURAS))
            invalidBtn.setVisibility(View.INVISIBLE);

            findViewById(R.id.keyboardview).setVisibility(View.VISIBLE);
            findViewById(R.id.descartar_btn).setVisibility(View.INVISIBLE);

//        findViewById(R.id.button_layout).setVisibility(View.INVISIBLE);
        } else {
            aceptarBtn.setVisibility(View.VISIBLE);
            seguienteBtn.setVisibility(View.VISIBLE);
            if(Consts.LOCALE.equals(Consts.HONDURAS))
            invalidBtn.setVisibility(View.VISIBLE);

            findViewById(R.id.descartar_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.keyboardview).setVisibility(View.GONE);
//        findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
        }

    }
    //-------------------------------------------------------------------------------------
    ThreeButtonFragment.ThreeButtonListener menuListener = new ThreeButtonFragment.ThreeButtonListener() {
        @Override
        public void onFirstButtonClicked() {
            //cancel butn
        }

        @Override
        public void onSecondButtonClicked() {
            //todo: persist data
            //NULOS BALLOTS:
            int nullBallolts = nullBallot.addToCount();
            String nullCount = String.valueOf(nullBallolts);
            valuesMap.put("NULOS",nullCount);
            updatePersistBallotCount();
            updateNullInMenu(nullBallolts);
            util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
            findViewById(R.id.siguiente_btn).performClick();
        }

        @Override
        public void onThirdButtonClicked() {
            //todo: persist data
            // EMPTY BALLOTS:
            int emptyBallots = emptyBallot.addToCount();
            String emptyCount = String.valueOf(emptyBallots);
            valuesMap.put("EN BLANCO",emptyCount);
            updatePersistBallotCount();
            updateBlankInMenu(emptyBallots);
            util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
            findViewById(R.id.siguiente_btn).performClick();
        }
    };

    FourButtonFragment.ToastMenuListener toastMenuListener = new FourButtonFragment.ToastMenuListener(){
        @Override
        public void onFirstButtonClicked(){}
        @Override
        public void onSecondButtonClicked(){
            String label = invalidBtn.getText().toString();
            switch (label){
                case INVALID:
                    currentBtnSelection = NULO;
                    setButtonsInvalidRentry();
                    break;
                case RE_INVALID:
                    if(currentBtnSelection.equals(NULO)){
                        challengeHelper.createDialog(getResources().getString(R.string.confirmInvalid)+NULO,SELECT_NULO);
                    }else{
                        rejectInvalidSelection();
                    }
                    break;
                default:
                    break;
            }
        }
        @Override
        public void onThirdButtonClicked(){
            String label = invalidBtn.getText().toString();
            switch (label){
                case INVALID:
                    currentBtnSelection = EMPTY;
                    setButtonsInvalidRentry();
                    break;
                case RE_INVALID:
                    if(currentBtnSelection.equals(EMPTY)){
                        challengeHelper.createDialog(getResources().getString(R.string.confirmInvalid)+EMPTY,SELECT_EMPTY);
                    }else{
                        rejectInvalidSelection();
                    }
                    break;
                default:
                    break;
            }
        }
        @Override
        public void onFourthButtonClicked(){
            String label = invalidBtn.getText().toString();
            switch (label){
                case INVALID:
                    currentBtnSelection = IMPUGNADO;
                    break;
                case RE_INVALID:
                    if(currentBtnSelection.equals(IMPUGNADO)){
                        challengeHelper.createDialog(getResources().getString(R.string.confirmInvalid)+IMPUGNADO,SELECT_IMPUGNADO);
                    }else{
                        rejectInvalidSelection();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void rejectInvalidSelection(){
        util.createCustomToast(getResources().getString(R.string.invalidEntryMismatch));
        setButtonsToStart();
    }

    private void setButtonsReadyForNext(){
        // set the proper label:
        seguienteBtn.setText(NEXT);
        invalidBtn.setText(INVALID);
        util.setButtonColorGreen(seguienteBtn);
        util.setButtonColorRed(aceptarBtn);
        util.setButtonColorRed(invalidBtn);
    }

    private void setButtonsInvalidRentry(){
        invalidBtn.setText(RE_INVALID);
        util.setButtonColorRed(seguienteBtn);
        util.setButtonColorRed(aceptarBtn);
        util.setButtonColorGreen(invalidBtn);
    }

    private void setButtonsToStart(){
        aceptarBtn.setText(STARTBALLOT);
        seguienteBtn.setText(NEXT);
        invalidBtn.setText(INVALID);
        descartarBtn.setText(DROP);
        setButtonColorGreen(invalidBtn);
        setButtonColorRed(descartarBtn);
        setButtonColorGreen(aceptarBtn);
        setButtonColorRed(seguienteBtn);
    }
    // toast menu methods:

    private void markBallotNulo(){
        //NULOS BALLOTS:
        int nullBallolts = nullBallot.addToCount();
        String nullCount = String.valueOf(nullBallolts);
        valuesMap.put("NULOS",nullCount);
        updatePersistBallotCount();
        updateNullInMenu(nullBallolts);
        util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
        setButtonsReadyForNext();
    }

    private void markBallotAbstained(){
        // EMPTY BALLOTS:
        int emptyBallots = emptyBallot.addToCount();
        String emptyCount = String.valueOf(emptyBallots);
        valuesMap.put("ABSTENCIONES",emptyCount);
        updatePersistBallotCount();
        updateBlankInMenu(emptyBallots);
        util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
        setButtonsReadyForNext();
    }

    private void markBallotImpugnado(){
        // IMPUGNADOS BALLOTS:
        int emptyBallots = impugnadoBallot.addToCount();
        String emptyCount = String.valueOf(emptyBallots);
        valuesMap.put("IMPUGNADOS",emptyCount);
        updatePersistBallotCount();
        updateImpugnadoInMenu(emptyBallots);
        util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
        setButtonsReadyForNext();
    }

    //-------------------------------------------------------------------------------------
    @Deprecated
    public BallotDialog.BallotDialogListener getDialogListener(){
        return new BallotDialog.BallotDialogListener() {
            @Override
            public void onDialogValidClick(DialogFragment dialog) {
                //cancel button.

            }

            @Override
            public void onDialogNullClick(DialogFragment dialog) {
                //count as null, move to next ballot
                //todo: check that is string string and not string int.
                int nullBallolts = nullBallot.addToCount();
                String nullCount = String.valueOf(nullBallolts);
                valuesMap.put("NULOS",nullCount);
                updatePersistBallotCount();
                updateNullInMenu(nullBallolts);
                util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
                (dialog.getActivity().findViewById(R.id.siguiente_btn)).performClick();


            }

            @Override
            public void onDialogBlankClick(DialogFragment dialog) {
                //count as blank, move to next ballot
                int emptyBallots = emptyBallot.addToCount();
                String emptyCount = String.valueOf(emptyBallots);
                valuesMap.put("EN BLANCO",emptyCount);
                updatePersistBallotCount();
                updateBlankInMenu(emptyBallots);
                util.savePreferences("HowManyBallotSoFar",currentBallotNumber);
                (dialog.getActivity().findViewById(R.id.siguiente_btn)).performClick();
            }
        };
    }

    //------------------------------------------------------------------------------------
}
