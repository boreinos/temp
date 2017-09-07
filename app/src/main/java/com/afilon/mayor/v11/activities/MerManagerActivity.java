package com.afilon.mayor.v11.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.google.gson.Gson;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.lang.Override;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui.DialogToConfirmDuiListener;
import com.afilon.mayor.v11.fragments.TimeWheelFragment;
import com.afilon.mayor.v11.fragments.TimeWheelFragment.OnTimeWheelDialogFragmentListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.User;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.webservice.WebServiceRestTask;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;

/**
 * Created by BReinosa on 1/12/2017.
 */
public class MerManagerActivity extends AfilonActivity implements DataResponseCallback, OnTimeWheelDialogFragmentListener {
    LinearLayout parentLayout;
    private CustomKeyboard customKeyboard;

    private ArrayList<EditText> duiList_et = new ArrayList<>();
    private ArrayList<TextView> title_tv_list = new ArrayList<>();
    private ArrayList<EditText>
            nameList = new ArrayList<>(),
            partyList = new ArrayList<>();
    private ArrayList<CheckBox> selectedList = new ArrayList<>();
    private ArrayList<Button> cambiosList = new ArrayList<>();
    private ArrayList<User> miembrosList = new ArrayList<>();
    private ArrayList<User> suplentes = new ArrayList<>();
    private ArrayList<User> proprietarios = new ArrayList<>();
    private String
            tempName = "",
            tempDui = "",
            tempParty = "";

    private VotingCenter vc;

    //dialog:
    private DialogToConfirmDui dialogToConfirmDui;
    private TwoButtonDialogFragment twoBtnDialogFragment;
    private ChallengeHelper challengeHelper;
    private DatabaseAdapterParlacen db_adapter;
    private Utilities utility;
    private String timeFirst = "";
    private String timeFinal = "";
    //constants
    private final static String TAG = "Mer Manager Activity";
    private final static String DEBUGSUM = "MEMBER COUNT: ";
    private final static String DEBUGROW = "ROW, COL: ";
    private final static String TAGL = "LOAD USERS!!";
    //constants, screen type:
    public final static String keyActivity = "Activity";
    public final static int SIGNIN = -1;
    public final static int PRESENT = 5;
    public final static int NOTPRESENT = 6;
    private final static int ACEPTAR = 4;
    private final static int CONTINUAR = 9;

    private int attemptCounter = 0;
    private int
            currentRow,
            noIndex,
            completedEntries = 0;
    public final static int Min_Number_Members_Present = 3;


    private final static String PROPRIETARIOS = "Propietarios";
    private final String SUPLENTES = "suplentes";
    private final String SUMMARY = "resumen";
    private final String INELECTION = "election";
    private String
            screen = INELECTION,
            thisjrv;//"Propietarios";

    private boolean
            isName = false,
            isParty = false;
    private Button
//            switchBtn,
            modificarBtn,
            aceptarBtn,
            clearBtn,
            shiftBtn,
            substituteBtn;
    private TextView Tabheader;
    //flags
    boolean nextScreen = false;
    boolean cambiosMode = false;//Always true since the modificar column is available from the beginning!! todo: GET RID OFF
    boolean lockedRow = false;
    boolean lockrequest = false;
    boolean successUnlock;
    boolean duiconfirmed = false;
    Bundle b;
    private CommonListeners textwatcher;
    private boolean
            isLast = false,
            prop = true,
            supp = false,
            stepOne = false,
            stepTwo = false;

    /*--------------------- LIFE CYCLE OF THE APPLICATION: ---------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.mer_activity_manager);
        parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(MerManagerActivity.this));
        textwatcher = new CommonListeners();
        //------------------------- END OF WINDOW SET UP -------------------------------------------
        utility = new Utilities(this);
        b = getIntent().getExtras();
        utility.saveCurrentScreen(this.getClass(),b);
        //------------------------ import new custom keyboard ------------------------
        customKeyboard = new CustomKeyboard(this, R.id.keyboardview, R.xml.tenhexkbd);

        findViewById(R.id.supplentesBtn).setVisibility(View.GONE);
//        switchBtn = (Button) findViewById(R.id.supplentesBtn);
        shiftBtn = (Button) findViewById(R.id.shiftBtn);
        modificarBtn = (Button) findViewById(R.id.modificar_btn);
        aceptarBtn = (Button) findViewById(R.id.aceptar_btn);
        clearBtn = (Button) findViewById(R.id.clear_btn);
        substituteBtn = (Button) findViewById(R.id.sustituirBtn);

        Tabheader = (TextView) findViewById(R.id.tableHeader);

        thisjrv = utility.loadPreferencesString(getResources().getString(R.string.jrvNumber));

        db_adapter = new DatabaseAdapterParlacen(this);
        challengeHelper = new ChallengeHelper(this);
//        challengeHelper.setTools(utility,db_adapter);
//        challengeHelper.addRoutine(REINICIAR, reiniciar);
        challengeHelper.addRoutine(ACEPTAR,sendRoutine);
        challengeHelper.addRoutine(CONTINUAR, continueRoutine);

        loadUpDataSqliteElection();
        loadTable();
        setupLayout();
        setupListeners();
        setupButtons();
        unlockAllCheckBoxes();
    }
    /*-------------------------- ONCREATE METHODS: --------------------------------------------*/
    private void loadUpDataSqliteElection() {
        String jrv = utility.loadPreferencesString(getResources().getString(R.string.jrvNumber));
//        jrv = "143";
        //String jrv =  "365";
        DatabaseAdapterParlacen db = new DatabaseAdapterParlacen(this);
        miembrosList = new ArrayList<>();//clear miembrosList just in case
        db.open();
        vc = db.getNewJrv(jrv);
//        proprietarios = db.getMesaMembers(String.valueOf(true), jrv);
//        suplentes = db.getMesaMembers(String.valueOf(false), jrv);
        proprietarios = db.getMesaMemberswParty(String.valueOf(true), jrv);
        suplentes = db.getMesaMemberswParty(String.valueOf(false), jrv);
        miembrosList = proprietarios; // we start with displaying proprietarios
        db.close();
    }

    private void loadTable() {
        int rowID = 0;
        for (User member : miembrosList) {
            if(!member.getTitle().contains("VC-Official")) {
                LinearLayout row = createLinearLayout(0);
                LinearLayout cbSelect = createLinearLayout(160);//120
                // create TextViews fields:
                //create text view(width, padding)
                TextView memberTitle_tv = createTextView(160, 15);
                TextView space_tv = createTextView(15, 0);
                TextView sp3 = createTextView(17, 0);
                // create edittext fields
                //create edit text (width, padding, text length, right margin)

                //edit text width changed from 250 to 230 for text
                EditText dui_et = createEditText(230, 10, 10, 0);
                if (Consts.LOCALE.contains("ELSA")) {
                    dui_et = createEditText(230, 10, 10, 0); //first 4 digits
                } else if (Consts.LOCALE.contains("HON")) {
                    dui_et = createEditText(230, 10, 15, 0); //first 4 digits
                }

                // edit text width changed from 280 to 260 for test

                final EditText memberParty_et = createEditText(100,10,0,20);
                memberParty_et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                memberParty_et.setInputType(InputType.TYPE_CLASS_TEXT);
                final InputMethodManager imm1 =  (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
                memberParty_et.setOnFocusChangeListener(
                        new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if(hasFocus){
                                    imm1.showSoftInput(memberParty_et, InputMethodManager.SHOW_IMPLICIT);
                                }
                            }
                        }
                );


                final EditText memberName_et = createEditText(260, 10, 0, 20); //name of the MER representative
                memberName_et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                final InputMethodManager imm =  (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
                memberName_et.setOnFocusChangeListener(
                        new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if(hasFocus){
                                    imm.showSoftInput(memberName_et, InputMethodManager.SHOW_IMPLICIT);
                                }
                            }
                        }
                );

                // create checkboxes & Button
                CheckBox selected = new CheckBox(this);

                Button modify = new Button(this);
                // assign ids
                dui_et.setId(rowID * (100) + 1);
                memberName_et.setId(rowID * (100) + 4);
                selected.setId(rowID * (100) + 5);
                modify.setId(rowID * (100) + 9);

                memberParty_et.setId(rowID * (100) + 7);

                //scale boxes:
                selected.setButtonDrawable(getResources().getDrawable(R.drawable.btn_checkbox_green_selector));
                selected.setLayoutParams(new LinearLayout.LayoutParams(convertToPx(80), LinearLayout.LayoutParams.WRAP_CONTENT));//

                // save view references
                duiList_et.add(dui_et);
                title_tv_list.add(memberTitle_tv);
                nameList.add(memberName_et);
                selectedList.add(selected);
                cambiosList.add(modify);

                partyList.add(memberParty_et);

                //set textview values
                modify.setText("C");
                dui_et.setText(member.getDUI());//member.getDUI()
                memberName_et.setText(member.getName());
                memberTitle_tv.setText(member.getTitle());

                memberParty_et.setText(member.getParty());
//                memberParty_et.setText("PTabc");

                //add check boxes to cb sub-layout
                cbSelect.addView(selected);
                //cbPresent.addView(sp3);
                cbSelect.setGravity(Gravity.CENTER_HORIZONTAL);
                //add Views to layouts
                row.addView(memberTitle_tv);
                row.addView(cbSelect);
                row.addView(dui_et);
                row.addView(space_tv);
                row.addView(memberName_et);
                row.addView(modify);
                row.addView(memberParty_et);

                parentLayout.addView(row);
                //register and set listeners
                customKeyboard.registerEditText(dui_et.getId());
                //---------------------text watcher ----------------------------------------------------
//            dui_et.addTextChangedListener(new CustomTextWatcher(memberName_et, presentYes));
                dui_et.setInputType(InputType.TYPE_CLASS_TEXT);
//                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                dui_et.addTextChangedListener(new DuiFormatTextWatcher(memberName_et, selected));
                dui_et.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                dui_et.setOnTouchListener(textwatcher.getDismissListener());
                //asdfasdf
//                memberName_et.addTextChangedListener(new DuiFormatTextWatcher(memberParty_et, selected));

                memberName_et.setOnKeyListener(presingDoneKey());
                memberParty_et.setOnKeyListener(presingDoneKey());
                //set listeners
                utility.setButtonColorRed(modify);
                modify.setPadding(25, 3, 25, 3);
                modify.setOnClickListener(modifyEntry());
                //todo: remove modify completly
                modify.setVisibility(View.GONE);
                //modify.setFocusable(false);
                enableEntries(rowID, false);
                rowID++;
            }
        }
        TextView footer = new TextView(this);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(convertToPx(200), convertToPx(100));
        footer.setLayoutParams(params);
        parentLayout.addView(footer);

    }

    private LinearLayout createLinearLayout(int width) {
        /** createLinearLayout(int width), helper method to create a linear layout with a desired
         * width and a height to wrap content. if width is set to zero,
         * then width becomes wrap_content */
        final LinearLayout row = new LinearLayout(this);
        if (width == 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(params);
        } else {
            int ewidth = convertToPx(width);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ewidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(params);
        }
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private void setupLayout() {
        //screen = SIGNIN;
        // values into headers:
//        Log.e("1", vc.getDepartamentoString());
//        Log.e("2", vc.getDepartment());
//        Log.e("3", vc.getMunicipality());
//        Log.e("4", vc.getJrvString());
//        Log.e("6", vc.getMunicipioString());
//        Log.e("7", vc.getVotingCenter());
//        Log.e("8", vc.getVoteCenterString());

//        ((TextView) findViewById(R.id.textView62)).setText("");
//        ((TextView) findViewById(R.id.textView22)).setText("");
//        ((TextView) findViewById(R.id.textView72)).setText("");
//        ((TextView) findViewById(R.id.textView35)).setText(vc.getJRV());
//        ((TextView) findViewById(R.id.textView32)).setText(utility.loadPreferencesString("barcodeSaved"));

        ((TextView) findViewById(R.id.textView62)).setText(vc.getVotingCenter());
        ((TextView) findViewById(R.id.textView22)).setText(vc.getMunicipality());
        ((TextView) findViewById(R.id.textView72)).setText(vc.getDepartment());
        ((TextView) findViewById(R.id.textView35)).setText(vc.getJRV());
        ((TextView) findViewById(R.id.textView32)).setText(utility.loadPreferencesString("barcodeSaved"));

//        ((TextView) findViewById(R.id.textView62)).setText(vc.getVoteCenterString());
//        ((TextView) findViewById(R.id.textView22)).setText(vc.getMunicipioString());
//        ((TextView) findViewById(R.id.textView72)).setText(vc.getDepartamentoString());
//        ((TextView) findViewById(R.id.textView35)).setText(vc.getJRV());
//        ((TextView) findViewById(R.id.textView32)).setText(utility.loadPreferencesString("barcodeSaved"));
    }

    private void setupListeners() {
        findViewById(R.id.textViewHour).setOnClickListener(getTime());
        findViewById(R.id.textViewMin).setOnClickListener(getTime());
        clearBtn.setOnClickListener(clearListener());
        substituteBtn.setOnClickListener(substitute());
        aceptarBtn.setOnClickListener(aceptarListener());
//        switchBtn.setOnClickListener(switchMembers());
        shiftBtn.setOnClickListener(shiftMembers());
        modificarBtn.setOnClickListener(modifyEntry());
        registerListeners();
    }

    private void registerListeners() {
        for (int i = 0; i < miembrosList.size(); i++) {
            selectedList.get(i).setOnClickListener(checkBoxListener());
//            duiList_et.get(i).setOnFocusChangeListener(focusChangeListener());
//            nameList.get(i).setOnFocusChangeListener(focusChangeListener());
            nameList.get(i).setOnClickListener(clear());
            duiList_et.get(i).setOnClickListener(clear());
            partyList.get(i).setOnClickListener(clear());
        }
    }

    private void setupButtons() {
        utility.setButtonColorGreen(aceptarBtn);
        utility.setButtonColorRed(clearBtn);
        utility.setButtonColorRed(substituteBtn);
        utility.setButtonColorRed(modificarBtn);
//        utility.setButtonColorGreen(switchBtn);
        utility.setButtonColorRed(shiftBtn);
        for(User member : miembrosList){
            if(stepOne){
                stepTwo = true;
            }
            if(member.getName().equals("")){
                stepOne = true;
            }
        }
        stepOne = false;
        if(stepTwo) {
            stepTwo = false;
//            utility.setButtonColorGreen(shiftBtn); toggle
            utility.setButtonColorRed(shiftBtn);
        }
    }
    /*--------------------------END ONCREATE METHODS: --------------------------------------------*/
    @Override
    public void onResume() {
        super.onResume(); //always call the superclass method first
        numberOfEntriesCompleted();
    }
    private void numberOfEntriesCompleted() {
        for (User miembro : miembrosList) {
            if (!miembro.getName().equals("") && !miembro.getIspresent().equals("")) {
                completedEntries++;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveToDb();
    }

    private void unlockAllCheckBoxes(){
        for(int i = 0; i < miembrosList.size(); i++) {
            enableCheckboxes(i , true);
        }
    }

    private void lockAllCheckBoxes(int x){
        currentRow = x;
        for(int i = 0; i < miembrosList.size(); i++) {
            if(i != x) {
                enableCheckboxes(i, false);
            }
        }
    }

    private void unlockallrows(){
        for(int i = 0; i < miembrosList.size(); i++){
            unlockRow(i);
        }
    }

    private void lockallotherrows(int x){
        for(int i = 0; i < miembrosList.size(); i++){
            if(i != x){
                lockRow(i);
            }
        }
    }

    private ChallengeHelper.OnApprove continueRoutine = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            Tabheader.setText("SUPPLENTES");
            screen = SUPLENTES;
            proprietarios = miembrosList;
            miembrosList = suplentes;
            updateTableWithUsers();
            prop = false;
            supp = true;
        }
    };


    private ChallengeHelper.OnApprove sendRoutine = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            sendMembers();

//            utility.setButtonColorRed(switchBtn);
            utility.setButtonColorRed(shiftBtn);
            utility.setButtonColorRed(modificarBtn);
            utility.setButtonColorRed(clearBtn);
            utility.setButtonColorRed(substituteBtn);
            utility.setButtonColorGreen(aceptarBtn);
            aceptarBtn.setText("CERRAR");

//            try {
//                synchronized (this) {
//                    wait(2000);
//                }
//            } catch (InterruptedException e) {
//
//            }
//
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(1);
//            closeApplication();
        }
    };

    private View.OnClickListener nextActivity() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        };
    }


















    /*--------------------  layout helper methods, for dynamic rendering ----------------------*/
    private EditText createEditText(int width, int padding, int maxLength, int rightMargin) {
        if (maxLength == 0) {
            maxLength = 40;
        }
        int eWidth = convertToPx(width);
        int ePadding = convertToPx(padding);
        int eRightMargin = convertToPx(rightMargin);
        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(eWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        final EditText editText = new EditText(this);
        InputFilter[] filterArray = editTextMaxLength(maxLength);
        lparams.rightMargin = eRightMargin;
        editText.setLayoutParams(lparams);
        editText.setPadding(ePadding, ePadding, ePadding, ePadding);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);//default 16 sp.
        editText.setFilters(filterArray);// set edit text to max character length.
        editText.setSingleLine(true);//single line
        return editText;
    }

    private TextView createTextView(int width, int padding) {
        final TextView textView = new TextView(this);
        int ePadding = convertToPx(padding);
        if (width == LinearLayout.LayoutParams.MATCH_PARENT) {
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
        } else {
            int eWidth = convertToPx(width);
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(eWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setPadding(ePadding, ePadding, ePadding, ePadding);
        return textView;

    }

    private int convertToPx(int dp) {
        /** converToPx(int dp), simple calculator that returns the value in px
         *  when tryting to use dp scale*/
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private InputFilter[] editTextMaxLength(int length) {
        /** editTextMaxLenght(int lenght), simple filter that limits the number of characters
         * to the desired length in an input field */
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        return filterArray;
    }

    /*----------------------  reusable methods ------------------------------------------------*/
    private void instalacionMER() {
        /** instalcionMER(), method that handles the layout of the Activity Instalcion de MER*/
        TextView horaIncial = (TextView) findViewById(R.id.textView41);
        horaIncial.setText(getString(R.string.horaInstalacion));
        horaIncial.requestFocus();
        findViewById(R.id.screenHeader).setVisibility(View.GONE);
        findViewById(R.id.linearLayoutHF).setVisibility(View.GONE);
        loadTable();
    }

    private int[] parseId(int id) {
        /**  parseId(int id) recieves the id of a view and it parses to get
         * the column # and row # of the view, it parse ids for grids up to 19 rows. */
        int[] parsedId = {0, 0};
        if (id < 100) {
            parsedId[0] = 0;
            parsedId[1] = id;
            return parsedId;
        } else if (id < 1000) {
            String sNumber = Integer.toString(id);
            char[] cNumber = sNumber.toCharArray();
            parsedId[0] = Character.getNumericValue(cNumber[0]);
            parsedId[1] = id - (100 * parsedId[0]);
            return parsedId;
        } else {
            String sNumber = Integer.toString(id);
            char[] cNumber = sNumber.toCharArray();
            parsedId[0] = Character.getNumericValue(cNumber[0] + cNumber[1]);
            parsedId[1] = id - (100 * parsedId[0]);
            return parsedId;
        }
    }

    private void lockRow(int i) {
        /** lockRow(int i)this method locks the entire row and calls to ask if this was
         * the last row the user needed to complete. */
        // findViewById(i).setBackgroundColor(0x00000000);
        //todo debug:
        Log.e(DEBUGROW, Integer.toString(i));
        enableEntries(i, false);
        completedEntries++;
        Log.e(DEBUGSUM, Integer.toString(completedEntries));
        lockedRow = false;

    }

    private boolean unlockRow(int i) {
        /** unlockRow(int i) enables and makes the input fields in this row focusable */
        if (lockedRow) {
            lockedRow = false;
            //findViewById(i).setBackgroundColor(Color.parseColor("#E5E4E4"));
            //todo debug:
            Log.e(DEBUGROW, Integer.toString(i));
            enableEntries(i, true);
            //todo: OUTOF BOUND EXCEPTION HANDLING.
            return true;
        }
        return false;
    }

    private void isEntryDone() {
        /** isEntryDone(int i)  checks if the form is completed and challenges the user if she/he
         * wants to make any changes to the form. */
        //this could be the last entry
        int nextEmptyRow = getNextEmptyRow();
        if (nextScreen && findMembersPresent() >= Min_Number_Members_Present){
            utility.setButtonColorGreen((Button) findViewById(R.id.aceptar_btn));
        }
        if (nextEmptyRow == -1) {
            noIndex = 1;
            utility.setButtonColorGreen((Button) findViewById(R.id.aceptar_btn));
        } else if (nextEmptyRow <= duiList_et.size()) {
            if (nextScreen) {
                unlockRow(nextEmptyRow);
            } else {
                clearCheckBoxes(nextEmptyRow, true);
            }
        }
    }

    private void enableContinuar() {
        /** enableContinuar, this method is called when the user is ready to continue to the next
         * page, it enables the continue button */
        Button bt = (Button) findViewById(R.id.aceptar_btn);
        utility.setButtonColorGreen(bt);
        bt.requestFocus();

    }

    private void disableContiuar() {
        /** diableContinuar(), this method disables the continue button, currently not in use*/
        Button bt = (Button) findViewById(R.id.aceptar_btn);
        bt.setFocusable(false);
        utility.setButtonColorRed(bt);
    }

    private void clearCheckBoxes(int row, boolean enable) {
        /** clearCheckBoxes(int row) this method clears the check boxes that correspond to
         *  the present check boxes and the firma check boxes of a given row */
        //todo debug:
        Log.e(DEBUGROW, Integer.toString(row));
        //todo this row returned 18 when it was supposed to be 17

        selectedList.get(row).setChecked(false);
        selectedList.get(row).setEnabled(enable);
        selectedList.get(row).setFocusable(enable);
    }

    private void clearEntries(int row) {
        /** clearEntries(int row, int signature), this method clears the input fields*/
        //todo debug:
        Log.e(DEBUGROW, Integer.toString(row));
        duiList_et.get(row).setText("");
        nameList.get(row).setText("");
        partyList.get(row).setText("");
    }

    private void enableCheckboxes(int i, boolean enable){
        utility.enableView(selectedList.get(i),enable);
    }

    private void enableEntries(int i, boolean enable) {
        /** enableEntries(int i, boolean enable), this method enables or disables a row, it takes
         * the row number and enables based on the boolean enable */
        //todo debug:
        if (i != -1) {
            Log.e(DEBUGROW, Integer.toString(i));
            duiList_et.get(i).setEnabled(enable);
            nameList.get(i).setEnabled(enable);
            duiList_et.get(i).setFocusable(enable);
            nameList.get(i).setFocusable(enable);
            partyList.get(i).setEnabled(enable);
            partyList.get(i).setFocusable(enable);
        }
    }

    private void unlockAllEntries() {
        for (int i = 0; i < miembrosList.size() - 1; i++) {
            enableEntries(i, true);
            Log.e("unlockAllEntires", Integer.toString(i));
        }
    }

    private void insertAllUser() {
        /** insertAllUser() this method opens the database and inserts every member in miembrosList
         * array into the local database(whether there is data in the user or not) */
        //create blank table before updating each.
        DatabaseAdapterParlacen db = new DatabaseAdapterParlacen(this);
        db.open();
        for (User user : miembrosList) {
            if(!user.getTitle().contains("VC-Official")) {
                db.insertMERMembers(user.getDUI(), user.getName(), user.getTitle(), user.getIspresent(), user.getIsconfirmed(),
                        user.getCargoOrder(), vc.getJRV());
            }
        }
        db.close();
    }

    private void saveToDb() {
        /** saveToDb(), this method iterates over each user in the miembrosList array and updates the
         * table based on the cargoOrder*/
        User miembro;
        User suplente;
        DatabaseAdapterParlacen db = new DatabaseAdapterParlacen(this);
        db.open();
        //for (User miembro : miembrosList) {
        // Log.e("saveToDb", miembro.toString());
        for (int i = 0; i < miembrosList.size() - 1; i++) {
            miembro = proprietarios.get(i);
            suplente = suplentes.get(i);
            db.updateMERMembers(miembro.getDUI(), miembro.getName(), miembro.getTitle(),
                    miembro.getIspresent(), miembro.getIsconfirmed(), miembro.getCargoOrder(), miembro.isPropietario());
            db.updateMERMembers(suplente.getDUI(), suplente.getName(), suplente.getTitle(),
                    suplente.getIspresent(), suplente.getIsconfirmed(), suplente.getCargoOrder(), suplente.isPropietario());

        }
//        for (User attendee: miembrosList){
//            db.insertActaAttendees(attendee);
//        }
        db.close();

    }

    private void fillTableWithUsers() {
        /** fillTableWithUsers(), this method iterates over the array miembrosList and loads the
         * information of each miembro into the given row, each meimbro has a cargoOrder assigned
         * which is the order in which the cargos are displayed on the screen. the cargoOrder
         * belongs to the row in which the user needs to be displayed. */

        int i = 0;
        for (User miembro : miembrosList) {
            if(!miembro.getTitle().contains("VC-Official")) {
                // we are going to define an empty row one with out name because it was partially filled
                enableEntries(i, false);
                title_tv_list.get(i).setText(miembro.getTitle());
//                duiList_et.get(i).setText("");
                duiList_et.get(i).setText(miembro.getDUI());
                nameList.get(i).setText(miembro.getName());
                selectedList.get(i).setChecked(false);
//                partyList.get(i).setText(miembro.getParty());
//                partyList.get(i).setText("PTabc");
                partyList.get(i).setText(miembro.getParty());
                Log.e("DEBUG: ", miembro.toString());
                i++;
            }
        }
    }

    private void updateTableWithUsers() {
        /** fillTableWithUsers(), this method iterates over the array miembrosList and loads the
         * information of each miembro into the given row, each meimbro has a cargoOrder assigned
         * which is the order in which the cargos are displayed on the screen. the cargoOrder
         * belongs to the row in which the user needs to be displayed. */

        int i = 0;
        for (User miembro : miembrosList) {
            if(!miembro.getTitle().contains("VC-Official")) {
                // we are going to define an empty row one with out name because it was partially filled

//                enableEntries(i, false);
                title_tv_list.get(i).setText(miembro.getTitle());
                duiList_et.get(i).setText(miembro.getDUI());//
                nameList.get(i).setText(miembro.getName());
                selectedList.get(i).setChecked(miembro.isPresent());
                partyList.get(i).setText(miembro.getParty());
//                partyList.get(i).setText("PTabc");
                //presenteNoList.get(i).setChecked(!miembro.isPresent());
                Log.e("DEBUG: ", miembro.toString());
                i++;
            }
        }
    }

    private boolean verifyDui(int row){
        return duiList_et.get(row).getText().toString().equals(miembrosList.get(row).getDUI());

    }


    /*-------------------------- miscellenaous methods ----------------------------------------*/

    private int nextAvailableEntry() {
        for (int i = 0; i < miembrosList.size(); i++) {
//            if (!presenteSiList.get(i).isChecked() && !presenteNoList.get(i).isChecked()) {
//                return i;
//            }
        }
        //enableContinuar();
        return -1;
    }

    private void loadUpDataSqlite() {
        String jrv = utility.loadPreferencesString(getResources().getString(R.string.jrvNumber));
        //String jrv =  "365";
        DatabaseAdapterParlacen db = new DatabaseAdapterParlacen(this);
        miembrosList = new ArrayList<>();//clear miembrosList just in case
        db.open();
        vc = db.getNewJrv(jrv);
        proprietarios = db.getMesaMembers(String.valueOf(true),jrv);
        suplentes = db.getMesaMembers(String.valueOf(false),jrv);
        for (int i = 0; i < proprietarios.size(); i++) {
            miembrosList.add(proprietarios.get(i));
            try {
                suplentes.get(i).setTitle("Suplente");
                miembrosList.add(suplentes.get(i));
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                continue;
            }
        }
        //miembrosList = proprietarios; // we start with displaying proprietarios
        db.close();
    }


    private void continueToNextEntry(){
//        updateMemberInList(currentRow);
        lockRow(currentRow);
        if(isLast){
            isLast = false;
            utility.setButtonColorGreen((Button) findViewById(R.id.aceptar_btn));
            utility.setButtonColorRed((Button) findViewById(R.id.modificar_btn));
        } else utility.setButtonColorGreen((Button)findViewById(R.id.modificar_btn));
//        isEntryDone();

    }

    //----- Todo: move
    public boolean verifyCandidateEntry() {
        boolean isIdCompleted = hasValues(duiList_et.get(currentRow));
        boolean isNameCompleted = hasValues(nameList.get(currentRow));
        boolean isPartyCompleted = hasValues(partyList.get(currentRow));
        if (!isIdCompleted) {
            utility.enableEditText(duiList_et.get(currentRow), true);
        }
        if (!isNameCompleted) {
            utility.enableEditText(nameList.get(currentRow), true);
        }
        if (!isPartyCompleted) {
            utility.enableEditText(partyList.get(currentRow), true);
        }
        return (isIdCompleted && isNameCompleted && isPartyCompleted);
    }


    /*------------------------- Listener methods -----------------------------------------------*/
    private View.OnClickListener clearListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                utility.enableView(selectedList.get(currentRow),false);
                utility.setButtonColorRed(clearBtn);
//                utility.setButtonColorRed(substituteBtn);
//                utility.setButtonColorRed(modificarBtn);
                utility.setButtonColorRed(shiftBtn);
//                utility.setButtonColorGreen(switchBtn);
//                utility.setButtonColorGreen(aceptarBtn);
                tempName = nameList.get(currentRow).getText().toString();
                tempDui = duiList_et.get(currentRow).getText().toString();
                tempParty = partyList.get(currentRow).getText().toString();

                duiList_et.get(currentRow).setText("");
                nameList.get(currentRow).setText("");
                partyList.get(currentRow).setText("");
                miembrosList.get(currentRow).setDUI("");
                miembrosList.get(currentRow).setName("");
                miembrosList.get(currentRow).setParty("");
//                miembrosList.get(currentRow).setParty("");
//                clearCheckBoxes(currentRow,true);
//                unlockAllCheckBoxes();

                for(User member : miembrosList){
                    if(stepOne){
                        stepTwo = true;
                    }
                    if(member.getName().equals("")){
                        stepOne = true;
                    }
                }
                stepOne = false;
                if(stepTwo) {
                    stepTwo = false;
//                    utility.setButtonColorGreen(shiftBtn); toggle
                    utility.setButtonColorRed(shiftBtn);
                }
            }
        };
    }

    private void shiftUpMembers(){
        for(int i = 0; i < (miembrosList.size() - 1); i ++){
            if(miembrosList.get(i).getName().equals("")){
                miembrosList.get(i).setName(miembrosList.get(i+1).getName());
                miembrosList.get(i).setDUI(miembrosList.get(i+1).getDUI());
                miembrosList.get(i).setParty(miembrosList.get(i+1).getParty());
                miembrosList.get(i+1).setName("");
                miembrosList.get(i+1).setDUI("");
                miembrosList.get(i+1).setParty("");
            }
        }
    }

    private void updateMemberInList(int row) {
//        miembrosList.get(row).setTitle(title_tv_list.get(row).getText().toString());
//        miembrosList.get(row).setDUI(duiList_et.get(row).getText().toString());
        miembrosList.get(row).setName(nameList.get(row).getText().toString());
        miembrosList.get(row).isPresent(selectedList.get(row).isChecked());
    }

    private View.OnClickListener checkBoxListener() {
        /** isPresent() returns the clickListener that handles the checkboxes for the present
         * column.  first takes the view id, parses the id and if the yes, then unlocks the row
         * and if the row is empty it request focus on the first input field.  if No is checked
         * then sets signature to no, and locks the row. it also clears the entry of the row (per
         * Charles request) */
        View.OnClickListener present = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                CheckBox cb = (CheckBox) findViewById(id);
                int[] iID = parseId(id);
                int i = iID[0];
                currentRow = i;
                int col = iID[1];
                //todo debug
                if(cb.isChecked()){
                    lockAllCheckBoxes(i);
                    utility.setButtonColorRed(shiftBtn);
                    utility.setButtonColorGreen(modificarBtn);
                    utility.setButtonColorGreen(clearBtn);
                    utility.setButtonColorRed(substituteBtn);
                    if(prop && !supp){
                        if(!suplentes.get(currentRow).getDUI().toString().equals("") && !suplentes.get(currentRow).getName().toString().equals("")){
                            utility.setButtonColorGreen(substituteBtn);
                        }
                    }
//                    utility.setButtonColorRed(switchBtn);
                    utility.setButtonColorRed(aceptarBtn);
                } else {
                    unlockAllCheckBoxes();
                    utility.setButtonColorRed(shiftBtn);
                    for(User member : miembrosList){
                        if(stepOne){
                            stepTwo = true;
                        }
                        if(member.getName().equals("")){
                            stepOne = true;
                        }
                    }
                    stepOne = false;
                    if(stepTwo) {
                        stepTwo = false;
//                        utility.setButtonColorGreen(shiftBtn); toggle
                        utility.setButtonColorRed(shiftBtn);
                    }
                    utility.setButtonColorRed(modificarBtn);
                    utility.setButtonColorRed(clearBtn);
                    utility.setButtonColorRed(substituteBtn);
//                    utility.setButtonColorGreen(switchBtn);
                    utility.setButtonColorGreen(aceptarBtn);
                }
            }
        };
        return present;
    }



    private View.OnClickListener checkBoxListenerOld() {
        /** isPresent() returns the clickListener that handles the checkboxes for the present
         * column.  first takes the view id, parses the id and if the yes, then unlocks the row
         * and if the row is empty it request focus on the first input field.  if No is checked
         * then sets signature to no, and locks the row. it also clears the entry of the row (per
         * Charles request) */
        View.OnClickListener present = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                CheckBox cb = (CheckBox) findViewById(id);
                int[] iID = parseId(id);
                int i = iID[0];
                currentRow = i;
                int col = iID[1];
                //todo debug
                if (i < miembrosList.size() && i >= 0) {
                    switch (col) {
                        case PRESENT:
                            if (cb.isChecked() && false) {
                                //both are checked, none should be checked
                                clearCheckBoxes(i, true);
                                utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                                utility.enableEditText(duiList_et.get(i), false);//lock edit text field
                                Log.e("PRESENT", "checked and No checked and neither firma is checked");
                                //todo create toast to warn that two boxes were marked "only one box may be checked"!!!!!!
                            } else if (cb.isEnabled() && cb.isChecked()) {
                                //is checked and enabled, then disable the other and open mre
                                utility.enableEditText(duiList_et.get(i), true);
                                duiList_et.get(i).requestFocus();
                                utility.setButtonColorRed((Button) findViewById(R.id.modificar_btn));
//                                miembrosList.get(i).isPresent(true);
                            } else {
                                //when it is deselected:
                                cb.setChecked(false);
                                utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                                utility.enableEditText(duiList_et.get(i), false);
                            }
                            if(getNextEmptyRow() == -1 ){
                                isLast = true;
                            }
                            break;
                        case NOTPRESENT:
                            if (cb.isChecked() && selectedList.get(i).isChecked()) {
                                //both are checked, none should be checked
                                clearCheckBoxes(i, true);
                                utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                                utility.enableEditText(duiList_et.get(i), false);//lock edit text field
                                //todo create toast to warn that two boxes were marked "only one box may be checked"!!!!!!
                            } else if (cb.isEnabled() && cb.isChecked()) {
                                // do what no is supposed to
                                selectedList.get(i).setChecked(false);
                                utility.enableEditText(duiList_et.get(i), false);//lock edit text field
                                utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                                utility.setButtonColorRed((Button) findViewById(R.id.modificar_btn));
//                                miembrosList.get(i).isPresent(false);
                            } else {
                                cb.setChecked(false);
                                utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                            }
                            if(getNextEmptyRow() == -1 ){
                                isLast = true;
                                utility.setButtonColorGreen((Button) findViewById(R.id.aceptar_btn));
                                utility.setButtonColorRed((Button) findViewById(R.id.modificar_btn));
                            }
                            break;
                        default:
                            if(getNextEmptyRow() == -1 ){
                                isLast = true;
                                utility.setButtonColorGreen((Button) findViewById(R.id.aceptar_btn));
                                utility.setButtonColorRed((Button) findViewById(R.id.modificar_btn));
                            }
                            break;
                    }
                } else {
                    Log.e(DEBUGROW, "OUT OF BOUNDS! " + Integer.toString(i) + "," + Integer.toString(col));
                }
                // lock Checkboxes
                enableCheckboxes(i,true);
                // enable modificar
//                utility.setButtonColorAmber(switchBtn);
            }
        };
        return present;
    }

    private View.OnClickListener substitute() {
        /** getTime(), this method returns the clickListener that handles the time input  */
        View.OnClickListener substitute = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utility.enableView(selectedList.get(currentRow),false);
                if(prop && !supp){
                    if(!suplentes.get(currentRow).getDUI().toString().equals("") && !suplentes.get(currentRow).getName().toString().equals("")){
                        miembrosList.get(currentRow).setDUI(suplentes.get(currentRow).getDUI().toString());
                        miembrosList.get(currentRow).setName(suplentes.get(currentRow).getName().toString());
                        miembrosList.get(currentRow).setParty(suplentes.get(currentRow).getParty().toString());
                        suplentes.get(currentRow).setDUI("");
                        suplentes.get(currentRow).setName("");
                        suplentes.get(currentRow).setParty("");
                        updateTableWithUsers();
                    }
                }
                utility.setButtonColorRed(clearBtn);
                utility.setButtonColorRed(modificarBtn);
                utility.setButtonColorRed(substituteBtn);
                utility.setButtonColorRed(shiftBtn);
                for(User member : miembrosList){
                    if(stepOne){
                        stepTwo = true;
                    }
                    if(member.getName().equals("")){
                        stepOne = true;
                    }
                }
                stepOne = false;
                if(stepTwo) {
                    stepTwo = false;
//                    utility.setButtonColorGreen(shiftBtn); toggle
                    utility.setButtonColorRed(shiftBtn);
                }
                utility.setButtonColorGreen(aceptarBtn);
                unlockAllCheckBoxes();
            }
        };
        return substitute;
    }


    private View.OnClickListener getTime() {
        /** getTime(), this method returns the clickListener that handles the time input  */
        View.OnClickListener time = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.textViewHour) {
                    startTimeWheel(1);
                } else if (v.getId() == R.id.textViewMin) {
                    startTimeWheel(2);
                } else if (v.getId() == R.id.textViewHourHF) {
                    startTimeWheel(3);
                } else {
                    startTimeWheel(4);
                }
            }
        };
        return time;
    }

    private View.OnFocusChangeListener focusChangeListener() {
        /** focusChangeListener(), this method returns the focusChangeListener that handles when
         * one input field is has focus, when the input lost focus, it saves the data inside
         * the input box into the corresponding user.  if it was gain focus the it shows the
         * custom keyboard, unless it doesn't need the custom keyboard*/
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText et = (EditText) v;
                int id = et.getId();
                int[] nID = parseId(id);
                int row = nID[0];
                int col = nID[1];

                //todo debug exceptions
                if (row < miembrosList.size() && row >= 0) {
                    if (!hasFocus) {
                        if (!et.getText().toString().equals("")) {
//                            currentRow = row;
//                            miembrosList.get(row).setUpdatedMember("yes");
                            switch (col) {
                                case 1:
                                    //miembrosList.get(row).setDUI(et.getText().toString());
                                    break;
                                case 2:
                                    //miembrosList.get(row).set_duiTwo(et.getText().toString());
                                    break;
                                case 3:
                                    //miembrosList.get(row).set_duiThree(et.getText().toString());
                                    break;
                                case 4:
                                    // miembrosList.get(row).setName(et.getText().toString());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } else {
                    //todo debug:
                    Log.e(DEBUGROW, "OUT OF BOUNDS! " + Integer.toString(row) + "," + Integer.toString(col));
                }
            }
        };
        return focusChangeListener;
    }

    private View.OnClickListener clear() {
        /** clear(), this method returns a clickListener that handles the input boxes when they are
         * clicked, by clearing any previous information in the input box.*/
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText) findViewById(v.getId());
//                et.setText("");
                if (et.isFocusable()) {
                    int id = et.getId();
                    int[] idp = parseId(id);
                    int row = idp[0];
                    int col = idp[1];
                    Log.e(DEBUGROW, "clear: " + Integer.toString(row) + "," + Integer.toString(col));
                    if (col != 4 && col != 7) {
                        customKeyboard.showCustomKeyboard(v);
                    }
                }
            }
        };
        return clickListener;
    }

    private DialogToConfirmDuiListener confirmDui() {

        DialogToConfirmDuiListener confirmDuiListener = new DialogToConfirmDuiListener() {
            @Override
            public void onYesButtonDialogToConfirmDuiClicked(String duiNumber) {
                try {
                    //NOT IN USE
                    int ws_task_number = 0;
                    String url = Consts.BASE_URI;// + Consts.PATH_LOGIN;
                    HttpGet searchRequest = new HttpGet(url);
                    WebServiceRestTask task = new WebServiceRestTask(ws_task_number);
                    task.setResponseDataCallback(MerManagerActivity.this);
                    task.execute(searchRequest);
                } catch (Exception e) {
                    Log.e("DUI REST ERROR: ", e.getMessage());
                }
            }

            @Override
            public void onNoButtonDialogToConfirmDuiClicked() {
                //do nothing:
            }
        };
        return confirmDuiListener;
    }

    private OnTwoButtonDialogFragmentListener dialogFragmentListener() {
        TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener fragmentListener = new OnTwoButtonDialogFragmentListener() {
            @Override
            public void onYesButtonForTwoButtonDialogClicked(int yesIdnex) {
                switch (yesIdnex) {
                    case 1:
                        break;
                    case 2:
                        //loadCambiosColumn();
                        completedEntries = duiList_et.size();
                        cambiosMode = true;
                        break;
                    case 3:
                        Log.e(TAG, Integer.toString(currentRow));
                        try {
                            if (unlockRow(currentRow)) {
                                completedEntries--;
                                disableContiuar();
                                clearCheckBoxes(currentRow, true);
                                successUnlock = true;
                            }
                            successUnlock = false;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        if (!duiconfirmed) {
                            createDialogToConfirmDui(getString(R.string.insertID), 1);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNoButtonForTwoButtonDialogClickedX() {
                switch (noIndex) {
                    case 1:
                        //Button bt = (Button)findViewById(R.id.aceptar_btn);
                        //bt.setFocusable(true);
                        //bt.requestFocus();
                        //setButtonColorGreen(bt);
                        //hideCambiosColumn();
                        //findViewById(R.id.cambioView).setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }
            }
        };
        return fragmentListener;
    }

    private View.OnKeyListener presingDoneKey() {
        return new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    v.clearFocus();
                    return true;
                }
                return false;
            }
        };
    }

    private View.OnClickListener undo(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                duiList_et.get(currentRow).setText(tempDui);
                nameList.get(currentRow).setText(tempName);
                partyList.get(currentRow).setText(tempParty);
                updateTableWithUsers();

//                unlockAllCheckBoxes();
//                clearCheckBoxes(currentRow, true);
//                clearCheckBoxes(currentRow, true);
//                utility.enableView(duiList_et.get(currentRow),false);
//                utility.setButtonColorRed(clearBtn);
//                utility.setButtonColorRed(substituteBtn);
//                utility.setButtonColorRed(modificarBtn);
//                utility.setButtonColorGreen(aceptarBtn);
            }
        };
    }

    private View.OnClickListener switchMembers(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockAllCheckBoxes();
//                if(switchBtn.getText().toString().contains("SUPPLENTES")){
//                    switchBtn.setText("PROPRIETARIOS");
//                    Tabheader.setText("PROPRIETARIOS");
//                    screen = PROPRIETARIOS;
//                    suplentes = miembrosList;
//                    miembrosList = proprietarios;
//                } else if (switchBtn.getText().toString().contains("PROPRIETARIOS")){
//                    switchBtn.setText("SUPPLENTES");
//                    Tabheader.setText("SUPPLENTES");
//                    screen = SUPLENTES;
//                    proprietarios = miembrosList;
//                    miembrosList = suplentes;
//                }

                //If button text == Supplentes
                //  switch list to supplentes
                //else if button text == Proprietarios
                //  switch list to proprietarios

                switch (screen) {
                    case PROPRIETARIOS:
                        updateTableWithUsers();
                        break;
                    case SUPLENTES:
                        updateTableWithUsers();
                        break;
                    case SUMMARY:
                        if (findMembersPresent() >= Min_Number_Members_Present) {
                            completeInstallation();
                        } else {
                            closeApplication();
                        }

                        break;
                    case INELECTION:
                        miembrosList = suplentes;
                        fillTableWithUsers();
                        screen = SUPLENTES;
                        Button iniciar = ((Button)findViewById(R.id.modificar_btn));
                        iniciar.setText("INICIAR");
                        utility.setButtonColorGreen(iniciar);
                        ((TextView) findViewById(R.id.tableHeader)).setText(getResources().getText(R.string.suplentes));
                        findViewById(R.id.tableHeader).setBackgroundColor(getResources().getColor(R.color.suplentes));
                        break;
                }

                clearCheckBoxes(currentRow, true);
                clearCheckBoxes(currentRow, true);
                utility.enableView(duiList_et.get(currentRow),false);
                utility.setButtonColorRed(clearBtn);
                utility.setButtonColorRed(substituteBtn);
                utility.setButtonColorRed(modificarBtn);
                utility.setButtonColorGreen(aceptarBtn);
            }
        };
    }
    private View.OnClickListener shiftMembers(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shiftUpMembers();
                updateTableWithUsers();
                utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
            }
        };
    }

    //asdfasdf need to add to acetar listener ability to after name is entered
    //  enteris pressed then it shifts focus and input to party field
    private View.OnClickListener aceptarListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempDui = duiList_et.get(currentRow).getText().toString();
                tempName = nameList.get(currentRow).getText().toString();
                tempParty = partyList.get(currentRow).getText().toString();

                utility.setButtonColorRed(clearBtn);
                utility.setButtonColorRed(substituteBtn);
                utility.setButtonColorRed(modificarBtn);
                if(aceptarBtn.getText().toString().contains("GUARDAR")) {
                    if(isParty){
                        isParty = false;
                        partyList.get(currentRow).setEnabled(false);
                        partyList.get(currentRow).setFocusable(false);
                        utility.enableEditText(partyList.get(currentRow), false);
                        for(int i=0; i < partyList.size() ; i++){
                            Log.e("party list " , partyList.get(i).getText().toString());
                            Log.e("party id " , Integer.toString(partyList.get(i).getId()));
                            Log.e("dui list " , duiList_et.get(i).getText().toString());
                            Log.e("dui id " , Integer.toString(duiList_et.get(i).getId()));
                            Log.e("name list " , nameList.get(i).getText().toString());
                            Log.e("name id " , Integer.toString(nameList.get(i).getId()));
                        }
                        miembrosList.get(currentRow).setDUI(duiList_et.get(currentRow).getText().toString());
                        miembrosList.get(currentRow).setName(nameList.get(currentRow).getText().toString());
                        miembrosList.get(currentRow).setParty(partyList.get(currentRow).getText().toString());
                        utility.setButtonColorRed(shiftBtn);
                        for(User member : miembrosList){
                            if(stepOne){
                                stepTwo = true;
                            }
                            if(member.getName().equals("")){
                                stepOne = true;
                            }
                        }
                        stepOne = false;
                        if(stepTwo) {
                            stepTwo = false;
//                            utility.setButtonColorGreen(shiftBtn); toggle
                            utility.setButtonColorRed(shiftBtn);
                        }
//                        utility.setButtonColorGreen(switchBtn);
                        utility.setButtonColorGreen(aceptarBtn);

                        clearCheckBoxes(currentRow,true);
                        unlockAllCheckBoxes();
                        updateTableWithUsers();
                        aceptarBtn.setText("ACEPTAR");
                    }else if(isName){
                        tempParty = partyList.get(currentRow).getText().toString();
                        isName = false;
                        isParty = true;
                        nameList.get(currentRow).setEnabled(false);
                        nameList.get(currentRow).setFocusable(false);
                        partyList.get(currentRow).setEnabled(true);
                        partyList.get(currentRow).setFocusable(true);
                        utility.enableEditText(partyList.get(currentRow), true);
                        partyList.get(currentRow).setText("");
                        partyList.get(currentRow).requestFocus();
                        partyList.get(currentRow).performClick();
                        partyList.get(currentRow).setSelection(partyList.get(currentRow).getText().length());
                        aceptarBtn.setText("GUARDAR");
                    }else {
                        tempName = nameList.get(currentRow).getText().toString();
                        isName = true;
                        isParty = false;
                        duiList_et.get(currentRow).setEnabled(false);
                        duiList_et.get(currentRow).setFocusable(false);
                        nameList.get(currentRow).setEnabled(true);
                        nameList.get(currentRow).setFocusable(true);
//                    nameList.get(currentRow).setText("");
                        utility.enableEditText(nameList.get(currentRow), true);
                        nameList.get(currentRow).requestFocus();
                        nameList.get(currentRow).performClick();
                        nameList.get(currentRow).setSelection(nameList.get(currentRow).getText().length());
                        utility.setButtonColorRed(shiftBtn);
//                    utility.setButtonColorRed(switchBtn);
                        utility.setButtonColorGreen(aceptarBtn);
                        aceptarBtn.setText("GUARDAR");
                    }
                } else if(aceptarBtn.getText().toString().contains("ACEPTAR")) {
                        //Send names and close application
//                        sendMembers();
                        if(prop && !supp){
//                            switchMembers();
//                            switchBtn.setText("SUPPLENTES");
                            challengeHelper.createDialog("Confirm Proprietarios?", CONTINUAR);

//                            Tabheader.setText("SUPPLENTES");
//                            screen = SUPLENTES;
//                            proprietarios = miembrosList;
//                            miembrosList = suplentes;
//                            updateTableWithUsers();
//                            prop = false;
//                            supp = true;

                        }else if(!prop && supp) {
                            challengeHelper.createDialog("Update Mesa Officials?", ACEPTAR);
                        }
                }else if (aceptarBtn.getText().toString().contains("CERRAR")){
                    utility.setButtonColorRed(aceptarBtn);
                    try {
                        synchronized (this) {
                            wait(2000);
                        }
                    } catch (InterruptedException e) {

                    }

                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            }
        };
    }


    private View.OnClickListener modifyEntry() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempDui = duiList_et.get(currentRow).getText().toString();
                utility.enableView(selectedList.get(currentRow),false);
//                clearMember();
//                unlockRow(currentRow);
                duiList_et.get(currentRow).setEnabled(true);
                duiList_et.get(currentRow).setFocusable(true);
                utility.enableEditText(duiList_et.get(currentRow), true);
//                utility.enableEditText(nameList.get(currentRow), true);
//                duiList_et.get(currentRow).setText("");
                duiList_et.get(currentRow).requestFocus();
                duiList_et.get(currentRow).setSelection(duiList_et.get(currentRow).getText().length());
                utility.setButtonColorGreen(aceptarBtn);
                aceptarBtn.setText("GUARDAR");
                utility.setButtonColorRed(clearBtn);
                utility.setButtonColorRed(substituteBtn);
                utility.setButtonColorRed(modificarBtn);
            }
        };
    }

    private View.OnClickListener clearEntry(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMember();
            }
        };
    }

    private void sendMembers(){
        String title;
        String url;
        String identity;
        String name;
        String party;

        for(User member : proprietarios){
            title = member.getTitle();
            identity = member.getDUI();
            name = member.getName().replaceAll(" ", "%20");
//            name = member.getName();
            party = member.getParty();
            url = Consts.MER_MANAGEMENT +"Prop/"+ "'"+title+"'" + "&" + "'"+name+"'" + "&" + "'"+identity+"'" + "&" + "'"+party+"'" + "&"+ thisjrv;
            HttpPost updateOficial = new HttpPost(url);
            WebServiceRestTask task = new WebServiceRestTask(0);
            task.execute(updateOficial);
            Log.e("URL for send : " , url);
//            HttpPost sendMembersRequest = new HttpPost(url);
//            sendMembersRequest.setHeader("content-type","application/json");
//            StringEntity entity = new StringEntity(fetch);
//            sendMembersRequest.setEntity(entity);
//            WebServiceRestTask task = new WebServiceRestTask(0);
//            task.setResponseDataCallback(MerManagerActivity.this);
//            task.execute(sendMembersRequest);
        }

        for(User member : suplentes){
            title = member.getTitle();
            identity = member.getDUI();
            name = member.getName().replaceAll(" ", "%20");
//            name = member.getName();
            party = member.getParty();
            url = Consts.MER_MANAGEMENT +"Supp/"+ "'"+title+"'" + "&" + "'"+name+"'" + "&" + "'"+identity+"'" + "&" + "'"+party+"'" + "&"+ thisjrv;
            HttpPost updateOficial = new HttpPost(url);
            WebServiceRestTask task = new WebServiceRestTask(0);
            task.execute(updateOficial);
            Log.e("URL for send : " , url);
        }

    }
    private void clearMember(){
    }

    private View.OnClickListener startEntry(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear and open check boxes
//                utility.setButtonColorRed(switchBtn);
                if(((Button)v).getText().equals("PROXIMO")){
                    updateMemberInList(currentRow);

                    if(!isLast) {
                        selectedList.get(currentRow + 1).setBackgroundResource(R.drawable.cbselected);
                    }
                    selectedList.get(currentRow).setBackgroundColor(Color.TRANSPARENT);
                }else{
                    selectedList.get(0).setBackgroundResource(R.drawable.cbselected);
                    ((Button)v).setText("PROXIMO");
                }

                isEntryDone();
//                utility.setButtonColorRed(switchBtn);
                utility.setButtonColorRed((Button) v);
            }
        };
    }

    /*------------------------- dialog and dui verification methods ----------------------------*/

    private void closeApplication(){
        String msg = "Comuníquese con el TSE para Resolver Esta Situación" +
                " E Iniciar de Nuevo Cuando Se Haya Resuelto." +
                " Esta Aplicación se Va Cerrar en ";
        createDialog(msg,-1);

    }

    public void createDialog(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        twoBtnDialogFragment = new TwoButtonDialogFragment();
        twoBtnDialogFragment.setOnButtonsClickedListenerOne(dialogFragmentListener());
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Si");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        if (yesIndex == 0) bndl.putString("invisible", "noButtons");
        else if(yesIndex==-1)bndl.putString("invisible", "closeApplication");
        else bndl.putString("invisible", "visible");
        twoBtnDialogFragment.setArguments(bndl);
        twoBtnDialogFragment.show(fm, "new triage dialog");
    }

    public void createDialogToConfirmDui(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        dialogToConfirmDui = new DialogToConfirmDui();
        dialogToConfirmDui.setOnButtonsClickedListenerOne(confirmDui());
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        bndl.putString("invisible", "invisible");
        dialogToConfirmDui.setArguments(bndl);
        dialogToConfirmDui.show(fm, "new triage dialog");
    }

    @Override
    public void onRequestDataSuccess(String response) {
        /** onRequestDataSuccess, when the dui has been confirmed, the cambios column is loaded. */
        // Process the response data (here we just display it)
        System.out.println("*****************************************************************");
        Intent search;
        search = new Intent(MerManagerActivity.this, Consts.PAPALETASACT);
        search.putExtras(b);
        startActivity(search);
        finish();
    }

    @Override
    public void onRequestDataError(Exception error) {
        /** onRequestDataError, when the dui is not confirmed, then the user is notified and
         * the continue button is enabled.*/
        utility.createCustomToast(getString(R.string.updateError));
        enableContinuar();
//        twoBtnDialogFragment.dismiss();
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }

    public void startTimeWheel(int yesIndex) {
        TimeWheelFragment newTimeWheelFragment = new TimeWheelFragment();
        Bundle bndl = new Bundle();
        bndl.putInt("yesIndex", yesIndex);
        newTimeWheelFragment.setArguments(bndl);
        newTimeWheelFragment.show(getFragmentManager(), "timeWheelFragment");
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    @Override
    public void setCurrentTime(int index, int currentItem, int currentMin, int amPmInt) {
        TextView horaOne = (TextView) findViewById(R.id.textViewHour);
        TextView horaTwo = (TextView) findViewById(R.id.textViewMin);
        TextView horaFinal = (TextView) findViewById(R.id.textViewHourHF);
        TextView horaFinalConfirm = (TextView) findViewById(R.id.textViewMinHF);
        String ampmString = "";
        Log.e("ROLCALL", "INDEX: " + index);
        switch (index) {
            case 1:
                if (amPmInt == 1) {
                    ampmString = "PM";
                } else if (amPmInt == 0) {
                    ampmString = "AM";
                }
                horaOne.setText(pad(currentItem) + " : " + pad(currentMin) + " " + ampmString);
                horaOne.setTypeface(Typeface.DEFAULT_BOLD);
                timeFirst = horaOne.getText().toString();
                horaOne.setText("****");
                break;
            case 2:
                if (amPmInt == 1) {
                    ampmString = "PM";
                } else if (amPmInt == 0) {
                    ampmString = "AM";
                }
                horaTwo.setText(pad(currentItem) + " : " + pad(currentMin) + " " + ampmString);
                horaTwo.setTypeface(Typeface.DEFAULT_BOLD);
                //try here:
                if (!horaOne.getText().toString().equals("")
                        && timeFirst.equals(horaTwo.getText().toString())) {
                    clearCheckBoxes(nextAvailableEntry(), true);
                    horaOne.setText(timeFirst);
                    horaOne.setEnabled(false);
                    horaTwo.setEnabled(false);
//                    setButtonColorGreen((Button)findViewById(R.id.entrar_btn));
                    findViewById(R.id.linearLayout4).setBackgroundColor(getResources().getColor(R.color.white));
                    findViewById(R.id.textView41).setBackgroundColor(getResources().getColor(R.color.white));
                    findViewById(R.id.textView415).setBackgroundColor(getResources().getColor(R.color.white));
                    //loadCambiosColumn();
                    //HoraCalculada.setHint(hora_de_conclucion_tv.getText().toString());
                } else {
                    utility.createCustomToast("La Hora Reingresada No Coincide", "Con La Hora Ingresada");
                    horaTwo.setText("");
                }
                break;
            case 3:
                if (amPmInt == 1) {
                    ampmString = "PM";
                } else if (amPmInt == 0) {
                    ampmString = "AM";
                }
                horaFinal.setText(pad(currentItem) + " : "
                        + pad(currentMin) + " " + ampmString);
                horaFinal.setTypeface(Typeface.DEFAULT_BOLD);
                timeFinal = horaFinal.getText().toString();
                horaFinal.setText("****");
                break;
            case 4:
                if (amPmInt == 1) {
                    ampmString = "PM";
                } else if (amPmInt == 0) {
                    ampmString = "AM";
                }
                horaFinalConfirm.setText(pad(currentItem) + " : "
                        + pad(currentMin) + " " + ampmString);
                horaFinalConfirm.setTypeface(Typeface.DEFAULT_BOLD);
                //try here:
                if (!horaFinal.getText().toString().equals("")
                        && timeFinal
                        .equals(horaFinalConfirm.getText()
                                .toString())
                        && !horaTwo.getText().toString().equals("")) {
                    //TODO:

                    horaFinal.setText(timeFinal);
                    horaFinal.setEnabled(false);
                    horaFinalConfirm.setEnabled(false);
                    unlockAllEntries();
                    // loadUserDb();
                    //fillTableWithUsers();

                    //todo unlock the first empty column.

                    //HoraCalculada.setHint(hora_de_conclucion_tv.getText().toString());
                } else if (!timeFinal
                        .equals(horaFinalConfirm.getText()
                                .toString())) {
                    utility.createCustomToast("La Hora Reingresada No Coincide", "Con La Hora Ingresada");
                    horaFinalConfirm.setText("");
                } else {
                    utility.createCustomToast("Ingrese la Hora Inicial", " de Escrutino");
                    horaFinalConfirm.setText("");
                }

                break;

            default:
                break;
        }
    }

    public int getNextEmptyRow() {
        int completeRow = -1;
//        for (int row = 0; row < presenteSiList.size(); row++) {
//            if (!presenteSiList.get(row).isChecked() && !presenteNoList.get(row).isChecked())
//                return row;
//        }
        return completeRow;
    }

    public int findMembersPresent() {
        int attendees = 0;
        for (int row = 0; row < miembrosList.size() - 1; row++) {
            if (selectedList.get(row).isChecked())
                attendees++;
        }
        return attendees;
    }

    /*----------------------------------------------------------------------------------------------*/
    public void completeInstallation() {
        saveToDb();
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        String listOfMembers = compileAttendeeMemberData();
        Log.e("ROLLCALL", listOfMembers);
        sendToWebServices(listOfMembers, Consts.NEWMEMBERS);
    }

    public void sendToWebServices(String jsonData, String url) {
        try {
            int ws_task_number = 0;
            HttpPost searchRequest = new HttpPost(url);
            searchRequest.setHeader("content-type", "application/json");
            StringEntity entity = new StringEntity(jsonData);
            searchRequest.setEntity(entity);
            WebServiceRestTask task = new WebServiceRestTask(ws_task_number);
            task.setResponseDataCallback(MerManagerActivity.this);
            task.execute(searchRequest);
        } catch (Exception e) {
            Log.e("EXIT ACT", e.getMessage());
        }
    }

    private String compileAttendeeMemberData() {
        DatabaseAdapterParlacen databaseAdapter = new DatabaseAdapterParlacen(MerManagerActivity.this);
        databaseAdapter.open();
        //List<User> MesaOfficials = databaseAdapter.getAttendeeMembers();
        List<User> MesaOfficials = new ArrayList<>();
        int title_Order = 0;
        for (User official :  miembrosList) {
            databaseAdapter.insertActaAttendees(official);
            databaseAdapter.insertActaAttendeesInOrder(official,title_Order);
            if (official.isPresent()) {
                official.setJRV(utility.loadPreferencesString(getResources().getString(R.string.jrvNumber)));
                MesaOfficials.add(official);
            }
            title_Order++;
        }
        databaseAdapter.close();
        Gson gson = new Gson();
        // STORE MESA OFFICIALS
        return gson.toJson(MesaOfficials);
    }

    private void moveEntriesUp() {
        for (int i = 0; i < miembrosList.size(); i++) {
            if (!miembrosList.get(i).isPresent()) {
                for (int j = i; j < miembrosList.size(); j++) {
                    //swap:
                    if (miembrosList.get(j).isPresent()) {
                        String title1 = miembrosList.get(j).getTitle();
                        String title2 = miembrosList.get(i).getTitle();
                        Collections.swap(miembrosList, i, j);
                        miembrosList.get(j).setTitle(title1);
                        miembrosList.get(i).setTitle(title2);
                        break;
                    }
                }
            }
        }
    }

    private void moveSuplentesUp() {
        HashMap<Integer, Integer> index = new HashMap<>();
        for (int i = 0; i < miembrosList.size(); i++) {
            if (!miembrosList.get(i).isPresent() && miembrosList.get(i).isProprietario()) {
                Log.e("THIS MESS: ", " I:" + String.valueOf(i) + " size of Array: " + String.valueOf(miembrosList.size()));
                index.put(i, i + 1);
            }
        }
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int i = (int) pair.getKey();
            int j = (int) pair.getValue();
            String title1 = miembrosList.get(j).getTitle();
            String title2 = miembrosList.get(i).getTitle();
            Collections.swap(miembrosList, i, j);
            miembrosList.get(j).setTitle(title1);
            miembrosList.get(i).setTitle(title2);
        }

        ArrayList<User> tempMiembrosList = new ArrayList<>();
        for (int i = 0; i < miembrosList.size(); i++) {
            if (i % 2 == 0) {
                Log.e("MODULO DIV ", " I:" + String.valueOf(i) + " size of Array: " + String.valueOf(miembrosList.size()));
                tempMiembrosList.add(miembrosList.get(i));
            }
        }
        miembrosList = tempMiembrosList;
    }


    /*-----------------------------------------------------------------------------------------*/
    private class CustomTextWatcher implements TextWatcher {
        private CheckBox presentCheckBox;
        private EditText nameEditText;

        public CustomTextWatcher(EditText name_et, CheckBox present_cb) {
            presentCheckBox = present_cb;
            nameEditText = name_et;
        }


        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (s.length() > 10) {
                Log.e("CHAR", String.valueOf(s.charAt(8)));
                nameEditText.requestFocus();
            }
        }

        public void afterTextChanged(Editable s) {
        }
    }

    private class DuiFormatTextWatcher implements TextWatcher {
        private EditText mEditText;
        private Button mButton;
        private CheckBox presentCheckBox;
        private EditText nameEditText;
        private EditText partyEditText;
        int realCount = 0;
        boolean replace = false, replace2 = false;

        public DuiFormatTextWatcher(EditText name_et, CheckBox present_cb) {
            presentCheckBox = present_cb;
            nameEditText = name_et;
        }

        public DuiFormatTextWatcher(EditText party_et, CheckBox present_cb, int i) {
            presentCheckBox = present_cb;
            partyEditText = party_et;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (Consts.LOCALE.contains("ELSA")) {
                if (s.length() == 9 && s.charAt(8) != '-') {
                    replace = true;
                }

                if (s.length() > 9) {
                    utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                } else {
                    utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                }
            } else if (Consts.LOCALE.contains("HON")){
                if(s.length()==5 && s.charAt(4) !='-'){
                    replace =true;
                }
                if(s.length()==10 && s.charAt(9)!='-'){
                    replace2=true;
                }

                if (s.length() > 14) {
                    utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                }else{
                    utility.setButtonColorRed((Button) findViewById(R.id.clear_btn));
                }
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (Consts.LOCALE.contains("ELSA")) {
                if (replace && realCount == 0) {
                    realCount++;
                    char d = s.charAt(8);
                    s.replace(8, 9, "-" + d);
                    replace = false;
                }
                realCount = 0;
            } else if (Consts.LOCALE.contains("HON")){
                if(replace && realCount==0){
                    realCount++;
                    char d = s.charAt(4);
                    s.replace(4,5,"-"+d);
                    replace=false;
                }else if(replace2 && realCount==0){
                    realCount++;
                    char d = s.charAt(9);
                    s.replace(9,10,"-"+d);
                    replace2=false;
                }
                realCount=0;
            }

        }
    }

    private boolean hasValues(EditText et) {
        Log.e("VALUES: ", et.getText().toString());
        Pattern p = Pattern.compile("\\w.*");
        Matcher m = p.matcher(et.getText().toString());
        return m.matches();
    }

    private void openBox(EditText et) {
        utility.enableEditText(et, true);
        et.requestFocus();
    }

    private void closeBox(EditText et) {
        utility.enableEditText(et, false);
    }


}
