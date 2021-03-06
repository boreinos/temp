package com.afilon.mayor.v11.activities;

/**
 * Created by Boris on 10/1/2017.
 */
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.afilon.mayor.v11.adapters.PartyListAdapter;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.FourButtonFragment;
import com.afilon.mayor.v11.fragments.ThreeButtonFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogEditTextFragment4Boletas;
import com.afilon.mayor.v11.model.DirectParty;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class VoteCounterActivityES extends AfilonActivity {
    PartyListAdapter view_adapter;
    DatabaseAdapterParlacen db_adapter;
    VotingCenter vc;
    Escrudata escrudata;
    ArrayList<DirectParty> partyList;
    Utilities utilities;
    private LinkedHashMap valuesMap;

    private int ballotNumber = 0, ballotTotal = 0;
    private static final int ADD_BALLOT       = 1,
                             DROP_BALLOT      = 2,
                             SELECT_NULO      = 3,
                             SELECT_EMPTY     = 4,
                             SELECT_IMPUGNADO = 5,
                             SELECT_CRUZADO   = 6;
    VoteCounterActivityES.BallotBreakdown summary;
    private ChallengeHelper challengeHelper;
    private static final String INVALID  = "Papeleta\nInvalida",
                                DISCARD  = "Descartar\nRestantes",
                                ADD      = "Añadir\nPapeletas",
                                INGRESSO = "Ingresar",
                                SAVE     = "Guardar",
                                REENTER  = "Reingresar",
                                CONFIRM  = "Confirmar",
                                CORREGIR = "Corregir",
                                ACCEPT   = "Aceptar",
                                NEXT     = "Proxima",
                                CANCEL   = "Cancelar",
                                NULO     = "Nulo",
                                EMPTY    = "Abstencion",
                                RE_INVALID ="Re-invalido",
                                IMPUGNADO= "Impugnado",
                                CROSS    = "Cruzado";
    private String currentBtnSelection;
    private Button invalidBtn,
                   discardBtn,
                   ingressoBtn,
                   acceptBtn,
                   nextBtn,
                   crossBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vote_counter_es);
        utilities = new Utilities(this);
        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();

        //------------------------------------------------------------------------------------+
        challengeHelper = new ChallengeHelper(this);
        // REGISTER ROUTINES TO CHALLENGE HELPER:
        challengeHelper.addRoutine(ADD_BALLOT, add_ballots);
        challengeHelper.addRoutine(DROP_BALLOT, drop_ballots);
        challengeHelper.addRoutine(SELECT_NULO,select_nulo);
        challengeHelper.addRoutine(SELECT_EMPTY,select_blank);
        challengeHelper.addRoutine(SELECT_IMPUGNADO,select_impugnado);
        challengeHelper.addRoutine(SELECT_CRUZADO,select_cruzado);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);
        challengeHelper.setTools(utilities, db_adapter);
        //-----------------------------------------------------------------------------------
        Bundle b = getIntent().getExtras();
        vc = b.getParcelable("com.afilon.tse.votingcenter");
        escrudata = b.getParcelable("com.afilon.tse.escrudata");
        utilities.saveCurrentScreen(this.getClass(),b);
        summary = new VoteCounterActivityES.BallotBreakdown();

        Gson gson = new Gson();
        String map = escrudata.getValueMap();
        valuesMap = gson.fromJson(map,LinkedHashMap.class);
        String votes = escrudata.getPartyVotes();

        summary.setInitial( utilities.parseInt((String)valuesMap.get("VOTOS VALIDOS"),0),
                           utilities.parseInt((String)valuesMap.get("NULOS"),0),
                           utilities.parseInt((String)valuesMap.get("ABSTENCIONES"),0),
                           utilities.parseInt((String)valuesMap.get("IMPUGNADOS"),0));

        ((TextView) findViewById(R.id.valid_votes)).setText(String.valueOf(summary.getValidBallots()));
        ((TextView) findViewById(R.id.null_votes)).setText(String.valueOf(summary.getNullBallots()));
        ((TextView) findViewById(R.id.enblanco_votes)).setText(String.valueOf(summary.getEmptyBallots()));
        ((TextView) findViewById(R.id.grantotal_votes)).setText(String.valueOf(summary.getGranTotalBallots()));
        ((TextView) findViewById(R.id.cruzado_votes)).setText(String.valueOf(summary.getCruzadoBallots()));
        ((TextView) findViewById(R.id.impugnado_votes)).setText(String.valueOf(summary.getImpugnados()));
        //-------------------------------------------------------------------------------------
        // set ballot Number:
        ballotNumber = utilities.loadPreferences("ballotNumber") + 1;
        ballotTotal =  utilities.parseInt(escrudata.getEntregadas(),0);  // utilities.parseInt((String)valuesMap.get("UTILIZADAS"),0); //utilized ballots //todo: update utilizadas
        ((TextView) findViewById(R.id.current_ballot)).setText(String.valueOf(ballotNumber));
        ((TextView) findViewById(R.id.total_ballots)).setText(String.valueOf(ballotTotal));
        //-------------------------------------------------------------------------------------
        //set party information:
        //todo load up values
        if(votes==null){
            partyList = new ArrayList<>();
            ArrayList<Party> partyArrayList = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());

            for (Party party : partyArrayList) {
                DirectParty dparty = new DirectParty();
                dparty.setPref_Election_id(party.getPref_election_id());
                dparty.setParty_preferential_election_id(party.getParty_preferential_election_id());
                dparty.setParty_name(party.getParty_name());
                dparty.setParty_order(party.getParty_order());
                dparty.setImgResId(getResources().getIdentifier(party.getParty_name().toLowerCase(), "drawable", getApplicationContext().getPackageName()));
                partyList.add(dparty);
            }
        }else {
            Type arrayObject = new TypeToken<ArrayList<DirectParty>>(){}.getType();
            partyList = gson.fromJson(votes,arrayObject);
            // clear check marks:
            for(DirectParty party: partyList){
                party.deselect();
                party.unConfirm();
            }
        }

        view_adapter = new PartyListAdapter(this, R.layout.party_item, partyList);
        view_adapter.setListener(checkListener);
        ListView table = (ListView) findViewById(R.id.party_list);
        table.setAdapter(view_adapter);
        // end set up layout ---------------------------------------------------------------------
        // test if return from being closed:
        initiateButtons();
        if (ballotNumber > ballotTotal) {
            utilities.setButtonColorRed(ingressoBtn);
            utilities.setButtonColorRed(invalidBtn);
            utilities.setButtonColorRed(acceptBtn);
            utilities.setButtonColorGreen(nextBtn);
        }
    }

    private void initiateButtons() {
        invalidBtn = (Button) findViewById(R.id.invalid_btn);
        discardBtn = (Button) findViewById(R.id.discard_btn);
        ingressoBtn = (Button) findViewById(R.id.ingreso_btn);
        acceptBtn = (Button) findViewById(R.id.accept_btn);
        nextBtn = (Button) findViewById(R.id.nextballot_btn);
        crossBtn = (Button) findViewById(R.id.crossvote_btn);
        if(getResources().getString(R.string.electionType).equals(Consts.ASAMBLEA)){
            crossBtn.setVisibility(View.VISIBLE);
            findViewById(R.id.cruzado_label).setVisibility(View.VISIBLE);
            findViewById(R.id.cruzado_votes).setVisibility(View.VISIBLE);
        }
        // set text and color:
        setButtonsToStart();
        // set Listeners:
        ingressoBtn.setOnClickListener(getIngresoListener());
        acceptBtn.setOnClickListener(getAcceptListener());
        nextBtn.setOnClickListener(getNextListener());
        discardBtn.setOnClickListener(getDropListener());
        invalidBtn.setOnClickListener(getInvalidMenu());
        crossBtn.setOnClickListener(getCrossvoteListener());
    }

    private View.OnClickListener getIngresoListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                String label = btn.getText().toString();
                switch (label) {
                    case INGRESSO:
                        view_adapter.intiateFirstEntry();
                        nextLabel = REENTER;
                        utilities.setButtonColorRed(btn);
                        utilities.setButtonColorRed(invalidBtn);
                        utilities.setButtonColorRed(crossBtn);
                        break;
                    case SAVE:
                        // turn to save mode:
//                        view_adapter.transition();
                        view_adapter.saveMode(true);

                        btn.setText(nextLabel);
                        break;
                    case REENTER:
                        //intiate reentry:
                        utilities.setButtonColorRed(btn);
                        view_adapter.initiateReEntry();
                        nextLabel = CONFIRM;
                        break;
                    case CONFIRM:
                        utilities.setButtonColorRed(btn);
                        view_adapter.IsInReview(true);
                        if (!mismatches()) {
                            utilities.setButtonColorGreen(acceptBtn);
                        }
                        break;
                    case CORREGIR:
                        btn.setText(INGRESSO);
                        view_adapter.correctMode(true);
                        break;

                }

            }
        };
    }

    private View.OnClickListener getAcceptListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save votes in db:
                updateVotes();
                utilities.setButtonColorRed((Button) v);
                utilities.setButtonColorAmber(discardBtn);
                discardBtn.setText((ballotNumber < ballotTotal)?DISCARD:ADD);
                utilities.setButtonColorGreen(nextBtn);
                String textValid = String.valueOf(summary.addValidBallot());
                valuesMap.put("VOTOS VALIDOS",textValid);
                updatePersistBallotCount();
                ((TextView) findViewById(R.id.valid_votes)).setText(textValid);
                ((TextView)findViewById(R.id.grantotal_votes)).setText(String.valueOf(summary.getGranTotalBallots()));
            }
        };
    }

    private View.OnClickListener getNextListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ballotNumber < ballotTotal) {
                    utilities.setButtonColorRed((Button) v);
                    utilities.setButtonColorRed(discardBtn);
                    nextBallot();
                } else {
                    // next Screen:
                    nextActivity();
                }
            }
        };
    }

    private View.OnClickListener getDropListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String label = ((Button) v).getText().toString();
                switch (label) {
                    case DISCARD:
                        int remaining = ballotTotal - ballotNumber;
                        String discardMessage = getResources().getString(R.string.dropBallotMessage);
                        challengeHelper.createDuiChallenge(String.format(discardMessage, remaining), DROP_BALLOT);
                        break;
                    case ADD:
                        challengeHelper.createDuiChallenge(getResources().getString(R.string.addBallotMessabe), ADD_BALLOT);
                        break;
                }
            }
        };
    }

    private View.OnClickListener getInvalidMenu(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                challengeHelper.createFourButtonMenu(CANCEL,NULO,EMPTY,IMPUGNADO,toastMenuListener);
            }
        };
    }

    private View.OnClickListener getCrossvoteListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                challengeHelper.createDialog(getResources().getString(R.string.confirmInvalid)+CROSS,SELECT_CRUZADO);
            }
        };
    }

    private void nextActivity() {
        //todo: persit ballot number and total ballots
        calculateEscrutadasFaltantes();
        if(!db_adapter.isOpen())db_adapter.open();
        db_adapter.updateConceptsCount(valuesMap,vc.getJRV()); // Save Concepts to DB
        storeVotesInDB();
        Intent search = new Intent(VoteCounterActivityES.this,  Consts.CONCEPTOACT);
        search.putExtras(prepareBundle());
        startActivity(search);
        finish();
    }

    private void calculateEscrutadasFaltantes(){
        int escrutadas = utilities.parseInt(escrudata.getSobrantes(),0) + utilities.parseInt(escrudata.getInutilizadas(),0) + summary.getGranTotalBallots();
        int faltantes = ballotTotal - summary.getGranTotalBallots(); //ballotTotal is entregadas
        valuesMap.put("ESCRUTADAS",String.valueOf(escrutadas));
        valuesMap.put("FALTANTES",String.valueOf(faltantes));
        valuesMap.put("SOBRANTES",escrudata.getSobrantes());
        valuesMap.put("INUTILIZADAS",escrudata.getInutilizadas());
        valuesMap.remove("GRAN TOTAL");
        valuesMap.remove("VOTOS VALIDOS"); //todo: find out if maybe we should keep them.
        Gson gson = new Gson();
        String jsonValueMap = gson.toJson(valuesMap);
        escrudata.setValuMap(jsonValueMap);
    }

    private boolean mismatches() {
        for (int k = 0; k < partyList.size(); k++) {
            if (!partyList.get(k).match()) {
                return true;
            }
        }
        return false;
    }

    private void updateVotes() {
        for (int k = 0; k < partyList.size(); k++) {
            partyList.get(k).setUpdatedAccumulation();
        }
        view_adapter.notifyDataSetChanged();
    }

    private void storeVotesInDB(){
        for (int k = 0; k < partyList.size(); k++) {
            db_adapter.insertPartiesPreferentialVotes(partyList.get(k),vc.getJRV()); //save votes to db
        }
    }

    private void nextBallot() {
        for (int k = 0; k < partyList.size(); k++) {
            partyList.get(k).setCurrentAccumulation();
            partyList.get(k).reset();
        }

        view_adapter.correctMode(false);
        view_adapter.saveMode(true);
        setButtonsToStart();
        ((TextView) findViewById(R.id.current_ballot)).setText(String.valueOf(++ballotNumber));

        //todo: persist data sqlite

    }

    private void updatePersistBallotCount(){
        valuesMap.put("GRAN TOTAL",String.valueOf(summary.getGranTotalBallots()));
        Gson gson = new Gson();
        String jsonValueMap = gson.toJson(valuesMap);
        String jsonParties = gson.toJson(partyList);
        escrudata.setValuMap(jsonValueMap);
        escrudata.setPartyVotes(jsonParties);
        utilities.savePreferences("ballotNumber",ballotNumber);
        utilities.saveCurrentScreen(this.getClass(),prepareBundle());
        //todo: save update on database.

    }

    private Bundle prepareBundle(){
        Bundle b = new Bundle();
        b.putParcelable("com.afilon.tse.votingcenter", vc);
        b.putParcelable("com.afilon.tse.escrudata", escrudata);
        return b;
    }

    String nextLabel;

    PartyListAdapter.PartyListListener checkListener = new PartyListAdapter.PartyListListener() {
        @Override
        public void onItemSelected() {
            ingressoBtn.setText(SAVE);
            utilities.setButtonColorGreen(ingressoBtn);
        }

        @Override
        public void onItemDeselected() {
            utilities.setButtonColorRed(ingressoBtn);
        }

        @Override
        public void onMatch() {
            utilities.setButtonColorGreen(acceptBtn);
        }

        @Override
        public void onMisMatch() {
            ingressoBtn.setText(CORREGIR);
            utilities.setButtonColorGreen(ingressoBtn);

        }
    };

    private class BallotBreakdown {
        private int validBallots = 0;
        private int nullBallots = 0;
        private int emptyBallots = 0;
        private int impugnadoBallots = 0;
        private int cruzadoBallots = 0;
        private int granTotalBallots;

        public int addValidBallot() {
            return ++validBallots;
        }

        public int addNullBallot() {
            return ++nullBallots;
        }

        public int addEmptyBallot() {
            return ++emptyBallots;
        }

        public int addCruzadoBallot(){++validBallots; return ++cruzadoBallots;}

        public int getCruzadoBallots(){
            return cruzadoBallots;
        }

        public int getGranTotalBallots() {
            return validBallots + nullBallots + emptyBallots+impugnadoBallots;  // cruzados are part of valid votes
        }

        public int getValidBallots() {
            return validBallots;
        }

        public int getNullBallots() {
            return nullBallots;
        }

        public int getEmptyBallots() {
            return emptyBallots;
        }

        public int addImpugnados(){ return ++impugnadoBallots;}

        public int getImpugnados() {return impugnadoBallots;}

        public void setInitial( int valid, int nullb, int empty, int impugnados){

            validBallots=valid;
            this.impugnadoBallots = impugnados;
            nullBallots=nullb;
            emptyBallots=empty;
        }
    }


    private void addBallots(int ballots) {
        ballotTotal = ballotNumber + ballots; // add to toal
        String textBallots = String.valueOf(ballotTotal);
        valuesMap.put("UTILIZADAS",textBallots);
        ((TextView) findViewById(R.id.total_ballots)).setText(textBallots);
        ((Button) findViewById(R.id.descartar_btn)).setText(DISCARD);
        updatePersistBallotCount();
        nextBallot();

    }

    private void discardBallots() {
        nextActivity();
    }

    private void keepDbOpen(){
        if(!db_adapter.isOpen())db_adapter.open();
    }

    //----------- Toast Callers and Listeners: -------------------------------
    private ChallengeHelper.OnApprove add_ballots = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            createDialogEditText("INGRESAR CANTIDAD DE PAPELETAS ADICIONALES", -1);
        }
    };

    private ChallengeHelper.OnApprove drop_ballots = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            discardBallots();
//            continueRoutine();
        }
    };

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

    TwoButtonDialogEditTextFragment4Boletas.OnTwoButtonBoletaDialogFragmentListener ballotListener =
            new TwoButtonDialogEditTextFragment4Boletas.OnTwoButtonBoletaDialogFragmentListener() {
                @Override
                public void onYesButtonBoletaDialogClicked(String numberOfPics) {
//                    editTextFragment.dismiss();
//                    hideSoftKeyBoard();


                    int ballots = utilities.parseInt(numberOfPics, -1);
                    if (ballots == -1) {
                        //reject values
                        utilities.createCustomToast("El numero de papeletas ingresado no es valido");
                        return;
                    } else if (ballots == 0) {
                        utilities.createCustomToast("El numero de papeletas debe ser mayor que zero");
                        return;
                    }
                    addBallots(ballots);
                }

                @Override
                public void onNoButtonBoletaDialogClicked() {

                }
            };


    private void markBallotImpugnado(){
        //todo: persist data
        //NULOS BALLOTS:
        setBtnColorsEntryCompleted();
        int impunadoBallots = summary.addImpugnados();
        String impugnadoCount = String.valueOf(impunadoBallots);
        valuesMap.put("IMPUGNADOS",impugnadoCount);
        updatePersistBallotCount();
        ((TextView)findViewById(R.id.impugnado_votes)).setText(impugnadoCount);
        ((TextView)findViewById(R.id.grantotal_votes)).setText(String.valueOf(summary.getGranTotalBallots()));

    }

    private void markBallotNulo(){
        //todo: persist data
        //NULOS BALLOTS:
        setBtnColorsEntryCompleted();
        int nullBallolts = summary.addNullBallot();
        String nullCount = String.valueOf(nullBallolts);
        valuesMap.put("NULOS",nullCount);
        updatePersistBallotCount();
        ((TextView)findViewById(R.id.null_votes)).setText(nullCount);
        ((TextView)findViewById(R.id.grantotal_votes)).setText(String.valueOf(summary.getGranTotalBallots()));
    }

    private void markBallotAbstained(){
        //todo: persist data
        // EMPTY BALLOTS:
        setBtnColorsEntryCompleted();
        int emptyBallots = summary.addEmptyBallot();
        String emptyCount = String.valueOf(emptyBallots);
        valuesMap.put("ABSTENCIONES",emptyCount);
        updatePersistBallotCount();
        ((TextView)findViewById(R.id.enblanco_votes)).setText(emptyCount);
        ((TextView)findViewById(R.id.grantotal_votes)).setText(String.valueOf(summary.getGranTotalBallots()));

    }

    private void markBallotCruzado(){
        //todo: have to display the cruzado counts
        setBtnColorsEntryCompleted();
        int cruzadoBallots = summary.addCruzadoBallot();
        String cruzadoCount = String.valueOf(cruzadoBallots);
        valuesMap.put("VOTOS CRUZADOS",cruzadoCount);
        utilities.savePreferences(Consts.VOTO_CRUZADO,cruzadoBallots); // save on preferences
        updatePersistBallotCount();
        ((TextView)findViewById(R.id.cruzado_votes)).setText(cruzadoCount);
        ((TextView)findViewById(R.id.valid_votes)).setText(String.valueOf(summary.getValidBallots()));
        ((TextView)findViewById(R.id.grantotal_votes)).setText(String.valueOf(summary.getGranTotalBallots()));
    }

    private void setBtnColorsEntryCompleted(){
        utilities.setButtonColorRed(invalidBtn);
        utilities.setButtonColorRed(ingressoBtn);
        utilities.setButtonColorRed(crossBtn);
        utilities.setButtonColorGreen(nextBtn);
    }

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

    private ChallengeHelper.OnApprove select_cruzado = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            markBallotCruzado();
        }
    };

    private void rejectInvalidSelection(){
        utilities.createCustomToast(getResources().getString(R.string.invalidEntryMismatch));
        setButtonsToStart();
    }

    private void setButtonsToStart(){
        invalidBtn.setText(INVALID);
        discardBtn.setText(DISCARD);
        ingressoBtn.setText(INGRESSO);
        acceptBtn.setText(ACCEPT);
        nextBtn.setText(NEXT);
        crossBtn.setText(CROSS);
        utilities.setButtonColorGreen(invalidBtn);
        utilities.setButtonColorRed(discardBtn);
        utilities.setButtonColorGreen(ingressoBtn);
        utilities.setButtonColorRed(acceptBtn);
        utilities.setButtonColorRed(nextBtn);
        utilities.setButtonColorGreen(crossBtn);
    }

    private void setButtonsInvalidRentry(){
        invalidBtn.setText(RE_INVALID);
        utilities.setButtonColorRed(nextBtn);
        utilities.setButtonColorRed(acceptBtn);
        utilities.setButtonColorGreen(invalidBtn);
    }

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
                    setButtonsInvalidRentry();
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

    //---------------------------------- HIDE BUTTONS ------------------------------------
    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        if (!hasfocus) {
            findViewById(R.id.keyboardview).setVisibility(View.VISIBLE);
            findViewById(R.id.button_layout).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.keyboardview).setVisibility(View.GONE);
        }
    }

}
