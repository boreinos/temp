package com.afilon.mayor.v11.activities;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.DigitsKeyListener;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.model.AppLog;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class VerticalConceptoTableActivity extends AfilonActivity implements
        DialogToConfirmDuiTwoBtns.DialogToConfirmDuiListener, OnTwoButtonDialogFragmentListener {

    private static final String CLASS_TAG = "VerticalConceptoTableActivity";
    private CustomKeyboard mCustomKeyboard;
    //	private CustomKeyboard mCustomKeyboardTwo;
    private Escrudata escrudata;
    private AppLog applog;
    private VotingCenter vc;
    private LinkedHashMap<String, String> valueMap;
    private ArrayList<Party> conceptosAndPartiesList;
    private ArrayList<EditText> columnOne;
    protected String editTextValueString;
    private ArrayList<EditText> columnTwo;
    private Utilities ah;
    private int[] lineWithMismatch;

    private int sumOfAllConcepts;
    private int TOTAL_PAPELETAS = 0;
    private int firstColumnSum = 0;
    private int secondColumnSum = 0;
    private Button entrarBtn;

    private Button continuarBtn;
    private Button reingresarBtn;
    private Button verificarBtn;

    protected boolean firstColumnCompleted;
    private TextView column_one_tv;
    private TextView column_two_tv;

    private boolean isDebugMode = false;
    private boolean isEscrutadasAdded = false;
    private boolean isEntregadasAdded = false;
    private boolean cntReady = false;
    private int valueOfEscrutadas = 0;
    private int valueOfEntregadas = 0;
    private int IdEscrutadasRow;
    private int IdEntregadasRow;
    private int IdSobrantesRow;
    private int IdCiudadanos, IdMer = 0, IdVotantes, IdUtilizadas, IdValidos, IdGranTotal, IdVotosCruzados = 0;
    private int qtyTotalEscrutadas = 0;
    private int qtyEntregadas = 0;
    private int qtyFaltantes = 0;
    private int qtySobrantes = 0;
    private int qtyInutilizadas = 0;
    private int qtyImpugnados = 0;
    private int qtyNulos = 0;
    private int qtyAbstenciones = 0;
    private int qtyOfPapeletasInicio = 0;
    private int qtyOfPapeletasFinal = 0;
    private DialogToConfirmDui dialogToConfirmDui;
    private TwoButtonDialogFragment twoBtnDialogFragment;
    private int noIndex;
    private String duiUserA = "";
    private String duiUserB = "";
    private String duiName;
    private String duiTypedIn_a = "";
    private String duiTypedIn_b = "";
    private String voteType;
    private boolean isAccepted;
    private boolean isDUIVerified_a = false;
    private boolean isDUIVerified_b = false;
    private DatabaseAdapterParlacen db_adapter = new DatabaseAdapterParlacen(this);
    private RelativeLayout conceptsParent;
    private DialogToConfirmDuiTwoBtns dialogToConfirmDuiTwoBtns;
    private SpannableString error;
    private Drawable errorImage;
    private ImageSpan errorSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ah = new Utilities(this);
        ah.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vertical_concepto);
        //Catch Unexpected Error:
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
                VerticalConceptoTableActivity.this));
        //--------- filter listeners ---------------------------------------------------
        CommonListeners listenerHandler = new CommonListeners();
        View.OnKeyListener altkeys = listenerHandler.getAltKeysListener();
        View.OnLongClickListener longClickListener = listenerHandler.getMouseListener();
        //--------- end filter listeners ------------------------------------------------
        Log.i("CLASS NAME : ", CLASS_TAG);

        mCustomKeyboard = new CustomKeyboard(this, R.id.keyboardview, R.xml.tenhexkbd);

        editTextValueString = "0";
        noIndex = 0;
        duiName = "";

        // CARLOS: Track what line has mismatch
        lineWithMismatch = new int[32];

        // CARLOS: Display a button on upper-right corner which fills out
        // sample data across all EditText objects (INSERT ONES (1) )
        // ===============================================================
        isDebugMode = false;

        if (isDebugMode) {
            Button filloutBtn = (Button) findViewById(R.id.fillout_btn);
            filloutBtn.setVisibility(View.VISIBLE);
        }
        // ===============================================================

        conceptsParent = (RelativeLayout) findViewById(R.id.conceptsParent);

        TextView votecenter_tv = (TextView) findViewById(R.id.vote_center);
        TextView municipio_tv = (TextView) findViewById(R.id.textView13);
        TextView departamento_tv = (TextView) findViewById(R.id.textView15);
        column_one_tv = (TextView) findViewById(R.id.textView23);
        column_two_tv = (TextView) findViewById(R.id.textView37);
        TextView jvr_tv = (TextView) findViewById(R.id.textView25);

        Bundle b = getIntent().getExtras();
        if (b != null) {

//            String escrudataMapString = b.getString("escrudataMap"); // Receiving
            // Concepts,
            // and
            // values
            // with
            // zeros

            escrudata = b.getParcelable("com.afilon.tse.escrudata");
            String escrudataMapString = escrudata.getValueMap();
//            applog = b.getParcelable("com.afilon.assembly.applog");
            vc = b.getParcelable("com.afilon.tse.votingcenter");
            Gson gson = new Gson();
            Type entityType = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
            valueMap = gson.fromJson(escrudataMapString.toUpperCase(), entityType);

        }
        ah.saveCurrentScreen(this.getClass(),b);

        db_adapter.open();
        //todo: debug:
        Log.e("ELECTION ID:",""+vc.getPref_election_id());

        ArrayList<Party> parties = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());
        int totalParties = parties.size();
        db_adapter.close();

        voteType = getResources().getString(R.string.voteType);
        TOTAL_PAPELETAS = ah.parseInt(escrudata.getPapeletasTotal(), 0);

        escrudata.setErrorTypeOne("false");
        escrudata.setErrorTypeTwo("false");
        escrudata.setErrorTypeThree("false");
        escrudata.setErrorTypeFour("false");
        escrudata.setErrorTypeFive("false");
        escrudata.setErrorTypeSix("false");

        votecenter_tv.setText(vc.getVoteCenterString());
        municipio_tv.setText(vc.getMunicipioString());
        departamento_tv.setText(vc.getDepartamentoString());
        column_one_tv.setText("0 / " + escrudata.getPapeletasTotal());
        column_two_tv.setText("0 / " + escrudata.getPapeletasTotal());
        jvr_tv.setText(vc.getJrvString());

        conceptosAndPartiesList = new ArrayList<Party>();

        // loop over the set using an entry set
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            String key = entry.getKey();

            conceptosAndPartiesList.add(new Party("", vc.getPref_election_id(),
                    vc.getVc_event2_locality_id(), key, "0"));

            Log.e("PAList VCevent2locid ", " =  " + vc.getVc_event2_locality_id());
            Log.e("PAList prefelecid ", " =  " + vc.getPref_election_id());
        }


        columnOne = new ArrayList<EditText>();
        columnTwo = new ArrayList<EditText>();

        TableLayout table = (TableLayout) findViewById(R.id.concepto_table);

        for (int i = 0; i < conceptosAndPartiesList.size(); i++) {

            TableRow row = new TableRow(this);

            LayoutParams params = new LayoutParams(200, LayoutParams.WRAP_CONTENT);
            params.setMargins(30, 10, 10, 10);
            // params.gravity = Gravity.CENTER_HORIZONTAL;

            TextView conceptoNameTextView = new TextView(this);
            conceptoNameTextView.setText(conceptosAndPartiesList.get(i).getParty_name());

            // Log.i("conceptosAndPartiesList.get(i).getParty_name()",
            // conceptosAndPartiesList.get(i)
            // .getParty_name());

            if (conceptosAndPartiesList.get(i).getParty_name().contains("ESCRUTADAS")) {
                IdEscrutadasRow = i;
            }
            if (conceptosAndPartiesList.get(i).getParty_name().contains("ENTREGADAS")) {
                IdEntregadasRow = i;
            }
            if (conceptosAndPartiesList.get(i).getParty_name().contains("SOBRANTES")) {
                IdSobrantesRow = i;
            }

            if(Consts.LOCALE.contains("HON")){
                for (int ii = 0; ii < conceptosAndPartiesList.size(); ii++) {
                    Log.e("conceptosAndPartiesList",conceptosAndPartiesList.get(ii).getParty_name());
                    if(conceptosAndPartiesList.get(ii).getParty_name().contains("CIUDADANOS")){
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

            conceptoNameTextView.setTextSize(25);
            conceptoNameTextView.setPadding(50, 0, 0, 0);
//            conceptoNameTextView.setCompoundDrawables(getResources().getDrawable(R.drawable.error),null,null,null);
            conceptoNameTextView.setFocusable(false);

            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(5);

            EditText editTextOne = new EditText(this);
            editTextOne.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            editTextOne.setFilters(filterArray);
            editTextOne.setOnKeyListener(altkeys);
            editTextOne.setOnLongClickListener(longClickListener);
            editTextOne.setText("");
            editTextOne.setLayoutParams(params);
            editTextOne.setId(i);
            editTextOne.setGravity(Gravity.CENTER_HORIZONTAL);

            EditText editTextTwo = new EditText(this);
            editTextTwo.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            editTextTwo.setFilters(filterArray);
            editTextOne.setOnKeyListener(altkeys);
            editTextOne.setOnLongClickListener(longClickListener);
            editTextTwo.setText("");
            editTextTwo.setLayoutParams(params);
            editTextTwo.setId(i + 100);
            editTextTwo.setGravity(Gravity.CENTER_HORIZONTAL);

            sumOfAllConcepts = 0;

            TextView emptyTextView = new TextView(this);
            emptyTextView.setLayoutParams(params);
            emptyTextView.setFocusable(false);
            emptyTextView.setId(i);

//            if((Consts.LOCALE.contains("HON"))&&conceptosAndPartiesList.get(i).getParty_name().contains("CRUZADOS")){
//                conceptoNameTextView.setText("VOTOS");
//            }

            row.addView(conceptoNameTextView);
            row.addView(editTextOne);
            row.addView(editTextTwo);
            row.addView(emptyTextView);

            table.addView(row, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            int id = editTextOne.getId();
            int idTwo = editTextTwo.getId();

            mCustomKeyboard.registerEditText(id);
            mCustomKeyboard.registerEditText(idTwo);


            columnOne.add(editTextOne);
            columnTwo.add(editTextTwo);


            //For Honduras Asamblea Hide MOVEMENT rows
            if(Consts.LOCALE.contains("HON")){
//            if(Consts.LOCALE.contains("HON")&&voteType.contains("PREF")){

                if (((i > IdSobrantesRow) && (i <= IdVotosCruzados)) || (i > IdUtilizadas + 3) ||((i > IdSobrantesRow) && (i < IdUtilizadas))) {
                    conceptosAndPartiesList.get(i).setParty_votes("0");
                    columnOne.get(i).setText("0");
                    conceptosAndPartiesList.get(i).setParty_votes_two("0");
                    columnTwo.get(i).setText("0");
                    row.setFocusable(false);
                    row.setVisibility(View.GONE);
                }
            }

            if(Consts.LOCALE.contains("HON")){
                if(i == 0){
                    conceptosAndPartiesList.get(i).setParty_votes(escrudata.getPapeletasTotal());
                    columnOne.get(i).setText(escrudata.getPapeletasTotal());
                    conceptosAndPartiesList.get(i).setParty_votes_two(escrudata.getPapeletasTotal());
                    columnTwo.get(i).setText(escrudata.getPapeletasTotal());
                    row.setFocusable(false);
                }
            }

        }

        for (EditText editOne : columnOne) {
            ah.enableEditText(editOne,false);
            editOne.setHint("CANTIDAD");
        }
        for (EditText editTwo : columnTwo) {
            ah.enableEditText(editTwo,false);
            editTwo.setHint("REINGRESAR");
        }

        entrarBtn = (Button) findViewById(R.id.iniciar_btn);

        ah.setButtonColorGreen(entrarBtn);
        entrarBtn.setText("INICIAR");
        entrarBtn.setOnClickListener(new OnClickListener() {

            @SuppressLint("LongLogTag")
            public void onClick(View v) {

                if(entrarBtn.getText().toString().contains("CORREGIR")){
                    entrarBtn.setText("ENTRAR");

                    for (EditText editOne : columnOne) {
                        ah.enableEditText(editOne,false);
                        editOne.setHint("CANTIDAD");
                    }
                    for (EditText editTwo : columnTwo) {
                        ah.enableEditText(editTwo,false);
                        editTwo.setHint("REINGRESAR");
                    }

                } else {

                    if (firstColumnCompleted) {
                        //At this point we already have the first DUI typed in
                        if (!isDUIVerified_b && entrarBtn.getText().toString().contains("INICIAR")) {
                            ah.setButtonColorRed(entrarBtn);
                            if (Consts.LOCALE.contains("HON")) {
                                createDialogToConfirmDuiSecretaryTwoBtns("Ingrese el IDENTIDAD " + Consts.DUI2, 8);
                            } else
                                createDialogToConfirmDuiSecretaryTwoBtns("Ingrese el DUI " + Consts.DUI2, 8);
                            duiUserB = "Yes";
                            noIndex = 8;


                        } else {
                            Log.i("if-else was reached for DUI if statement - First Column", "true");
                        }
                        // this code is for second column
                        if (Consts.LOCALE.contains("HON")) {
                            secondColumnSum = sumTotalHon(columnTwo);
                        } else
                            secondColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnTwo);

                        int y;
                        if (isDUIVerified_b) {
                            y = enableNextEmptyCell(columnTwo, true);
                            if (y > 0)
                                ((ScrollView) findViewById(R.id.concepto_table_sv)).smoothScrollBy(0, (int) convertDpToPixel(60, VerticalConceptoTableActivity.this));
                        } else {
                            y = enableNextEmptyCell(columnTwo, false);
                        }


                        if (y >= 0) {
                            entrarBtn.setText("ENTRAR");
                            if (Consts.LOCALE.contains("HON")) {
                                secondColumnSum = sumTotalHon(columnTwo);
                            } else
                                secondColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnTwo);
                            column_two_tv.setText(secondColumnSum + " / " + escrudata.getPapeletasTotal());

                        } else if (y == -1) {

//						Log.d("else if (y == -1)", "true");
                            // entrarBtn.setText("INICIAR");
                            columnTwo.get(columnTwo.size() - 1).setFocusableInTouchMode(false);
                            columnTwo.get(columnTwo.size() - 1).setFocusable(false);
                            ah.setButtonColorGreen(verificarBtn);
                            ah.setButtonColorRed(entrarBtn);
                            reingresarBtn.requestFocus();

                            column_two_tv.setText(secondColumnSum + " / " + escrudata.getPapeletasTotal());
                            for (int i = 0; i < columnTwo.size(); i++) {
                                columnTwo.get(i).setFocusableInTouchMode(false);
                                columnTwo.get(i).setFocusable(false);
                            }

                        } else {

                            entrarBtn.setText("ENTRAR");
                            if (!conceptosAndPartiesList.get(y - 1).getParty_name().equals("TOTAL PAPELETAS ESCRUTADAS")) {

                            }

                            if (Consts.LOCALE.contains("HON")) {
                                secondColumnSum = sumTotalHon(columnTwo);
                            } else
                                secondColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnTwo);
                            column_two_tv.setText(secondColumnSum + " / " + escrudata.getPapeletasTotal());
                        }
                    } else {
                        /** For FIRST COLUMN ******************/

                        if (duiName.equals("") && entrarBtn.getText().toString().contains("INICIAR")) {

                            //CARLOS: 2016-11-01
                            ah.setButtonColorRed(entrarBtn);

                            if (Consts.LOCALE.contains("HON")) {
                                createDialogToConfirmDuiPresidentTwoBtns("Ingrese el IDENTIDAD " + Consts.DUI1, 7);
                            } else
                                createDialogToConfirmDuiPresidentTwoBtns("Ingrese el DUI " + Consts.DUI1, 7);
                            duiUserA = "Yes";
                            noIndex = 7;

                        } else {
                            Log.i("if-else was reached for DUI if statement - Second Column", "true");
                        }

                        if (Consts.LOCALE.contains("HON")) {
                            firstColumnSum = sumTotalHon(columnOne);
                        } else
                            firstColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnOne);

                        Log.i("Watching this NOW : isDUIVerified_a", String.valueOf(isDUIVerified_a));
//					// this is the first column code
                        int x;
                        if (isDUIVerified_a) {
                            //enbale next cell and scroll
                            x = enableNextEmptyCell(columnOne, true);
                            if (x > 0)
                                ((ScrollView) findViewById(R.id.concepto_table_sv)).smoothScrollBy(0, (int) convertDpToPixel(60, VerticalConceptoTableActivity.this));
                        } else {
                            x = enableNextEmptyCell(columnOne, false);
                        }

                        if (x >= 0) {

                            entrarBtn.setText("ENTRAR");

                            if (Consts.LOCALE.contains("HON")) {
                                firstColumnSum = sumTotalHon(columnOne);
                            } else
                                firstColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnOne);

                            column_one_tv.setText(firstColumnSum + " / " + escrudata.getPapeletasTotal());

                        } else if (x == -1) {

//                        columnOne.get(columnOne.size()-1).setFocusableInTouchMode(false);
//                        columnOne.get(columnOne.size()-1).setFocusable(false);
//                        columnOne.get(IdSobrantesRow+8).setFocusableInTouchMode(false);
//                        columnOne.get(IdSobrantesRow+8).setFocusable(false);
                            Log.e("column One size", Integer.toString(columnOne.size()));
                            ah.setButtonColorGreen(reingresarBtn);
                            ah.setButtonColorRed(entrarBtn);
                            reingresarBtn.requestFocus();
                            column_one_tv.setText(firstColumnSum + " / " + escrudata.getPapeletasTotal());
                            for (int i = 0; i < columnOne.size(); i++) {
//                            Log.e("columnOne Size", String.format("%d", columnOne.size()));
//                            columnOne.get(i).clearFocus();
                                columnOne.get(i).setFocusableInTouchMode(false);
                                columnOne.get(i).setFocusable(false);
                            }
                            Log.e("AFTER CLEAR FOCUS FOR ALL", "X == -1");
                        } else {

                            entrarBtn.setText("ENTRAR");

                            // CARLOS: Trap if Escrutadas or Entregadas is added to
                            // the total
                            if (conceptosAndPartiesList.get(x - 1).getParty_name().equals("TOTAL PAPELETAS ESCRUTADAS")
                                    ) { //||conceptosAndPartiesList.get(x-1).getParty_name().equals("UTILIZADAS")
                                isEscrutadasAdded = true;
                                valueOfEscrutadas = ah.parseInt(escrudata
                                        .getPapeletasTotal(),0);
                            } else if (conceptosAndPartiesList.get(x - 1).getParty_name().equals("PAPELETAS ENTREGADAS")) {
                                isEntregadasAdded = true;
                                valueOfEntregadas = ah.parseInt(escrudata
                                        .getPapeletasTotal(),0);
                            }

                            if (Consts.LOCALE.contains("HON")) {
                                firstColumnSum = sumTotalHon(columnOne);
                            } else
                                firstColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnOne);

                            column_one_tv.setText(firstColumnSum + " / " + escrudata.getPapeletasTotal());
                            ah.setButtonColorRed(reingresarBtn);
                        }
                    }
                }

            }
        });

        reingresarBtn = (Button) findViewById(R.id.reingresar_btn);
        ah.setButtonColorRed(reingresarBtn);
        reingresarBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                entrarBtn.setText("RE-INICIAR");

                ah.setButtonColorRed(reingresarBtn);
                ah.setButtonColorGreen(entrarBtn);

                for (int i = 0; i < columnOne.size(); i++) {
                    if (!columnOne.get(i).getText().toString().equals("")) {
                        conceptosAndPartiesList.get(i).setParty_votes(columnOne.get(i).getText().toString());
                        columnOne.get(i).setText("****");
                        columnOne.get(i).setFocusable(false);
                    }
                }

                firstColumnCompleted = true;

            }
        });

        verificarBtn = (Button) findViewById(R.id.verificar_btn);
        ah.setButtonColorRed(verificarBtn);
        verificarBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                entrarBtn.setText("INICIAR");
                ah.setButtonColorRed(verificarBtn);

                for (int i = 0; i < columnTwo.size(); i++) {
                    if (!columnTwo.get(i).getText().toString().equals("")) {
                        conceptosAndPartiesList.get(i).setParty_votes_two(columnTwo.get(i).getText().toString());
                        columnTwo.get(i).setFocusable(false);
                    }
                }

                // if(!isDebugMode) {
                compareNumbersInTwoColomns();
                // }

                String thingy = "false";
                if(hasEmptyEditText(columnTwo)){
                    thingy = "true";
                }
                String countReady = "false";
                if(cntReady){
                    countReady = "true";
                }

                Log.e("has Empty Edit Text", thingy);
                Log.e("cntready", countReady);
                if (hasEmptyEditText(columnTwo) && cntReady) {
                    ah.setButtonColorGreen(continuarBtn);
                } else {
                    ah.setButtonColorRed(continuarBtn);
                }
            }
        });

        continuarBtn = (Button) findViewById(R.id.continuar_btn);
        ah.setButtonColorRed(continuarBtn);
        continuarBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                if (hasEmptyEditText(columnTwo) && cntReady) {
                    for (Party par : conceptosAndPartiesList) {
                        valueMap.put(par.getParty_name(), par.getParty_votes());
                        sumOfAllConcepts += ah.parseInt(par.getParty_votes(), 0);
                    }

                    Gson gson = new Gson();
                    String list = gson.toJson(valueMap);
                    escrudata.setValuMap(list);
                    if(Consts.LOCALE.contains("HON")){
                        calculateErrorsHon();
                    }else calculateErrors();

                    Bundle bndl = new Bundle();
                    bndl.putParcelable("com.afilon.tse.votingcenter", vc);
                    bndl.putParcelable("com.afilon.tse.escrudata", escrudata);
//                    bndl.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
//                    bndl.putString("escrudataMap", list);
                    Intent search = new Intent(
                            VerticalConceptoTableActivity.this,
                            Consts.FINALTABLEACT);
                    search.putExtras(bndl);
                    ah.savePreferences("TOTALVOTES",sumTotalofCellsByColumn(columnOne));
//                    search.putExtra(,);
                    startActivity(search);
                    finish();

                } else {

                    ah.createCustomToast("Por favor llene todas las", "celdas vacias y presione veificar");
                }

            }
        });

        conceptsParent.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

//					column_one_tv.requestFocus();
                    mCustomKeyboard.showCustomKeyboard(column_one_tv);
                    Log.e(">>> DEBUG Enabling custom Keyboard", "");
                    return true;
                }
                return false;
            }
        });

    }

    //CARLOS: 2016-12-18
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

    protected void compareNumbersInTwoColomns() {
        int mismatch = 0;

        for (int i = 0; i < conceptosAndPartiesList.size(); i++) {

            if (conceptosAndPartiesList.get(i).getParty_votes().equals(conceptosAndPartiesList.get(i).getParty_votes_two())) {

                columnOne.get(i).setText(conceptosAndPartiesList.get(i).getParty_votes());

            } else {
                mismatch++;

                // empty first column wrong cells
                columnOne.get(i).setText("");
                error = new SpannableString("   CANTIDAD");
                errorImage = getResources().getDrawable(R.drawable.error);
                errorImage.setBounds(0,0,25,25);
                errorSpan = new ImageSpan(errorImage, ImageSpan.ALIGN_BASELINE);
                error.setSpan(errorSpan,0,3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                columnOne.get(i).setHint(error);

                if (!conceptosAndPartiesList.get(i).getParty_name().equals("TOTAL PAPELETAS ESCRUTADAS"))
                    firstColumnSum -= ah.parseInt(conceptosAndPartiesList.get(i).getParty_votes(), 0);

                // CARLOS: trace cells with mismatches
                lineWithMismatch[i] = columnOne.get(i).getId();

                // emply second column wrong cells
                columnTwo.get(i).setText("");
                error = new SpannableString("   REINGRESAR");
                errorImage = getResources().getDrawable(R.drawable.error);
                errorImage.setBounds(0,0,25,25);
                errorSpan = new ImageSpan(errorImage, ImageSpan.ALIGN_BASELINE);
                error.setSpan(errorSpan,0,3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                columnTwo.get(i).setHint(error);

                if (!conceptosAndPartiesList.get(i).getParty_name().equals("TOTAL PAPELETAS ESCRUTADAS"))
                    secondColumnSum -= ah.parseInt(conceptosAndPartiesList.get(i).getParty_votes_two(), 0);
                ah.setButtonColorGreen(entrarBtn);
                entrarBtn.setText("CORREGIR");
            }
        }

        // CARLOS: COMMENTED OUT ON 2014-08-27
        if(Consts.LOCALE.contains("HON")){
            firstColumnSum = sumTotalHon(columnOne);
        }else firstColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnOne); // CARLOS
        column_one_tv.setText(firstColumnSum + " / " + escrudata.getPapeletasTotal());

        if(Consts.LOCALE.contains("HON")){
            secondColumnSum = sumTotalHon(columnTwo);
        }else secondColumnSum = sumTotalofCellsByColumnWithoutEscrutadasEntregadas(columnTwo); // CARLOS
        column_two_tv.setText(secondColumnSum + " / " + escrudata.getPapeletasTotal());

        firstColumnCompleted = false;

        //flag for continuar being ready
        cntReady = !(mismatch > 0);

    }

    // CARLOS:
    private int sumTotalofCellsByColumn(ArrayList<EditText> arrayOfCells) {
        int iTotal = 0;
        for (int i = 0; i < arrayOfCells.size(); i++) {
            if (i != 12) { // By-Pass Total Papeletas Escrutadas
                if (i != 14) { // By-Pass Papeletas Entregadas
                    // Log.i("evaluating (i) in Column 1: ", String.valueOf(i));
                    iTotal += arrayOfCells.get(i).getText().toString()
                            .equals("") ? 0 : ah.parseInt(arrayOfCells
                            .get(i).getText().toString(),0);
                }
            }
        }
        return iTotal;
    }

    private boolean isFirstRowSelected(ArrayList<EditText> arrayOfCells) {
        boolean iTotal = false;
        for (int i = 0; i < arrayOfCells.size(); i++) {

            iTotal = (i == IdSobrantesRow) ? true : false;
            return iTotal;

        }
        return iTotal;
    }

    private int sumTotalofCellsByColumnWithoutEscrutadasEntregadas(
            ArrayList<EditText> arrayOfCells) {
        int iTotal = 0;
        for (int i = 0; i < arrayOfCells.size(); i++) {
            if (i != IdEscrutadasRow && i != IdEntregadasRow) {
                int temp = iTotal;
                try {
                    iTotal += arrayOfCells.get(i).getText().toString().equals("") ? 0
                            : ah.parseInt(arrayOfCells.get(i).getText()
                            .toString(),0);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    iTotal = temp;
                    ah.createCustomToast(getResources().getString(R.string.numberFormatError));
                    arrayOfCells.get(i).setText("");
                    arrayOfCells.get(i).requestFocus();
                }
            }
        }
        return iTotal;
    }

    private int sumTotalHon(ArrayList<EditText> arrayOfCells) {
        int iTotal = 0;
        for (int i = 0; i < arrayOfCells.size(); i++) {
//            if ((i >= IdSobrantesRow && i <= (IdUtilizadas-1))
            if(i==IdMer){
                Log.e("CONCEPTS:","This is the mer box");
                int textInt = ah.parseInt(arrayOfCells.get(i).getText().toString(),0);
                if(!(db_adapter.isOpen())) db_adapter.open();
                if(textInt>db_adapter.getActaAttendeesNumber()){
                    ah.createCustomToast(getResources().getString(R.string.merWarning));
                    arrayOfCells.get(i).setText("");
                    arrayOfCells.get(i).requestFocus();
                }
            }
            if ((i >= IdSobrantesRow && i <= (IdUtilizadas-1)) || i == (IdValidos + 1) || i == (IdValidos + 2) || (i == IdValidos && voteType.contains("PREF"))) {
                int temp = iTotal;
                try {
                    iTotal += arrayOfCells.get(i).getText().toString().equals("") ? 0
                            :ah.parseInt(arrayOfCells.get(i).getText().toString(),0);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    iTotal = temp;
                    ah.createCustomToast(getResources().getString(R.string.numberFormatError));
                    arrayOfCells.get(i).setText("");
                    arrayOfCells.get(i).requestFocus();
                }
            }
        }
        return iTotal;
    }

    private void requestFocusForEmptyCell() {
        for (EditText ed : columnOne) {
            if (ed.getText().toString().equals("")) {
                ed.requestFocus();
                break;
            }
        }

    }

    public boolean compareVotesNumbers(ArrayList<Party> plist) {

        for (Party par : plist) {
            if (!par.getParty_votes().equals(par.getParty_votes_two())) {
                return false;
            }
        }
        return true;
    }

    private void calculateErrors() {

        qtyTotalEscrutadas = ah.parseInt(valueMap.get("ESCRUTADAS"), 0);
        qtyEntregadas = ah.parseInt(valueMap.get("ENTREGADAS"), 0);
        qtyFaltantes = ah.parseInt(valueMap.get("FALTANTES"), 0);
        qtySobrantes = ah.parseInt(valueMap.get("SOBRANTES"), 0);
        qtyInutilizadas = ah.parseInt(valueMap.get("INUTILIZADAS"), 0);
        qtyImpugnados = ah.parseInt(valueMap.get("IMPUGNADOS"), 0);
        qtyNulos = ah.parseInt(valueMap.get("NULOS"), 0);
        qtyAbstenciones = ah.parseInt(valueMap.get("ABSTENCIONES"), 0);
        int votosCruzados = ah.parseInt(valueMap.get("VOTOS CRUZADOS"), 0);
        if(Consts.LOCALE.equals("HON")){
            votosCruzados =ah.parseInt(valueMap.get("CRUZADOS"),0);
        }
        ah.savePreferences(Consts.VOTO_CRUZADO, votosCruzados);
        ah.savePreferences(Consts.CURRENT_JRV, vc.getJrvString());
        qtyOfPapeletasInicio = ah.parseInt(escrudata.getPapeletasInicio(), 0);
        qtyOfPapeletasFinal = ah.parseInt(escrudata.getPapeletasFinal(), 0);


        // CARLOS: 2014-08-21
        int qtyRealSumOfAllConcepts = (sumOfAllConcepts - (qtyTotalEscrutadas + qtyEntregadas));
        int qtyJustPartyVotes = (qtyRealSumOfAllConcepts - Math
                .abs((qtySobrantes + qtyInutilizadas + qtyImpugnados + qtyNulos + qtyAbstenciones + qtyFaltantes)));
        escrudata.setVotosValidos(String.valueOf(qtyJustPartyVotes));

        //--------------------------------------------------------------------------------------
        /* ERROR #1 2014-09-16 Updated by Charles
         *          2016-05-24 second update
         * El calculo de Votos Validos no coincide con el numero de boletas
         */
        // Changes:
        //- (qtyImpugnados + qtyNulos + qtyAbstenciones) - qtyFaltantes)) {
        if (qtyJustPartyVotes > Math.abs(TOTAL_PAPELETAS - (qtySobrantes + qtyInutilizadas)) || qtyJustPartyVotes > qtyEntregadas) {
            escrudata.setErrorTypeOne("true");
            Log.i("Vertical Error type ONE", "True");
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
            Log.i("Vertical Error type TWO", "True");
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
            Log.i("Vertical Error ", "type THREE: True");
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
            Log.i("Vertical Error", "type FOUR: True");
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
            Log.i("Vertical Error", " type FIVE: True");
        } else {
            escrudata.setErrorTypeFive("false");
        }
        //---------------------------------------------------------------------------------------
        /* ERROR #6
         * El calculo de papeletas entregadas no coincide con el numero reportado
         */
        if (TOTAL_PAPELETAS - (qtySobrantes + qtyInutilizadas) != qtyEntregadas) {
            escrudata.setErrorTypeSix("true");
            Log.i("Vertical Error", "type SIX: true");
        } else {
            escrudata.setErrorTypeSix("false");
        }
    }

    private void calculateErrorsHon() {
        db_adapter.open();

        LinkedHashMap<String, String> conceptNumbers = db_adapter.getConceptsCountPreferential();
        Log.e("CHECK ACTIVITY","**************************");
        Log.e("concepts: ", conceptNumbers.toString());
        conceptNumbers.remove("PREFERENTIAL ELECTION ID");

//        int votosCruzados =ah.parseInt(valueMap.get("VOTOS VALIDOS"),0);
        int votosCruzados =ah.parseInt(valueMap.get("UTILIZADAS"),0);
        ah.savePreferences(Consts.VOTO_CRUZADO, votosCruzados);

        //Calculate sum of all party
        // party ids between Sobrantes and Utilizadas
        int totalPartyVotes = 0;
        for(int i =IdSobrantesRow+1; (i>IdSobrantesRow)&&(i<IdUtilizadas);i++){
            totalPartyVotes += ah.parseInt(conceptosAndPartiesList.get(i).getParty_votes(),0);
        }

        int qtyPartyVotes = totalPartyVotes;

        //Variables to retrieve conceptos counts
        int qtyPapRec = ah.parseInt(conceptosAndPartiesList.get(0).getParty_votes(),0);
        int qtyNoUtil = ah.parseInt(conceptosAndPartiesList.get(IdSobrantesRow).getParty_votes(),0);
        int qtyUtil = ah.parseInt(conceptosAndPartiesList.get(IdUtilizadas).getParty_votes(),0);
        int qtyCiudadanos = ah.parseInt(conceptosAndPartiesList.get(IdMer - 1).getParty_votes(),0);
        int qtyMer = ah.parseInt(conceptosAndPartiesList.get(IdMer).getParty_votes(),0);
        int qtyTotVoters = ah.parseInt(conceptosAndPartiesList.get(IdVotantes).getParty_votes(),0);
        int qtyValid = ah.parseInt(conceptosAndPartiesList.get(IdValidos).getParty_votes(),0);
        int qtyBlanco = ah.parseInt(conceptosAndPartiesList.get(IdGranTotal - 2).getParty_votes(),0);
        int qtyNul = ah.parseInt(conceptosAndPartiesList.get(IdGranTotal - 1).getParty_votes(),0);
        int qtyTotal = ah.parseInt(conceptosAndPartiesList.get(IdGranTotal).getParty_votes(),0);

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

//		//Set Total Party Votes Value
//		escrudata.setVotosValidos(String.valueOf(totalPartyVotes));
//		Log.e("Sum of sumOfAllConcepts", " : " + String.valueOf(sumOfAllConcepts));
//		Log.e("party votes ", Integer.toString(qtyPartyVotes));


        //Calculate for Error Type One
        //		ErrorTypeOne if the party vote count is larger than the number of recieved ballots
        Log.e("Party Votes   ", Integer.toString(qtyPartyVotes));
        Log.e("Pap Recividas ", Integer.toString(qtyPapRec));
        if(voteType.contains("PREF")){
            qtyPartyVotes = qtyValid;
        }
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
        if((qtyUtil != qtyTotVoters)|| (qtyTotVoters != (qtyMer + qtyCiudadanos))){
            escrudata.setErrorTypeSix("true");
            Log.i("ChckList Error TypeSIX","True");
        } else escrudata.setErrorTypeSix("false");
        db_adapter.close();
    }

    public boolean hasEmptyEditText(ArrayList<EditText> array) {
        for (EditText ed : array) {
            if (ed.getText().toString().equals("")) {
                return false;
            }
        }
        return true;
    }

    protected int enableNextEmptyCell(ArrayList<EditText> arrayOfCells, boolean duiOk) {
        Log.i("<<< >>>value of duiOk", String.valueOf(duiOk));
        if(Consts.LOCALE.contains("HON")&&voteType.contains("PREF")){
            for (int i = 0; i < arrayOfCells.size(); i++) {
                if(((i <= IdSobrantesRow ) || (((i > IdVotosCruzados)))) && i <= IdUtilizadas+3 ) {
                    if (arrayOfCells.get(i).getText().toString().equals("")) {

                        if (duiOk) {

                            arrayOfCells.get(i).setFocusableInTouchMode(true);
                            arrayOfCells.get(i).requestFocus();
                            if(arrayOfCells.get(i).getHint().toString().contains("CANTIDAD")) {
                                arrayOfCells.get(i).setHint("CANTIDAD");
                            }else arrayOfCells.get(i).setHint("REINGRESAR");

                        } else {
                            arrayOfCells.get(i).setFocusableInTouchMode(false);
                            arrayOfCells.get(i).setFocusable(false);
                        }
                        return i;
                    }
                }
            }
        } else {
            for (int i = 0; i < arrayOfCells.size(); i++) {

                if (arrayOfCells.get(i).getText().toString().equals("")) {

                    if (duiOk) {
                        arrayOfCells.get(i).setEnabled(true);
                        arrayOfCells.get(i).setFocusable(true);
                        arrayOfCells.get(i).setFocusableInTouchMode(true);
                        arrayOfCells.get(i).requestFocus();
                        if(arrayOfCells.get(i).getHint().toString().contains("CANTIDAD")) {
                            arrayOfCells.get(i).setHint("CANTIDAD");
                        }else arrayOfCells.get(i).setHint("REINGRESAR");

                    } else {
                        arrayOfCells.get(i).setFocusableInTouchMode(false);
                        arrayOfCells.get(i).setFocusable(false);
                    }

                    // and return cell index
                    return i;
                }
                arrayOfCells.get(i).setEnabled(false);
                arrayOfCells.get(i).setFocusable(false);
                arrayOfCells.get(i).setFocusableInTouchMode(false);
            }
        }
        return -1;

    }

    @SuppressLint("DefaultLocale")
    protected void enableEachCell(ArrayList<EditText> arrayOfCells, boolean enableMode) {
        for (int i = 0; i < arrayOfCells.size(); i++) {
            arrayOfCells.get(i).setEnabled(enableMode);

        }
    }

    @Override
    public void onBackPressed() {
//		if (mCustomKeyboard.isCustomKeyboardVisible()) {
//			mCustomKeyboard.hideCustomKeyboard();
//		} else {
//			Log.d("Horizontal", "ConceptoActivity on BackButtonPressed Called");
//			Intent setIntent = new Intent(Intent.ACTION_MAIN);
//			setIntent.addCategory(Intent.CATEGORY_HOME);
//			setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(setIntent);
//		}
    }

    // CARLOS: This method is available is app is in DEBUG MODE
    public void onClick_FillOut(View v) {
        // Fill out TextEdit in column ONE
        for (int i = 0; i < columnOne.size(); i++) {
            columnOne.get(i).setText("1");
            columnOne.get(i).setFocusable(false);
        }
        // Fill out TextEdit in column TWO
        for (int i = 0; i < columnTwo.size(); i++) {
            columnTwo.get(i).setText("1");
            columnTwo.get(i).setFocusable(false);
        }

        // Assing votes for both columns
        for (int i = 0; i < columnTwo.size(); i++) {
            if (columnTwo.get(i).getText().toString().equals("1")) {
                // Log.i("etParty_votes_two is been SET", "TRUE");
                conceptosAndPartiesList.get(i).setParty_votes_two(
                        columnTwo.get(i).getText().toString());
                columnTwo.get(i).setFocusable(false);
            }
        }

        for (int i = 0; i < columnOne.size(); i++) {
            if (columnOne.get(i).getText().toString().equals("1")) {
                // Log.i("setParty_votes is been SET", "TRUE");
                conceptosAndPartiesList.get(i).setParty_votes(
                        columnOne.get(i).getText().toString());
                // columnOne.get(i).setText("****");
                columnOne.get(i).setFocusable(false);
            }
        }

        // Update the column_one_tv and column_two_tv on TOP of the screen
        firstColumnSum = sumTotalofCellsByColumn(columnOne);
        column_one_tv.setText(firstColumnSum + " / "
                + escrudata.getPapeletasTotal());

        secondColumnSum = sumTotalofCellsByColumn(columnTwo);
        column_two_tv.setText(secondColumnSum + " / "
                + escrudata.getPapeletasTotal());

        firstColumnCompleted = true;

        ah.setButtonColorRed(reingresarBtn);

        ah.setButtonColorRed(entrarBtn);

        ah.setButtonColorGreen(verificarBtn);
        verificarBtn.requestFocus();
    }

    @Override
    public void onYesButtonDialogToConfirmDuiClicked(String duiNumber) {
        // TODO change string to VARIABLE!
        switch (noIndex) {
            case 7: //PRESIDENTE
                Log.e("DUI 1 WAS TYPED IN: ", "true");
                //isDUIVerified_a = true;
                //CARLOS: Pass dui number to duiTypedIn variable
                duiTypedIn_a = duiNumber;
                try {
                    Log.e("OPEN DB : ", "true");
                    db_adapter.open();
                    isDUIVerified_a = db_adapter.verifyDui(DatabaseAdapterParlacen.PRESIDENT, duiNumber, vc.getJrvString());
                    db_adapter.close();
                    Boolean bol = isDUIVerified_a;
                    Log.e("DUI VERIFIED", bol.toString());

                } catch (Exception e) {
                    Log.e("DUI REST ERROR: ", e.getMessage());
                    isDUIVerified_a = false;
                }
                if (!isDUIVerified_a) { //If DUI fails...

                    entrarBtn.setText("INICIAR");
                    ah.setButtonColorGreen(entrarBtn);
                    duiName = "";
                    if(Consts.LOCALE.contains("HON")){
                        ah.createCustomToast("IDENTIDAD INVALIDO, INTENTE DE NUEVO!");
                    }else ah.createCustomToast("DUI INVALIDO, INTENTE DE NUEVO!");
                    enableEachCell(columnOne, false);
                } else {
                    if(Consts.LOCALE.contains("HON")){
                        ah.createCustomToast("IDENTIDAD CONFIRMADO");
                    }else ah.createCustomToast("DUI CONFIRMADO");
                    //CARLOS: 2016-11-01
                    ah.setButtonColorGreen(entrarBtn);
                }


                break;

            case 8: //CARLOS: SECRETARIO
                Log.e("DUI 2 WAS TYPED IN: ", "true");
                isDUIVerified_b = true;
                //CARLOS: Pass dui number to duiTypedIn variable
                duiTypedIn_b = duiNumber;
                if (!duiTypedIn_a.contains(duiNumber)) {
                    try {
                        db_adapter.open();
//                        if(Consts.LOCALE.contains("ELSA")) {
                            isDUIVerified_b = db_adapter.verifyDui(DatabaseAdapterParlacen.SECRETARIO, duiNumber, vc.getJrvString());
//                        }else if (Consts.LOCALE.contains("HON")){
//                            isDUIVerified_b = db_adapter.verifyDui(DatabaseAdapterParlacen.SECRETARIOHON, duiNumber, vc.getJrvString());
//                        }
                        db_adapter.close();


                    } catch (Exception e) {
                        Log.e("DUI REST ERROR: ", e.getMessage());
                        isDUIVerified_b = false;
                    }
                    if (!isDUIVerified_b) {
                        entrarBtn.setText("INICIAR");
                        duiName = "";
                        if(Consts.LOCALE.contains("HON")){
                            ah.createCustomToast("IDENTIDAD INVALIDO, INTENTE DE NUEVO!");
                        }else ah.createCustomToast("DUI INVALIDO, INTENTE DE NUEVO!");
                        enableEachCell(columnTwo, false);
                        ah.setButtonColorGreen(entrarBtn);
                        entrarBtn.setText("RE-INICIAR");
                    } else {
                        ah.setButtonColorGreen(entrarBtn);
                        if(Consts.LOCALE.contains("HON")){
                            ah.createCustomToast("IDENTIDAD CONFIRMADO");
                        }else ah.createCustomToast("DUI CONFIRMADO");
                    }

                } else {
                    if(Consts.LOCALE.contains("HON")){
                        ah.createCustomToast("Este IDENTIDAD ya fue ingresado para la columna izquierda", "por favor ingrese uno diferente!");
                    }else ah.createCustomToast("Este DUI ya fue ingresado para la columna izquierda", "por favor ingrese uno diferente!");
                    ah.setButtonColorGreen(entrarBtn);
                    entrarBtn.setText("INICIAR"); //CARLOS:
                    if (isDUIVerified_a) {
                        isDUIVerified_b = false;
                    } else {
                        isDUIVerified_a = false;
                    }
                }
                break;
        }


    }

    @Override
    public void onNoButtonDialogToConfirmDuiClicked() {
        // TODO Auto-generated method stub

        ah.setButtonColorGreen(entrarBtn);
        entrarBtn.setText("INICIAR");
    }

    @Override
    public void onYesButtonForTwoButtonDialogClicked(int yesIdnex) {
        // TODO Auto-generated method stub
        switch (yesIdnex) {
            case 1:

                isAccepted = true;

                break;
            case 2:

                break;
            case 3:

                break;

            case 4:
                if(Consts.LOCALE.contains("HON")){
                    createDialogToConfirmDuiPresidentTwoBtns("Ingrese el IDENTIDAD "+Consts.DUI1, 7);
                }else createDialogToConfirmDuiPresidentTwoBtns("Ingrese el DUI "+Consts.DUI1, 7);

                break;
            default:
                finish();
                break;
        }
    }

    @Override
    public void onNoButtonForTwoButtonDialogClickedX() {
        // TODO Auto-generated method stub
        //todo: when is this called?
        switch (noIndex) {
            case 1:

                break;

            default:

                Bundle bc = new Bundle();
                bc.putParcelable("com.afilon.tse.votingcenter", vc);
                bc.putParcelable("com.afilon.tse.escrudata",escrudata);
                bc.putString("escrudataMap","error");
                Intent intent = new Intent(VerticalConceptoTableActivity.this,
                        Consts.PAPALETASACT);
                intent.putExtras(bc);
                startActivity(intent);
                finish();
                break;
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
