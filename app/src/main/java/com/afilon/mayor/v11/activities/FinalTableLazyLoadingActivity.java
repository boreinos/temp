package com.afilon.mayor.v11.activities;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.model.AppLog;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.User;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import static android.R.attr.tag;

public class FinalTableLazyLoadingActivity extends AfilonActivity implements
        OnTwoButtonDialogFragmentListener,
        DataResponseCallback,
        DialogToConfirmDuiTwoBtns.DialogToConfirmDuiListener {

    private static final String CLASS_TAG = "FinalTableLazyLoadingActivity";
    private TwoButtonDialogFragment twoBtnDialogFragment;
    private boolean isAccepted;
    private boolean isDebugMode = false;
    private Utilities ah;
    private Escrudata escrudata;
    private AppLog applog;
    private VotingCenter vc;
    private Button modificarBtn;
    private Button aceptarBtn;
    private Button rechezarBtn;
    private Button aceptar_cambioBtn;
    private Button continuarBtn;
    private LinkedHashMap<String, String> valuesMap;
    private LinkedHashMap<String, String> valueMapFromVerticalConcepts;
    private CustomKeyboard mCustomKeyboard;
//    private CustomKeyboard mCustomKeyboardTwo;

    private String escrudataMapString;
    private DatabaseAdapterParlacen db_adapter;

    private boolean isCustomKeyboardRegistred = false;
    private ArrayList<EditText> EditTextArray;
    private ArrayList<TextView> TextViewArray;
    private int totalVotes = 0;
    private ArrayList<CheckBox> arrayRadioBtn;
    private boolean isEditTextEditable = false;

    private int totalTimesModificarBtnWasPressed = 0;
    private ScrollView myScrollView;
    private ArrayList<Party> conceptosAndPartiesList;

    private int noIndex;
    private DialogToConfirmDuiTwoBtns dialogToConfirmDuiTwoBtns;

    private String duiName;
    private String duiTypedIn = "";
    private String conceptValue = "";
    private String conceptDescription = "";
    private String conceptValueFinal = "";
    private String duiPresident = "";

    // CARLOS: To manipulate errors
    private int qtyTotalEscrutadas = 0;
    private int qtyEntregadas = 0;
    private int qtyFaltantes = 0;
    private int qtySobrantes = 0;
    private int qtyInutilizadas = 0;
    private int qtyImpugnados = 0;
    private int qtyNulos = 0;
    private int qtyAbstenciones = 0;
    private int TOTAL_PAPELETAS = 0;
    private int firstColumnSum = 0;
    private int secondColumnSum = 0;
    private int qtyOfPapeletasInicio = 0;
    private int qtyOfPapeletasFinal = 0;
    private int sumOfAllConcepts = 0;

    private boolean escapePressed = false, isError = false;
    private int flagCancelReject = 0;
    private ChallengeHelper challengeHelper;
    private static int MODIFICAR = 1;
    private static int ACEPT_CAMBIO = 2;
    private static int RECHAZAR = 3;
    private static int ENTRAR = 4;
    private static int CONTINUAR = 5;
    private static int error1 = 6;

    private static final String checkboxView = "Checkbox";
    private static final String descriptionView = "Description";
    private static final String valuesView = "Values";

    private static int IdCiudadanos, IdUtilizadas, IdMer, IdValidos, IdVotantes, IdGranTotal, IdVotosCruzados, IdSobrantesRow;

    Toast errToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ah = new Utilities(this);
        ah.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_final_table_lazy_loading);
        //Catch Unexpected Error:
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
                FinalTableLazyLoadingActivity.this));
        //--------- filter listeners ---------------------------------------------------
        CommonListeners listenerHandler = new CommonListeners();
        View.OnKeyListener altkeys = listenerHandler.getAltKeysListener();
        View.OnLongClickListener longClickListener = listenerHandler.getMouseListener();
        //--------- end filter listeners ------------------------------------------------

        Log.i("CLASS NAME : ", CLASS_TAG);

        // CARLOS: SET DEBUG MODE
        isDebugMode = true;


        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();
        noIndex = 0;
        duiName = "";

        TextViewArray = new ArrayList<TextView>();
        EditTextArray = new ArrayList<EditText>();
        arrayRadioBtn = new ArrayList<CheckBox>();
        conceptosAndPartiesList = new ArrayList<Party>();

        mCustomKeyboard = new CustomKeyboard(FinalTableLazyLoadingActivity.this, R.id.keyboardview, R.xml.tenhexkbd);

        //CARLOS: 2016-10-20
//        mCustomKeyboardTwo = new CustomKeyboard(
//                FinalTableLazyLoadingActivity.this, R.id.keyboardview2,
//                R.xml.tenhexkbd);

        //-----------------------------------------------------------------------------------------
        challengeHelper = new ChallengeHelper(this);
        // REGISTER ROUTINES TO CHALLENGE HELPER:
        challengeHelper.addRoutine(error1, errorDui1);
        challengeHelper.addRoutine(-1,errorExit);
        challengeHelper.addRoutine(CONTINUAR, duisConfirmed);
        challengeHelper.addRoutine(MODIFICAR, modificarRoutine);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);// FOR DUI CONFIRMATION ONLY
        challengeHelper.setTools(ah, db_adapter); //TOOLS ARE FOR DUI CONFIRMATION ONLY
        //----------------------------------------------------------------------------------------
        myScrollView = (ScrollView) findViewById(R.id.concepts_table);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            escrudata = b.getParcelable("com.afilon.tse.escrudata");
//            applog = b.getParcelable("com.afilon.assembly.applog");
            vc = b.getParcelable("com.afilon.tse.votingcenter");
            escrudataMapString = escrudata.getValueMap();// b.getString("escrudataMap");
            ah.savePreferences("escrudataMap", escrudataMapString);
            totalVotes = ah.loadPreferences("TOTALVOTES");//    b.getInt("TOTALVOTES");
            Gson gson = new Gson();

            Type entityType = new TypeToken<LinkedHashMap<String, String>>() {
            }.getType();

            valuesMap = gson.fromJson(escrudataMapString, entityType);

            // WORKING ON.... *******************************************
            // loop over the set using an entry set
            for (Map.Entry<String, String> entry : valuesMap.entrySet()) {
                String key = entry.getKey();

                conceptosAndPartiesList.add(new Party("", vc.getPref_election_id(), vc.getVc_event2_locality_id(), key, "0"));
            }
            // *********************************************************

            TOTAL_PAPELETAS = ah.parseInt(escrudata.getPapeletasTotal(), 0);
        }
        ah.saveCurrentScreen(this.getClass(),b);


        // JUST TO DEBUG ERRORS ====================================================================
        if (isDebugMode) {
            printErrors();
        }
        // =========================================================================================

        populateTopHeaders(vc);

//        db_adapter.insertConceptsCountPreferential(valuesMap);
//		ArrayList<Party> upperedList = new ArrayList<Party>();
        Set<Entry<String, String>> set = valuesMap.entrySet();
        Iterator<Entry<String, String>> i = set.iterator();

        TableLayout table = (TableLayout) findViewById(R.id.list_table);

        if(Consts.LOCALE.contains("HON")){
            for (int ii = 0; ii < conceptosAndPartiesList.size(); ii++) {
                Log.e("conceptosAndPartiesList",conceptosAndPartiesList.get(ii).getParty_name());
                Log.e("conceptlist", conceptosAndPartiesList.get(ii).getParty_name());
                if(conceptosAndPartiesList.get(ii).getParty_name().contains("SOBRANTES")) {
                    IdSobrantesRow = ii;
                    Log.e("idsobrantes", Integer.toString(IdSobrantesRow));
                } else
                if(conceptosAndPartiesList.get(ii).getParty_name().equals("UTILIZADAS")){
                    IdVotosCruzados = ii;
                    Log.e("idvotoscruzados", Integer.toString(IdVotosCruzados));
                }
            }
        }

        if(Consts.LOCALE.contains("HON")){
            for (int ii = 0; ii < conceptosAndPartiesList.size(); ii++) {
                Log.e("conceptosAndPartiesList",conceptosAndPartiesList.get(ii).getParty_name());
                if(conceptosAndPartiesList.get(ii).getParty_name().contains("CIUDADANOS QUE VOTARON")){
                    IdCiudadanos = ii;
                } else if(conceptosAndPartiesList.get(ii).getParty_name().contains("UTILIZADAS")){
                    IdUtilizadas = ii;
                } else  if(conceptosAndPartiesList.get(ii).getParty_name().contains("MER")){
                    IdMer = ii;
                } else if(conceptosAndPartiesList.get(ii).getParty_name().contains("VALIDOS")){
                    IdValidos = ii;
                } else if(conceptosAndPartiesList.get(ii).getParty_name().contains("VOTANTES")){
                    IdVotantes =ii;
                } else if(conceptosAndPartiesList.get(ii).getParty_name().contains("GRAN")){
                    IdGranTotal = ii;
                }
                if(conceptosAndPartiesList.get(ii).getParty_name().contains("CRUZ")){
                    IdVotosCruzados = ii;
                }
            }
        }

        int rowId = 0;
        // Display elements
        while (i.hasNext()) {
//            if(!row.getParty_name().contains("MOVIMIENT")&&!row.getParty_name().contains("CRUZADO"))

            Map.Entry me = (Map.Entry) i.next();
            String description = me.getKey() + "";
            String value = me.getValue() + "";
            String rowString = String.valueOf(rowId);

            int descriptionId = (rowString + descriptionView).hashCode() & 0xfffffff; // number must be positive
            int checkboxId = (rowString + checkboxView).hashCode() & 0xfffffff;
            int valueId = (rowString + valuesView).hashCode() & 0xfffffff;

            RowContainer row = new RowContainer(rowId);
            TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.activity_final_table_lazy_loading_row, null);

            TextView description_a = (TextView) tr.findViewById(R.id.txt_name_b);

            description_a.setId(descriptionId);

            if(Consts.LOCALE.contains("HON") && getResources().getString(R.string.voteType).contains("PREF") && description.contains("CRUZADOS")){
                description_a.setText("VOTOS");
            }else description_a.setText(description);
            ah.enableTextView(description_a, false);
            TextViewArray.add(description_a);

            EditText vote_a = (EditText) tr.findViewById(R.id.txt_vote_b);
            vote_a.addTextChangedListener(textWatcher); //CARLOS: See TextWatcher down there
            vote_a.setId(valueId);
            vote_a.setText(value);
            vote_a.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            vote_a.setOnLongClickListener(longClickListener);

            ah.enableEditText(vote_a, false);
            EditTextArray.add(vote_a);
            vote_a.setOnFocusChangeListener(isValueInteger());

            //-------------------------------------------------------
            CheckBox cb = (CheckBox) tr.findViewById(R.id.radioBtn);
            cb.setVisibility(View.VISIBLE);
            cb.setId(checkboxId);
            arrayRadioBtn.add(cb);
            cb.setOnClickListener(checkBoxListener);
            cb.setButtonDrawable(R.drawable.lazy_load_checkbox_selector);
            ah.enableView(cb, false);
            //-------------------------------------------------------

            row.addItem(checkboxId, checkboxView);
            row.addItem(valueId, valuesView);
            tableRows.add(row);

            rowId++;

            // Line separator for each row
            View v = new View(this);
            v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            v.setBackgroundColor(getResources().getColor(R.color.divisor));
            table.addView(tr);
            table.addView(v);
            mCustomKeyboard.registerEditText(vote_a.getId());
            registerForContextMenu(tr);
            if(Consts.LOCALE.contains("HON")){
//            if(Consts.LOCALE.contains("HON")&&getResources().getString(R.string.voteType).contains("PREF")){
                if ((((rowId-1) > IdSobrantesRow) && ((rowId-1) <= IdVotosCruzados))
                        || ((rowId-1) > IdUtilizadas + 3)
                        ){
                    tr.setFocusable(false);
                    tr.setVisibility(View.GONE);
                    v.setFocusable(false);
                    v.setVisibility(View.GONE);
                }
            }

        }

        modificarBtn = (Button) findViewById(R.id.modificar_btn);
        modificarBtn.setOnClickListener(modificarBtnListerner);
        ah.setButtonColorRed(modificarBtn);

        aceptar_cambioBtn = (Button) findViewById(R.id.aceptar_cambios_btn);
        ah.setButtonColorRed(aceptar_cambioBtn);
        aceptar_cambioBtn.setVisibility(View.INVISIBLE);
        aceptar_cambioBtn.setOnClickListener(getAceptarCambio_btnListerner());

        aceptarBtn = (Button) findViewById(R.id.aceptar_btn);
        ah.setButtonColorGreen(aceptarBtn);
        aceptarBtn.setOnClickListener(getAceptarBtnListener());

        rechezarBtn = (Button) findViewById(R.id.rechezar_btn);
        ah.setButtonColorGreen(rechezarBtn);
        rechezarBtn.setOnClickListener(getRechazarBtnListener());

        //CARLOS: 2016-10-20
        continuarBtn = (Button) findViewById(R.id.continuar_btn);
        continuarBtn.setOnClickListener(getContinuarBtnListener());

        //CARLOS: 2016-10-20
        //disable all buttons except CONTINUAR
//        ah.setButtonColorGreen(continuarBtn);
//        ah.setButtonColorGreen(aceptarBtn);
//        ah.setButtonColorRed(modificarBtn);
////        ah.setButtonColorRed(aceptarBtn);
//        ah.setButtonColorRed(rechezarBtn);
        ah.setButtonColorAmber(modificarBtn);
        ah.setButtonColorGreen(aceptarBtn);
        ah.setButtonColorGreen(rechezarBtn);

        Log.e("ELECTION ID",""+vc.getPref_election_id());
        Log.e("ELECTION ID 2",""+vc.getPreferential_election2_id());
        Log.e("LOCALITY ID",""+vc.getEvent1_locality_id());
        Log.e("VCLOCALITY ID",""+vc.getVc_event1_locality_id());
        Log.e("vclOCALITY 2 ID",""+vc.getVc_event2_locality_id());

        valuesMap.put("JRV",vc.getJRV());

        valuesMap.put("PREFERENTIAL ELECTION ID",vc.getPref_election_id());
        db_adapter.insertConceptsCountPreferential(valuesMap);
        valuesMap.remove("JRV");
        valuesMap.remove("PREFERENTIAL ELECTION ID");

        fireErrorWarning();
    }

    protected void onResume() {
        super.onResume();
        if (!db_adapter.isOpen()) {
            db_adapter.open();
        }
    }


    // CARLOS:

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fifth, menu);
        return true;
    }

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


    //CARLOS: 2016-10-21
    public void createDialogToConfirmDuiPresidentTwoBtns(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        dialogToConfirmDuiTwoBtns = new DialogToConfirmDuiTwoBtns();
        dialogToConfirmDuiTwoBtns.setOnButtonsClickedListenerOne(this);
        dialogToConfirmDuiTwoBtns.setCustomKeyboard(mCustomKeyboard);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "Cancelar");
        bndl.putString("question", msg);
        bndl.putString("invisible", "No");//set invisible to hide 'Cancelar' btn
        dialogToConfirmDuiTwoBtns.setArguments(bndl);
        dialogToConfirmDuiTwoBtns.show(fm, "new triage dialog");
    }

    //CARLOS:2016-10-20
    public void createDialogToConfirmDuiSecretaryTwoBtns(String msg, int yesIndex) {

        android.app.FragmentManager fm2 = getFragmentManager();
        dialogToConfirmDuiTwoBtns = new DialogToConfirmDuiTwoBtns();
        dialogToConfirmDuiTwoBtns.setOnButtonsClickedListenerOne(this);
        dialogToConfirmDuiTwoBtns.setCustomKeyboard(mCustomKeyboard);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "Cancelar");
        bndl.putString("question", msg);
        bndl.putString("invisible", "No");//set invisible to hide 'Cancelar' btn
        dialogToConfirmDuiTwoBtns.setArguments(bndl);
        dialogToConfirmDuiTwoBtns.show(fm2, "new "+getResources().getString(R.string.dui)+" 2 dialog");

    }

    @Override
    public void onYesButtonForTwoButtonDialogClicked(int x) {
        switch (x) {
            case 1:

                isAccepted = true;

                // (task-11)
                // *************CARLOS******************************
                Gson gson = new Gson();
                String listFilledOut = gson.toJson(valuesMap);
                // *************************************************
                escrudata.setValuMap(listFilledOut);
                Bundle b = new Bundle();
                b.putParcelable("com.afilon.tse.escrudata", escrudata);
//                b.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
                b.putParcelable("com.afilon.tse.votingcenter", vc);

                // (task-11)
                // **** We save the new values for each EditText and transfer to the
                // next screen*********
//                b.putString("escrudataMapFilledOut", listFilledOut);
//                escrudataMapString = b.getString("escrudataMapFilledOut");
                ah.savePreferences("escrudataMap", listFilledOut);
                // **************************************************************************************

                Intent search = new Intent(FinalTableLazyLoadingActivity.this,
                        Consts.RECACT);

                search.putExtras(b);
                startActivity(search);
                finish();

                break;
            case 2:

                //CARLOS: 2016-12-07
                //User pressed SI to reject the changes
                ah.savePreferences("flagCancelReject", 1);
                createDialog("SI RECHAZA TENDRA QUE REINICIAR LOS DATOS", 32);
                noIndex = 43; //this catches if user presses NO btn
                break;
            case 3:
                challengeHelper.createDuiChallenge(CONTINUAR);
                break;

            case 4:

                createDialogToConfirmDuiPresidentTwoBtns("Ingrese su "+getResources().getString(R.string.dui), 1);

                break;
            case 32:

                Bundle buc = new Bundle();
                buc.putParcelable("com.afilon.tse.votingcenter", vc);
                buc.putParcelable("com.afilon.tse.escrudata", new Escrudata(vc.getJRV()));
                Intent inte = new Intent(FinalTableLazyLoadingActivity.this, Consts.PAPALETASACT);
                inte.putExtras(buc);
                startActivity(inte);
                finish();
                break;
            default:
                finish();
                break;
        }
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onNoButtonForTwoButtonDialogClickedX() {

        flagCancelReject = ah.loadPreferences("FlagToClearUpMarks");
        switch (noIndex) {
            case 1:
                Log.e("No Btn triggered case 1", "true");
                break;

            case 33: //Errors were found and user want to cancel everything, he/she wont continue
                Bundle buc = new Bundle();
                buc.putParcelable("com.afilon.tse.votingcenter", vc);
                buc.putParcelable("com.afilon.tse.escrudata", new Escrudata(vc.getJRV()));
                Intent inte = new Intent(FinalTableLazyLoadingActivity.this,
                        Consts.PAPALETASACT);
                inte.putExtras(buc);
                startActivity(inte);
                finish();

                break;

            case 43:
                //user pressed NO Btn, so do nothing
                break;
            default:

                Log.e("value of escapePressed", String.valueOf(escapePressed));
                if (flagCancelReject == 1) {
                    //Do nothing :)
                    Log.e("Do nothing :)", "true");

                } else {
                    Log.e("No Btn triggered case default", "true");
                    Bundle bc = new Bundle();
                    bc.putParcelable("com.afilon.tse.votingcenter", vc);
                    bc.putParcelable("com.afilon.tse.escrudata", new Escrudata(vc.getJRV()));
//                    bc.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
                    Intent intent = new Intent(FinalTableLazyLoadingActivity.this,
                            Consts.PAPALETASACT);
                    intent.putExtras(bc);
                    startActivity(intent);
                    finish();
                }

                break;
        }

    }

    //CARLOS: Text Watcher
    private TextWatcher textWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {

            conceptValueFinal = s.toString();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    };

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
        Log.d("FinalTable Activity", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);

/*        if (mCustomKeyboard.isCustomKeyboardVisible()) {
            mCustomKeyboard.hideCustomKeyboard();
        } else {
            Log.d("FinalTable Activity", "onBackPressed Called");
            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
        }*/
    }

    public void registerKeyboard(CustomKeyboard mCustomKeyboard2) {

        mCustomKeyboard = mCustomKeyboard2;
        isCustomKeyboardRegistred = true;

    }

    /**
     * @param arrayEditTxt
     * @param position
     */
    // CARLOS: Control the EditView inside the adapter
    @Deprecated
    private void setEditVote(ArrayList<EditText> arrayEditTxt,
                             final int position) {

        for (EditText vote : arrayEditTxt) {
            if (vote.getId() == position) {

                //CARLOS: Store the original value of text for AppLog
                conceptValue = vote.getText().toString();
                ah.enableEditText(vote, true);


                if (isCustomKeyboardRegistred) {
                    View vi = vote;
                    if (vi instanceof EditText) {
                        //CARLOS: 2016-10-26
                        //Register key events on EditText and enable disable aceptar_cambioBtn
                        TextView currentCell = (TextView) vi;
                        currentCell.addTextChangedListener(tw3);

                    }
                }

                vote.requestFocus();
                vote.setTextColor(Color.BLUE);
                vote.setSelection(vote.getText().length());
            }
        }//aceptar_cambioBtn

        // Set rest of EditText look like TextView
        for (EditText vote : arrayEditTxt) {
            if (vote.getId() != position) {
                vote.setBackgroundResource(R.drawable.bg);
            }
        }

    }

    private void setEditVote(int id) {
        EditText editText = (EditText) findViewById(id);
        ah.enableEditText(editText, true);
        editText.requestFocus();
        editText.setTextColor(Color.BLUE);
        editText.setSelection(editText.getText().length());
        editText.addTextChangedListener(tw3);

        for (EditText vote : EditTextArray) {
            if (vote.getId() != id) {
                vote.setBackgroundResource(R.drawable.bg);
            }
        }
    }

    //CARLOS: 2016-10-26
    //Register key events on EditText and enable disable aceptar_cambioBtn
    TextWatcher tw3 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d("tw3 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(">>> concept value chars count", String.valueOf(count));
            if (s.length() >= 1) {
                ah.setButtonColorGreen(aceptar_cambioBtn);
            } else {
                ah.setButtonColorRed(aceptar_cambioBtn);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw3 afterTextChanged", "x");

        }
    };

    private void allowEdit() {
        ah.createCustomToast(getResources().getString(R.string.dui)+" CONFIRMED");

        totalTimesModificarBtnWasPressed += 1;
        ah.setButtonColorRed(aceptarBtn);
        ah.setButtonColorRed(rechezarBtn);
        // this statement is alwasy true.
        isEditTextEditable = (totalTimesModificarBtnWasPressed > 0 ? true
                : false);

        // WORKING ON ...
        if (isEditTextEditable) {
            ah.setButtonColorRed(modificarBtn);
            for (EditText vote : EditTextArray) {
                vote.setBackgroundResource(R.drawable.bg);
            }
            for (CheckBox cb : arrayRadioBtn) {
                ah.enableView(cb, true);
            }
        }
    }

    @Override
    public void onRequestDataSuccess(String response) {
        // TODO Auto-generated method stub
        // Process the response data (here we just display it)
        Log.d("REST OK :", response);
        // mResult.setText(response); //Display the string from the Web Service

        response = response.substring(0, response.length() - 1);
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("User", User.class);
        User user = new User();

        user = (User) xstream.fromXML(response);

        duiName = user.getDUI();

        ah.createCustomToast(getResources().getString(R.string.dui)+" CONFIRMED");

        totalTimesModificarBtnWasPressed += 1;

        ah.setButtonColorRed(aceptarBtn);
        ah.setButtonColorRed(rechezarBtn);

        isEditTextEditable = (totalTimesModificarBtnWasPressed > 0 ? true
                : false);

        // WORKING ON ...
        if (isEditTextEditable) {
            ah.setButtonColorRed(modificarBtn);
            for (EditText vote : EditTextArray) {
                vote.setBackgroundResource(R.drawable.bg);
            }
            for (CheckBox cb : arrayRadioBtn) {
                cb.setButtonDrawable(R.drawable.checked);
            }

        }
    }

    @Override
    public void onRequestDataError(Exception error) {
        // TODO Auto-generated method stub
        duiName = "";
        ah.createCustomToast(getResources().getString(R.string.dui)+" NOT CONFIRMED");
    }

    private void calculateErrors() {
//        valueMapFromVerticalConcepts.remove("Preferential_Election_ID");

        qtyTotalEscrutadas = ah.parseInt(
                valueMapFromVerticalConcepts.get("ESCRUTADAS"),
                0);
        qtyEntregadas = ah.parseInt(
                valueMapFromVerticalConcepts.get("ENTREGADAS"), 0);
        qtyFaltantes = ah.parseInt(
                valueMapFromVerticalConcepts.get("FALTANTES"), 0);
        qtySobrantes = ah.parseInt(
                valueMapFromVerticalConcepts.get("SOBRANTES"), 0);
        qtyInutilizadas = ah.parseInt(
                valueMapFromVerticalConcepts.get("INUTILIZADAS"), 0);
        qtyImpugnados = ah.parseInt(
                valueMapFromVerticalConcepts.get("IMPUGNADOS"), 0);
        qtyNulos = ah.parseInt(valueMapFromVerticalConcepts.get("NULOS"), 0);
        qtyAbstenciones = ah.parseInt(
                valueMapFromVerticalConcepts.get("ABSTENCIONES"), 0);
        qtyOfPapeletasInicio = ah.parseInt(escrudata.getPapeletasInicio(), 0);
        qtyOfPapeletasFinal = ah.parseInt(escrudata.getPapeletasFinal(), 0);

        int votosCruzados = ah.parseInt(valueMapFromVerticalConcepts.get("VOTOS CRUZADOS"), 0);
        ah.savePreferences(Consts.VOTO_CRUZADO, votosCruzados);

        // loop over the set using an entry set
        sumOfAllConcepts = 0;
        for (Map.Entry<String, String> entry : valueMapFromVerticalConcepts
                .entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
//            Log.e(key,val);
            sumOfAllConcepts += ah.parseInt(val,0);
        }
        Log.i("Sum of sumOfAllConcepts", " : " + String.valueOf(sumOfAllConcepts));

        // CARLOS: 2014-08-21
        int qtyRealSumOfAllConcepts = (sumOfAllConcepts - (qtyTotalEscrutadas + qtyEntregadas));
        int qtyJustPartyVotes = (qtyRealSumOfAllConcepts - Math
                .abs((qtySobrantes + qtyInutilizadas + qtyImpugnados + qtyNulos
                        + qtyAbstenciones + qtyFaltantes)));
        escrudata.setVotosValidos(String.valueOf(qtyJustPartyVotes));

//        Log.e("Escrutadas",""+String.valueOf(qtyTotalEscrutadas));
//        Log.e("Entregadas",""+String.valueOf(qtyEntregadas));
//        Log.e("Faltantes",""+String.valueOf(qtyFaltantes));
//        Log.e("Sobrantes",""+String.valueOf(qtySobrantes));
//        Log.e("Inutilizadas",""+String.valueOf(qtyInutilizadas));
//        Log.e("Impugnados",""+String.valueOf(qtyImpugnados));
//        Log.e("Nulos",""+String.valueOf(qtyNulos));
//        Log.e("Abstenciones",""+String.valueOf(qtyAbstenciones));

        //--------------------------------------------------------------------------------------
        /* ERROR #1 2014-09-16 Updated by Charles
         *          2016-05-24 second update
         * El calculo de Votos Validos no coincide con el numero de boletas
         */
        // Changes:
        //- (qtyImpugnados + qtyNulos + qtyAbstenciones) - qtyFaltantes)) {
        if (qtyJustPartyVotes > Math.abs(TOTAL_PAPELETAS - (qtySobrantes + qtyInutilizadas))
                || qtyJustPartyVotes > qtyEntregadas) {
            escrudata.setErrorTypeOne("true");
            Log.i("lazy Error type ONE", "True");
        } else {
            escrudata.setErrorTypeOne("false");
        }
        //-------------------------------------------------------------------------------------
        /* ERROR #2 : 2014-09-16 No need to apply change, will use same formula..
         *            2016-05-24 second update
         * El numero calculado de votantes es mayor que el numero de papeletas disponibles
        */
        int votantes = qtyRealSumOfAllConcepts - (qtySobrantes + qtyInutilizadas);
        if (votantes > TOTAL_PAPELETAS) {
            escrudata.setErrorTypeTwo("true");
            Log.i("Lazy Error type TWO", "True");
        } else {
            escrudata.setErrorTypeTwo("false");
        }
        //--------------------------------------------------------------------------------------
        /* ERROR #3 2014-09-16 No need to apply change, will use same formula
         *          2016-05-24 second update
         * El calculo de votantes es mayor que el numero total de papeletas
         */
        // Changes made:
        // if ((TOTAL_PAPELETAS) != (iRealSumOfAllConcepts - iFaltantes))
        // if ((qtyFaltantes) != (TOTAL_PAPELETAS - (qtyRealSumOfAllConcepts - qtyFaltantes))) {

        if (qtyRealSumOfAllConcepts > TOTAL_PAPELETAS) {
            escrudata.setErrorTypeThree("true");
            Log.i("Lazy  Error ", "type THREE: True");
        } else {
            escrudata.setErrorTypeThree("false");
        }
        //--------------------------------------------------------------------------------------
        /* ERROR #4 2014-09-16 No need to apply change, will use same formula
         *          2016-05-24 second update
         * El calculo de papeletas escrutadas no coincide con el numero reportado
         */
        //Changes made:
        //if (qtyEntregadas != (TOTAL_PAPELETAS - (qtySobrantes + qtyInutilizadas))) {
        if ((qtyRealSumOfAllConcepts - qtyFaltantes) != qtyTotalEscrutadas) {
            escrudata.setErrorTypeFour("true");
            Log.i("Lazy Error", "type FOUR: True");
        } else {
            escrudata.setErrorTypeFour("false");
        }
        //---------------------------------------------------------------------------------------
        /* ERROR #5 : 2014-09-16
         *            2016-05-24
         * El numero calculado de papeletas faltantes no coincide con el numero reportado
         */
        // Changes made
        // if (qtyRealSumOfAllConcepts != ((qtyOfPapeletasFinal - qtyOfPapeletasInicio) + 1)) {
        if ((TOTAL_PAPELETAS - (qtyRealSumOfAllConcepts - qtyFaltantes)) != qtyFaltantes) {
            escrudata.setErrorTypeFive("true");
            Log.i("Lazy Error", " type FIVE: True");
            Log.i("TOTAL PAPELETAS: ", Integer.toString(TOTAL_PAPELETAS));
        } else {
            escrudata.setErrorTypeFive("false");
        }
        //---------------------------------------------------------------------------------------
        /* ERROR #6
         * El calculo de papeletas entregadas no coincide con el numero reportado
         */
        if (TOTAL_PAPELETAS - (qtySobrantes + qtyInutilizadas) != qtyEntregadas) {
            escrudata.setErrorTypeSix("true");
            Log.i("Lazy Error", "type SIX: true");
        } else {
            escrudata.setErrorTypeSix("false");
            Log.i("Lazy Error", "type SIX: false");
        }
    }

    private void calculateErrorsHon() {
        Log.e("LAZY LOADING","HONDURAS VERSION!!");
        LinkedHashMap<String, String> conceptNumbers = db_adapter.getConceptsCountPreferential();
        Log.e("CHECK ACTIVITY","**************************");
        Log.e("concepts: ", conceptNumbers.toString());
        conceptNumbers.remove("PREFERENTIAL ELECTION ID");
        conceptNumbers.remove("JRV");

        //BR: "check errors"
//        List<PreferentialPartyVotes> partyVotes;
        int totalPartyVotes = 0;
        int initialVotes = 0;
        int qtyPartyVotes = totalPartyVotes;

//        for (Map.Entry<String, String> par : valuesMap.entrySet()) {
//            String partyKey = par.getKey();
//            String partyVal = par.getValue();
//
//            if(partyKey.contains("MOVIMIENTO") || partyKey.contains("CRUZADOS")){
//                qtyPartyVotes += Integer.parseInt(partyVal);
//            }
//        }
        int votosCruzados =ah.parseInt(valuesMap.get("VOTOS VALIDOS"),0);
        ah.savePreferences(Consts.VOTO_CRUZADO, votosCruzados);

        int qtyPapRec = ah.parseInt(valuesMap.get("PAPELETAS RECIBIDAS"),0);
        int qtyNoUtil = ah.parseInt(valuesMap.get("NO UTILIZADAS / SOBRANTES"),0);
        int qtyUtil = ah.parseInt(valuesMap.get("UTILIZADAS"),0);
        int qtyCiudadanos = ah.parseInt(valuesMap.get("CIUDADANOS QUE VOTARON"),0);
//		int qtyMer = Integer.parseInt(valuesMap.get("CIUDADANOS"));
        int qtyMer = ah.parseInt(valuesMap.get("CIUDADANOS MER QUE VOTARON"),0);
        int qtyTotVoters = ah.parseInt(valuesMap.get("TOTAL VOTANTES"),0);
        int qtyValid = ah.parseInt(valuesMap.get("VOTOS VALIDOS"),0);
        int qtyBlanco = ah.parseInt(valuesMap.get("EN BLANCO"),0);
        int qtyNul = ah.parseInt(valuesMap.get("NULOS"),0);
        int qtyTotal = ah.parseInt(valuesMap.get("GRAN TOTAL"),0);

        for(int i =0; i < conceptosAndPartiesList.size();i++){
            if((i>IdSobrantesRow)&&(i<IdUtilizadas)) {
                totalPartyVotes += ah.parseInt(conceptosAndPartiesList.get(i).getParty_votes(),0);
                Log.e(conceptosAndPartiesList.get(i).getParty_name() + " = ", Integer.toString(totalPartyVotes));
            }
        }

        qtyPartyVotes = totalPartyVotes;

        Log.e("qtyPartyVotes",Integer.toString(qtyPartyVotes));
        Log.e("qtyPapRec",Integer.toString(qtyPapRec));
        Log.e("qtyNoUtil",Integer.toString(qtyNoUtil));
        Log.e("qtyUtil",Integer.toString(qtyUtil));
        Log.e("qtyCiudadanos",Integer.toString(qtyCiudadanos));
        Log.e("qtyMer",Integer.toString(qtyMer));
        Log.e("qtyTotVoters",Integer.toString(qtyTotVoters));
        Log.e("qtyValid",Integer.toString(qtyValid));
        Log.e("qtyBlanco",Integer.toString(qtyBlanco));
        Log.e("qtyNul",Integer.toString(qtyNul));
        Log.e("qtyTotal",Integer.toString(qtyTotal));

        String voteType = getResources().getString(R.string.voteType);
        if(voteType.contains("PREF")){
            qtyPartyVotes = qtyValid;
        }
        //Calculate for Error Type One
        //		ErrorTypeOne if the party vote count is larger than the number of recieved ballots
//        if(qtyPartyVotes > qtyPapRec) {
//            escrudata.setErrorTypeOne("true");
//            Log.i("Vertical Error typeONE", "True");
//        } else escrudata.setErrorTypeOne("false");
        escrudata.setErrorTypeOne("false");

        //Calculate for Error Type Two
        //		ErrorTypeTwo if the party vote count is larger than the used ballots
//        if(qtyPartyVotes > qtyUtil){
//            escrudata.setErrorTypeTwo("true");
//            Log.i("ChckList Error typeTWO","True");
//        } else escrudata.setErrorTypeTwo("false");
        escrudata.setErrorTypeTwo("false");

        //Calculate for Error Type Three
        //		ErrorTypeThree if the party vote count is larger than the total number of voters
//        if(qtyPartyVotes > qtyTotVoters){
//            escrudata.setErrorTypeThree("true");
//            Log.i("ChckList Error typeTHRE","True");
//        } else escrudata.setErrorTypeThree("false");

        escrudata.setErrorTypeThree("false");

        //Calculate for Error Type Four
        //		ErrorTypeFour if the number of valid votes does not equal number of valid voters
//        if(qtyPartyVotes != qtyValid){
//            escrudata.setErrorTypeFour("true");
//            Log.i("ChckList Error TypeFOUR","True");
//        } else escrudata.setErrorTypeFour("false");
        escrudata.setErrorTypeFour("false");

        //Calculate for Error Type Five
        //		ErrorTypeFive if valid votes plus invalid votes does not equal total voters
//        if((qtyPartyVotes + qtyBlanco + qtyNul) != qtyTotVoters){
//            escrudata.setErrorTypeFive("true");
//            Log.i("ChckList Error TypeFIVE","True");
//        } else escrudata.setErrorTypeFive("false");
        escrudata.setErrorTypeFive("false");

        //Calculate for Error Type Six
        // 		ErrorTypeSix if valid votes plus invalid votes does not equal grand total
//        if((qtyUtil != qtyTotVoters)||(qtyUtil != qtyTotal)||(qtyTotVoters != qtyTotal) || (qtyTotVoters != (qtyMer + qtyCiudadanos))){
        if((qtyUtil != qtyTotVoters)|| (qtyTotVoters != (qtyMer + qtyCiudadanos))){
            escrudata.setErrorTypeSix("true");
            Log.i("ChckList Error TypeSIX","True");
        } else escrudata.setErrorTypeSix("false");
    }

    //CARLOS: 2016-10-21
    //DUI Dialog Two Buttons
    @Override
    public void onYesButtonDialogToConfirmDuiClicked(String duiNumber) {
        switch (noIndex) {
            case 1:
                boolean confirm = false;

                try {
                    if (!db_adapter.isOpen()) {
                        db_adapter.open();
                    }
                    confirm = db_adapter.verifyDui(DatabaseAdapterParlacen.PRESIDENT, duiNumber, vc.getJrvString());
                    if (!confirm) {
                        confirm = db_adapter.verifyDui(DatabaseAdapterParlacen.SECRETARIO, duiNumber, vc.getJrvString());
                    }
                    if (confirm) {
                        duiName = duiNumber;
                        allowEdit();
                    } else {
                        duiName = "";
                        ah.createCustomToast(getResources().getString(R.string.dui)+" NOT CONFIRMED");
                    }


                } catch (Exception e) {

                    Log.e("DUI REST ERROR: ", e.getMessage());
                }

                break;
            case 7: //CARLOS: 2016-10-20
//				aceptar_cambioBtn.setEnabled(false);
                duiPresident = duiNumber;
                try {
                    if (!db_adapter.isOpen()) {
                        db_adapter.open();
                    }
                    confirm = db_adapter.verifyDui(DatabaseAdapterParlacen.PRESIDENT, duiNumber, vc.getJrvString());
                    if (!confirm) {
                        confirm = db_adapter.verifyDui(DatabaseAdapterParlacen.SECRETARIO, duiNumber, vc.getJrvString());
                    }
                    if (confirm) {
                        duiName = duiNumber;
                        ah.createCustomToast(getResources().getString(R.string.dui)+" CONFIRMADO, INGRESE 2do "+getResources().getString(R.string.dui));
                        noIndex = 8;
//                        createDialogToConfirmDuiSecretaryTwoBtns("Ingrese el DUI # 2", 8);
                        createDialogToConfirmDuiSecretaryTwoBtns("Ingrese el "+getResources().getString(R.string.dui)+" " + Consts.DUI2, 8);
                    } else {
//						continuarBtn.setEnabled(true);
//                        ah.setButtonColorGreen(continuarBtn);
                        ah.setButtonColorGreen(aceptarBtn);
                        duiName = "";
                        ah.createCustomToast(getResources().getString(R.string.dui)+" NOT CONFIRMED");
                    }


                } catch (Exception e) {
//                    continuarBtn.setEnabled(true);
                    aceptarBtn.setEnabled(true);
                    Log.e("DUI REST ERROR: ", e.getMessage());
                }
                break;
            case 8: //CARLOS: 2016-10-20
                if (duiPresident.contains(duiNumber)) {
                    aceptar_cambioBtn.setEnabled(true);
                    duiName = "";
                    ah.createCustomToast("El "+getResources().getString(R.string.dui)+" del Presidente y el Secretario no pueden ser iguales");
//                    ah.setButtonColorGreen(continuarBtn);
                    ah.setButtonColorGreen(aceptarBtn);
                    ah.setButtonColorRed(modificarBtn);
//                    ah.setButtonColorRed(aceptarBtn);
                    ah.setButtonColorRed(rechezarBtn);
                } else {
                    try {
                        if (!db_adapter.isOpen()) {
                            db_adapter.isOpen();
                        }
                        confirm = db_adapter.verifyDui(DatabaseAdapterParlacen.PRESIDENT, duiNumber, vc.getJrvString());
                        if (!confirm) {
                            confirm = db_adapter.verifyDui(DatabaseAdapterParlacen.SECRETARIO, duiNumber, vc.getJrvString());
                        }
                        if (confirm) {
                            duiName = duiNumber;
                            ah.createCustomToast(getResources().getString(R.string.dui)+" CONFIRMADO, PUEDE PROCEDER A MODIFICAR O ACEPTAR");
                            //CARLOS: 2016-10-20
                            //CONTINUAR and enable the rest of the buttons
//                            ah.setButtonColorRed(continuarBtn);
                            ah.setButtonColorAmber(modificarBtn);
//                            ah.setButtonColorGreen(aceptarBtn);
                            ah.setButtonColorRed(aceptarBtn);
                            ah.setButtonColorGreen(rechezarBtn);

                        } else {
                            duiName = "";
//                            ah.createCustomToast("DUI #2 NOT CONFIRMED");
                            ah.createCustomToast(getResources().getString(R.string.dui)+" "+Consts.DUI2+" NOT CONFIRMED");
                            //CARLOS: 2016-10-20
                            //disable all buttons except CONTINUAR
//                            ah.setButtonColorGreen(continuarBtn);
                            ah.setButtonColorGreen(aceptarBtn);
                            ah.setButtonColorRed(modificarBtn);
//                            ah.setButtonColorRed(aceptarBtn);
                            ah.setButtonColorRed(rechezarBtn);
                        }


                    } catch (Exception e) {
//                        continuarBtn.setEnabled(true);
                        aceptarBtn.setEnabled(true);
                        Log.e("DUI REST ERROR: ", e.getMessage());
                    }

                }


                break;
            default:

                Bundle bc = new Bundle();
                bc.putParcelable("com.afilon.tse.votingcenter", vc);
                bc.putParcelable("com.afilon.tse.escrudata", new Escrudata(vc.getJRV()));
//                bc.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
                Intent intent = new Intent(FinalTableLazyLoadingActivity.this,
                        Consts.PAPALETASACT);
                intent.putExtras(bc);
                startActivity(intent);
                finish();
                break;
        }

    }

    //CARLOS: 2016-10-21
    //DUI Dialog Two Buttons
    @SuppressLint("LongLogTag")
    @Override
    public void onNoButtonDialogToConfirmDuiClicked() {
//        switch (errorExit) {
//            case 1:
//                Bundle bc = new Bundle();
//                bc.putParcelable("com.afilon.tse.votingcenter", vc);
//                bc.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
//                Intent intent = new Intent(FinalTableLazyLoadingActivity.this,
//                        PapeletasActivity.class);
//                intent.putExtras(bc);
//                startActivity(intent);
//                finish();
//                break;
//            default:
//
//                break;
//        }

//        ah.setButtonColorGreen(continuarBtn);
        ah.setButtonColorGreen(aceptarBtn);
    }

    private void printErrors() {
        if (escrudata.getErrorTypeOne().equals("true")) {
            Log.i("Final Table Error", " Type ONE: True");
        } else {
            Log.i("Final Table Error", " Type ONE: False");
        }

        if (escrudata.getErrorTypeTwo().equals("true")) {
            Log.i("Final Table Error", " Type TWO: True");
        } else {
            Log.i("Final Table Error", " Type TWO: False");
        }

        if (escrudata.getErrorTypeThree().equals("true")) {
            Log.i("Final Table Error", " Type THREE: True");
        } else {
            Log.i("Final Table Error", " Type THREE: False");
        }

        if (escrudata.getErrorTypeFour().equals("true")) {
            Log.i("Final Table Error", " Type FOUR: True");
        } else {
            Log.i("Final Table Error", " Type FOUR: False");
        }

        if (escrudata.getErrorTypeFive().equals("true")) {
            Log.i("Final Table Error", " Type FIVE: True");
        } else {
            Log.i("Final Table Error", " Type FIVE: False");
        }
        if (escrudata.getErrorTypeSix().equals("true")) {
            Log.i("Final Table Error", " type SIX: True");
        } else {
            Log.i("Final Table Error", " type SIX: False");
        }
    }

    private void fireErrorWarning() {
        if (escrudata.getErrorTypeOne().equals("true")
                | escrudata.getErrorTypeTwo().equals("true")
                | escrudata.getErrorTypeThree().equals("true")
                | escrudata.getErrorTypeFour().equals("true")
                | escrudata.getErrorTypeFive().equals("true")
                | escrudata.getErrorTypeSix().equals("true")) {

            isError = true;
            modificarBtn.setText("CORREGIR");
            errorToast();
            ah.createCustomToast(0,"HAY ERRORES ARITMETICOS");
//            Toast.makeText(getApplicationContext(), "HAY ERRORES ARITMETICON", Toast.LENGTH_LONG).show();
//            continuarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error,0,0,0);
//            createDialog("HAY ERRORES ARITMETICOS\n" +"\u00BFDESEA CONTINUAR CON ERRORES?",3);
//            noIndex = 33;
        } else {
            isError = false;
//            continuarBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            aceptarBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            modificarBtn.setText("MODIFICAR");
//            errToast.cancel();
        }
    }

    private void populateTopHeaders(VotingCenter vc) {
        ((TextView) findViewById(R.id.vote_center)).setText(vc.getVoteCenterString());
        ((TextView) findViewById(R.id.textView13)).setText(vc.getMunicipioString());
        ((TextView) findViewById(R.id.textView15)).setText(vc.getDepartamentoString());
        ((TextView) findViewById(R.id.textView23)).setText(ah.loadPreferencesString("barcodeSaved"));
        ((TextView) findViewById(R.id.textView25)).setText(vc.getJrvString());
    }

    private OnClickListener checkBoxListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isEditTextEditable) {
                ((CheckBox) v).setButtonDrawable(R.drawable.dchecked);

                ah.setButtonColorGreen(aceptar_cambioBtn);
                aceptar_cambioBtn.setVisibility(View.VISIBLE);

                int whichCheckbox = v.getId();
                int rowId = getRowId(whichCheckbox);
                int valuesId = (String.valueOf(rowId) + valuesView).hashCode() & 0xfffffff;
                setEditVote(valuesId);

                //CARLOS: trap the description of the row to be changed e.g. Sobrantes, FMLN, Impugnados, etc.
                for (TextView tView : TextViewArray) {
                    if (tView.getId() == whichCheckbox) {
                        conceptDescription = tView.getText().toString();
                    }
                }

                for (CheckBox radioBtn : arrayRadioBtn) {
                    if (radioBtn.getId() != whichCheckbox) {
                        ah.enableView(radioBtn, false);
                    }
                }
            }
        }
    };

    private OnClickListener modificarBtnListerner = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (duiName.equals("")) {
                challengeHelper.createDuiChallenge("PARA MODIFICAR LOS DATOS INGRESE SU "+getResources().getString(R.string.dui)+". DESEA MODIFICAR",MODIFICAR);

//                createDialog(
//                        "PARA MODIFICAR LOS DATOS INGRESE SU DUI. DESEA MODIFICAR",
//                        4);
                noIndex = 1;
            } else {

                totalTimesModificarBtnWasPressed += 1;
                isEditTextEditable = (totalTimesModificarBtnWasPressed > 0);
                // WORKING ON ...
                if (isEditTextEditable) {
                    ah.setButtonColorRed(aceptarBtn);
                    ah.setButtonColorRed(rechezarBtn);
//                    ah.setButtonColorRed(continuarBtn);
                    ah.setButtonColorRed(modificarBtn);

                    for (EditText vote : EditTextArray) {
                        vote.setBackgroundResource(R.drawable.bg);
                    }

                    for (CheckBox cb : arrayRadioBtn) {
                        ah.enableView(cb, true);
                    }

                }
            }
        }

    };

    private OnClickListener getAceptarCambio_btnListerner() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!db_adapter.isOpen()){
                    db_adapter.open();
                }

                // *** Save the new value for each EditText (task-11)
                // ****************************

                for (int i = 0; i < EditTextArray.size(); i++) {
                    String value = EditTextArray.get(i).getText().toString();
                    if (!ah.isInteger(value)) {
                        ah.createCustomToast(getResources().getString(R.string.numberFormatError));
                        int valuesId = (String.valueOf(i) + valuesView).hashCode() & 0xfffffff;
                        setEditVote(valuesId);
                        return;
                    }
                    ah.enableEditText(EditTextArray.get(i), false);
                    conceptosAndPartiesList.get(i).setParty_votes(value);
                }

                for (Party pa : conceptosAndPartiesList) {
                    valuesMap.put(pa.getParty_name(), pa.getParty_votes());
                    String currentDateTime = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                    Log.i("value of ", "conceptValueFinal :=> " + conceptValueFinal);
                }
                // CARLOS: Trying to save to database impugnadas
                db_adapter.updateConceptsCount(valuesMap,vc.getJRV());
                // *********************************************************************************

                // CARLOS: Calculate ERRORS after changes
                // CARLOS: re-runing the Errors method after change was made in
                // this screen
                valueMapFromVerticalConcepts = valuesMap;
                if(Consts.LOCALE.contains("HON")){
                    calculateErrorsHon();
                }else calculateErrors();

                if (escrudata.getErrorTypeOne().equals("true")
                        | escrudata.getErrorTypeTwo().equals("true")
                        | escrudata.getErrorTypeThree().equals("true")
                        | escrudata.getErrorTypeFour().equals("true")
                        | escrudata.getErrorTypeFive().equals("true")
                        | escrudata.getErrorTypeSix().equals("true")) {

                    // enable RECHAZAR
                    ah.setButtonColorGreen(rechezarBtn);

                    isError = true;
                    modificarBtn.setText("CORREGIR");
                    errorToast();
//                    ah.createCustomToast("HAY ERRORES ARITMETICON");
//                    Toast.makeText(getApplicationContext(), "HAY ERRORES ARITMETICON", Toast.LENGTH_LONG).show();
//                    continuarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error,0,0,0);
//                    createDialog(
//                            "HAY ERRORES ARITMETICOS\n"
//                                    + "\u00BFDESEA CONTINUAR CON ERRORES?",
//                            3);
//                    noIndex = 33;
                } else {
                    isError = false;
//                    errToast.cancel();
//                    continuarBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    aceptarBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    modificarBtn.setText("MODIFICAR");
                    ah.setButtonColorGreen(aceptarBtn);
                    ah.setButtonColorRed(rechezarBtn);
                    //CARLOS: 2016-10-20
//                    ah.setButtonColorRed(continuarBtn);
                }

                // Restore modificarBtn to its original state
                ah.setButtonColorAmber(modificarBtn);
                aceptar_cambioBtn.setVisibility(View.INVISIBLE);
                ah.enableView(aceptar_cambioBtn, false);

                for (EditText vote : EditTextArray) {
                    vote.setBackgroundResource(R.drawable.bg);
                    vote.clearFocus();
                }

                // WORKING ON...
                myScrollView.scrollTo(0, myScrollView.getTop());
                isEditTextEditable = false;
                for (CheckBox radioBtn : arrayRadioBtn) {
                    radioBtn.setButtonDrawable(R.drawable.lazy_load_checkbox_selector);
                    ah.enableView(radioBtn,false);
//                    radioBtn.setButtonDrawable(R.drawable.disabled_cb);
                }

                for (EditText vote : EditTextArray) {
                    vote.setBackgroundResource(R.drawable.bg);
                    vote.setEnabled(false);
                }

            }
        };
    }

    private OnClickListener getAceptarBtnListener() {
        return new OnClickListener() {
            public void onClick(View v) {

//                if (!isAccepted) {
//                    createDialog("¿DESEA ACEPTAR LOS RESULTADOS?", 1);
//                } else {
//                    ah.createCustomToast("YA HA SIDO ACEPTADA", "");
//                }
                if(isError == true){

                    challengeHelper.createSingleDuiChallenge("Ingrese el "+getResources().getString(R.string.dui)+" "+Consts.DUI1+" tiene errors!",error1, db_adapter.getDui(DatabaseAdapterParlacen.PRESIDENT));
//                    errorExit = 1;
                }else challengeHelper.createDuiChallenge(CONTINUAR);
            }
        };
    }

    private OnClickListener getRechazarBtnListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                if (!isAccepted) {
                    createDialog("¿DESEA RECHAZAR LOS RESULTADOS?", 2);
                    noIndex = 43;
                } else {
                    ah.createCustomToast("YA HA SIDO ACEPTADA",
                            "NO PUEDE RECHAZAR");
                }
            }
        };
    }

    private OnClickListener getContinuarBtnListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isError == true){

                    challengeHelper.createSingleDuiChallenge("Ingrese el "+getResources().getString(R.string.dui)+" "+Consts.DUI1+" tiene errors!",error1, db_adapter.getDui(DatabaseAdapterParlacen.PRESIDENT));
//                    errorExit = 1;
                }else challengeHelper.createDuiChallenge(CONTINUAR);
            }
        };
    }

    private ChallengeHelper.OnApprove errorDui1 = new ChallengeHelper.OnApprove(){
        @Override
        public void approved() {
            challengeHelper.createSingleDuiChallenge("Ingrese el "+getResources().getString(R.string.dui)+" "+Consts.DUI2+" tiene errors!",CONTINUAR,db_adapter.getDui(DatabaseAdapterParlacen.SECRETARIO));
        }

    };

    private ChallengeHelper.OnApprove errorExit = new ChallengeHelper.OnApprove(){
        @Override
        public void approved() {
            if (!isAccepted) {
                createDialog("¿DESEA RECHAZAR LOS RESULTADOS?", 2);
                noIndex = 43;
            } else {
                ah.createCustomToast("YA HA SIDO ACEPTADA",
                        "NO PUEDE RECHAZAR");
            }
        }
    };


    private ChallengeHelper.OnApprove duisConfirmed = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
//            ah.createCustomToast("PUEDE PROCEDER A MODIFICAR O ACEPTAR");
////            ah.setButtonColorRed(continuarBtn);
//            ah.setButtonColorAmber(modificarBtn);
//            ah.setButtonColorGreen(aceptarBtn);
//            ah.setButtonColorGreen(rechezarBtn);
            isAccepted = true;

            // (task-11)
            // *************CARLOS******************************
            Gson gson = new Gson();
            String listFilledOut = gson.toJson(valuesMap);
            // *************************************************
            escrudata.setValuMap(listFilledOut);
            Bundle b = new Bundle();
            b.putParcelable("com.afilon.tse.escrudata", escrudata);
//            b.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
            b.putParcelable("com.afilon.tse.votingcenter", vc);

            // (task-11)
            // **** We save the new values for each EditText and transfer to the
            // next screen*********
//            b.putString("escrudataMapFilledOut", listFilledOut);
//            escrudataMapString = b.getString("escrudataMapFilledOut");
//            ah.savePreferences("escrudataMap", escrudataMapString);
            // **************************************************************************************
            Intent search;

            if(Consts.LOCALE.equals(Consts.HONDURAS)&& getResources().getString(R.string.voteType).equals(Consts.DIRECT)){
                search= new Intent(FinalTableLazyLoadingActivity.this,
                        Consts.VOTECOUNTER);
            }else{
               search = new Intent(FinalTableLazyLoadingActivity.this,
                        Consts.RECACT);

            }

            search.putExtras(b);
            startActivity(search);
            finish();
        }
    };
    private ChallengeHelper.OnApprove modificarRoutine = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            allowEdit();
        }
    };

    private OnFocusChangeListener isValueInteger() {
        return new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String value = ((EditText) v).getText().toString();
                    if (!ah.isInteger(value)) {
                        ah.createCustomToast(getResources().getString(R.string.numberFormatError));
                        ((EditText) v).setText("0");
                        v.requestFocus();
                    }
                }
            }
        };
    }

    private int getRowId(int viewId) {
        for (RowContainer row : tableRows) {
            if (row.hasItem(viewId)) {
                return row.getRowId();
            }
        }
        return -1;
    }
//    private int  getItemFromRow(int rowId, String item){
//        return tableRows.get(rowId).getItemId(item);
//    }

    private ArrayList<RowContainer> tableRows = new ArrayList<>();

    private class RowContainer {
        private int rowId;
        private HashMap<Integer, String> children;


        public RowContainer(int id) {
            rowId = id;
            children = new HashMap<>();
        }

        public int getRowId() {
            return rowId;
        }

        public boolean hasItem(int itemId) {
            return children.containsKey(itemId);
        }

        public void addItem(int itemId, String item) {
            children.put(itemId, item);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Log.e("FRAGMENT","MASKED ACTION:"+String.valueOf(event.getActionMasked()));
        Log.e("FRAGMENT",event.toString());
        if(event.getActionMasked()==MotionEvent.ACTION_OUTSIDE){
            Log.e("FRAGMENT","outside event handled!");
            return true;
        }
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus){
        if(!hasfocus){
            findViewById(R.id.button_layout).setVisibility(View.INVISIBLE);
        }else{
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
        }

    }

    public void errorToast(){

        errToast = ah.createCustomToast(0,"HAY ERRORES ARITMETICOS");
        new CountDownTimer(9000, 2000)
        {
            public void onTick(long millisUntilFinished) {errToast.show();}
            public void onFinish() {errToast.show();}
        }.start();

//        while(isError){
//        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"HAY ERRORES ARITMETICON",Snackbar.LENGTH_INDEFINITE);
//        snackbar.show();
//            ah.createCustomToast(0,"HAY ERRORES ARITMETICON");
//        }
    }

}
