package com.afilon.mayor.v11.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.NoCheckboxArrayAdapter;
import com.afilon.mayor.v11.fragments.TwoButtonDialogEditTextFragment4Boletas;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogRechesarButtonFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogToConfirmAddBoletasFragment;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui.DialogToConfirmDuiListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogEditTextFragment4Boletas.OnTwoButtonBoletaDialogFragmentListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogRechesarButtonFragment.OnTwoButtonDialogForRechesarButtonFragmentListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogToConfirmAddBoletasFragment.OnTwoButtonDialogToConfirmAddBoletasFragmentListener;
import com.afilon.mayor.v11.model.Candidate;
import com.afilon.mayor.v11.model.CrossVoteBundle;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.PreferentialPartyVotes;
import com.afilon.mayor.v11.model.PreferentialVotoBanderas;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;
import com.google.gson.Gson;

public class ParlacenCandidateListActivity extends ListActivity implements
        DataResponseCallback, OnTwoButtonBoletaDialogFragmentListener,
        OnTwoButtonDialogToConfirmAddBoletasFragmentListener {

    private static final String CLASS_TAG = "ParlacenCandidateListActivity";
    private TwoButtonDialogFragment twoBtnDialogFragment;
    private boolean isAccepted;
    private Utilities util;
    private Escrudata escrudata;
    private VotingCenter vc;
    private Button aceptarBtn;
    private Button rechezarBtn;
    private NoCheckboxArrayAdapter adapter;
    private int partyArrayIndex;
    private DatabaseAdapterParlacen db_adapter;
    private ArrayList<Party> partyArrayList;
    private TextView votecenter_tv;
    private ArrayList<Candidate> selectedCandidatesList;
    private Button addBoletaBtn;
    private TwoButtonDialogRechesarButtonFragment twoBtnDialogRechesarFragment;
    private TwoButtonDialogToConfirmAddBoletasFragment twoBtnDialogToConfirmAddBoletasFragment;
    //    private TwoButtonDialogFragment twoBtnDialogToConfirmAddBoletasFragment;
    private CustomKeyboard mCustomKeyboard;

    private ChallengeHelper challengeHelper;
    private static int
            ANADIR = 1,
            RECHAZAR = 2,
            ACEPTAR = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = new Utilities(this);
        util.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_candidate_table);

        Log.i("CLASS NAME : ", CLASS_TAG);

        // Trap unexpected error
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(ParlacenCandidateListActivity.this));

        mCustomKeyboard = new CustomKeyboard(this, R.id.keyboardview, R.xml.tenhexkbd);
        votecenter_tv = (TextView) findViewById(R.id.vote_center);

        TextView jvr_tv = (TextView) findViewById(R.id.textView13);
        TextView departamento_tv = (TextView) findViewById(R.id.textView14);

        Bundle b = getIntent().getExtras();
        vc = b.getParcelable("com.afilon.tse.votingcenter");
        escrudata = b.getParcelable("com.afilon.tse.escrudata");
        partyArrayIndex = b.getInt("partyNumber");

        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();


        //---------------------------CHALLENGE HELPER INITIATION ----------------------------------
        //-----------------------------------------------------------------------------------------
        challengeHelper = new ChallengeHelper(this);
        // REGISTER ROUTINES TO CHALLENGE HELPER:
        challengeHelper.addRoutine(ANADIR, anadir);
        challengeHelper.addRoutine(RECHAZAR, rechazar);
        challengeHelper.addRoutine(ACEPTAR, aceptar);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);// FOR DUI CONFIRMATION ONLY
        challengeHelper.setTools(util, db_adapter); //TOOLS ARE FOR DUI CONFIRMATION ONLY
        //----------------------------------------------------------------------------------------
        final String voteType = getResources().getString(R.string.voteType);
        partyArrayList = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());
        List<PreferentialPartyVotes> prefPartyVotes = db_adapter.getPartiesPreferentialVotes();

        for (Party party : partyArrayList) {
            for (PreferentialPartyVotes ppv : prefPartyVotes) {

                if (party.getParty_preferential_election_id().equals(ppv.getParty_preferential_election_id())) {
                    party.setParty_votes("" + ppv.getParty_votes());
                    party.setParty_boletas("" + ppv.getParty_boletas());
                }
            }
        }


        selectedCandidatesList = b.getParcelableArrayList("selectedCandidates");

        int currentBallotNumber = util.loadPreferences("currentBallotNumber");

        int totalNumberOfBallots = (int) util.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes(), 0);
        String outMessage;
        if (currentBallotNumber == totalNumberOfBallots) {
            outMessage = "Boletas Contadas: " + currentBallotNumber + " / Votos " + partyArrayList.get(partyArrayIndex).getParty_name() + ": " + String.valueOf(totalNumberOfBallots);
            votecenter_tv.setText(outMessage);
            votecenter_tv.setTextColor(getResources().getColor(R.color.Black));
        } else {
            outMessage = "Boletas Contadas : " + currentBallotNumber + " / Votos " + partyArrayList.get(partyArrayIndex).getParty_name() + ": " + String.valueOf(totalNumberOfBallots) + "  -  \u00A1Existe Error de Escrutunio!";
            votecenter_tv.setText(outMessage);
            votecenter_tv.setTextColor(getResources().getColor(R.color.Red));
        }

        departamento_tv.setText(vc.getDepartamentoString());
        jvr_tv.setText(vc.getJrvString());

        addBoletaBtn = (Button) findViewById(R.id.extra_boleta_btn);
        addBoletaBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                challengeHelper.createDuiChallenge(getResources().getString(R.string.addBallotMessabe),ANADIR);
//                challengeHelper.createSingleDuiChallenge(ANADIR);
            }
        });

        aceptarBtn = (Button) findViewById(R.id.aceptar_btn);
        aceptarBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!isAccepted) {
                    challengeHelper.createDialog("¿DESEA ACEPTAR LOS RESULTADOS?", ACEPTAR);
                } else {
                    util.createCustomToast("YA HA SIDO ACEPTADA", "");
                }
            }
        });

        rechezarBtn = (Button) findViewById(R.id.rechezar_btn);
        rechezarBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!isAccepted) {
                    challengeHelper.createDialog("¿DESEA RECHAZAR LOS RESULTADOS?", RECHAZAR);
                } else {
                    util.createCustomToast("YA HA SIDO ACEPTADA", "NO PUEDE RECHAZAR");
                }
            }
        });

        if (selectedCandidatesList != null) {
            adapter = new NoCheckboxArrayAdapter(this, selectedCandidatesList);
            setListAdapter(adapter);
        } else {

            //list is empty fill from db
            selectedCandidatesList = db_adapter.getParlacenCandidatesArrayList(partyArrayList.get(partyArrayIndex).getParty_preferential_election_id());
            // insert into vote database: for the case that zero votes were caster for a party:
            Log.e(partyArrayList.get(partyArrayIndex).getParty_name(), "GOT ZERO VOTES");
            Log.e("CANDIDATE LIST", "INSERTING VOTES FOR EMPTY PARTY");
            for (Candidate candidate : selectedCandidatesList) {
                Log.e(candidate.getCandidate_name(), "ASSIGNED " + candidate.getVotesNumber() + " VOTES");
                db_adapter.insertPreferentialCandidateVote(Integer.valueOf(vc.getJrvString()), candidate.getPartyPreferentialElectionID(),
                        candidate.getCandidatePreferentialElectionID(),
                        candidate.getVotesNumber(),
                        candidate.getBanderaNumbers(),
                        candidate.getPreferentialVotes(),
                        vc.getPref_election_id());
                db_adapter.insertMarks(vc.getJrvString(), vc.getPref_election_id(), candidate.getCandidatePreferentialElectionID(), candidate.getPartyPreferentialElectionID(), "4", "0");
            }
            /** also if list is empty threre is no party Preferential votes assigned to the table
             * add here
             //-------------------------------------------------------------------------------- */
            Log.e("PARTY W/ ZERO", "VOTES IS " + partyArrayList.get(partyArrayIndex).getParty_name());
            //insert votes
            PreferentialVotoBanderas emptyVote = new PreferentialVotoBanderas();
            emptyVote.setJrv(vc.getJrvString());
            emptyVote.setParty_cross_votes(0f);
            emptyVote.setParty_votes(0f);
            emptyVote.setParty_preferential_votes(0f);
            emptyVote.setParty_preferential_election_id(partyArrayList.get(partyArrayIndex).getParty_preferential_election_id());
            emptyVote.setBandera_preferential_election_id(partyArrayList.get(partyArrayIndex).getParty_preferential_election_id());
            emptyVote.setPreferential_election_id(partyArrayList.get(partyArrayIndex).getPref_election_id());
            db_adapter.insertBanderaVotes(emptyVote, vc.getJrvString());
            //------------------------------------------------------------------------------------

            adapter = new NoCheckboxArrayAdapter(this, selectedCandidatesList);
            setListAdapter(adapter);
            if (!(Consts.LOCALE.contains("HON") && voteType.contains("PREF"))) {
                util.createCustomToast(
                        "Partido "
                                + partyArrayList.get(partyArrayIndex)
                                .getParty_name(), "No Saco Votos");
            }
        }
        // add empty view to the end of the list
        View view = getLayoutInflater().inflate(R.layout.empty_text_view, null);
        TextView oneMoreLineToListView = (TextView) view.findViewById(R.id.empty_text_view);
        getListView().addFooterView(oneMoreLineToListView);
        if (util.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes(), 0) < 1f) {
            util.setButtonColorRed(rechezarBtn);
        }

        if (Consts.LOCALE.contains("HON") && voteType.contains("PREF")) {
//            aceptarBtn.performClick();
            aceptarRoutine();
        }


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

/*        Candidate item = (Candidate) getListAdapter().getItem(position);
        util.createCustomToast(item.getCandidatePreferentialElectionID() + " "
                + item.getPartyPreferentialElectionID(), item.getCandidateID()
                + " " + item.getPartyName());*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fifth, menu);
        return true;
    }

    //CARLOS:
    public void createDialogToConfirmAddBoletas(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        twoBtnDialogToConfirmAddBoletasFragment = new TwoButtonDialogToConfirmAddBoletasFragment();
//        twoBtnDialogToConfirmAddBoletasFragment = new TwoButtonDialogFragment();
        twoBtnDialogToConfirmAddBoletasFragment.setOnButtonsClickedListenerOne(this);
//                .setOnButtonsClickedListenerOne(this);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Si");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        bndl.putString("invisible", "visible");
        twoBtnDialogToConfirmAddBoletasFragment.setArguments(bndl);
        twoBtnDialogToConfirmAddBoletasFragment.show(fm,
                "new dialog to confirm add boletas");
    }

    TwoButtonDialogEditTextFragment4Boletas twoBtnDialogEditTextFragment = new TwoButtonDialogEditTextFragment4Boletas();

    public void createDialogEditText(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();

        twoBtnDialogEditTextFragment.setOnButtonsClickedListenerOne(this);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        bndl.putString("invisible", "invisible");
        twoBtnDialogEditTextFragment.setArguments(bndl);
        twoBtnDialogEditTextFragment.show(fm, "new triage dialog");

    }

    @Override
    public void onRequestDataSuccess(String response) {
        // TODO Auto-generated method stub
        util.createCustomToast(getResources().getString(R.string.dui)+" CONFIRMED");
        createDialogEditText("INGRESAR CANTIDAD DE BOLETAS ADICIONALES ", 1);
    }

    @Override
    public void onRequestDataError(Exception error) {
        // TODO Auto-generated method stub
        util.createCustomToast(getResources().getString(R.string.dui)+" NOT CONFIRMED");
    }

    @Override
    public void onYesButtonBoletaDialogClicked(String numberOfBoletasAdded) {
        int intNumberOfBoletas = util.parseInt(numberOfBoletasAdded, 0);

        createDialogToConfirmAddBoletas("Ingreso " + intNumberOfBoletas + " adicionales.  ¿Es esta la cantidad correcta? ",
                intNumberOfBoletas);

    }

    @Override
    public void onYesButtonForToConfirmAddBoletasDialogClicked(int yesIdnex) {
        if (yesIdnex > 0) {
            String partyBoletas = String.valueOf(yesIdnex + util.parseInt(partyArrayList.get(partyArrayIndex).getParty_boletas(), 0));

            db_adapter = new DatabaseAdapterParlacen(this);
            db_adapter.open();

            db_adapter.updateNumberOfChangeBoletasAndPartyBoletasToParty(String.valueOf(yesIdnex), partyBoletas, partyArrayList.get(partyArrayIndex).getParty_preferential_election_id());
            //set flag to reset:
            //TODO
            util.savePreferences("anadir", true);
            util.savePreferences("firstScreen", false);

            Bundle b = new Bundle();
            b.putParcelable("com.afilon.tse.escrudata", escrudata);
            b.putParcelable("com.afilon.tse.votingcenter", vc);
            b.putInt("partyNumber", partyArrayIndex);
            Intent search = new Intent(ParlacenCandidateListActivity.this, Consts.VOTETABLEACT);


            search.putExtras(b);
            startActivity(search);
            finish();
        } else {
            twoBtnDialogEditTextFragment.dismiss();
        }
    }

    @Override
    public void onNoButtonBoletaDialogClicked() {
        // TODO Auto-generated method stub

    }

//    @Override
//    public void onYesButtonForToConfirmAddBoletasDialogClicked(int yesIdnex) {
//        if (yesIdnex > 0) {
//            String partyBoletas = String.valueOf(yesIdnex
//                    + util.parseInt(partyArrayList.get(partyArrayIndex)
//                    .getParty_boletas(), 0));
//
//            db_adapter = new DatabaseAdapterParlacen(this);
//            db_adapter.open();
//
//            db_adapter.updateNumberOfChangeBoletasAndPartyBoletasToParty(String
//                            .valueOf(yesIdnex), partyBoletas,
//                    partyArrayList.get(partyArrayIndex)
//                            .getParty_preferential_election_id());
//            //set flag to reset:
//            //TODO
//            util.savePreferences("anadir", true);
//            util.savePreferences("firstScreen", false);
//
//            Bundle b = new Bundle();
//            b.putParcelable("com.afilon.tse.escrudata", escrudata);
//            b.putParcelable("com.afilon.tse.votingcenter", vc);
//            b.putInt("partyNumber", partyArrayIndex);
//            Intent search = new Intent(ParlacenCandidateListActivity.this,
//                    Consts.VOTETABLEACT);
//
//
//            search.putExtras(b);
//            startActivity(search);
//            finish();
//        } else {
//            twoBtnDialogEditTextFragment.dismiss();
//        }
//    }

    @Override
    public void onNoButtonForToConfirmAddBoletasDialogClicked() {
        // TODO Auto-generated method stub

    }

    private void reLoadNextPartyInThisActivity() {
        Log.e("ACCEPTED", "NEXT PARTY HAS ZERO VOTES");
        final String voteType = getResources().getString(R.string.voteType);
        String outMessage = " Votos "
                + partyArrayList.get(partyArrayIndex)
                .getParty_name()
                + ": "
                + String.format(Locale.US, "%.0f", Float.parseFloat(partyArrayList.get(partyArrayIndex)
                .getParty_votes()));

        votecenter_tv.setText(outMessage);
        votecenter_tv.setTextColor(getResources().getColor(
                R.color.Black));


        if (selectedCandidatesList != null) {
            selectedCandidatesList.clear();
            if (!db_adapter.isOpen()) {
                db_adapter.open();
            }
            selectedCandidatesList = db_adapter.getParlacenCandidatesArrayList(partyArrayList.
                    get(partyArrayIndex).getParty_preferential_election_id());
            util.savePreferences("currentBallotNumber", 0);
            util.savePreferences("firstScreen", true);
            for (Candidate selected : selectedCandidatesList) {
                db_adapter.insertPreferentialCandidateVote(
                        Integer.parseInt(vc.getJrvString()),
                        selected.getPartyPreferentialElectionID(),
                        selected.getCandidatePreferentialElectionID(),
                        selected.getVotesNumber(),
                        selected.getBanderaNumbers(),
                        selected.getPreferentialVotes(),
                        vc.getPref_election_id());
                db_adapter.insertMarks(vc.getJrvString(), vc.getPref_election_id(), selected.getCandidatePreferentialElectionID(), selected.getPartyPreferentialElectionID(), "4", "0");
            }
            // insert bandera votes:
            PreferentialVotoBanderas votoBandera = new PreferentialVotoBanderas();
            votoBandera.setJrv(vc.getJrvString());
            votoBandera.setPreferential_election_id(partyArrayList.get(partyArrayIndex).getPref_election_id());
            String partyElectionId = partyArrayList.get(partyArrayIndex).getParty_preferential_election_id();
            votoBandera.setBandera_preferential_election_id(partyElectionId);
            votoBandera.setParty_preferential_election_id(partyElectionId);
            votoBandera.setParty_votes(0.0f);
            votoBandera.setParty_preferential_votes(0);
            db_adapter.insertBanderaVotes(votoBandera, vc.getJrvString());
            db_adapter.close();
            adapter = new NoCheckboxArrayAdapter(this, selectedCandidatesList);
            setListAdapter(adapter);
        }
        util.setButtonColorRed(rechezarBtn);
        isAccepted = false;
        if (Consts.LOCALE.contains("HON") && voteType.contains("PREF")) {
            aceptarRoutine();
        } else {
            util.createCustomToast(
                    "Partido "
                            + partyArrayList.get(partyArrayIndex)
                            .getParty_name(), "No Saco Votos");
        }


    }

    private void loadNextPartyPreferentialVoteAcitvity() {
        Log.e("ACCEPTED", "NEXT PARTY HAS VOTES");
        util.savePreferences("currentBallotNumber", 0);
        util.savePreferences("firstScreen", true);

        Bundle b = new Bundle();
        b.putParcelable("com.afilon.tse.escrudata", escrudata);
        b.putParcelable("com.afilon.tse.votingcenter", vc);
        b.putInt("partyNumber", partyArrayIndex);
        Intent search = new Intent(
                ParlacenCandidateListActivity.this,
                Consts.VOTETABLEACT);
        search.putExtras(b);
        startActivity(search);
        finish();
    }

    private void loadCrossVotesActivity() {
        ArrayList<CrossVoteBundle> CrossVoteB = new ArrayList<CrossVoteBundle>();
        Bundle b = new Bundle();
        b.putParcelable("com.afilon.tse.escrudata", escrudata);
        b.putParcelable("com.afilon.tse.votingcenter", vc);
        b.putString("currentJrv", vc.getJrvString());
        CrossVoteB = null;
        b.putParcelable("crossVoteBundle", (Parcelable) CrossVoteB);
        Intent search = new Intent(ParlacenCandidateListActivity.this,
                Consts.CROSSVOTEACT);
        search.putExtras(b);
        startActivity(search);
        finish();
    }

    private void loadVoteSummary() {
        Bundle b = new Bundle();
        escrudata.setPageTitle(Consts.TOTAL_VOTE_SUMMARY);
        b.putParcelable("com.afilon.tse.escrudata", escrudata);
        b.putParcelable("com.afilon.tse.votingcenter", vc);
//        b.putString("title", Consts.TOTAL_VOTE_SUMMARY);

        Intent search = new Intent(ParlacenCandidateListActivity.this,
                Consts.SUMCROSSACT);
        search.putExtras(b);
        startActivity(search);
        finish();

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
        Log.d("CandidateListActivity", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    private ChallengeHelper.OnApprove anadir = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            anadirRoutine();
        }
    };

    private ChallengeHelper.OnApprove rechazar = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            rechazarRoutine();
        }
    };

    private ChallengeHelper.OnApprove aceptar = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            aceptarRoutine();
        }
    };

    private void anadirRoutine() {
        createDialogEditText("INGRESAR CANTIDAD DE BOLETAS ADICIONALES ", 1);
    }

    private void rechazarRoutine() {

        String partyId = partyArrayList.get(partyArrayIndex).getParty_preferential_election_id();
        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();
        db_adapter.deleteAllPreferentialCandidateVotes(partyId);
        db_adapter.deletePreferentialVotoBanderas(partyId);
        util.savePreferences("firstScreen", true);
        db_adapter.deleteAllCandidateMarks(partyId);
        util.savePreferences("currentBallotNumber", 0);
        Bundle b = new Bundle();
        b.putParcelable("com.afilon.tse.escrudata", escrudata);
        b.putParcelable("com.afilon.tse.votingcenter", vc);
        b.putInt("partyNumber", partyArrayIndex);
        Intent search = new Intent(ParlacenCandidateListActivity.this, Consts.VOTETABLEACT);
        search.putExtras(b);
        startActivity(search);
        finish();

    }

    private void aceptarRoutine() {
        isAccepted = true;
        /** if the party array list is less than the the last index (array size -1) */
        if (partyArrayIndex < partyArrayList.size() - 1) {
            /** increase the party index*/
            partyArrayIndex++;
            if (Float.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes()) == 0) {
                /** else if the next party to load received zero votes then reload this screen */
                reLoadNextPartyInThisActivity();
            } else {
                loadNextPartyPreferentialVoteAcitvity();
            }

        } else if (util.loadPreferences(Consts.VOTO_CRUZADO) > 0) {
            /** if the array index is greater than the last index and there are more than 0
             cross votes: */
            loadCrossVotesActivity();
        } else {
            /**  finally just go to the summary: */
            loadVoteSummary();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        if (!hasfocus) {
            findViewById(R.id.button_layout).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
        }

    }
}
