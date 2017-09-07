package com.afilon.mayor.v11.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.methods.HttpGet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui.DialogToConfirmDuiListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.model.Candidate;
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
import com.afilon.mayor.v11.webservice.WebServiceRestTask;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("DefaultLocale")
public class ParlacenVoteTableActivity extends Activity implements OnTwoButtonDialogFragmentListener, DialogToConfirmDuiListener, DataResponseCallback {

    private static final String CLASS_TAG = "ParlacenVoteTableActivity";
    private static final int DROP_BALLOT=1;
    private TextView
            ballotNmbTv,
            banderaVotesTv,
            partyNameTv,
            partido_votes,
            partidoPrevious;
    private int
            initialCandidateMarks = 0,
            currentBallotNumber,
            minBallotNumber,
            partyArrayIndex,
            isVotosOrTodosPressed,
            editTextBox = 0,
            checkBoxColumn = 1;
    private float
            preferentialVotes,
            partido_votes_int,
            currentBallotPreferentialVotes;
    private boolean [] errorIndex;
    private boolean
            isUpdate,
            firstScreen = true,
            isIndependentParty = false,
            bandError = false,
            verpress = false,
            anotherPress = false,
            pressAgain = false,
            isErr = false,
            isPref = false,
            todosGuardar1st = false,
            isTodosReingresar = false,
            isTodos = false,
            isBandera = false;
    private CustomKeyboard mCustomKeyboard;
    private VotingCenter vc;
    private Escrudata escrudata;
    private DatabaseAdapterParlacen db_adapter;
    private Utilities ah;
    private String partyPreferentialElectionId;
    private List<Candidate> candidatesList;
    private CheckBox
            partido_cb_one,
            partido_cb_two;
    private EditText
            partido_et_one,
            partido_et_two;
    private String
            banderaVoteIngresar,
            banderaVoteIngresarTwo;
    private Candidate partidoCandidate;
    private Flag
            isVotoBanderaBtnPressed,
            isTodosBtnPressed,
            isIngresoBtnPressed,
            isReingresarBtnPressed,
            isVerificarBtnPressed,
            isReiniciarBoletaBtnPressed,
            isAceptarBtnPressed,
            isProximaBoletaPressed,
            isAbortBtnPressed;
    private TextView
            mismatchTv,
            colOneTV,
            colTwoTV,
            colThreeTV;
    private StringBuilder buffer;
    private ImageLoader imageLoader;
    private ArrayList<Party> partyArrayList;
    private ArrayList<CheckBox>
            firstColumn,
            secondColumn;
    private ArrayList<ImageView>
            firstErr,
            secondErr;
    private ArrayList<TextView>
            currentVotes,
            previousVotes,
            accumulatedVotes,
            currentMarks;
    private ArrayList<CrossVoteBundle> cbundle;
    private Button
            ingresoBtn,
            reingressoBtn,
            verificarBtn,
            votoBanderaBtn,
            todosBtn,
            reinitiateBoletaBtn,
            aceptarBtn,
            proxBoletaBtn,
            descartarBtn;
    private TextView partido_votes_two;
    private TwoButtonDialogFragment twoBtnDialogFragment;
    private List<PreferentialCandidateVotes> selectedCandidatesVotesForCurrentParty;
    private SpannableString error;
    private Drawable errorImage;
    private ImageSpan errorSpan;
    private ChallengeHelper challengeHelper;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ah = new Utilities(ParlacenVoteTableActivity.this);
        ah.tabletConfiguration(Build.MODEL, this);
        //set up window:
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preferential_vote_table);

        Log.i("CLASS NAME : ", CLASS_TAG);

        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(ParlacenVoteTableActivity.this));
        //--------- filter listeners ---------------------------------------------------
        CommonListeners listenerHandler = new CommonListeners();
        View.OnKeyListener altkeys = listenerHandler.getAltKeysListener();
        View.OnLongClickListener longClickListener = listenerHandler.getMouseListener();
        //------------------------------------------------------------------------------

        mCustomKeyboard = new CustomKeyboard(this, R.id.keyboardview, R.xml.tenhexkbd);

        imageLoader = ImageLoader.getInstance();

        //////////////////////////////////////////////////////////////////////////////
        Bundle b = getIntent().getExtras();
        vc = b.getParcelable("com.afilon.tse.votingcenter");
        escrudata = b.getParcelable("com.afilon.tse.escrudata");
        partyArrayIndex = b.getInt("partyNumber");
        cbundle = b.getParcelableArrayList("crossVoteBundle");
/////////////////////////////////////////////////////////////////////////////////
//        vc = new VotingCenter("123","234","345","456","567");
//        escrudata = new Escrudata("");
//        partyArrayIndex = 1;
//        cbundle = new ArrayList<>(null);
        ////////////////////////////////////////////////////////////////////////////

        if (cbundle != null) {
            Log.i(">>> DEBUG " + CLASS_TAG, "CrossVoteBundle size " + cbundle.size() + " -- " + cbundle.toString());
        } else {
            Log.i(">>> DEBUG " + CLASS_TAG, "CrossVoteBundle is NULL ");
        }

        //Initialize the vote-type count
        partido_votes_int = 0.000f; // Bandera Votes
        preferentialVotes = 0.000f; // Preferential Votes
        currentBallotPreferentialVotes = 0.000f; //proposed assigned votes.

        //---------------------------------------------------------------------------------
        // Top Layout Textviews
        ballotNmbTv = (TextView) this.findViewById(R.id.ballot_tv);
        partyNameTv = (TextView) this.findViewById(R.id.party_tv);
        mismatchTv = (TextView) this.findViewById(R.id.mismatch_tv);
        banderaVotesTv = (TextView) this.findViewById(R.id.bandera_tv);
        colOneTV = (TextView) this.findViewById(R.id.colOne);
        colTwoTV = (TextView) this.findViewById(R.id.colTwo);
        colThreeTV = (TextView) this.findViewById(R.id.colThree);

        firstScreen = ah.loadPreferencesBool("firstScreen");
        db_adapter = new DatabaseAdapterParlacen(this);
        //--------------------------------------------------
        // set up tools and databse connection:
        challengeHelper = new ChallengeHelper(this);
        // REGISTER ROUTINES TO CHALLENGE HELPER:
//        challengeHelper.addRoutine(ADD_BALLOT, add_ballots);
        challengeHelper.addRoutine(DROP_BALLOT, drop_ballots);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);// FOR DUI CONFIRMATION ONLY
        challengeHelper.setTools(ah, db_adapter); //TOOLS ARE FOR DUI CONFIRMATION ONLY
        //----------------------------------------------------

//        db_adapter.open();
        db_adapter.open();

        // read if we got here as update:
        isUpdate = ah.loadPreferencesUpdate("anadir");
        // read from party tables and party votes
        escrudata.setActaImageLink(vc.getPref_election_id());
        partyArrayList = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());
        final List<PreferentialPartyVotes> prefPartyVotes = db_adapter.getPartiesPreferentialVotes();
        // for every party and every vote class where party ids match assign votes to the party
        for (Party party : partyArrayList) {
            for (PreferentialPartyVotes ppv : prefPartyVotes) {
                if (party.getParty_preferential_election_id().equals(ppv.getParty_preferential_election_id())) {
                    party.setParty_votes("" + ppv.getParty_votes());
                    party.setParty_boletas("" + ppv.getParty_boletas());
                }
            }
        }

        //---------- Initiate button flags: -----------------------------------------------------
        isAbortBtnPressed = new Flag(false);
        isIngresoBtnPressed = new Flag(false);
        isReingresarBtnPressed = new Flag(true);
        isVerificarBtnPressed = new Flag(true);
        isVotoBanderaBtnPressed = new Flag(false);
        isTodosBtnPressed = new Flag(false);
        isReiniciarBoletaBtnPressed = new Flag(true);
        isAceptarBtnPressed = new Flag(true);
        isProximaBoletaPressed = new Flag(true);
        isVotosOrTodosPressed = 0;

        //-----------------------------------------------------------------------------------------
        //set up button: DESCARTAR BOLETAS
        descartarBtn = (Button) findViewById(R.id.abort_btn);
        warningButtons(descartarBtn);
        descartarBtn.setOnClickListener(descartarButtonListener());

        //set up button: INGRESO
        ingresoBtn = (Button) findViewById(R.id.ingreso_btn);
//        ingresoBtn.setOnClickListener(ingresoButtonListener());
        ingresoBtn.setOnClickListener(ingresoListener());
        //set up button: RE-INGRESO
        reingressoBtn = (Button) findViewById(R.id.reingreso_btn);
//        reingressoBtn.setOnClickListener(reingresoButtonListener());
        reingressoBtn.setOnClickListener(reingresoListener());
        //set up button: VERIFICAR
        buffer = new StringBuilder();
        verificarBtn = (Button) findViewById(R.id.verificacion_btn);
//        verificarBtn.setOnClickListener(verificarButtonListener());
        verificarBtn.setOnClickListener(verificarListener());
        //-----------------------------------------------------------------------------------------
        //Set up Views per Party Row:
        TextView partido_name = (TextView) findViewById(R.id.txt_name_b);
        partido_votes = (TextView) findViewById(R.id.party_votes_txt);
        partido_votes_two = (TextView) findViewById(R.id.party_votes_txt_two);
        partidoPrevious = (TextView) findViewById(R.id.party_votes_prev);

        partido_cb_one = (CheckBox) findViewById(R.id.item_check_one_b);
        partido_cb_two = (CheckBox) findViewById(R.id.item_check_two_b);

        checkBoxDisabled(partido_cb_one);
        checkBoxDisabled(partido_cb_two);


        partido_et_one = (EditText) findViewById(R.id.partido_et_one);
        partido_et_one.setOnLongClickListener(longClickListener);
        partido_et_one.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        partido_et_two = (EditText) findViewById(R.id.partido_et_two);
        partido_et_two.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        partido_et_two.setOnLongClickListener(longClickListener);

        mCustomKeyboard.registerEditText(R.id.partido_et_one);
        mCustomKeyboard.registerEditText(R.id.partido_et_two);

        partido_et_one.addTextChangedListener(tw1);
        partido_et_two.addTextChangedListener(tw2);

        closeEditText(partido_et_one);
        closeEditText(partido_et_two);

        //-----------------------------------------------------------------------------------------
        //set up Button: VOTO BANDERA
        votoBanderaBtn = (Button) findViewById(R.id.bandera_btn);
//        votoBanderaBtn.setOnClickListener(banderaButtonListener());
        votoBanderaBtn.setOnClickListener(banderaListener());
        //set up Button: TODOS
        todosBtn = (Button) findViewById(R.id.todos_btn);
//        todosBtn.setOnClickListener(todosButtonListener());
        todosBtn.setOnClickListener(todosListener());
        //set up Button: RE-INICIAR BOLETA
        reinitiateBoletaBtn = (Button) findViewById(R.id.reninitate_boleta_btn);
//        reinitiateBoletaBtn.setOnClickListener(reinitateButtonListener());
        reinitiateBoletaBtn.setOnClickListener(reinitateListener());
        //set up Button: ACEPTAR
        aceptarBtn = (Button) findViewById(R.id.aceptar_btn);
//        aceptarBtn.setOnClickListener(aceptarButtonListener());
        aceptarBtn.setOnClickListener(aceptarListener());
        //set up Button: PROXIMA BOLETA
        proxBoletaBtn = (Button) findViewById(R.id.nextballot_btn);
//        proxBoletaBtn.setOnClickListener(proximaboletaButtonListener());
        proxBoletaBtn.setOnClickListener(proximoListener());
        disableButtons(proxBoletaBtn, aceptarBtn, reinitiateBoletaBtn, todosBtn, ingresoBtn,
                reingressoBtn, verificarBtn);
        enableButtons(votoBanderaBtn);
        if (isUpdate && !firstScreen) {
            enableButtons(todosBtn, ingresoBtn);
        }
        //end button set up

        // create list of candidates by first obtaining the party preferential id of the
        // party we are currently processing
        partyPreferentialElectionId = partyArrayList.get(partyArrayIndex).getParty_preferential_election_id();
        // obtain the list of candidates based on party preferntial id
        candidatesList = db_adapter.getParlacenCandidatesArrayList(partyPreferentialElectionId);

        // get list of candidate votes already assign to this party:
        selectedCandidatesVotesForCurrentParty = db_adapter.getPreferentialElectionCandidateVotesForThisParty(partyPreferentialElectionId);
        //maybe try MARKS HERE!
//        candidateMarks = db_adapter.get
        //todo if there are votes then we need to flag that we are coming back from candidate list
        //db_adapter.resetCandidateVotesbyParty(partyPreferentialElectionId);
        //---------------------------------------------------------------------------------------
        // set bandera votes row, get bandera votes values from database
        partidoCandidate = db_adapter.getCurrentBanderaVotes(partyPreferentialElectionId);
        // if there are no bandera votes assigned then create a candidate to fill the top row:
        if (partidoCandidate.getVotesNumber() == 0) {
            partidoCandidate = new Candidate(partyArrayList.get(partyArrayIndex).getParty_name(),
                    partyArrayList.get(partyArrayIndex).getParty_preferential_election_id(),
                    partyArrayList.get(partyArrayIndex).getParty_preferential_election_id(),
                    partyArrayList.get(partyArrayIndex).getParty_preferential_election_id(),
                    partyArrayList.get(partyArrayIndex).getParty_order());
            System.out.println("------ @@@@@ !!A NEW PARTIDO CANDIDATE WAS CREATED!! @@@@@ ----");
        } else {
            // there are bandera votes, then update the textview and update the bandera votes
            // variable partido_votes_int
            partido_votes_int = partidoCandidate.getVotesNumber();
            preferentialVotes = partidoCandidate.getPreferentialVotes();
            //TODO Try putting MARKS HERE!
            partido_votes_two.setText(String.format(Locale.US, "%.0f", partido_votes_int));
            partidoPrevious.setText(String.format(Locale.US, "%.0f", partido_votes_int));
        }
        partidoCandidate.setCandidate_name(partyArrayList.get(partyArrayIndex).getParty_name());
        partidoCandidate.setCandidate_order(partyArrayList.get(partyArrayIndex).getParty_order());
        partidoCandidate.setCandidateID(partyArrayList.get(partyArrayIndex).getParty_order());
        partidoCandidate.setCandidatePreferentialElectionID(partyArrayList
                .get(partyArrayIndex).getParty_preferential_election_id());
        partidoCandidate.setPartyPreferentialElectionID(partyArrayList
                .get(partyArrayIndex).getPref_election_id());
        partidoCandidate.setCandidateID("0");
        //set image:
        ImageView banderaImageView = (ImageView) this
                .findViewById(R.id.image_icon_b);
        try {
            //CARLOS: Remove INDPT from bandera row
            isIndependentParty = partidoCandidate.getCandidate_name().toLowerCase().contains("indpt") ? true : false;

            //CARLOS: 2016-12-30
            //Disable BANDERA BTN in case INDPT party is found
            Log.e("value of isIndependentParty", String.valueOf(isIndependentParty));
            if (isIndependentParty) {

                partido_et_one.setText("0");
                partido_et_two.setText("0");
                banderaVoteIngresar = partido_et_one.getText().toString();
                banderaVoteIngresarTwo = partido_et_two.getText().toString();
                verifyBanderaValues();
                acceptBanderaVotes();

                simNextBallot(); //CARLOS: 2017-01-02 Simulate user clicked PROX. BOLETA

                enableButtons(ingresoBtn, todosBtn);
                disableButtons(votoBanderaBtn);

                raiseFlags(isReingresarBtnPressed, isVerificarBtnPressed, isReiniciarBoletaBtnPressed,
                        isAceptarBtnPressed, isProximaBoletaPressed);
                lowerFlags(isIngresoBtnPressed, isVotoBanderaBtnPressed, isTodosBtnPressed,
                        isAbortBtnPressed);
                isVotosOrTodosPressed = 0;
                ingresoBtn.setText("Preferencia");
                reingressoBtn.setText("Re-ingresar\nPreferencia");

            } else {
                enableButtons(votoBanderaBtn);
            }

            //CARLOS: passing party name as part of the .png file nale. e.g for FMLN would be fmln.png
            banderaImageView.setImageBitmap(getBitmapFromAsset(partidoCandidate.getCandidate_name().toLowerCase() + ".png"));
            banderaImageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));


        } catch (IOException e) {
            e.printStackTrace();
        }

        //CARLOS: 2016-12-30
        //Remove BANDERA LABEL FROM INDPT Party
        if (isIndependentParty) {
            partido_name.setText(partidoCandidate.getCandidate_name());
        } else {
            partido_name.setText("Bandera " + partidoCandidate.getCandidate_name());
        }


        //---------------------------------------------------------------------------------------
        // set the labels to the top of the screen:

        String outMessage = "";
        String votesCount = "";
        String cumbol = "";
        String columnHeader = "";
        currentBallotNumber = ah.loadPreferences("currentBallotNumber") + 1;

        outMessage = "Boleta  No: " + currentBallotNumber + "\n"
                + "Votos de Partido: "
                + String.format(Locale.US, "%.0f", Float.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes())) + "\n"
                + "Total de Boletas: "
                + partyArrayList.get(partyArrayIndex).getParty_boletas();
        cumbol = String.valueOf(currentBallotNumber - 1);
        votesCount = "Contados"
                + "\n"
                + "Votos Bandera: "
                + String.format(java.util.Locale.US, "%.0f", partido_votes_int)
                + "\n"
                + "Votos Preferenciales: "
                + String.format(java.util.Locale.US, "%.0f", preferentialVotes);

        columnHeader = "Votos";
        colOneTV.setText("Bol" + String.valueOf(currentBallotNumber));
        colTwoTV.setText("Cum");
        colThreeTV.setText("Cum");
        partyNameTv.setText(columnHeader);
        banderaVotesTv.setText(votesCount);
        ballotNmbTv.setText(outMessage);
        //------------------------------------------------------------------------------------------
        firstColumn = new ArrayList<CheckBox>();
        secondColumn = new ArrayList<CheckBox>();

        firstErr = new ArrayList<ImageView>();
        secondErr = new ArrayList<ImageView>();

        currentVotes = new ArrayList<TextView>();
        previousVotes = new ArrayList<TextView>();
        accumulatedVotes = new ArrayList<TextView>();
        currentMarks = new ArrayList<TextView>();
        errorIndex = new boolean[candidatesList.size()];

        for(int index = 0; index < errorIndex.length; index++ ){
            errorIndex[index] = false;
        }

        // load the table:
        TableLayout table = (TableLayout) findViewById(R.id.preferential_table);
        for (int i = 0; i < candidatesList.size(); i++) {
            for (int y = 0; y < selectedCandidatesVotesForCurrentParty.size(); y++) {
                if (candidatesList.get(i).getCandidatePreferentialElectionID().equals(selectedCandidatesVotesForCurrentParty.get(y).getCandidate_preferential_election_id())) {
                    candidatesList.get(i).setPreferentialVotes(selectedCandidatesVotesForCurrentParty.get(y).getCandidate_preferential_votes());
                    candidatesList.get(i).setBanderaNumber(selectedCandidatesVotesForCurrentParty.get(y).getCandidate_bandera_votes());
                    candidatesList.get(i).setVotesNumber(selectedCandidatesVotesForCurrentParty.get(y).getCandidate_votes());
//                    candidatesList.get(i).setMarcas(selectedCandidatesVotesForCurrentParty.get(y).);
                }
            }
            //load each row:
            TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.preferential_votes_table_row_item, null);
            CheckBox cbOne = (CheckBox) tr.findViewById(R.id.item_check_one_b);
            ImageView errOne = (ImageView) tr.findViewById(R.id.errorChkOne);
            cbOne.setChecked(false);
            cbOne.setId(i);
            firstColumn.add(cbOne);
            firstErr.add(errOne);

            CheckBox cbTwo = (CheckBox) tr.findViewById(R.id.item_check_two_b);
            ImageView errTwo = (ImageView) tr.findViewById(R.id.errorChkTwo);
            cbTwo.setChecked(false);
            cbTwo.setId(i);
            secondColumn.add(cbTwo);
            secondErr.add(errTwo);

            ImageView ivPicture = (ImageView) tr.findViewById(R.id.image_icon_b);
            ivPicture.setId(i);

            imageLoader.displayImage("assets://drawable/" + candidatesList.get(i).getCandidatePreferentialElectionID() + ".png", ivPicture);

            TextView tvName = (TextView) tr.findViewById(R.id.txt_name_b);
            tvName.setId(i);
            tvName.setText(candidatesList.get(i).getCandidate_name());

            TextView tvNumber = (TextView) tr.findViewById(R.id.txt_candidate_nmb_b);
            tvNumber.setId(i);
            tvNumber.setText("Candidato Numero: "
                    + candidatesList.get(i).getCandidateID());


            TextView tvVotes = (TextView) tr.findViewById(R.id.txt_selections_nmb);
            tvVotes.setText(formatFloat(0f));
            tvVotes.setId(i);

            TextView tvVotesPrevious = (TextView) tr.findViewById(R.id.txt_votes_previous);
            String candidateVotes = "" + formatFloat(candidatesList.get(i).getVotesNumber());
            tvVotesPrevious.setText(candidateVotes);

//            //TODO candidate.setMarcas(read from where marcas is saved)
            candidatesList.get(i).setMarcas(db_adapter.getCandidatMarks(candidatesList.get(i).getCandidatePreferentialElectionID(), "4"));
            Log.e("Post set : ", Integer.toString(candidatesList.get(i).getMarcas()));
            TextView tvMarks = (TextView) tr.findViewById(R.id.txt_selections_marks);
            tvMarks.setId(i);
            tvMarks.setText(Integer.toString(candidatesList.get(i).getMarcas()));
            initialCandidateMarks = candidatesList.get(i).getMarcas();

            TextView tvVotesTwo = (TextView) tr.findViewById(R.id.txt_votes_two);
            tvVotes.setId(i); //id is useless?
            tvVotesTwo.setText(candidateVotes);


            currentMarks.add(tvMarks);
            currentVotes.add(tvVotes);
            previousVotes.add(tvVotesPrevious);
            accumulatedVotes.add(tvVotesTwo);

            View v = new View(this);
            v.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, 1));
            v.setBackgroundColor(Color.rgb(51, 51, 51));

            table.addView(tr);
            table.addView(v);

            registerForContextMenu(tr);
        }
        for (CheckBox cb : firstColumn) {
            checkBoxDisabled(cb);
        }

        for (CheckBox cb : secondColumn) {
            checkBoxDisabled(cb);
        }

        error = new SpannableString("   ");
        errorImage = getResources().getDrawable(R.drawable.error);
        errorImage.setBounds(0,0,50,50);
        errorSpan = new ImageSpan(errorImage, ImageSpan.ALIGN_BASELINE);
        error.setSpan(errorSpan,0,1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);


    }

    /*******************************************************************************************
     * INTERRUPT ROUTINES FOR BUTTONS:                                **
     * **
     ******************************************************************************************/
    private View.OnFocusChangeListener getKeyBoard() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mCustomKeyboard.showCustomKeyboard(v);
//                    v.setEnabled(false);
                }
            }
        };
    }

    private void enterBanderVotes() {
        String inValue = banderaVoteIngresar;
        int totalPartyMarks = Integer.valueOf(inValue);//* candidatesList.size();
        partido_votes.setText(String.valueOf(totalPartyMarks));
        int proposedBallot = currentBallotNumber + Integer.parseInt(inValue) - 1;
        String message = "Boleta  No: " + currentBallotNumber + " - " + proposedBallot + "\n" + "Votos de Partido: " + String.format(Locale.US, "%.0f", Float.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes())) + "\n" + "Total de Boletas: " + partyArrayList.get(partyArrayIndex).getParty_boletas();
        ballotNmbTv.setText(message);
        // set up current pref votes:
        for (TextView current : currentVotes) {
            currentBallotPreferentialVotes = Float.parseFloat(inValue) / ((float) candidatesList.size());
            current.setText(formatFloat(currentBallotPreferentialVotes));
            current.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        openEditText(partido_et_two);
        partido_et_two.setHint("");
        ingresoBtn.setText("Preferencial");

    }

    private void verifyBanderaValues() {
        String value = partido_et_two.getText().toString();
        value = banderaVoteIngresarTwo;
        if (value.equals("")) {
            value = "0";
        }
        if (banderaVoteIngresar.equals(value) && Float.parseFloat(value) <= Float.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes())) {
            partido_et_one.setText(banderaVoteIngresar);
            partido_et_two.setText(banderaVoteIngresarTwo);
            warningButtons(reinitiateBoletaBtn);
            enableButtons(aceptarBtn);
        } else {
            if (Float.parseFloat(value) > Float.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes())) {
                ah.createCustomToast("Voto Bandera Es Mayor \nQue El Numero de Votos Recibidos");
            } else ah.createCustomToast("Entradas de Voto Bandera No Coinciden");
            bandError = true;
            rejectBanderaVotes();
            partido_et_one.addTextChangedListener(tw1);
            partido_et_two.addTextChangedListener(tw2);


        }

    }

    @SuppressLint("LongLogTag")
    private void acceptBanderaVotes() {
        String value = partido_et_two.getText().toString();
        value = banderaVoteIngresarTwo;
        if (value.equals("")) {
            value = "0";
        }
        partido_votes_int += Float.parseFloat(value);
        partidoCandidate.setVotesNumber(partido_votes_int);
        partidoCandidate.setPreferentialVotes(partidoCandidate.getVotesNumber());
        partido_votes_two.setText(String.format(Locale.US, "%.0f", partido_votes_int));//remains 0.0f because we want to display digit
        float divider = candidatesList.size();
        float addVotes = partido_votes_int / divider;

        if (candidatesList != null) {
            Log.e("candidatesList IS NULL", "true");
        } else {
            Log.e("candidatesList IS NOT null", "true");
        }

        if (accumulatedVotes != null) {
            for (int i = 0; i < candidatesList.size(); i++) {
                //ADD THE BANDERA VOTE
                float number = candidatesList.get(i).getVotesNumber();
                candidatesList.get(i).setVotesNumber(number + addVotes);
                //for display:
                candidatesList.get(i).setBanderaNumber(partido_votes_int / candidatesList.size());
                //actual display:
                accumulatedVotes.get(i).setText(formatFloat(candidatesList.get(i).getVotesNumber()));
            }
        }
        raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVerificarBtnPressed,
                isVotoBanderaBtnPressed, isTodosBtnPressed, isReiniciarBoletaBtnPressed, isAceptarBtnPressed);
        disableButtons(ingresoBtn, reingressoBtn, verificarBtn, votoBanderaBtn, todosBtn,
                reinitiateBoletaBtn, aceptarBtn, descartarBtn);

        lowerFlags(isProximaBoletaPressed);
        enableButtons(proxBoletaBtn);


        currentBallotNumber += partido_votes_int - 1;
        //todo: transfer the value to the preferencias,  text view and make check boxes visible.
        partido_et_one.setVisibility(View.GONE);
        partido_et_two.setVisibility(View.GONE);
        partido_cb_one.setVisibility(View.VISIBLE);
        partido_cb_two.setVisibility(View.VISIBLE);
        // votoBanderaBtn.setEnabled(false);
    }

    private void rejectBanderaVotes() {
        //Reset the views:
        partido_votes.setText("0");
        for (TextView current : currentVotes) {
            current.setText("0.000");
            current.setTypeface(null, Typeface.NORMAL);
        }
        String outMessage = "Boleta  No: " + currentBallotNumber + "\n"
                + "Votos de Partido: "
                + String.format(Locale.US, "%.0f", Float.parseFloat(
                partyArrayList.get(partyArrayIndex).getParty_votes()))
                + "\n"
                + "Total de Boletas: "
                + partyArrayList.get(partyArrayIndex).getParty_boletas();
        ballotNmbTv.setText(outMessage);
        partido_et_two.setText("");
        partido_et_one.setText("");
        if(bandError) {
            votoBanderaBtn.setText("Corregir");
            partido_et_one.setHint(error);
            partido_et_two.setHint(error);
            bandError = false;
        }
        //reset buttons:
        disableButtons(aceptarBtn, reinitiateBoletaBtn);
        enableButtons(votoBanderaBtn);
    }

    //CARLOS: 2017-01-02
    //Simulate user clicked PROX. BOLETA
    private void simNextBallot() {
        findViewById(R.id.marcas_tv).setVisibility(View.INVISIBLE);
        //if ballots is less than max ballots, load next ballot:
        for (Candidate selected : candidatesList) {
            selected.setCbOneSelected(false);
            selected.setCbTwoSelected(false);
        }
        currentBallotNumber++;
        if (firstScreen) {
            firstScreen = false;
        }
        String outMessage = "Boleta No: "
                + currentBallotNumber
                + "\n"
                + "Votos de Partido: "
                + String.format(Locale.US, "%.0f", Float.parseFloat(
                partyArrayList.get(partyArrayIndex).getParty_votes()))
                + "\n"
                + "Total de Boletas: "
                + partyArrayList.get(partyArrayIndex)
                .getParty_boletas();
        String cumbol = String.valueOf(currentBallotNumber - 1);
        String columnHeader = "Votos"; //Preferencias
        partyNameTv.setText(columnHeader);
        colOneTV.setText("Bol" + String.valueOf(currentBallotNumber));
        colTwoTV.setText("Cum");
        colThreeTV.setText("Cum");


        ballotNmbTv.setText(outMessage);
        partido_votes.setText("0");
        partidoPrevious.setText(partido_votes_two.getText());

        raiseFlags(isReingresarBtnPressed,isVerificarBtnPressed, isReiniciarBoletaBtnPressed, isAceptarBtnPressed , isProximaBoletaPressed );
        lowerFlags(isIngresoBtnPressed,isVotoBanderaBtnPressed,isTodosBtnPressed,isAbortBtnPressed);
        isVotosOrTodosPressed = 0;

        enableButtons(todosBtn,ingresoBtn);
        disableButtons(reingressoBtn,verificarBtn,reinitiateBoletaBtn,aceptarBtn,proxBoletaBtn);
        if (isUpdate) {
            enableButtons(votoBanderaBtn);
        }
        warningButtons(descartarBtn);
    }

    //--------------------------------------------------------------------------------------------------
    private OnClickListener ingresoListener() {
        final OnClickListener ingresoListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //ingresar Button
                //  possible lables
                //      Preferencial
                //      Ingresar
                //      Corregir
                warningButtons(reinitiateBoletaBtn);
                if(ingresoBtn.getText().toString().contains("Pref")){
                    // if text Preferencial
                    //  set isPref = true
                    isPref = true;
                    //  set text ingreso button "Ingresar"
                    ingresoBtn.setText("Ingresar");
                    //  disable todos button
                    //  disable bandera button
                    disableButtons(todosBtn, votoBanderaBtn);
                    if(checkBoxColumn == 1) {
                        //  if checkBoxColumn = 1
                        //      ingreso button remains enabled
                        enableButtons(ingresoBtn);
                    }else if(checkBoxColumn ==2) {
                        //  if checkBoxColumn = 2
                        //      set text verificar button "Guardar"
                        verificarBtn.setText("Guardar");
                        //      unlock second column for preferential votes
                        unlockColumn(2, false);
                        //      diable ingreso button
                        disableButtons(ingresoBtn);
                    }


                } else if(ingresoBtn.getText().toString().contains("Ingresar")) {
                    // if text Ingresar
                    //  set text verificar button "Guardar"
                    verificarBtn.setText("Guardar");
                    if(firstScreen) {
                        //  if firstScreen
                        //      unlock first edit text box for bandera votes
                        openEditText(partido_et_one);
                        partido_et_one.setHint("");
                    }else if(isErr) {
                        //  else if isErr
                        //      set text verificar button "Guardar"
                        verificarBtn.setText("Guardar");
                        //      unlock first column at errorIndex = true
                        unlockColumn(1,true);
                        //      enable verificar button
                        enableButtons(verificarBtn);
                        disableButtons(ingresoBtn);
                    }else if(isPref) {
                        //  else if isPref
                        //      unlock first column for preferential votes
                        unlockColumn(1,false);
                        //      disable ingreso button
                        disableButtons(ingresoBtn);
                    }else if(isBandera) {
                        //  else if isBandera
                        //      check first checkBox for bandera vote
                        partido_cb_one.setButtonDrawable(R.drawable.dchecked);
                        //      disable ingreso button
                        disableButtons(ingresoBtn);
                        //      enable verificar button
                        enableButtons(verificarBtn);
                    }
                } else if(ingresoBtn.getText().toString().contains("Corregir")){
                    // if text Corregir
                    //  set text ingreso button "Ingresar"
                    ingresoBtn.setText("Ingresar");
                    //  clear out error icons
                    for(int index = 0; index < errorIndex.length; index++){
                        firstErr.get(index).setVisibility(View.GONE);
                        secondErr.get(index).setVisibility(View.GONE);
                    }
                    //  ingreso button remains enabled
                    enableButtons(ingresoBtn);
                }
            }
        };
        return ingresoListener;
    }

    private OnClickListener ingresoButtonListener() {
        final OnClickListener ingresoBtnListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if((ingresoBtn.getText().toString().contains("Pref")&&!isTodosReingresar) || (ingresoBtn.getText().toString().contains("Corr"))){
                    if(ingresoBtn.getText().toString().contains("Pref")){
                        disableButtons(todosBtn, votoBanderaBtn);
                    }
                    if(ingresoBtn.getText().toString().contains("Cor")){
                        for(int index = 0; index < errorIndex.length; index++ ){
                            if(errorIndex[index] == true){
                                firstErr.get(index).setVisibility(View.GONE);
                                secondErr.get(index).setVisibility(View.GONE);
                                candidatesList.get(index).setCbOneSelected(false);
//                                candidatesList.get(index).setCbTwoSelected(false);
//                                errorIndex[index] = false;
                            }
                        }
                        int i = 0;
                        for (final CheckBox cb : firstColumn) {
                            if(errorIndex[i]==true) {
                                checkBoxEnable(cb);
                                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(
                                            CompoundButton buttonView, boolean isChecked) {
                                        int getPosition = (Integer) buttonView.getId();
                                        candidatesList.get(getPosition).setCbOneSelected(buttonView.isChecked());
                                        enableButtons(verificarBtn);
                                        disableButtons(ingresoBtn);
                                        verificarBtn.setText("Guardar");
                                        firstErr.get(firstColumn.indexOf(cb)).setVisibility(View.GONE);
                                        editTextBox = 3;
                                    }
                                });
                            }
                            i++;
                        }
                    }
                    ingresoBtn.setText("Ingresar");
                    verificarBtn.setText("Guardar");
                }else {
                    isTodosReingresar = false;
                    if (isTodos) {
                        if(ingresoBtn.getText().toString().contains("Pref")){
                            ingresoBtn.setText("Ingresar");
                            todosGuardar1st = false;
                            isTodos = false;
                            verificarBtn.setText("Guardar");
                            disableButtons(ingresoBtn, todosBtn);

                                editTextBox = 2;
                                partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                                partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);

                                for (final CheckBox cb : secondColumn) {
                                    checkBoxEnable(cb);
                                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                            int getPosition = (Integer) buttonView.getId();
                                            candidatesList.get(getPosition).setCbTwoSelected(buttonView.isChecked());
                                            enableButtons(verificarBtn);
                                            disableButtons(reingressoBtn);
                                            verificarBtn.setText("Guardar");
                                        }
                                    });
                                }
                                // button Flags:
                                raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVotoBanderaBtnPressed,
                                        isTodosBtnPressed, isAceptarBtnPressed, isProximaBoletaPressed, isAbortBtnPressed);
                                lowerFlags(isReiniciarBoletaBtnPressed, isVerificarBtnPressed);


                        } else {
//                            asdf
//                            todosGuardar1st = true;
//                            ingresoBtn.setText("Preferencial");
//                            if (!isTodosBtnPressed.raised() && (!firstScreen || isUpdate)) {
//                                int i = 0;
//                                for (CheckBox cb : firstColumn) {
//                                    cb.setButtonDrawable(R.drawable.dchecked);
//                                    cb.setEnabled(false);
//                                    cb.setChecked(true);
//                                    candidatesList.get(i).setCbOneSelected(cb.isChecked());
//                                    i++;
//                                }
//                                for (CheckBox cb : secondColumn) {
//                                    cb.setButtonDrawable(R.drawable.disabled_cb);
//                                    cb.setChecked(false);
//                                    cb.setEnabled(false);
//                                }
//
//                                raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVerificarBtnPressed, isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed, isAbortBtnPressed);
//                                lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);
//
//                                isVotosOrTodosPressed = 2;
//
//                                enableButtons(verificarBtn);
//                                verificarBtn.setText("Guardar");
//                                warningButtons(reinitiateBoletaBtn);
//                                disableButtons(ingresoBtn, reingressoBtn, aceptarBtn, votoBanderaBtn, todosBtn, proxBoletaBtn, descartarBtn);
//                            }
//                            asdf
                        }
                    } else if (isBandera) {
                        checkBoxColumn = 2;
                        isBandera = false;
                        partido_cb_one.setButtonDrawable(R.drawable.dchecked);
                        verificarBtn.setText("Guardar");
                        enableButtons(verificarBtn);
                        disableButtons(ingresoBtn);
                    }else {
                        if (pressAgain) {
                            pressAgain = false;
                            disableButtons(ingresoBtn);
                            ah.setButtonColorGreen(verificarBtn);
                            for (int index = 0; index < errorIndex.length; index++) {
                                if (errorIndex[index] == true) {
                                    checkBoxEnable(firstColumn.get(index));
                                    firstErr.get(index).setVisibility(View.GONE);
                                }
                            }
                        }

                        if (ingresoBtn.getText().toString().contains("Corregir")) {
                            ingresoBtn.setText("Preferencial");
                            for (int index = 0; index < errorIndex.length; index++) {
                                if (errorIndex[index] == true) {
                                    firstErr.get(index).setVisibility(View.GONE);
                                    secondErr.get(index).setVisibility(View.GONE);
//                            errorIndex[index] = false;
                                }
                            }
                            editTextBox = 3;
                            pressAgain = true;
                        } else {
                            disableButtons(ingresoBtn);
                            editTextBox = 1;
                            if (!isIngresoBtnPressed.raised() && (!firstScreen || isUpdate)) {
                                partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                                partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);
                                verpress = false;
                                for (final CheckBox cb : firstColumn) {
                                    checkBoxEnable(cb);
                                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(
                                                CompoundButton buttonView, boolean isChecked) {
                                            int getPosition = (Integer) buttonView.getId();
                                            candidatesList.get(getPosition).setCbOneSelected(buttonView.isChecked());
                                            enableButtons(verificarBtn);
                                            disableButtons(ingresoBtn);
                                            verificarBtn.setText("Guardar");
                                            firstErr.get(firstColumn.indexOf(cb)).setVisibility(View.GONE);
                                            editTextBox = 3;
                                        }
                                    });
                                }
                                for (CheckBox cb : secondColumn) {
                                    checkBoxDisabled(cb);
                                }
                                //Set flags:
                                raiseFlags(isIngresoBtnPressed, isVerificarBtnPressed, isVotoBanderaBtnPressed,
                                        isTodosBtnPressed, isAceptarBtnPressed, isProximaBoletaPressed, isAbortBtnPressed);
                                lowerFlags(isReiniciarBoletaBtnPressed, isReingresarBtnPressed);
                                disableButtons(todosBtn);
                            } else if (firstScreen) {
                                openEditText(partido_et_one);
                                partido_et_one.setHint("");
                            }
                        }
                    }
                }
            }
        };
        return ingresoBtnListener;
    }

    private OnClickListener descartarButtonListener() {
        OnClickListener descartarBtnListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAbortBtnPressed.raised()) {
                    int totalVotes = ah.parseInt(partyArrayList.get(partyArrayIndex).getParty_boletas(), 0);
                    String discarBallotMessage = getResources().getString(R.string.dropBallotMessage);
                    int remaingBallots = totalVotes - currentBallotNumber+1;
                    challengeHelper.createDuiChallenge(String.format(discarBallotMessage,remaingBallots),DROP_BALLOT);
//                    createConfirmationDialog("¿DESEA DESCARTAR LAS PAPELETAS " + currentBallotNumber
//                            + " EN ADELANTE?", 3);
                }
            }
        };
        return descartarBtnListener;
    }

    private OnClickListener reingresoListener() {
        final OnClickListener reingresoListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Re-ingresar Button
                //  Button primarily enables preferential, todos, and Bandera buttons (Bandera only when added ballots)
                //      also when inital bandera calls function enterBanderVotes()

                warningButtons(reinitiateBoletaBtn);
                if(firstScreen) {
                    // if intial Bandera vote
                    //  enterBanderVotes();
                    enterBanderVotes();
                    //  disableButtons(verificarBtn);
                    disableButtons(verificarBtn);
                }else {
                    // if any other votes
                    if(isErr) {
                        //  if isErr
                        //      unlock second column errorIndex
                        unlockColumn(2,true);
                        //      set text verificar button "Guardar"
                        verificarBtn.setText("Guardar");
                        //      enable verificar button
                        enableButtons(verificarBtn);
                        disableButtons(reingressoBtn);
                    }else {
                        //  if !isErr
                        //      set text ingreso button "Preferencial"
                        ingresoBtn.setText("Preferencial");
                        //      disable re-ingresar button
                        disableButtons(reingressoBtn);
                        //      enable todos button
                        //      enable ingreso button
                        enableButtons(todosBtn, ingresoBtn);
                        if(isUpdate && !isIndependentParty) {
                            //          if added ballot votes
                            //              enable bandera button}
                            enableButtons(votoBanderaBtn);
                        }
                    }

                }
            }
        };
        return reingresoListener;
    }

    private OnClickListener reingresoButtonListener() {
        final OnClickListener reingresoBtnListener = new OnClickListener() {
            @Override
            public void onClick(View v) {



                if(isTodos){

                        ingresoBtn.setText("Preferencial");
                        isTodosReingresar = true;
                        enableButtons(ingresoBtn, todosBtn);
                        disableButtons(reingressoBtn);

                }else {
                    if (isErr) {
                        isErr = false;
                        disableButtons(reingressoBtn);
                        enableButtons(verificarBtn);
                        for (int index = 0; index < errorIndex.length; index++) {
                            if (errorIndex[index] == true) {
                                checkBoxDisabled(firstColumn.get(index));
                                checkBoxEnable(secondColumn.get(index));
                                secondErr.get(index).setVisibility(View.GONE);
                            }
                        }
                    } else {
                        editTextBox = 2;
                        if (!isReingresarBtnPressed.raised()) {

                            partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                            partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);

                            for (final CheckBox cb : secondColumn) {
                                checkBoxEnable(cb);
                                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(
                                            CompoundButton buttonView, boolean isChecked) {
                                        int getPosition = (Integer) buttonView.getId();
                                        candidatesList.get(getPosition).setCbTwoSelected(buttonView.isChecked());
                                        enableButtons(verificarBtn);
                                        disableButtons(reingressoBtn);
                                        verificarBtn.setText("Guardar");
                                        secondErr.get(secondColumn.indexOf(cb)).setVisibility(View.GONE);
                                        editTextBox = 4;
                                    }
                                });
                            }
                            // button Flags:
                            raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVotoBanderaBtnPressed,
                                    isTodosBtnPressed, isAceptarBtnPressed, isProximaBoletaPressed, isAbortBtnPressed);
                            lowerFlags(isReiniciarBoletaBtnPressed, isVerificarBtnPressed);
                            //---------------------------------------
                            //set colors :
                        } else if (firstScreen) {
                            enterBanderVotes();
                            disableButtons(verificarBtn);

                        }
                    }
                }
            }
        };
        return reingresoBtnListener;
    }

    private OnClickListener verificarListener() {
        OnClickListener verificarListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verificar Button
                // Possible Labels
                //      Guardar
                //      Verificar

                warningButtons(reinitiateBoletaBtn);
                isErr = false;
                for (int index = 0; index < errorIndex.length; index++) {
                    if (errorIndex[index] == true) {
                        isErr = true;
                        break;
                    }
                }

                if(verificarBtn.getText().toString().contains("Guardar")) {
                    // if contains Guardar

                    if(firstScreen){
                        //THIS IS GUARDAR FOR FIRST BANDERA VOTE
                        if (checkBoxColumn == 1) {
                            checkBoxColumn = 2;
                            banderaVoteIngresar = partido_et_one.getText().toString();
                            partido_et_one.removeTextChangedListener(tw1);
                            partido_et_one.setText("***");
                            enableButtons(reingressoBtn);
                            disableButtons(verificarBtn);
                            closeEditText(partido_et_one);
                        } else if (checkBoxColumn == 2) {
                            checkBoxColumn = 1;
                            verificarBtn.setText("Verificar");
                            banderaVoteIngresarTwo = partido_et_two.getText().toString();
                            partido_et_two.removeTextChangedListener(tw2);
                            partido_et_two.setText("***");
                            enableButtons(verificarBtn);
                            closeEditText(partido_et_two);
                        }
                    }else if((isTodos||isPref)&&!isBandera){
                    //  Check which process it is currently in
                    //      isTodos, isPref, isBandera
                    //          check which column, checkBoxColumn = 1,2
                        if(checkBoxColumn == 1){
                            //      if checkBoxColumn = 1
                            //          lock first column
                            for (CheckBox cb : firstColumn) {
                                checkBoxDisabled(cb);
                            }
                            for (CheckBox cb : secondColumn) {
                                checkBoxDisabled(cb);
                            }
                            //          set checkBoxColumn = 2
                            checkBoxColumn = 2;
                            //          set text verificar button "Verificar"
                            verificarBtn.setText("Verificar");
                            //          disable verificar button
                            disableButtons(verificarBtn);
                            //          enable Re-ingresar button
                            enableButtons(reingressoBtn);
                        }else if(checkBoxColumn ==2){
                            //      if checkBoxColumn = 2
                            //          lock second column
                            for (CheckBox cb : firstColumn) {
                                cb.setButtonDrawable(R.drawable.disabled_cb);
                                cb.setEnabled(false);
                            }
                            for (CheckBox cb : secondColumn) {
                                cb.setButtonDrawable(R.drawable.disabled_cb);
                                cb.setEnabled(false);
                            }
                            //          set checkBoxColumn = 1
                            checkBoxColumn = 1;
                            //          set text verificar button "Verificar"
                            verificarBtn.setText("Verificar");
                            //          verificar button remains enabled
                            enableButtons(verificarBtn);
                        }
                    }else if(isBandera) {
                        //  if isBandera
                        if(checkBoxColumn == 1) {
                            //      if checkBoxColumn = 1
                            //          set checkBoxColumn = 2
                            checkBoxColumn = 2;
                            //          set text verificar button "Verificar"
                            verificarBtn.setText("Verificar");
                            //          lock first check box (just set drawable, do not actually change any logic, this is only visual)
                            partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                            partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);
                            //          disable verificar button
                            disableButtons(verificarBtn);
                            //          enable re-ingresar button
                            enableButtons(reingressoBtn);
                        }else if(checkBoxColumn == 2) {
                            //      if checkBoxColumn = 2
                            //          set checkBoxColumn = 1
                            checkBoxColumn = 1;
                            //          set text verificar button "Verificar"
                            verificarBtn.setText("Verificar");
                            //          lock second check box (just set drawable, do not actually change any logic, this is only visual)
                            partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                            partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);
                            //          verificar button remains enabled
                            enableButtons(verificarBtn);
                        }
                    }
                }else if(verificarBtn.getText().toString().contains("Verificar")) {
                    // else if contain Verificar
                    // disable verificar button
                    disableButtons(verificarBtn);

                    if(firstScreen){
                        //for bandera on firstscreen (aka, not an added ballot) run verifyBanderaVote function
                        verifyBanderaValues();
                    }else {
                        if(isBandera && !(isTodos||isPref)) {
                            //  if isBandera   // this is case for correct bandera vote
                            //      if !fistScreen (aka, it is an added ballot)
                            //          check both Boxes
                            partido_cb_one.setButtonDrawable(R.drawable.dchecked);
                            partido_cb_two.setButtonDrawable(R.drawable.dchecked);
                            //          disable verificar button
                            disableButtons(verificarBtn);
                            //          enable aceptar button (aceptar button to run code to actually apply bandera vote (or right here just add code that bandera used to do**)
                            enableButtons(aceptarBtn);
                            // add votobanderbtn click code here
                            partido_votes.setText("1");
                        }else if((isTodos||isPref)&&!isBandera) {
                            for (TextView current : currentVotes) {
                                currentBallotPreferentialVotes = 1.0f / ((float) candidatesList.size());
                                current.setText(formatFloat(currentBallotPreferentialVotes));
                                current.setTypeface(null, Typeface.BOLD_ITALIC);
                            }

                            //CARLOS: 2016-09-08
                            for (int i = 0; i < candidatesList.size(); i++) {
                                if (candidatesList.get(i).isCbOneSelected() && candidatesList.get(i).isCbTwoSelected()) {

                                    currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
                                    currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                                }
                            }

                            // run code to verify preferential votes
                            verifyPreferentialVotes();
                        }else {
                            // this is error resulting in bandera vote mixed with pref vote, consolut on how to handle
                            ah.createCustomToast("Error in vote type, re-initiating ballot");
                            warningButtons(reinitiateBoletaBtn);
                            reinitiateBoletaBtn.performClick();
                        }
                    }
                }
            }
        };
        return verificarListener;
    }

    private OnClickListener verificarButtonListener() {
        OnClickListener verificarBtnListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isTodos){
                    if(todosGuardar1st){
                        todosGuardar1st = false;
                            for (CheckBox cb : firstColumn) {
                                cb.setButtonDrawable(R.drawable.disabled_cb);
                                cb.setEnabled(false);
                            }
                            for (CheckBox cb : secondColumn) {
                                cb.setButtonDrawable(R.drawable.disabled_cb);
                                cb.setEnabled(false);
                            }
                            for (Candidate cand : candidatesList) {
                                cand.setCbOneSelected(true);
                            }
                            for (TextView current : currentVotes) {
                                currentBallotPreferentialVotes = 1.0f / ((float) candidatesList.size());
                                current.setText(formatFloat(currentBallotPreferentialVotes));
                                current.setTypeface(null, Typeface.BOLD_ITALIC);
                            }

                            //CARLOS: 2016-09-08
                            for (int i = 0; i < candidatesList.size(); i++) {
                                if (candidatesList.get(i).isCbOneSelected() && candidatesList.get(i).isCbTwoSelected()) {

                                    currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
                                    currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                                }
                            }

                            // Show the candidates with marcas:
                            String marcas = "Candidatos Con Marcas: " + String.valueOf(candidatesList.size());
                            ((TextView) findViewById(R.id.marcas_tv)).setText(marcas);
                            findViewById(R.id.marcas_tv).setVisibility(View.VISIBLE);
                            raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVerificarBtnPressed, isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed, isAbortBtnPressed);
                            lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);

                            isVotosOrTodosPressed = 2;

                            enableButtons(reingressoBtn);
                            warningButtons(reinitiateBoletaBtn);
                            disableButtons(ingresoBtn, aceptarBtn, verificarBtn, votoBanderaBtn, todosBtn,
                                    proxBoletaBtn, descartarBtn);
                            verificarBtn.setText("Verificar");
                    }else {
                        if(verificarBtn.getText().toString().contains("Guardar")){

                                for(int index = 0; index < candidatesList.size(); index ++){
                                    candidatesList.get(index).setCbOneSelected(firstColumn.get(index).isChecked());
                                }

                                for (CheckBox cb : firstColumn) {
                                    cb.setButtonDrawable(R.drawable.disabled_cb);
                                    cb.setEnabled(false);
                                }
                                for (CheckBox cb : secondColumn) {
                                    cb.setButtonDrawable(R.drawable.disabled_cb);
                                    cb.setEnabled(false);
                                }
                                for (Candidate cand : candidatesList) {
//                                    cand.setCbOneSelected(true);
                                    cand.setCbTwoSelected(true);
                                }
                                for (TextView current : currentVotes) {
                                    currentBallotPreferentialVotes = 1.0f / ((float) candidatesList.size());
                                    current.setText(formatFloat(currentBallotPreferentialVotes));
                                    current.setTypeface(null, Typeface.BOLD_ITALIC);
                                }

                                //CARLOS: 2016-09-08
                                for (int i = 0; i < candidatesList.size(); i++) {
                                    if (candidatesList.get(i).isCbOneSelected()
                                            && candidatesList.get(i).isCbTwoSelected()) {

                                        currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
                                        currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                                    }
                                }

                                // Show the candidates with marcas:
                                String marcas = "Candidatos Con Marcas: " + String.valueOf(candidatesList.size());
                                ((TextView) findViewById(R.id.marcas_tv)).setText(marcas);
                                findViewById(R.id.marcas_tv).setVisibility(View.VISIBLE);
                                raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVerificarBtnPressed,
                                        isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed,
                                        isAbortBtnPressed);
                                lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);

                                isVotosOrTodosPressed = 2;

                                enableButtons(verificarBtn);
                                warningButtons(reinitiateBoletaBtn);
                                disableButtons(ingresoBtn, aceptarBtn, reingressoBtn, votoBanderaBtn, todosBtn,
                                        proxBoletaBtn, descartarBtn);
                                verificarBtn.setText("Verificar");
                        }else {
                                for (CheckBox cb : firstColumn) {
                                    cb.setButtonDrawable(R.drawable.dchecked);
                                    cb.setEnabled(false);
                                }
                                for (CheckBox cb : secondColumn) {
                                    cb.setButtonDrawable(R.drawable.dchecked);
                                    cb.setEnabled(false);
                                }
                                for (Candidate cand : candidatesList) {
//                                    cand.setCbOneSelected(true);
                                    cand.setCbTwoSelected(true);
                                }
                                for (TextView current : currentVotes) {
                                    currentBallotPreferentialVotes = 1.0f / ((float) candidatesList.size());
                                    current.setText(formatFloat(currentBallotPreferentialVotes));
                                    current.setTypeface(null, Typeface.BOLD_ITALIC);
                                }

                                //CARLOS: 2016-09-08
                                for (int i = 0; i < candidatesList.size(); i++) {
                                    if (candidatesList.get(i).isCbOneSelected()
                                            && candidatesList.get(i).isCbTwoSelected()) {

                                        currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
                                        currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                                    }
                                }

                                // Show the candidates with marcas:
                                String marcas = "Candidatos Con Marcas: " + String.valueOf(candidatesList.size());
                                ((TextView) findViewById(R.id.marcas_tv)).setText(marcas);
                                findViewById(R.id.marcas_tv).setVisibility(View.VISIBLE);
                                raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVerificarBtnPressed,
                                        isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed,
                                        isAbortBtnPressed);
                                lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);

                                isVotosOrTodosPressed = 2;

                                enableButtons(aceptarBtn);
                                warningButtons(reinitiateBoletaBtn);
                                disableButtons(ingresoBtn, reingressoBtn, verificarBtn, votoBanderaBtn, todosBtn,
                                        proxBoletaBtn, descartarBtn);
                                verificarBtn.setText("Verificar");
                        }
                    }
                }else {
                    if (verificarBtn.getText().toString().contains("Guardar")) {
                        if (anotherPress) {
                            isTodos = false;
                            anotherPress = false;
                            pressAgain = false;
                            verificarBtn.setText("Verificar");
                            enableButtons(verificarBtn);
                        } else {

                            /////
                            isErr = false;
                            for (int index = 0; index < errorIndex.length; index++) {
                                if (errorIndex[index] == true) {
                                    isErr = true;
                                    break;
                                }
                            }
                            if (isErr) {
                                verificarBtn.setText("Guardar");
                                enableButtons(reingressoBtn);
                                ah.setButtonColorRed(verificarBtn);
                                anotherPress = true;
//                            for (int index = 0; index < errorIndex.length; index++) {
//                                if (errorIndex[index] == true) {
//                                    checkBoxDisabled(firstColumn.get(index));
//                                    checkBoxEnable(secondColumn.get(index));
//                                    secondErr.get(index).setVisibility(View.GONE);
//                                }
//                            }
                            } else {
                                verificarBtn.setText("Verificar");
                                if (!partido_et_one.getText().toString().contains("*")) {
                                    banderaVoteIngresar = partido_et_one.getText().toString();
                                    Log.e("BVIngresar", banderaVoteIngresar);
                                    partido_et_one.setText("***");
                                }
                                if (editTextBox == 1) {
                                    partido_et_one.removeTextChangedListener(tw1);
                                    enableButtons(reingressoBtn);
                                    disableButtons(verificarBtn);
                                    closeEditText(partido_et_one);
                                } else if (editTextBox == 2) {
                                    partido_et_two.removeTextChangedListener(tw2);
                                    enableButtons(verificarBtn);
                                    closeEditText(partido_et_two);
                                } else if (editTextBox == 3) {
                                    if (!verpress) {
                                        verpress = true;
                                        disableButtons(verificarBtn, votoBanderaBtn, todosBtn, proxBoletaBtn, descartarBtn);
                                        warningButtons(reinitiateBoletaBtn);
                                        enableButtons(reingressoBtn);
                                        for (CheckBox cb : firstColumn) {
                                            // Lock the first column
                                            checkBoxDisabled(cb);
                                        }
                                    }

                                } else if (editTextBox == 4) {
                                    disableButtons(reingressoBtn, votoBanderaBtn, todosBtn, aceptarBtn, proxBoletaBtn, descartarBtn);
                                    warningButtons(reinitiateBoletaBtn);
                                    enableButtons(verificarBtn);
                                    for (CheckBox cb : secondColumn) {
                                        // Lock the second column
                                        checkBoxDisabled(cb);
                                    }
                                    for (CheckBox cb : firstColumn) {
                                        // Lock the first column
                                        checkBoxDisabled(cb);
                                    }

                                }
                            }
                            /////
                        }
                    } else if (verificarBtn.getText().toString().contains("Verificar")) {
                        verificarBtn.setText("Verificar");
                        float candidatesSelected = 0.000f;
                        if (!isVerificarBtnPressed.raised() && (!firstScreen || isUpdate)) {

                            buffer.setLength(0);
                            mismatchTv.setText("");

                            for (int i = 0; i < candidatesList.size(); i++) {

                                if (candidatesList.get(i).isCbOneSelected() && candidatesList.get(i).isCbTwoSelected()) {
                                    //COUNT CANDIDATES:
                                    candidatesSelected += 1.0f;
                                    // MATCH! lock boxes:
                                    firstColumn.get(i).setButtonDrawable(R.drawable.dchecked);
                                    secondColumn.get(i).setButtonDrawable(R.drawable.dchecked);
                                    firstColumn.get(i).setEnabled(false);
                                    secondColumn.get(i).setEnabled(false);
                                    firstErr.get(i).setVisibility(View.GONE);
                                    secondErr.get(i).setVisibility(View.GONE);
                                    errorIndex[i] = false;

                                } else if (!candidatesList.get(i).isCbOneSelected() && !candidatesList.get(i).isCbTwoSelected()) {
                                    // MATCH! lock boxes
                                    checkBoxDisabled(firstColumn.get(i));
                                    checkBoxDisabled(secondColumn.get(i));
                                    firstErr.get(i).setVisibility(View.GONE);
                                    secondErr.get(i).setVisibility(View.GONE);
                                    errorIndex[i] = false;

                                } else {
                                    //NO MATCH, enable boxes
                                    String messageToAppend = (ah.parseInt(candidatesList.get(i).getCandidateID(), 0)) + ", ";
                                    buffer.append(messageToAppend);
                                    checkBoxDisabled(firstColumn.get(i));
                                    firstColumn.get(i).setChecked(false);
                                    checkBoxDisabled(secondColumn.get(i));
                                    secondColumn.get(i).setChecked(false);
                                    firstErr.get(i).setVisibility(View.VISIBLE);
                                    secondErr.get(i).setVisibility(View.VISIBLE);
                                    errorIndex[i] = true;
//                                ah.setButtonColorRed(verificarBtn);
//                                ah.setButtonColorGreen(ingresoBtn);
//                                ingresoBtn.setText("CORREGIR");
                                }
                            }
                            // Done counting, now assign the vote values to the text box.
                            for (int i = 0; i < candidatesList.size(); i++) {
                                if (candidatesList.get(i).isCbOneSelected() && candidatesList.get(i).isCbTwoSelected()) {
                                    currentBallotPreferentialVotes = 1.0f / candidatesSelected;
                                    currentVotes.get(i).setText(formatFloat(currentBallotPreferentialVotes));
                                    currentVotes.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                                    // currentVotes.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);

                                    currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
                                    currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                                } else {
                                    currentVotes.get(i).setTypeface(null, Typeface.NORMAL);
                                    currentVotes.get(i).setTextColor(getResources().getColor(R.color.gray));
                                    //gray edit text
                                }
                            }
                            // Show the candidates with marcas:
                            String marcas = "Candidatos Con Marcas: " + String.format(Locale.US, "%.0f", candidatesSelected); //whole digits
                            ((TextView) findViewById(R.id.marcas_tv)).setText(marcas);
                            findViewById(R.id.marcas_tv).setVisibility(View.VISIBLE);

                            if (buffer.length() != 0) {
                                String message = "Error en Candidato(s): " + buffer.toString().substring(0, buffer.toString().length() - 2);
                                mismatchTv.setText(message);
                            }
                            raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isProximaBoletaPressed,
                                    isAbortBtnPressed, isVotoBanderaBtnPressed, isTodosBtnPressed);
                            lowerFlags(isReiniciarBoletaBtnPressed);
                            isVerificarBtnPressed.setFlagValue(!(mismatchTv.length() > 0));
                            isAceptarBtnPressed.setFlagValue(mismatchTv.length() > 0);
                            isVotosOrTodosPressed = 2;
                            disableButtons(ingresoBtn, reingressoBtn, votoBanderaBtn, todosBtn, proxBoletaBtn, descartarBtn);
                            warningButtons(reinitiateBoletaBtn);
//                        enableButtons(reinitiateBoletaBtn);

                            if (mismatchTv.length() == 0 && candidatesSelected != 0) {
                                enableButtons(aceptarBtn);
                                disableButtons(verificarBtn);
                            } else if (candidatesSelected == 0) {
                                ah.createCustomToast("Completar Preferencias!");
                                disableButtons(aceptarBtn, verificarBtn);
                            } else {
                                ah.setButtonColorRed(verificarBtn);
                                ah.setButtonColorGreen(ingresoBtn);
                                ingresoBtn.setText("Corregir");
                                pressAgain = true;
//                            disableButtons(aceptarBtn);
//                            enableButtons(verificarBtn);
                            }

                        } else if (firstScreen) {
//                        mCustomKeyboard.hideCustomKeyboard();
                            verifyBanderaValues();
                            disableButtons(verificarBtn);
                        }
                    }
                }
            }
        };
        return verificarBtnListener;
    }

    private OnClickListener banderaListener() {
        return new OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                //Bandera Button
                //  possible lables
                //      Bandera
                //      Corregir
                //  on firstScreen number input of bandera votes
                //  when added ballots bandera button flow similar to pref and todos
                warningButtons(reinitiateBoletaBtn);
                if(votoBanderaBtn.getText().toString().contains("Bandera")) {
                    // if text is Bandera
                    if(firstScreen) {
                        partido_cb_one.setVisibility(View.GONE);
                        partido_cb_two.setVisibility(View.GONE);
                        partido_et_one.setVisibility(View.VISIBLE);
                        partido_et_two.setVisibility(View.VISIBLE);
                        //  if firstScreen
                        //  set text ingresar button "Ingresar"
                        ingresoBtn.setText("Ingresar");
                        //  disable bander button
                        disableButtons(votoBanderaBtn);
                        //  enable ingresar button
                        enableButtons(ingresoBtn);
                    }else {
                        isBandera = true;
                        //  if !firstScreen
                        //      disable todos button
                        //      disable bandera button
                        disableButtons(todosBtn,votoBanderaBtn);
                        if(checkBoxColumn == 1) {
                            //      if checkBoxColumn = 1
                            //          set isBandera = true
                            isBandera = true;
                            //          set text ingreso button "Ingresar"
                            ingresoBtn.setText("Ingresar");
                            //          enable ingreso button
                            enableButtons(ingresoBtn);
                        }else if(checkBoxColumn ==2) {
                            //      if checkBoxColumn = 2
                            //          set text verificar button "Guardar"
                            verificarBtn.setText("Guardar");
                            //          check second bandera checkbox
                            partido_cb_two.setButtonDrawable(R.drawable.dchecked);
                            //          disable ingreso button
                            disableButtons(ingresoBtn);
                            //          enable verificar button
                            enableButtons(verificarBtn);
                        }
                    }
                }else if(votoBanderaBtn.getText().toString().contains("Corregir")) {
                    // if text is Corregir
                    //  set text ingreso button "Ingresar
                    ingresoBtn.setText("Ingresar");
                    //  set text bandera button "Bandera"
                    votoBanderaBtn.setText("Bandera");
                    //  clear error icons
                    partido_et_one.setHint("");
                    partido_et_two.setHint("");
                    //asdfasdf this is where ingreso was enable incorrectly
                }
            }
        };
    }

    private OnClickListener banderaButtonListener() {
        return new OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {


                if(votoBanderaBtn.getText().toString().contains("Corregir")){
                    votoBanderaBtn.setText("Bandera");
                    partido_et_one.setHint("");
                    partido_et_two.setHint("");
                }
                Log.i("Bandera Button was pressed", "true");
                Log.i("firstScreen value ", String.valueOf(firstScreen));
                ingresoBtn.setText("Ingresar");
                if (!isVotoBanderaBtnPressed.raised()) {
                    if (firstScreen) {
                        disableButtons(votoBanderaBtn);
                        partido_cb_one.setVisibility(View.GONE);
                        partido_cb_two.setVisibility(View.GONE);
                        partido_et_one.setVisibility(View.VISIBLE);
                        partido_et_two.setVisibility(View.VISIBLE);
                        enableButtons(ingresoBtn);
                    } else {
                        //if statement to test if first or second check box
                        if (checkBoxColumn == 1) {
                            isBandera = true;
                            //Check first cb for bandera vote
//                            partido_cb_one.setButtonDrawable(R.drawable.dchecked);
                            //enable reingresar
                            //disable bandera button
                            //set variable for re-entry of bandera vote (do not allow into this if statement)
                            // reingrasar button then only diables itself and enables bandera button
//                            checkBoxColumn = 2;
                            enableButtons(ingresoBtn);
                            disableButtons(votoBanderaBtn);
                        } else {
                            partido_cb_one.setButtonDrawable(R.drawable.dchecked);
                            partido_cb_two.setButtonDrawable(R.drawable.dchecked);
                            partido_votes.setText("1");

                            raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVerificarBtnPressed,
                                    isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed, isAbortBtnPressed);
                            lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);
                            isVotosOrTodosPressed = 1;

                            enableButtons(aceptarBtn);
                            warningButtons(reinitiateBoletaBtn);
                            disableButtons(ingresoBtn, reingressoBtn, verificarBtn, votoBanderaBtn,
                                    todosBtn, proxBoletaBtn, descartarBtn);
                            for (TextView current : currentVotes) {
                                float proposedVote = 1.0f / ((float) candidatesList.size());
                                current.setText(formatFloat(proposedVote));
                                current.setTypeface(null, Typeface.BOLD_ITALIC);
                            }
                        }
                    }
                }
            }
        };
    }

    private OnClickListener todosListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todos Button

                warningButtons(reinitiateBoletaBtn);

                isTodos = true;
                verificarBtn.setText("Guardar");
                // set text verificar button "Guardar"
                // disable todos button
                // disable bandera button
                // disable ingreso button
                disableButtons(todosBtn, votoBanderaBtn, ingresoBtn);
                // enable verificar button
                enableButtons(verificarBtn);
                if(checkBoxColumn == 1) {
                    // if checkBoxColumn = 1
                    //  set isTodos = true
                    isTodos = true;
                    //  check all boxes in first column
                    int i = 0;
                    for (CheckBox cb : firstColumn) {
                        cb.setButtonDrawable(R.drawable.dchecked);
                        cb.setEnabled(false);
                        cb.setChecked(true);
                        candidatesList.get(i).setCbOneSelected(cb.isChecked());
                        i++;
                    }
                    for (CheckBox cb : secondColumn) {
                        cb.setButtonDrawable(R.drawable.disabled_cb);
//                        cb.setChecked(false);
                        cb.setEnabled(false);
                    }
                    warningButtons(reinitiateBoletaBtn);

                }else if(checkBoxColumn == 2) {
                    // if checkBoxColumn = 2
                    //  check all boxes in second column
                    int i = 0;
                    for (CheckBox cb : secondColumn) {
                        cb.setButtonDrawable(R.drawable.dchecked);
                        cb.setEnabled(false);
                        cb.setChecked(true);
                        candidatesList.get(i).setCbTwoSelected(cb.isChecked());
                        i++;
                    }
                    for (CheckBox cb : firstColumn) {
                        cb.setButtonDrawable(R.drawable.disabled_cb);
//                        cb.setChecked(false);
                        cb.setEnabled(false);
                    }
                    warningButtons(reinitiateBoletaBtn);
                }
            }
        };
    }

    private OnClickListener todosButtonListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons(votoBanderaBtn);
                //TODO
                //if first todos press

                //  If first column is unlocked
                //      Select all members of first column
                //  else
                //      Select all members of second column

                //else
                //  if first column is unlocked
                //      Unselect all members of first column
                //  else
                //      Unselect all members of second column
                //end TODO

                //State button label = Todos
                //On press button label change to Ingresar
                //Button remains enabled
                if(isTodosReingresar){
                    isTodosReingresar = false;

                    for (CheckBox cb : firstColumn) {
                        cb.setButtonDrawable(R.drawable.disabled_cb);
                        cb.setEnabled(false);
                    }
                    for (CheckBox cb : secondColumn) {
                        cb.setButtonDrawable(R.drawable.dchecked);
                        cb.setEnabled(false);
                    }
                    for (Candidate cand : candidatesList) {
                        cand.setCbOneSelected(true);
                        cand.setCbTwoSelected(true);
                    }
                    for (TextView current : currentVotes) {
                        currentBallotPreferentialVotes = 1.0f / ((float) candidatesList.size());
                        current.setText(formatFloat(currentBallotPreferentialVotes));
                        current.setTypeface(null, Typeface.BOLD_ITALIC);
                    }

                    //CARLOS: 2016-09-08
                    for (int i = 0; i < candidatesList.size(); i++) {
                        if (candidatesList.get(i).isCbOneSelected()
                                && candidatesList.get(i).isCbTwoSelected()) {

                            currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
                            currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                        }
                    }

                    // Show the candidates with marcas:
                    String marcas = "Candidatos Con Marcas: " + String.valueOf(candidatesList.size());
                    ((TextView) findViewById(R.id.marcas_tv)).setText(marcas);
                    findViewById(R.id.marcas_tv).setVisibility(View.VISIBLE);
                    raiseFlags(isIngresoBtnPressed ,isReingresarBtnPressed ,isVerificarBtnPressed,
                            isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed,
                            isAbortBtnPressed);
                    lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);

                    isVotosOrTodosPressed = 2;

                    enableButtons(verificarBtn);
                    warningButtons(reinitiateBoletaBtn);
                    disableButtons(ingresoBtn, reingressoBtn, aceptarBtn, votoBanderaBtn, todosBtn,
                            proxBoletaBtn, descartarBtn);
                    verificarBtn.setText("Guardar");
                }else {
                    if (!isTodos) {
//                        enableButtons(ingresoBtn);
//                        ingresoBtn.setText("Ingresar");
                        disableButtons(todosBtn);
                        isTodos = true;
                        todosGuardar1st = true;
                        ingresoBtn.setText("Preferencial");
                        if (!isTodosBtnPressed.raised() && (!firstScreen || isUpdate)) {
                            int i = 0;
                            for (CheckBox cb : firstColumn) {
                                cb.setButtonDrawable(R.drawable.dchecked);
                                cb.setEnabled(false);
                                cb.setChecked(true);
                                candidatesList.get(i).setCbOneSelected(cb.isChecked());
                                i++;
                            }
                            for (CheckBox cb : secondColumn) {
                                cb.setButtonDrawable(R.drawable.disabled_cb);
                                cb.setChecked(false);
                                cb.setEnabled(false);
                            }

                            raiseFlags(isIngresoBtnPressed, isReingresarBtnPressed, isVerificarBtnPressed, isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed, isAbortBtnPressed);
                            lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);

                            isVotosOrTodosPressed = 2;

                            enableButtons(verificarBtn);
                            verificarBtn.setText("Guardar");
                            warningButtons(reinitiateBoletaBtn);
                            disableButtons(ingresoBtn, reingressoBtn, aceptarBtn, votoBanderaBtn, todosBtn, proxBoletaBtn, descartarBtn);
                        }
                    } else {
                        enableButtons(reingressoBtn);
                        disableButtons(todosBtn);
                    }
                }
                //State button label = Ingresar
                //On press button label change to Guardar
                //First column cb are checked
                //Button remains enabled

                //State button label = Guardar
                //First column cb are disabled
                //On press button label change to Reingresar
                //Button remains enabled

                //State button label = Reingresar
                //On press button label change to Guardar
                //Second column cb are checked
                //Button remains enabled

                //State button label = Guardar
                //On press button label change to Verificar
                //Second column cb are disabled
                //Button remains enabled

                //State button label = Verificar
                //On press button label change to Todos
                //Do previous actions to count todos





//                if (!isTodosBtnPressed.raised() && (!firstScreen || isUpdate)) {
//                    for (CheckBox cb : firstColumn) {
//                        cb.setButtonDrawable(R.drawable.dchecked);
//                        cb.setEnabled(false);
//                    }
//                    for (CheckBox cb : secondColumn) {
//                        cb.setButtonDrawable(R.drawable.dchecked);
//                        cb.setEnabled(false);
//                    }
//                    for (Candidate cand : candidatesList) {
//                        cand.setCbOneSelected(true);
//                        cand.setCbTwoSelected(true);
//                    }
//                    for (TextView current : currentVotes) {
//                        currentBallotPreferentialVotes = 1.0f / ((float) candidatesList.size());
//                        current.setText(formatFloat(currentBallotPreferentialVotes));
//                        current.setTypeface(null, Typeface.BOLD_ITALIC);
//                    }
//
//                    //CARLOS: 2016-09-08
//                    for (int i = 0; i < candidatesList.size(); i++) {
//                        if (candidatesList.get(i).isCbOneSelected()
//                                && candidatesList.get(i).isCbTwoSelected()) {
//
//                            currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
//                            currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
//                        }
//                    }
//
//                    // Show the candidates with marcas:
//                    String marcas = "Candidatos Con Marcas: " + String.valueOf(candidatesList.size());
//                    ((TextView) findViewById(R.id.marcas_tv)).setText(marcas);
//                    findViewById(R.id.marcas_tv).setVisibility(View.VISIBLE);
//                    raiseFlags(isIngresoBtnPressed ,isReingresarBtnPressed ,isVerificarBtnPressed,
//                            isVotoBanderaBtnPressed, isTodosBtnPressed, isProximaBoletaPressed,
//                            isAbortBtnPressed);
//                    lowerFlags(isReiniciarBoletaBtnPressed, isAceptarBtnPressed);
//
//                    isVotosOrTodosPressed = 2;
//
//                    enableButtons(aceptarBtn);
//                    warningButtons(reinitiateBoletaBtn);
//                    disableButtons(ingresoBtn, reingressoBtn, verificarBtn, votoBanderaBtn, todosBtn,
//                            proxBoletaBtn, descartarBtn);
//                }
            }
        };
    }

    private OnClickListener reinitateListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firstScreen){
                    rejectBanderaVotes();
                    partido_et_one.addTextChangedListener(tw1);
                    partido_et_two.addTextChangedListener(tw2);
                    initialVoteConditions();
                }else initialVoteConditions();
            }
        };
    }

    private OnClickListener reinitateButtonListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxColumn = 1;
                isErr = false;
                anotherPress = false;
                pressAgain = false;
                isBandera = false;
                isTodos = false;
                todosGuardar1st = false;
                isTodosReingresar = false;
                if(ingresoBtn.getText().toString().contains("Ingresar")){
                    ingresoBtn.setText("Preferencial");
                }
//                isTodosBtnPressed;
//                isIngresoBtnPressed.setFlagValue(false);
//                isReingresarBtnPressed.setFlagValue(false);
//                isVerificarBtnPressed.setFlagValue(false);
//                isReiniciarBoletaBtnPressed.setFlagValue(false);
//                isAceptarBtnPressed.setFlagValue(false);
//                isProximaBoletaPressed;
//                isAbortBtnPressed;

                if(ingresoBtn.getText().toString().contains("Corregir")){
                    ingresoBtn.setText("Preferencial");
                }

                if(votoBanderaBtn.getText().toString().contains("Corregir")){
                    votoBanderaBtn.setText("Bandera");
                }

                for(int index = 0; index < errorIndex.length; index++){
                    if(errorIndex[index] == true){
                        firstErr.get(index).setVisibility(View.GONE);
                        secondErr.get(index).setVisibility(View.GONE);
                        errorIndex[index] = false;
                    }
                }
                partido_et_two.setHint("");
                partido_et_one.setHint("");
                bandError = false;
                if (!isReiniciarBoletaBtnPressed.raised()) {

                    db_adapter = new DatabaseAdapterParlacen(ParlacenVoteTableActivity.this);
                    db_adapter.open();

                    //CARLOS: 2016-09-10
                    for (int i = 0; i < candidatesList.size(); i++) {
                        if (candidatesList.get(i).isCbOneSelected()
                                && candidatesList.get(i).isCbTwoSelected()) {
//                            currentMarks.get(i).setText("0");

                            Log.e("CARLOS getMarcas()", String.valueOf(currentMarks.get(i).getText()));
                            int showThis = Integer.parseInt(String.valueOf(currentMarks.get(i).getText()));
                            currentMarks.get(i).setText(showThis > 0 ? String.valueOf((showThis - 1)) : String.valueOf(showThis));
                            currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
                        }
                    }

                    partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                    partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);


                    for (CheckBox cb : firstColumn) {
                        cb.setChecked(false);
                        checkBoxDisabled(cb);
                    }

                    for (CheckBox cb : secondColumn) {
                        cb.setChecked(false);
                        checkBoxDisabled(cb);
                    }

                    for (Candidate cand : candidatesList) {
                        cand.setCbOneSelected(false);
                        cand.setCbTwoSelected(false);
                    }
                    for (TextView current : currentVotes) {
                        current.setText("0.000");
                        current.setTypeface(null, Typeface.NORMAL);
                        current.setTextColor(getResources().getColor(R.color.DarkBlue));
                    }


                    partido_votes.setText("0");
                    findViewById(R.id.marcas_tv).setVisibility(View.INVISIBLE);

                    raiseFlags(isReingresarBtnPressed,isVerificarBtnPressed, isReiniciarBoletaBtnPressed,
                            isAceptarBtnPressed,isProximaBoletaPressed);
                    lowerFlags(isIngresoBtnPressed,isVotoBanderaBtnPressed,isTodosBtnPressed,
                            isAbortBtnPressed);
                    isVotosOrTodosPressed = 0;

                    mismatchTv.setText("");
                    enableButtons(ingresoBtn, todosBtn);
                    disableButtons(reingressoBtn, verificarBtn, reinitiateBoletaBtn, aceptarBtn, proxBoletaBtn);
                    warningButtons(descartarBtn);
                    if (isUpdate) {
                        enableButtons(votoBanderaBtn);
                    }
                } else if (firstScreen) {
                    rejectBanderaVotes();
                    partido_et_one.addTextChangedListener(tw1);
                    partido_et_two.addTextChangedListener(tw2);
                }
            }
        };
    }

    private OnClickListener aceptarListener() {
        return new OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                //Aceptar Button
                // button only enabled from verificar functionality
                // this button accepts vote and submits them properly
                disableButtons(reinitiateBoletaBtn);
                // if firstScreen
                if(firstScreen) {
                    acceptBanderaVotes();
                    ingresoBtn.setText("Preferencial");
                    reingressoBtn.setText("Re-ingresar\nPreferencia");
                } else {
                    // else
                    if (isBandera && !(isTodos||isPref)) {
                        partidoCandidate.setVotesNumber(++partido_votes_int);
                        partidoCandidate.setPreferentialVotes(partidoCandidate.getVotesNumber());
                        partido_votes_two.setText(String.format(Locale.US, "%.0f", partido_votes_int));
                        //attempt at adding preferencias to each candidate:
                        for (int i = 0; i < candidatesList.size(); i++) {
                            float number = candidatesList.get(i).getVotesNumber();
                            float divider = (float) candidatesList.size();
                            float candidateBanderaVote = 1.0f / divider;
                            candidatesList.get(i).setVotesNumber(number + candidateBanderaVote);
                            //for display:
                            candidatesList.get(i).setBanderaNumber(partido_votes_int / candidatesList.size());
                            accumulatedVotes.get(i).setText(formatFloat(candidatesList.get(i).getVotesNumber()));
                        }
                    }
                    // if todos button pressed add to candidates and display
                    // number
                    if (!isBandera && (isTodos||isPref)) {
                        ++preferentialVotes;

                        for (int i = 0; i < candidatesList.size(); i++) {
                            if (candidatesList.get(i).isCbOneSelected() & candidatesList.get(i).isCbTwoSelected()) {
                                float prefVotes = candidatesList.get(i).getPreferentialVotes();
                                float number = candidatesList.get(i).getVotesNumber();
                                int marcas = candidatesList.get(i).getMarcas();
                                candidatesList.get(i).setVotesNumber(number + currentBallotPreferentialVotes);
                                candidatesList.get(i).setPreferentialVotes(prefVotes + currentBallotPreferentialVotes);
                                candidatesList.get(i).setMarcas(++marcas);
                                accumulatedVotes.get(i).setText(formatFloat(candidatesList.get(i).getVotesNumber()));
                            }
                        }
                    }
                    disableButtons(ingresoBtn, reingressoBtn, verificarBtn, votoBanderaBtn, todosBtn, reinitiateBoletaBtn, aceptarBtn, descartarBtn);
                    enableButtons(proxBoletaBtn);
                }

                // this code always runs
                String voteCounts = "Contados" + "\n" + "Votos Bandera: " + String.format(Locale.US, "%.0f", partido_votes_int) + "\n" + "Votos Preferenciales: " + String.format(Locale.US, "%.0f", preferentialVotes);
                String bol = String.valueOf(currentBallotNumber);
                String columnHeader = "Votos"; //Preferencias
                colOneTV.setText("Bol" + bol);
                colTwoTV.setText("Cum");
                colThreeTV.setText("Cum");
                partyNameTv.setText(columnHeader);
                banderaVotesTv.setText(voteCounts);
            }
        };
    }

    private OnClickListener aceptarButtonListener() {
        return new OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                isTodosReingresar = false;
                isTodos = false;
                todosGuardar1st = false;
                if(ingresoBtn.getText().toString().contains("Ingresar")){
                    ingresoBtn.setText("Preferencial");
                }
                if (!isAceptarBtnPressed.raised()) {
                    Log.e("isAceptarBtnPressed false", "!isAceptarBtnPressed");
                    // if voto bandera button pressed add to partido and display
                    // number
                    if (isVotosOrTodosPressed == 1) {
                        partidoCandidate.setVotesNumber(++partido_votes_int);
                        partidoCandidate.setPreferentialVotes(partidoCandidate.getVotesNumber());
                        partido_votes_two.setText(String.format(Locale.US, "%.0f", partido_votes_int));
                        //attempt at adding preferencias to each candidate:
                        for (int i = 0; i < candidatesList.size(); i++) {
                            float number = candidatesList.get(i).getVotesNumber();
                            float divider = (float) candidatesList.size();
                            float candidateBanderaVote = 1.0f / divider;
                            candidatesList.get(i).setVotesNumber(number + candidateBanderaVote);
                            //for display:
                            candidatesList.get(i).setBanderaNumber(partido_votes_int / candidatesList.size());
                            accumulatedVotes.get(i).setText(formatFloat(candidatesList.get(i).getVotesNumber()));
                        }
                    }
                    // if todos button pressed add to candidates and display
                    // number
                    if (isVotosOrTodosPressed == 2) {
                        ++preferentialVotes;

                        for (int i = 0; i < candidatesList.size(); i++) {
                            if (candidatesList.get(i).isCbOneSelected()
                                    & candidatesList.get(i).isCbTwoSelected()) {
                                float prefVotes = candidatesList.get(i).getPreferentialVotes();
                                float number = candidatesList.get(i).getVotesNumber();
                                int marcas = candidatesList.get(i).getMarcas();
                                candidatesList.get(i).setVotesNumber(number + currentBallotPreferentialVotes);
                                candidatesList.get(i).setPreferentialVotes(prefVotes + currentBallotPreferentialVotes);
                                candidatesList.get(i).setMarcas(++marcas);
                                accumulatedVotes.get(i).setText(formatFloat(candidatesList.get(i).getVotesNumber()));
                            }
                        }
                    }
                    raiseFlags(isIngresoBtnPressed ,isReingresarBtnPressed ,isVerificarBtnPressed,
                            isVotoBanderaBtnPressed,isTodosBtnPressed, isReiniciarBoletaBtnPressed,
                            isAceptarBtnPressed);
                    lowerFlags(isProximaBoletaPressed);
                    disableButtons(ingresoBtn, reingressoBtn, verificarBtn, votoBanderaBtn, todosBtn,
                            reinitiateBoletaBtn, aceptarBtn, descartarBtn);
                    enableButtons(proxBoletaBtn);
                } else if (firstScreen) {
                    Log.e("isAceptarBtnPressed true", "isAceptarBtnPressed");
                    acceptBanderaVotes();
                    ingresoBtn.setText("Preferencial");
                    reingressoBtn.setText("Re-ingresar\nPreferencia");

                }
                String voteCounts = "Contados"
                        + "\n"
                        + "Votos Bandera: "
                        + String.format(Locale.US, "%.0f", partido_votes_int)
                        + "\n"
                        + "Votos Preferenciales: "
                        + String.format(Locale.US, "%.0f", preferentialVotes);
                String bol = String.valueOf(currentBallotNumber);
                String columnHeader = "Votos"; //Preferencias
                colOneTV.setText("Bol" + bol);
                colTwoTV.setText("Cum");
                colThreeTV.setText("Cum");
                partyNameTv.setText(columnHeader);
                banderaVotesTv.setText(voteCounts);
            }
        };
    }

    private OnClickListener proximoListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Proximo Button
                //  this button moves to next ballot and reset initial vote conditions
                //  and if there are no longer any ballots, moves to parlacenCandList activity

                disableButtons(reinitiateBoletaBtn);
                int totalVotes = ah.parseInt(partyArrayList.get(partyArrayIndex).getParty_boletas(), 0);
                //TODO EXAMINE THIS CODE

                if (currentBallotNumber < totalVotes) {
                    findViewById(R.id.marcas_tv).setVisibility(View.INVISIBLE);
                    //if ballots is less than max ballots, load next ballot:
                    for (Candidate selected : candidatesList) {
                        selected.setCbOneSelected(false);
                        selected.setCbTwoSelected(false);
                    }
                    for (CheckBox cb : firstColumn) {
                        cb.setChecked(false);
                        firstErr.get(firstColumn.indexOf(cb)).setVisibility(View.GONE);
                        checkBoxDisabled(cb);
                    }
                    for (CheckBox cb : secondColumn) {
                        cb.setChecked(false);
                        secondErr.get(secondColumn.indexOf(cb)).setVisibility(View.GONE);
                        checkBoxDisabled(cb);
                    }
                    partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                    partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);

                    currentBallotNumber++;
                    if (firstScreen) {
                        firstScreen = false;
                    }
                    String outMessage = "Boleta No: " + currentBallotNumber + "\n" + "Votos de Partido: " + String.format(Locale.US, "%.0f", Float.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes())) + "\n" + "Total de Boletas: " + partyArrayList.get(partyArrayIndex).getParty_boletas();
                    String cumbol = String.valueOf(currentBallotNumber - 1);
                    String columnHeader = "Votos"; //Preferencias
                    partyNameTv.setText(columnHeader);
                    colOneTV.setText("Bol" + String.valueOf(currentBallotNumber));
                    colTwoTV.setText("Cum");
                    colThreeTV.setText("Cum");

                    ballotNmbTv.setText(outMessage);
                    for (int i = 0; i < candidatesList.size(); i++) {
                        currentVotes.get(i).setText(formatFloat(0f));
                        currentVotes.get(i).setTypeface(null, Typeface.NORMAL);
                        currentVotes.get(i).setTextColor(getResources().getColor(R.color.DarkBlue));
                        previousVotes.get(i).setText(accumulatedVotes.get(i).getText());
                    }
                    partido_votes.setText("0");
                    partidoPrevious.setText(partido_votes_two.getText());
                    initialVoteConditions();
                    warningButtons(descartarBtn);

                } else {
                    // if this is the last ballot then move on and save to db:
                    ArrayList<Candidate> candidatesListSelectedArrayList = saveOrUpdateCandidateVotes(false);
                    ah.savePreferences("currentBallotNumber", currentBallotNumber);
                    ah.savePreferences("firstScreen", false);
                    Bundle b = new Bundle();
                    b.putParcelable("com.afilon.tse.votingcenter", vc);
                    b.putParcelable("com.afilon.tse.escrudata", escrudata);
                    b.putInt("partyNumber", partyArrayIndex);
                    b.putParcelableArrayList("selectedCandidates", candidatesListSelectedArrayList);
                    Intent search = new Intent(ParlacenVoteTableActivity.this, Consts.CANDLISTACT);
                    search.putExtras(b);
                    startActivity(search);
                    finish();
                    }
            }
        };
    }

    private OnClickListener proximaboletaButtonListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalVotes = ah.parseInt(partyArrayList.get(partyArrayIndex).getParty_boletas(), 0);
                // AFTER PRESSING ACEPTAR isProximaBoletaPressed is FALSE
                if (currentBallotNumber < totalVotes) {
                    findViewById(R.id.marcas_tv).setVisibility(View.INVISIBLE);
                    //if ballots is less than max ballots, load next ballot:
                    for (Candidate selected : candidatesList) {
                        selected.setCbOneSelected(false);
                        selected.setCbTwoSelected(false);
                    }
                    for (CheckBox cb : firstColumn) {
                        cb.setChecked(false);
                        firstErr.get(firstColumn.indexOf(cb)).setVisibility(View.GONE);
                        checkBoxDisabled(cb);
                    }
                    for (CheckBox cb : secondColumn) {
                        cb.setChecked(false);
                        secondErr.get(secondColumn.indexOf(cb)).setVisibility(View.GONE);
                        checkBoxDisabled(cb);
                    }
                    partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
                    partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);

                    currentBallotNumber++;
                    if (firstScreen) {
                        firstScreen = false;
                    }
                    String outMessage = "Boleta No: " + currentBallotNumber + "\n" + "Votos de Partido: " + String.format(Locale.US, "%.0f", Float.parseFloat(partyArrayList.get(partyArrayIndex).getParty_votes())) + "\n" + "Total de Boletas: " + partyArrayList.get(partyArrayIndex).getParty_boletas();
                    String cumbol = String.valueOf(currentBallotNumber - 1);
                    String columnHeader = "Votos"; //Preferencias
                    partyNameTv.setText(columnHeader);
                    colOneTV.setText("Bol" + String.valueOf(currentBallotNumber));
                    colTwoTV.setText("Cum");
                    colThreeTV.setText("Cum");

                    ballotNmbTv.setText(outMessage);
                    for (int i = 0; i < candidatesList.size(); i++) {
                        currentVotes.get(i).setText(formatFloat(0f));
                        currentVotes.get(i).setTypeface(null, Typeface.NORMAL);
                        currentVotes.get(i).setTextColor(getResources().getColor(R.color.DarkBlue));
                        previousVotes.get(i).setText(accumulatedVotes.get(i).getText());
                    }
                    partido_votes.setText("0");
                    partidoPrevious.setText(partido_votes_two.getText());
                    isVotosOrTodosPressed = 0;
                    raiseFlags(isReingresarBtnPressed,isVerificarBtnPressed,isReiniciarBoletaBtnPressed,isAceptarBtnPressed, isProximaBoletaPressed);

                    lowerFlags(isIngresoBtnPressed,isVotoBanderaBtnPressed,isTodosBtnPressed,isAbortBtnPressed);
                    isVotosOrTodosPressed = 0;
                    disableButtons(reingressoBtn, verificarBtn, reinitiateBoletaBtn, aceptarBtn, proxBoletaBtn);
                    enableButtons(ingresoBtn, todosBtn);
                    if(isUpdate){
                        enableButtons(votoBanderaBtn);
                    }
                    warningButtons(descartarBtn);
//                    ArrayList<Candidate> candidatesListSelectedArrayList = saveOrUpdateCandidateVotes(false);
                } else {
                    // if this is the last ballot then move on and save to db:
                    ArrayList<Candidate> candidatesListSelectedArrayList = saveOrUpdateCandidateVotes(false);
                    ah.savePreferences("currentBallotNumber", currentBallotNumber);
                    ah.savePreferences("firstScreen", false);
                    Bundle b = new Bundle();
                    b.putParcelable("com.afilon.tse.votingcenter", vc);
                    b.putParcelable("com.afilon.tse.escrudata", escrudata);
                    b.putInt("partyNumber", partyArrayIndex);
                    b.putParcelableArrayList("selectedCandidates", candidatesListSelectedArrayList);
                    Intent search = new Intent(ParlacenVoteTableActivity.this, Consts.CANDLISTACT);
                    search.putExtras(b);
                    startActivity(search);
                    finish();
                }
            }
        };
    }

//--------------------------------------------------------------------------------------------------

    private void unlockColumn(int column, boolean error){
        //unlock column entries
        //if error, unlock only entries at errorIndex = true
        //else unlock all
        partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
        partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);

        if(column == 1){
            for (CheckBox cb : secondColumn) {
                checkBoxDisabled(cb);
            }

            if(error){
                for(int index = 0; index < errorIndex.length; index++) {
                    if(errorIndex[index] == true) {
                        candidatesList.get(index).setCbOneSelected(false);
                    }
                }
                for(int index = 0; index < errorIndex.length; index++){
                    if(errorIndex[index] == true){
                        checkBoxEnable(firstColumn.get(index));
                        firstColumn.get(index).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                int getPosition = (Integer) buttonView.getId();
                                candidatesList.get(getPosition).setCbOneSelected(buttonView.isChecked());
                                enableButtons(verificarBtn);
                                disableButtons(ingresoBtn);
                                verificarBtn.setText("Guardar");
                                editTextBox = 3;
                            }
                        });
                    }
                }
            }else{
                for ( final CheckBox cb : firstColumn){
                    checkBoxEnable(cb);
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            int getPosition = (Integer) buttonView.getId();
                            candidatesList.get(getPosition).setCbOneSelected(buttonView.isChecked());
                            enableButtons(verificarBtn);
                            disableButtons(ingresoBtn);
                            verificarBtn.setText("Guardar");
                        }
                    });
                }
            }
        }else if(column ==2){ //////////////////////////////////////////////////////////////////////
            for (CheckBox cb : firstColumn) {
                checkBoxDisabled(cb);
            }
            if(error){
                for(int index = 0; index < errorIndex.length; index++) {
                    if(errorIndex[index] == true) {
                        candidatesList.get(index).setCbTwoSelected(false);
                    }
                }
                for(int index = 0; index < errorIndex.length; index++){
                    if(errorIndex[index] == true){
                        checkBoxEnable(secondColumn.get(index));
                        secondColumn.get(index).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                int getPosition = (Integer) buttonView.getId();
                                candidatesList.get(getPosition).setCbTwoSelected(buttonView.isChecked());
                                enableButtons(verificarBtn);
                                disableButtons(ingresoBtn);
                                verificarBtn.setText("Guardar");
                            }
                        });
                    }
                }
            }else{
                for ( final CheckBox cb : secondColumn){
                    checkBoxEnable(cb);
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(
                                CompoundButton buttonView, boolean isChecked) {
                            int getPosition = (Integer) buttonView.getId();
                            candidatesList.get(getPosition).setCbTwoSelected(buttonView.isChecked());
                            enableButtons(verificarBtn);
                            disableButtons(ingresoBtn);
                            verificarBtn.setText("Guardar");
                        }
                    });
                }

            }
        }

    }

    private void initialVoteConditions(){
        isTodos = false;
        isPref = false;
        isTodosReingresar = false;
        isBandera = false;
        isErr = false;
        checkBoxColumn = 1;
        ingresoBtn.setText("Preferential");
        verificarBtn.setText("Verificar");
        votoBanderaBtn.setText("Bandera");
        disableButtons(reingressoBtn);

        for(int index = 0; index < errorIndex.length; index++){
            if(errorIndex[index] == true){
                firstErr.get(index).setVisibility(View.GONE);
                secondErr.get(index).setVisibility(View.GONE);
                errorIndex[index] = false;
            }
        }
        partido_et_two.setHint("");
        partido_et_one.setHint("");
        bandError = false;
////////////////////////////////////////////////////////////////////////////////////////////////////////

        db_adapter = new DatabaseAdapterParlacen(ParlacenVoteTableActivity.this);
        db_adapter.open();

        //CARLOS: 2016-09-10
        for (int i = 0; i < candidatesList.size(); i++) {
            if (candidatesList.get(i).isCbOneSelected()
                    && candidatesList.get(i).isCbTwoSelected()) {
//                            currentMarks.get(i).setText("0");

                Log.e("CARLOS getMarcas()", String.valueOf(currentMarks.get(i).getText()));
                int showThis = Integer.parseInt(String.valueOf(currentMarks.get(i).getText()));
                currentMarks.get(i).setText(showThis > 0 ? String.valueOf((showThis - 1)) : String.valueOf(showThis));
                currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
            }
        }

        partido_cb_one.setButtonDrawable(R.drawable.disabled_cb);
        partido_cb_two.setButtonDrawable(R.drawable.disabled_cb);


        for (CheckBox cb : firstColumn) {
            cb.setChecked(false);
            checkBoxDisabled(cb);
        }

        for (CheckBox cb : secondColumn) {
            cb.setChecked(false);
            checkBoxDisabled(cb);
        }

        for (Candidate cand : candidatesList) {
            cand.setCbOneSelected(false);
            cand.setCbTwoSelected(false);
        }
        for (TextView current : currentVotes) {
            current.setText("0.000");
            current.setTypeface(null, Typeface.NORMAL);
            current.setTextColor(getResources().getColor(R.color.DarkBlue));
        }


        partido_votes.setText("0");
        findViewById(R.id.marcas_tv).setVisibility(View.INVISIBLE);

        mismatchTv.setText("");

        //////////////////////////////////////////////////////////////////////////

        if(!firstScreen) {
            enableButtons(ingresoBtn, todosBtn);
        }
        if(isUpdate&&!isIndependentParty){
            enableButtons(votoBanderaBtn);
        }
        disableButtons(reingressoBtn, verificarBtn, reinitiateBoletaBtn, aceptarBtn, proxBoletaBtn);
    }

    private void verifyPreferentialVotes(){
        buffer.setLength(0);
        mismatchTv.setText("");
        float candidatesSelected = 0.000f;
        for (int i = 0; i < candidatesList.size(); i++) {
            if(candidatesList.get(i).isCbOneSelected()){
                Log.e("candList ONE", "true");
            }else Log.e("candList ONE", "false");

            if(candidatesList.get(i).isCbTwoSelected()){
                Log.e("candList TWO", "true");
            }else Log.e("candList TWO", "false");
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        for (int i = 0; i < candidatesList.size(); i++) {

            if (candidatesList.get(i).isCbOneSelected() && candidatesList.get(i).isCbTwoSelected()) {
                //COUNT CANDIDATES:
                candidatesSelected += 1.0f;
                // MATCH! lock boxes:
                firstColumn.get(i).setButtonDrawable(R.drawable.dchecked);
                secondColumn.get(i).setButtonDrawable(R.drawable.dchecked);
                firstColumn.get(i).setEnabled(false);
                secondColumn.get(i).setEnabled(false);
                firstErr.get(i).setVisibility(View.GONE);
                secondErr.get(i).setVisibility(View.GONE);
                errorIndex[i] = false;

            } else if (!candidatesList.get(i).isCbOneSelected() && !candidatesList.get(i).isCbTwoSelected()) {
                // MATCH! lock boxes
                checkBoxDisabled(firstColumn.get(i));
                checkBoxDisabled(secondColumn.get(i));
                firstErr.get(i).setVisibility(View.GONE);
                secondErr.get(i).setVisibility(View.GONE);
                errorIndex[i] = false;

            } else {
                //NO MATCH, enable boxes
                String messageToAppend = (ah.parseInt(candidatesList.get(i).getCandidateID(), 0)) + ", ";
                buffer.append(messageToAppend);
                checkBoxDisabled(firstColumn.get(i));
                firstColumn.get(i).setChecked(false);
                checkBoxDisabled(secondColumn.get(i));
                secondColumn.get(i).setChecked(false);
                firstErr.get(i).setVisibility(View.VISIBLE);
                secondErr.get(i).setVisibility(View.VISIBLE);
                errorIndex[i] = true;

            }
        }
        // Done counting, now assign the vote values to the text box.
        for (int i = 0; i < candidatesList.size(); i++) {
            if (candidatesList.get(i).isCbOneSelected() && candidatesList.get(i).isCbTwoSelected()) {
                currentBallotPreferentialVotes = 1.0f / candidatesSelected;
                currentVotes.get(i).setText(formatFloat(currentBallotPreferentialVotes));
                currentVotes.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
//                 currentVotes.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);

                currentMarks.get(i).setText(String.valueOf(candidatesList.get(i).getMarcas() + 1));
                currentMarks.get(i).setTypeface(null, Typeface.BOLD_ITALIC);
            } else {
                currentVotes.get(i).setTypeface(null, Typeface.NORMAL);
                currentVotes.get(i).setTextColor(getResources().getColor(R.color.gray));
                //gray edit text
            }
        }
        // Show the candidates with marcas:
        String marcas = "Candidatos Con Marcas: " + String.format(Locale.US, "%.0f", candidatesSelected); //whole digits
        ((TextView) findViewById(R.id.marcas_tv)).setText(marcas);
        findViewById(R.id.marcas_tv).setVisibility(View.VISIBLE);

        if (buffer.length() != 0) {
            String message = "Error en Candidato(s): " + buffer.toString().substring(0, buffer.toString().length() - 2);
            mismatchTv.setText(message);
        }
        disableButtons(ingresoBtn, reingressoBtn, votoBanderaBtn, todosBtn, proxBoletaBtn, descartarBtn);
        warningButtons(reinitiateBoletaBtn);
//                        enableButtons(reinitiateBoletaBtn);

        if (mismatchTv.length() == 0 && candidatesSelected != 0) {
            enableButtons(aceptarBtn);
            disableButtons(verificarBtn);
        } else if (candidatesSelected == 0) {
            ah.createCustomToast("Completar Preferencias!");
            disableButtons(aceptarBtn, verificarBtn);
        } else {
            ah.setButtonColorRed(verificarBtn);
            ah.setButtonColorGreen(ingresoBtn);
            ingresoBtn.setText("Corregir");
            pressAgain = true;
        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        isErr = false;
        for (int index = 0; index < errorIndex.length; index++) {
            if (errorIndex[index] == true) {
                isErr = true;
                ingresoBtn.setText("Corregir");
                enableButtons(ingresoBtn);
                break;
            }
        }

        if(!isErr){
            enableButtons(aceptarBtn);
        }

    }

    public void createConfirmationDialog(String msg, int yesIndex) {
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
        twoBtnDialogFragment.show(fm, "discrard boleta dialog");
    }

    public void createDialogToConfirmDui(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        DialogToConfirmDui dialogToConfirmDui = new DialogToConfirmDui();
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

    private Bitmap getBitmapFromAsset(String strName) throws IOException {
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open("drawable/" + strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        istr.close();
        return bitmap;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.preferential_vote, menu);
        return true;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBackPressed() {

        Log.d(CLASS_TAG, "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    @Override
    public void onYesButtonForTwoButtonDialogClicked(int yesIdnex) {
        switch (yesIdnex) {
            case 1:
                break;
            case 2:
                break;
            case 3:

                ah.savePreferences("currentBallotNumber", --currentBallotNumber);
                ArrayList<Candidate> candidatesListSelectedArrayList = saveOrUpdateCandidateVotes(true);
                Bundle b = new Bundle();
                b.putParcelable("com.afilon.tse.votingcenter", vc);
                b.putParcelable("com.afilon.tse.escrudata", escrudata);
                b.putInt("partyNumber", partyArrayIndex);
                b.putParcelableArrayList("selectedCandidates",
                        candidatesListSelectedArrayList);

                Intent search = new Intent(ParlacenVoteTableActivity.this,
                        Consts.CANDLISTACT);
                search.putExtras(b);
                startActivity(search);
                finish();

                break;
            default:
                finish();
                break;
        }

    }

    public ArrayList<Candidate> saveOrUpdateCandidateVotes(boolean dropBoletas) {
        ArrayList<Candidate> candidatesListSelectedArrayList = new ArrayList<Candidate>();
        db_adapter = new DatabaseAdapterParlacen(ParlacenVoteTableActivity.this);
        db_adapter.open();
        if (dropBoletas) {
            db_adapter.updateNumberOfChangeBoletasAndPartyBoletasToParty("0", String.valueOf(currentBallotNumber), partyArrayList.get(partyArrayIndex).getParty_preferential_election_id());
        }
        partidoCandidate.setPreferentialVotes(partidoCandidate.getVotesNumber());
        candidatesListSelectedArrayList.add(0, partidoCandidate);
        PreferentialVotoBanderas votoBandera = new PreferentialVotoBanderas();
        votoBandera.setJrv(vc.getJrvString());
        votoBandera.setPreferential_election_id(vc.getPref_election_id());
        votoBandera.setBandera_preferential_election_id(partidoCandidate.getCandidatePreferentialElectionID());
        votoBandera.setParty_preferential_election_id(partidoCandidate.getCandidatePreferentialElectionID());
        votoBandera.setParty_votes(partido_votes_int);
        votoBandera.setParty_preferential_votes(preferentialVotes);
        if (isUpdate) {
            for (Candidate selected : candidatesList) {
                candidatesListSelectedArrayList.add(selected);
                db_adapter.updatePreferentialCandidateVote(
                        Integer.parseInt(vc.getJrvString()),
                        selected.getPartyPreferentialElectionID(),
                        selected.getCandidatePreferentialElectionID(),
                        selected.getVotesNumber(),
                        selected.getBanderaNumbers(),
                        selected.getPreferentialVotes());
                // todo: update marcas, make sure marcas were deleted when update was requested.
//                db_adapter.deletePartyMarks(selected.getPartyPreferentialElectionID(),"4");
                db_adapter.insertMarks(vc.getJrvString(), vc.getPref_election_id(), selected.getCandidatePreferentialElectionID(),
                        selected.getPartyPreferentialElectionID(), "4", String.valueOf(selected.getMarcas() - initialCandidateMarks));
            }
            db_adapter.updateBanderaVotes(votoBandera, vc.getJrvString());
            ah.removePreferences("anadir");
        } else {
            for (Candidate selected : candidatesList) {
                candidatesListSelectedArrayList.add(selected);
                db_adapter.insertPreferentialCandidateVote(
                        Integer.parseInt(vc.getJrvString()),
                        selected.getPartyPreferentialElectionID(),
                        selected.getCandidatePreferentialElectionID(),
                        selected.getVotesNumber(),
                        selected.getBanderaNumbers(),
                        selected.getPreferentialVotes(),
                        vc.getPref_election_id());
                db_adapter.insertMarks(vc.getJrvString(),
                        vc.getPref_election_id(), selected.getCandidatePreferentialElectionID(),
                        selected.getPartyPreferentialElectionID(), "4", String.valueOf(selected.getMarcas()));
            }
            db_adapter.insertBanderaVotes(votoBandera, vc.getJrvString());
        }
        candidatesList.clear();
        candidatesList = null;
        return candidatesListSelectedArrayList;
    }

    @Override
    public void onNoButtonForTwoButtonDialogClickedX() {
//        raiseFlags( isReingresarBtnPressed ,isVerificarBtnPressed ,isReiniciarBoletaBtnPressed
//                ,isAceptarBtnPressed , isProximaBoletaPressed );
//        lowerFlags(isIngresoBtnPressed, isVotoBanderaBtnPressed ,isTodosBtnPressed, isAbortBtnPressed);
//        if (!firstScreen) {
//            enableButtons(ingresoBtn,todosBtn);
//        }
//        enableButtons(votoBanderaBtn);
//        disableButtons(reingressoBtn,verificarBtn,reinitiateBoletaBtn,aceptarBtn,proxBoletaBtn);
//        warningButtons(descartarBtn);
    }

    @Override
    public void onYesButtonDialogToConfirmDuiClicked(String duiNumber) {

        if (duiNumber.equals(ah.loadPreferencesString("duiNumber"))) {
            ah.createCustomToast(getResources().getString(R.string.dui)+" CONFIRMED");

            disableButtons(descartarBtn);
//            ah.setButtonColorRed(descartarBtn);

            isAbortBtnPressed.setFlagValue(true);
        } else {

            try {
                int ws_task_number = 0;

                String url = Consts.BASE_URI + Consts.PATH_NAME + duiNumber
                        + "&" + "Afilon";
                HttpGet searchRequest = new HttpGet(url);
                WebServiceRestTask task = new WebServiceRestTask(ws_task_number);
                task.setResponseDataCallback(ParlacenVoteTableActivity.this);
                task.execute(searchRequest);

            } catch (Exception e) {

                Log.e("DUI REST ERROR: ", e.getMessage());
            }
        }
    }

    @Override
    public void onNoButtonDialogToConfirmDuiClicked() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRequestDataSuccess(String response) {
        // TODO Auto-generated method stub
        ah.createCustomToast(getResources().getString(R.string.dui)+" CONFIRMED");
        disableButtons(descartarBtn);
//        ah.setButtonColorRed(descartarBtn);
        isAbortBtnPressed.setFlagValue(true);
    }

    @Override
    public void onRequestDataError(Exception error) {
        // TODO Auto-generated method stub
        ah.createCustomToast(getResources().getString(R.string.dui)+" NOT CONFIRMED");
    }

    private String formatFloat(float value) {
        return String.format(Locale.US, getResources().getString(R.string.floatFormating), value);
    }

    TextWatcher tw1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (partido_et_one.getText().length() > 0 && partido_et_one.hasFocus()) {
                enableButtons(verificarBtn);
                disableButtons(ingresoBtn);
                verificarBtn.setText("Guardar");
            } else {
                disableButtons(verificarBtn);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw1 afterTextChanged", "x");

        }
    };

    TextWatcher tw2 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (partido_et_two.getText().length() > 0 && partido_et_two.hasFocus()) {
                enableButtons(verificarBtn);
                disableButtons(reingressoBtn);
                verificarBtn.setText("Guardar");
            } else {
                disableButtons(verificarBtn);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw1 afterTextChanged", "x");

        }
    };

    private void checkBoxDisabled(CheckBox cb) {
        cb.setButtonDrawable(R.drawable.disabled_cb);
        ah.enableView(cb, false);

    }

    private void checkBoxEnable(CheckBox cb) {
        cb.setButtonDrawable(R.drawable.btn_checkbox_selector);
        ah.enableView(cb, true);
    }

    private void openEditText(EditText et) {
        ah.enableEditText(et, true);
        et.requestFocus();
    }

    private void closeEditText(EditText et) {
        ah.enableEditText(et, false);
    }

    private void disableButtons(Button... buttons) {
        for (Button btn : buttons) {
            ah.setButtonColorRed(btn);
            btn.setPadding(10, 10, 10, 10);
        }
    }

    private void enableButtons(Button... buttons) {
        for (Button btn : buttons) {
            ah.setButtonColorGreen(btn);
            btn.setPadding(10, 10, 10, 10);
        }
    }

    private void warningButtons(Button... buttons){
        for(Button btn: buttons){
            ah.setButtonColorAmber(btn);
            btn.setPadding(10,10,10,10);
            btn.setFocusable(false);
        }
    }

    private void raiseFlags(Flag... flags) {
        for (Flag flag : flags) {
            flag.setFlagValue(true);
        }
    }

    private void lowerFlags(Flag... flags) {
        for (Flag flag : flags) {
            flag.setFlagValue(false);
        }
    }

    private class Flag{
        boolean flagValue;
        public Flag(boolean initialValue){
            this.flagValue = initialValue;
        }
        public void setFlagValue(boolean value){
            this.flagValue = value;
        }
        public boolean raised(){
            return flagValue;
        }
    }

    private ChallengeHelper.OnApprove drop_ballots = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            dropBallot();

        }
    };
    private void dropBallot(){
        ah.savePreferences("currentBallotNumber", --currentBallotNumber);
        ArrayList<Candidate> candidatesListSelectedArrayList = saveOrUpdateCandidateVotes(true);
        Bundle b = new Bundle();
        b.putParcelable("com.afilon.tse.votingcenter", vc);
        b.putParcelable("com.afilon.tse.escrudata", escrudata);
        b.putInt("partyNumber", partyArrayIndex);
        b.putParcelableArrayList("selectedCandidates",
                candidatesListSelectedArrayList);

        Intent search = new Intent(ParlacenVoteTableActivity.this,
                Consts.CANDLISTACT);
        search.putExtras(b);
        startActivity(search);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        if (!hasfocus) {
            findViewById(R.id.linear_layout_btns).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.linear_layout_btns).setVisibility(View.VISIBLE);
        }

    }

}
