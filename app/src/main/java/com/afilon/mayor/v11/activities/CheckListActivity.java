package com.afilon.mayor.v11.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.drawSignature;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.PreferentialPartyVotes;
import com.afilon.mayor.v11.model.PresidenteStaff;
import com.afilon.mayor.v11.model.User;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.ChallengeHelper.OnApprove;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.google.gson.Gson;


@SuppressLint("DefaultLocale")
public class CheckListActivity extends AfilonActivity {

	private static final String CLASS_TAG = "CheckListActivity";

	private VotingCenter vc;
	private Escrudata escrudata;
	private Utilities ah;
	private Button continuarBtn, proximoBtn;
	private int count;
	private int countSecondColumn;

    private DatabaseAdapterParlacen db_adapter;
    private ChallengeHelper challengeHelper;

	private LinkedHashMap<String, String> signaturesMap;

    private String[] SignaturesSealItems;
    private String[] actaSealItems;
    private String[] ActaQualityItems;
    private String SEAL = "STAMP";
    private String NO_DEFECT = "WITHOUTDEFECTS";
    LinkedHashMap<Integer, PresidenteStaff> signaturesSeals;// = new LinkedHashMap<>();
    LinkedHashMap<Integer, PresidenteStaff> actaSeals;
    LinkedHashMap<Integer, PresidenteStaff> actaQuality;// = new LinkedHashMap<>();
    private ArrayList<User> users, userNames;
    private final static String SIGNATURES_COMPLETE = "NoSignaturesLeft";
    private final static String ACTA_SEALS_COMPLETE ="NoSealsLeft";
    private final static String ACTA_QUALITY_COMPLETE = "NoQualityLeft";
    private final static String AFFIRMATIVE = "_affirmative";
    private final static String NEGATIVE = "_negative";
    private final static String NAME = "_name";
    private final static String NO_ITEM_FOUND="NoItemFound";
    private final int SIGNATURES = 1;
    private final int SEALS =2;
    private final int QUALITY = 0;
    private String currentItem = "NoItemSelected";
    private static int REINICIAR = 1;
    private static int ENTRAR = 2;
    private static int CONTINUAR = 3;
    private boolean signatureStage = true; // we start at the signature table
    private boolean sealStage = false, isSello = false;
    private boolean permission = false, proximoOff = false, storageChange;
    private boolean wasSeal = false, secretarioPresent = false, presidentePresent = false, vocalOnePresent = false, vocalTwoPresent = false, vocalThreePresent = false;
    private ArrayList<User> vcOfficial = new ArrayList<>();
    private drawSignature.drawSignatureListener sigList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ah = new Utilities(CheckListActivity.this);
		ah.tabletConfiguration(Build.MODEL,this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_checklist2);

		// // // Trap unexpected error
		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
				CheckListActivity.this));

		Log.i("CLASS NAME : ", CLASS_TAG);

        db_adapter = new DatabaseAdapterParlacen(this);
        //-----------------------------------------------------------------------------------------
        challengeHelper = new ChallengeHelper(this);
        // REGISTER ROUTINES TO CHALLENGE HELPER:
        challengeHelper.addRoutine(-1,onNoDefault);
        challengeHelper.addRoutine(REINICIAR, reiniciar);
        challengeHelper.addRoutine(CONTINUAR, continuar);
        challengeHelper.addRoutine(ENTRAR,enter);
        challengeHelper.addCustomKeyBoard(R.id.keyboardview);// FOR DUI CONFIRMATION ONLY
        challengeHelper.setTools(ah,db_adapter); //TOOLS ARE FOR DUI CONFIRMATION ONLY
        //----------------------------------------------------------------------------------------
        db_adapter.open();
//        signaturesSeals = db_adapter.getCheckListItemsFromCatalog(SIGNATURES);
        signaturesSeals = db_adapter.getCheckListItemsFromAttendees();
        actaSeals = db_adapter.getCheckListItemsFromCatalog(SEALS);
        actaQuality = db_adapter.getCheckListItemsFromCatalog(QUALITY);
        users = db_adapter.getActaAttendees();
        userNames = db_adapter.getMERMembers();
//        vcOfficial = db_adapter.getVCOfficial(String.valueOf(true),vc.getJRV());


        Bundle b = getIntent().getExtras();
        if (b != null) {
            escrudata = b.getParcelable("com.afilon.tse.escrudata");
            vc = b.getParcelable("com.afilon.tse.votingcenter");
        }
        ah.saveCurrentScreen(this.getClass(),b);

        vcOfficial = db_adapter.getVCOfficial(String.valueOf(true), vc.getJRV());


        permission = (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED)&&(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED);
        storageChange = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED);

        if(Consts.LOCALE.contains("HON")){
            calculateErrorsHon();
        }else calculateErrors();
        loadHeader(vc);
//----------------------------------- LOAD dynamically ---------------------------------------------
        TableLayout tableSignatures = (TableLayout) findViewById(R.id.signatures);
//        TableLayout tableSeals = (TableLayout) findViewById(R.id.seals);
        TableLayout tableSeals = (TableLayout) findViewById(R.id.seals2);
        TableLayout tableActaQuality = (TableLayout) findViewById(R.id.acta_quality);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1);
        SignaturesSealItems = createTable(signaturesSeals, tableSignatures, rowParams);
        ActaQualityItems = createTableQuality(actaQuality, tableActaQuality, rowParams);
        actaSealItems = createTable(actaSeals,tableSeals,rowParams);
        // set current Item to be the first in the list of signatures & we select the signature block:
        currentItem = SignaturesSealItems[0];
//        findViewById(R.id.signatures).setBackgroundColor(getResources().getColor(R.color.blockSelected));
//        findViewById(R.id.seals).setBackgroundColor(getResources().getColor(R.color.blockSelected));
//--------------------------------------------------------------------------------------------------
        signaturesMap = new LinkedHashMap<String, String>();
        count = 0;
        countSecondColumn = 0;
        //disable continuar:
//-------------------------- SET BUTTONS -----------------------------------------------------------
        continuarBtn = (Button) findViewById(R.id.continuar_btn);
        proximoBtn = (Button) findViewById(R.id.proximo_btn);
        ah.setButtonColorRed(continuarBtn);
        continuarBtn.setOnClickListener(contnuarBtn());
        proximoBtn.setOnClickListener(pressProximo());
        findViewById(R.id.entrar_btn).setOnClickListener(pressEnter());
        findViewById(R.id.restart_btn).setOnClickListener(pressReiniciar());
        ah.setButtonColorGreen(proximoBtn);
        ah.setButtonColorRed((Button) findViewById(R.id.entrar_btn));
//        ah.setButtonColorAmber((Button) findViewById(R.id.restart_btn));
        ah.setButtonColorRed((Button) findViewById(R.id.restart_btn));
//------------------------- LOCK ABSENT MEMBERS ----------------------------------------------------
        lockAbsentMembers();
//        processNextItem();

        sigList = new drawSignature.drawSignatureListener() {
            @Override
            public void onContinueClicked() {
                enterRoutine();
            }

            @Override
            public void onCancleClicked() {
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
//        reiniciarRoutine();

        permission = (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED);
        if(permission){
            permission = (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }

//        if(storageChange!=(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
//            storageChange = (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
//        }
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

    @Override
    public void onBackPressed() {

        //Log.d("Preferential Vote Activity", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

//--------------------------------------------------------------------------------------------------
    // DISPLAY METHODS:

    private void loadHeader(VotingCenter vc) {
        TextView votecenter_tv = (TextView) findViewById(R.id.vote_center);
        TextView municipio_tv = (TextView) findViewById(R.id.textView13);
        TextView departamento_tv = (TextView) findViewById(R.id.textView15);
        TextView barcode_tv = (TextView) findViewById(R.id.textView23);
        TextView jvr_tv = (TextView) findViewById(R.id.textView25);
        votecenter_tv.setText(vc.getVoteCenterString());
        municipio_tv.setText(vc.getMunicipioString());
        departamento_tv.setText(vc.getDepartamentoString());
        barcode_tv.setText(ah.loadPreferencesString("barcodeSaved"));
        jvr_tv.setText(vc.getJrvString());
    }

    private String[] createTable(LinkedHashMap<Integer, PresidenteStaff> listOfItems, TableLayout table, TableRow.LayoutParams rowParams) {
        String[] ItemKeys = new String[listOfItems.size()];
        int i = 0;
        for (Map.Entry<Integer, PresidenteStaff> entry : listOfItems.entrySet()) {
            PresidenteStaff item = entry.getValue();
            View v = new View(this);
            v.setLayoutParams(rowParams);
            v.setBackgroundColor(getResources().getColor(R.color.divisor));
            table.addView(createRow(item.getStaffID(), item.getDescription()));
            table.addView(v);
            ItemKeys[i] = item.getStaffID();
            i++;
        }
        return ItemKeys;
    }

    private String[] createTableQuality(LinkedHashMap<Integer, PresidenteStaff> listOfItems, TableLayout table, TableRow.LayoutParams rowParams) {
        String[] ItemKeys = new String[listOfItems.size()];
        int i = 0;
        for (Map.Entry<Integer, PresidenteStaff> entry : listOfItems.entrySet()) {
            PresidenteStaff item = entry.getValue();
            View v = new View(this);
            v.setLayoutParams(rowParams);
            v.setBackgroundColor(getResources().getColor(R.color.divisor));
            table.addView(createRowQuality(item.getStaffID(), item.getDescription()));
            table.addView(v);
            ItemKeys[i] = item.getStaffID();
            i++;
        }
        return ItemKeys;
    }

    private TableRow createRow(String item, String description) {
        TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.check_item, null);
        tr.setId(getRowViewId(item));
        ((TextView) tr.findViewById(R.id.item_name)).setText(description);
        View cbAffirmative = tr.findViewById(R.id.affirmative_box);
        View cbNegative = tr.findViewById(R.id.negative_box);

        cbAffirmative.setEnabled(false);
        cbAffirmative.setFocusable(false);
        cbAffirmative.setOnClickListener(checkBoxListener());
        cbAffirmative.setId(getAffirmativeBoxID(item));

        cbNegative.setEnabled(false);
        cbNegative.setFocusable(false);
        cbNegative.setOnClickListener(checkBoxListener());
        cbNegative.setId(getNegativeBoxID(item));
        tr.findViewById(R.id.item_name).setId((item + NAME).hashCode() & 0xfffffff); //TODO: ID IS CURRENTLY UNUSED
        return tr;
    }

    private TableRow createRowQuality(String item, String description) {
        TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.check_item, null);
        tr.setId(getRowViewId(item));
        ((TextView) tr.findViewById(R.id.item_name)).setText(description);
        View cbAffirmative = tr.findViewById(R.id.affirmative_box);
        View cbNegative = tr.findViewById(R.id.negative_box);

        cbAffirmative.setEnabled(false);
        cbAffirmative.setFocusable(false);
        cbAffirmative.setOnClickListener(checkBoxListener());
        cbAffirmative.setId(getAffirmativeBoxID(item));

        cbNegative.setEnabled(false);
        cbNegative.setFocusable(false);
        cbNegative.setOnClickListener(checkBoxListener());
        cbNegative.setId(getNegativeBoxID(item));
        tr.findViewById(R.id.item_name).setId((item + NAME).hashCode() & 0xfffffff); //TODO: ID IS CURRENTLY UNUSED
        return tr;
    }
   // ID PARSING:

    private int getAffirmativeBoxID(String item) {
        return (item + AFFIRMATIVE).hashCode() & 0xfffffff;
    }

    private int getNegativeBoxID(String item) {
        return (item + NEGATIVE).hashCode() & 0xfffffff;
    }

    private int getRowViewId(String item) {
        return item.hashCode() & 0xfffffff;
    }

    private String getItemFromID(int id) {
        //TODO: reconstruct the item name more efficiently - consider hash table
        for (String item : SignaturesSealItems) {
            if (id == getAffirmativeBoxID(item) || id == getNegativeBoxID(item)) {
                return item;
            }
        }
        for (String item : actaSealItems) {
            if (id == getAffirmativeBoxID(item) || id == getNegativeBoxID(item)) {
                return item;
            }
        }
        for (String item : ActaQualityItems) {
            if (id == getAffirmativeBoxID(item) || id == getNegativeBoxID(item)) {
                return item;
            }
        }
        return NO_ITEM_FOUND;//TODO: REMOVE HARDCODED STRING
    }

    // ROW BEHAVIOURS:

    private void unlockFirstSignature() {
//		scroll to top
        ((ScrollView) findViewById(R.id.sv_signatures)).smoothScrollTo(0, 0);
//        findViewById(R.id.signatures).setBackgroundColor(getResources().getColor(R.color.blockSelected));
//        findViewById(R.id.seals).setBackgroundColor(getResources().getColor(R.color.blockSelected));
        findViewById(R.id.acta_quality).setBackgroundColor(getResources().getColor(R.color.transparent));
        currentItem = SignaturesSealItems[0];
        unLockItem(true, currentItem); // todo cover on try, guarantee that first item exists
    }

    private void unLockFirstSeal(){

        ((ScrollView) findViewById(R.id.sv_signatures2)).smoothScrollTo(0, 0);
//        findViewById(R.id.signatures).setBackgroundColor(getResources().getColor(R.color.blockSelected));
        findViewById(R.id.acta_quality).setBackgroundColor(getResources().getColor(R.color.transparent));
        currentItem = actaSealItems[0];
        unLockItem(true, currentItem); // todo cover on try, guarantee that first item exists
    }

    private void unlockFirstActaQuality() {
        //scroll to top:
        ((ScrollView) findViewById(R.id.sv_signatures)).smoothScrollTo(0, 0);
        findViewById(R.id.signatures).setBackgroundColor(getResources().getColor(R.color.transparent));
//        findViewById(R.id.seals).setBackgroundColor(getResources().getColor(R.color.transparent));
        findViewById(R.id.seals2).setBackgroundColor(getResources().getColor(R.color.transparent));
//        findViewById(R.id.acta_quality).setBackgroundColor(getResources().getColor(R.color.blockSelected));
        currentItem = ActaQualityItems[0];//todo guarantee actaquality is not empty
        unLockItem(true, currentItem);
    }

    private void unLockItem(boolean enable, String item) {
        // R.color.checkItemSelected is color for unlocked and selected
        // R.color.transparent is color for locked and unselected
        int colorID;
        if (enable) {
            colorID = R.color.checkItemSelected;
        } else colorID = R.color.transparent;
        Log.e("ITEM TO UNLOCK", item + " " + String.valueOf(enable));
        TableRow tr = (TableRow) findViewById(getRowViewId(item));
        tr.setBackgroundColor(getResources().getColor(colorID));
        tr.findViewById(getAffirmativeBoxID(item)).setEnabled(enable); //affirmative checkbox
        tr.findViewById(getNegativeBoxID(item)).setEnabled(enable); // negative checkbox
        tr.findViewById(getAffirmativeBoxID(item)).setFocusable(enable); //affirmative checkbox
        tr.findViewById(getNegativeBoxID(item)).setFocusable(enable); // negative checkbox
        tr.findViewById(getAffirmativeBoxID(item)).setBackgroundResource(R.drawable.cbselected); //affirmative checkbox
        tr.findViewById(getNegativeBoxID(item)).setBackgroundResource(R.drawable.cbselected); // negative checkbox
    }

    private void clearCheckBoxes(String item, boolean enable) {
        /** clearCheckBoxes(int row) this method clears the check boxes that correspond to
         *  the present check boxes and the firma check boxes of a given row */
        CheckBox cbAffirmative = ((CheckBox) findViewById(getAffirmativeBoxID(item)));
        CheckBox cbNegative = ((CheckBox) findViewById(getNegativeBoxID(item)));
        cbAffirmative.setChecked(false);
        cbNegative.setChecked(false);
        cbNegative.setEnabled(enable);
        cbAffirmative.setEnabled(enable);
        cbNegative.setFocusable(enable);
        cbAffirmative.setFocusable(enable);
        cbNegative.setBackgroundColor(Color.TRANSPARENT);
        cbAffirmative.setBackgroundColor(Color.TRANSPARENT);
    }

    private void resetAllRows() {
        PresidenteStaff currentStaff;
        for (String item : SignaturesSealItems) {
            clearCheckBoxes(item, false);
            currentStaff = signaturesSeals.get(item.hashCode());
            currentStaff.setCbOneSelected(false);
            currentStaff.setCbTwoSelected(false);
            findViewById(getRowViewId(item)).setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        for (String item : actaSealItems) {
            clearCheckBoxes(item, false);
            currentStaff = actaSeals.get(item.hashCode());
            currentStaff.setCbOneSelected(false);
            currentStaff.setCbTwoSelected(false);
            findViewById(getRowViewId(item)).setBackgroundColor(getResources().getColor(R.color.transparent));
        }

        for (String item : ActaQualityItems) {
            clearCheckBoxes(item, false);
            currentStaff = actaQuality.get(item.hashCode());
            currentStaff.setCbOneSelected(false);
            currentStaff.setCbTwoSelected(false);
            findViewById(getRowViewId(item)).setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        //unlockFirstSignature();
        signatureStage = true;
        presidentePresent = false;
        secretarioPresent = false;
        vocalOnePresent = false;
        vocalTwoPresent = false;
        vocalThreePresent = false;

    }

    private void lockAbsentMembers(){
        for(User user: users){
            if(!user.isPresent()){
                String item = user.getTitle();
                Log.i("CHCKLIST",item);
                Log.e("CHKLIST",item);
                int hash = item.hashCode();
                signaturesSeals.get(hash).setCbTwoSelected(true);
                int negativeBoxId = getNegativeBoxID(item);
                ((CheckBox)findViewById(negativeBoxId)).setChecked(true);
            }
        }
    }

    // TOOLS

    private String nextAvailbleSignature() {
        for (String item : SignaturesSealItems) {
            PresidenteStaff currentItem = signaturesSeals.get(item.hashCode());
            if (currentItem.isCbOneSelected() || currentItem.isCbTwoSelected()) {
                continue;
            }
            return item;
        }
        return SIGNATURES_COMPLETE;
    }

    private String nextAvailableSeal(){

        for (String item : actaSealItems) {
            PresidenteStaff currentItem = actaSeals.get(item.hashCode());
            if (currentItem.isCbOneSelected() || currentItem.isCbTwoSelected()) {
                continue;
            }
            return item;
        }
        return  ACTA_SEALS_COMPLETE;
    }

    private String nextAvailbleQualityCheck() {
        for (String item : ActaQualityItems) {
            PresidenteStaff currentItem = actaQuality.get(item.hashCode());
            if (currentItem.isCbOneSelected() || currentItem.isCbTwoSelected()) {
                continue;
            }
            return item;
        }
        return ACTA_QUALITY_COMPLETE;
    }

    private int quantityOfSignatures() {
        int count = 0;
        for (String item : SignaturesSealItems) {
            PresidenteStaff currentItem = signaturesSeals.get(item.hashCode());
            if (currentItem.isCbOneSelected()) {
                count++;
            }
        }
        return count;
    }

    private int quanityOfQualityCheck() {
        int count = 0;
        for (String item : ActaQualityItems) {
            if (item.equals(NO_DEFECT)) { //TODO REMOVE HARDOCED ITEM
                continue;
            }
            PresidenteStaff currentItem = actaQuality.get(item.hashCode());
            if (currentItem.isCbOneSelected()) {
                count++;
            }
        }
        return count;
    }

    private void UpdateSignature(String item) {
        int ID = item.hashCode();
        Log.e("ITEM", item + "; ID COMPILED :" + (item + AFFIRMATIVE) + " : " + String.valueOf(getAffirmativeBoxID(item)));
        if(isSello == false) {
            signaturesSeals.get(ID).setCbOneSelected(((CheckBox) findViewById(getAffirmativeBoxID(item))).isChecked());
            signaturesSeals.get(ID).setCbTwoSelected(((CheckBox) findViewById(getNegativeBoxID(item))).isChecked());
        }
    }

    private void UpdateSeal(String item){
        int ID = item.hashCode();
        actaSeals.get(ID).setCbOneSelected(((CheckBox) findViewById(getAffirmativeBoxID(item))).isChecked());
        actaSeals.get(ID).setCbTwoSelected(((CheckBox) findViewById(getNegativeBoxID(item))).isChecked());
    }

    private void UpdateQuality(String item) {
        int ID = item.hashCode();
        actaQuality.get(ID).setCbOneSelected(((CheckBox) findViewById(getAffirmativeBoxID(item))).isChecked());
        actaQuality.get(ID).setCbTwoSelected(((CheckBox) findViewById(getNegativeBoxID(item))).isChecked());
    }

//------------   BUTTON CALLS  & ASSOCIATED ROUTINES -----------------------------------------------

    private OnClickListener pressReiniciar() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {

                challengeHelper.createDialog(getResources().getString(R.string.resetChecklist), REINICIAR);
                isSello = false;
                wasSeal = false;
                secretarioPresent = false;
                presidentePresent = false;
                vocalOnePresent = false;
                vocalTwoPresent = false;
                vocalThreePresent = false;
            }
        };
    }

    private OnClickListener pressProximo(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(proximoBtn.getText().equals("Ingresar")){
                    proximoBtn.setText("Proximo");
                    unlockFirstSignature();
                }
                    ah.setButtonColorRed(proximoBtn);
                    //Unlock next checkbox
//                    CheckBox cbAffirmative = (CheckBox) findViewById(getAffirmativeBoxID(currentItem));
//                    CheckBox cbNegative = (CheckBox) findViewById(getNegativeBoxID(currentItem));
//                    cbAffirmative.setBackgroundColor(Color.TRANSPARENT);
//                    cbNegative.setBackgroundColor(Color.TRANSPARENT);
                    processNextItem();
            }
        };
    }

    private OnClickListener pressEnter() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {

                ah.setButtonColorAmber((Button) findViewById(R.id.restart_btn));
//                for(String item : SignaturesSealItems ){
//                    ((CheckBox) findViewById(getAffirmativeBoxID(item))).setBackgroundColor(Color.TRANSPARENT);
//                    ((CheckBox) findViewById(getNegativeBoxID(item))).setBackgroundColor(Color.TRANSPARENT);
//                }
//                for(String item : actaSealItems ){
//                    ((CheckBox) findViewById(getAffirmativeBoxID(item))).setBackgroundColor(Color.TRANSPARENT);
//                    ((CheckBox) findViewById(getNegativeBoxID(item))).setBackgroundColor(Color.TRANSPARENT);
//                }
//                for(String item : ActaQualityItems ){
//                    ((CheckBox) findViewById(getAffirmativeBoxID(item))).setBackgroundColor(Color.TRANSPARENT);
//                    ((CheckBox) findViewById(getNegativeBoxID(item))).setBackgroundColor(Color.TRANSPARENT);
//                }

//				challengeHelper.createDialog("DESEA ENTRAR",ENTRAR);
                if(signatureStage && ((CheckBox) findViewById(getAffirmativeBoxID(currentItem))).isChecked()){
                    String Id="";

//                    ah.createCustomToast(currentItem);
                    //display dui challenge and ask for dui, pass along dui to confirm against
                    for(User user: users){
                        if(currentItem.contains("Secretario") || currentItem.contains("SECRETARIO")){
                            secretarioPresent = true;
                        }else if(currentItem.contains("Presidente") || currentItem.contains("PRESIDENTE")){
                            presidentePresent = true;
                        }else if(currentItem.contains("1")){
                            vocalOnePresent = true;
                        }else if(currentItem.contains("2")){
                            vocalTwoPresent = true;
                        }else if(currentItem.contains("3")){
                            vocalThreePresent = true;
                        }

                        if(currentItem.equals(user.getTitle())){
                            Id = user.getDUI();
//                            ah.createCustomToast(user.getTitle());
                        } else if(currentItem.contains("STAMP")){
                            isSello = true;
                        }
                    }

                    String message = "";

                    if(isSello == true){
                        if(currentItem.contains("STAMP2")){
                            message = getResources().getString(R.string.selloMessageP);
                            if(presidentePresent) {
//                                presidentePresent = false;
                                for (User user : users) {
                                    if (user.getTitle().equals("Presidente")) {
                                        Id = user.getDUI();
                                    }
                                }
                            }else {
                                message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion";
//                                if(Consts.LOCALE.contains("HON")){
//                                    for (User user : users) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n "+ user.getName();
//                                        }
//                                    }
//                                }else {
                                Id = vcOfficial.get(1).getDUI();
                                message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n"+ vcOfficial.get(1).getName();
//                                    for (User user : vcOfficial) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n"+ user.getName();
//                                        }
//                                    }
//                                }

                            }
                        }else {
                            message = getResources().getString(R.string.selloMessageS);
                            if(secretarioPresent) {
//                                secretarioPresent = false;
                                for (User user : users) {
                                    if (user.getTitle().equals("Secretario")) {
                                        Id = user.getDUI();
                                    }
                                }
                            }else {
                                message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion";
//                                if(Consts.LOCALE.contains("HON")){
//                                    for (User user : users) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n "+ user.getName();
//                                        }
//                                    }
//                                }else {
                                    Id = vcOfficial.get(0).getDUI();
                                    message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n"+ vcOfficial.get(0).getName();
//                                    for (User user : vcOfficial) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n "+ user.getName();
//                                        }
//                                    }
//                                }
                            }
                        }
                    } else {
                        message = "Entrar "+ getResources().getString(R.string.dui) +" de " + currentItem;
                    }

                    challengeHelper.createSingleDuiChallenge(message, ENTRAR, Id);// compare agains this id
                }else{
                    if(currentItem.contains("STAMP")) isSello = true;
                    String message = "";
                    String Id="";
                    if(isSello == true && ((CheckBox) findViewById(getAffirmativeBoxID(currentItem))).isChecked()){
                        wasSeal = true;
                        if(currentItem.contains("STAMP2")){
                            message = getResources().getString(R.string.selloMessageP);
                            if(presidentePresent) {
//                                presidentePresent = false;
                                for (User user : users) {
                                    if (user.getTitle().equals("Presidente")) {
                                        Id = user.getDUI();
                                    }
                                }
                            }else {
                                message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion";
//                                if(Consts.LOCALE.contains("HON")){
//                                    for (User user : users) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n "+ user.getName();
//                                        }
//                                    }
//                                }else {
                                Id = vcOfficial.get(1).getDUI();
                                message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n"+ vcOfficial.get(1).getName();
//                                    for (User user : vcOfficial) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n "+ user.getName();
//                                        }
//                                    }
//                                }
                            }
                        }else {
                            message = getResources().getString(R.string.selloMessageS);
                            if(secretarioPresent) {
//                                secretarioPresent = false;
                                for (User user : users) {
                                    if (user.getTitle().equals("Secretario")) {
                                        Id = user.getDUI();
                                    }
                                }
                            }else {
                                message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion";
//                                if(Consts.LOCALE.contains("HON")){
//                                    for (User user : users) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n "+ user.getName();
//                                        }
//                                    }
//                                }else {
                                Id = vcOfficial.get(0).getDUI();
                                message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n"+ vcOfficial.get(0).getName();
//                                    for (User user : vcOfficial) {
//                                        if (user.getTitle().equals("VC-Official")) {
//                                            Id = user.getDUI();
//                                            message = "Ingrese el "+getResources().getString(R.string.dui)+" del Oficial del TSE en el Centro de Votacion\n "+ user.getName();
//                                        }
//                                    }
//                                }
                            }
                        }
                        challengeHelper.setLogIndex("11");
                        challengeHelper.createSingleDuiChallenge(message, ENTRAR, Id);// compare agains this id
                    } else {
                        enterRoutine();
                    }
                }

            }
        };
    }

    private OnClickListener contnuarBtn() {
        return new OnClickListener() {
            public void onClick(View v) {

                    if (!actaSeals.get(SEAL.hashCode()).isCbOneSelected()) {
                        ah.createCustomToast("No puede proceder", "sin sello");
                    } else if (quantityOfSignatures() <= getResources().getInteger(R.integer.minimumMREmembers)) {//move to 3
                        ah.createCustomToast("Seleccione firmantes", "");
                    } else {

                        continueRoutine();
//                        challengeHelper.createDuiChallenge(getResources().getString(R.string.continueChecklist), CONTINUAR);
                    }

            }
        };

    }

    private OnApprove reiniciar = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            reiniciarRoutine();
            proximoBtn.setText("Ingresar");
            ah.setButtonColorGreen(proximoBtn);
            ah.setButtonColorRed((Button) findViewById(R.id.restart_btn));
            proximoOff = false;
        }
    };

    private OnApprove continuar = new OnApprove() {
        @Override
        public void approved() {
            continueRoutine();
        }
    };

    private void enterRoutine() {
        // disable button:, lock entry, update the object associated with item,
        // find next item, open next item
        ((ScrollView) findViewById(R.id.sv_signatures)).smoothScrollBy(0, getResources().getDimensionPixelOffset(R.dimen.verticalDifferential));
        ah.setButtonColorRed((Button) findViewById(R.id.entrar_btn));
        unLockItem(false, currentItem);
//        processNextItem();
//        if(proximoOff == true){
//            ah.createCustomToast("true");
//        } else ah.createCustomToast("false");

        if(proximoOff == true) {
            processNextItem();
        } else {
            ah.setButtonColorGreen(proximoBtn);
        }
    }

    private void continueRoutine() {
        //------------------------------------------------------------------------------------------
        count = quantityOfSignatures();
        countSecondColumn = quanityOfQualityCheck();
        //------------------------------------------------------------------------------------------

        //condittion sello is checked and three more checkboxes checked - total four are checked
        //current rule: 1st SEAL is check and count is one.  count will always be one if the box is checked.

        if (actaSeals.get(SEAL.hashCode()).isCbOneSelected() && count >= getResources().getInteger(R.integer.minimumMREmembers)) {


            //CARLOS:
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            signaturesMap.put("JRV", vc.getJrvString());
            signaturesMap.put("ELECTION_ID", vc.getPref_election_id());

            if (countSecondColumn > 0) {
                signaturesMap.put("PROCESSIBLE", "0");
                signaturesMap.put("PROVISIONAL_ACCEPT", "0");
            } else {
                signaturesMap.put("PROCESSIBLE", "1");
                signaturesMap.put("PROVISIONAL_ACCEPT", "1");
            }
            //---------------- save items in simple hashmap---------------------------------
            for (String item : SignaturesSealItems) {
                signaturesMap.put(item, signaturesSeals.get(item.hashCode()).isCbOneSelected() ? "1" : "0");
            }
            for(String item: actaSealItems){
                signaturesMap.put(item, actaSeals.get(item.hashCode()).isCbOneSelected() ? "1" : "0");
            }
            for (String item : ActaQualityItems) {
                signaturesMap.put(item, actaQuality.get(item.hashCode()).isCbOneSelected() ? "1" : "0");
            }
            signaturesMap.put("DATETIME_LOG", formattedDate);

            Gson gson = new Gson();
            String json = gson.toJson(signaturesMap);
            ah.savePreferences("jsonCheckList", json);

            if (!db_adapter.isOpen()) {
                db_adapter.open();
            }
            db_adapter.deleteCheckListItems();
            db_adapter.saveCheckListItems(signaturesMap);


            Bundle b = new Bundle();
            b.putParcelable("com.afilon.tse.votingcenter", vc);
            b.putParcelable("com.afilon.tse.escrudata", escrudata);
            b.putString("actaSignatureCount", Integer.toString(quantityOfSignatures()));
//            Intent search = new Intent(CheckListActivity.this, Consts.CAMACT);
            Intent search = new Intent(CheckListActivity.this, ReclamosActivity1.class);
            search.putExtras(b);
            startActivity(search);
            finish();

        } else {
            if (!actaSeals.get(SEAL.hashCode()).isCbOneSelected()) {
                ah.createCustomToast("No puede proceder",
                        "sin sello");
                //reset again the counter for column 1 and 2
                count = 0;
                countSecondColumn = 0;
            } else {
                ah.createCustomToast("Seleccione firmantes",  //will never be set.
                        "");
                //reset again the counter for column 1 and 2
                count = 0;
                countSecondColumn = 0;
            }
        }
    }

    private void reiniciarRoutine() {
        ah.setButtonColorRed((Button) findViewById(R.id.entrar_btn));
        ah.setButtonColorRed((Button) findViewById(R.id.continuar_btn));
        resetAllRows();
        lockAbsentMembers();
        sealStage = false;
        isSello = false;
        permission = false;
        proximoOff = false;
        signatureStage = true;
        secretarioPresent = false;
        presidentePresent = false;
        vocalOnePresent = false;
        vocalTwoPresent = false;
        vocalThreePresent = false;
    }

	private OnApprove enter = new OnApprove() {
		@Override
		public void approved() {
            boolean doEnter = true;
            for(User user : users){
                if(currentItem.equals(user.getTitle())){
                    for(User un : userNames) {
                        if(user.getDUI().equals(un.getDUI())) {
                            challengeHelper.signaturePad(sigList, vc.getPref_election_id(), vc.getJRV(), un.getName(), user.getTitle(), user.getDUI());
                            doEnter = false;
                            break;
                        }
                    }
                }
            }
            if(doEnter) enterRoutine();
		}
	};

    private OnApprove onNoDefault =  new OnApprove() {
        @Override
        public void approved() {
            return;
        }
    };

    private View.OnClickListener checkBoxListener() {
        /**  returns the clickListener that handles the checkboxes for the Negative and Possitive
         * columns. */
        View.OnClickListener present = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ah.setButtonColorGreen((Button) findViewById(R.id.entrar_btn));
                int id = v.getId();
                String item = getItemFromID(id);
                if (item.equals("NoItemFound")) return; //TODO: remove hardcoded string

                CheckBox cbAffirmative = (CheckBox) findViewById(getAffirmativeBoxID(item));
                CheckBox cbNegative = (CheckBox) findViewById(getNegativeBoxID(item));

                if (cbAffirmative.isChecked() && cbNegative.isChecked()) {
                    clearCheckBoxes(item, true);
                    ah.setButtonColorRed((Button) findViewById(R.id.entrar_btn));
                    Log.e("CHECKBOXES", "both where selected, neither is selected");
                    return;
                } else if (cbAffirmative.isEnabled() && cbAffirmative.isChecked()) {
                    cbNegative.setChecked(false);
                } else if (cbNegative.isEnabled() && cbNegative.isChecked()) {
                    cbAffirmative.setChecked(false);
                } else {
                    clearCheckBoxes(item, true);
                    ah.setButtonColorRed((Button) findViewById(R.id.entrar_btn));
                }
            }
        };
        return present;
    }


    private void processNextItem() {
            ((CheckBox) findViewById(getAffirmativeBoxID(currentItem))).setBackgroundColor(Color.TRANSPARENT);
            ((CheckBox) findViewById(getNegativeBoxID(currentItem))).setBackgroundColor(Color.TRANSPARENT);

        if (signatureStage) {
            if(!isSello) {
                UpdateSignature(currentItem);
                currentItem = nextAvailbleSignature();
            } else {
                UpdateSeal(currentItem);
                currentItem = nextAvailableSeal();
            }
        }else if(sealStage){
            UpdateSeal(currentItem);
            currentItem = nextAvailableSeal();

        }else {
            if(currentItem.equals(ACTA_QUALITY_COMPLETE)) return;
            UpdateQuality(currentItem);
            currentItem = nextAvailbleQualityCheck();
        }
        switch (currentItem) {
//            case SIGNATURES_COMPLETE:
//                // signature table is now complete,
//                // unlock acta quality table and find first item.
////                signatureStage = false;
//                signatureStage = true;
//                sealStage = true;
//                if(!isSello){
//                    unLockFirstSeal();
//                    break;
//                } else {
//                    signatureStage = false;
//                    sealStage = false;
//                    proximoOff = true;
//                    unlockFirstActaQuality();
//                    break;
//                }
//            case ACTA_SEALS_COMPLETE:
//                signatureStage = false;
//                sealStage = false;
//                proximoOff = true;
//                unlockFirstActaQuality();
//                break;
//            case ACTA_QUALITY_COMPLETE:
//                proximoOff = true;
//                findViewById(R.id.acta_quality).setBackgroundColor(getResources().getColor(R.color.transparent));
//                ah.setButtonColorGreen((Button) findViewById(R.id.continuar_btn));
//                break;
//            default:
//                unLockItem(true, currentItem);
//                break;
            case SIGNATURES_COMPLETE:
                // signature table is now complete,
                // unlock acta quality table and find first item.
//                signatureStage = false;
                signatureStage = false;
                sealStage = false;
                proximoOff = true;
                unlockFirstActaQuality();
                break;
//            case ACTA_SEALS_COMPLETE:
            case ACTA_QUALITY_COMPLETE:
                signatureStage = false;
                sealStage = true;
                proximoOff = true;
                if(!isSello){
                    unLockFirstSeal();
                    break;
                } else {
                    signatureStage = false;
                    sealStage = false;
                    proximoOff = true;
                    findViewById(R.id.acta_quality).setBackgroundColor(getResources().getColor(R.color.transparent));
                    ah.setButtonColorGreen((Button) findViewById(R.id.continuar_btn));
                    break;
                }
//            case ACTA_QUALITY_COMPLETE:
            case ACTA_SEALS_COMPLETE:
                proximoOff = true;
                findViewById(R.id.seals2).setBackgroundColor(getResources().getColor(R.color.transparent));
                ah.setButtonColorGreen((Button) findViewById(R.id.continuar_btn));
                break;
            default:
                unLockItem(true, currentItem);
                break;
        }
    }


//--------------------------------------------------------------------------------------------------

	//TODO: errors have to be recalculated else where
	private void calculateErrors() {
        //BR: "check errors"
        List<PreferentialPartyVotes> partyVotes;
        int totalPartyVotes = 0;
        int initialVotes = 0;
	        partyVotes = db_adapter.getPartiesPreferentialVotes();
        for (PreferentialPartyVotes party: partyVotes ){
			//patch:
			boolean pass = true;
			int partyBoleats = 0;
			try{
				partyBoleats =Integer.parseInt( party.getParty_boletas());
			}catch (NumberFormatException nfe){
				pass = false;
			}
			if(!pass){
				partyBoleats = (int) Float.parseFloat(party.getParty_boletas());
			}
            totalPartyVotes += partyBoleats;
            initialVotes +=  party.getParty_votes();
            Log.e(" CHECKLIST","******************************************");

            Log.e("*******","*********************************************");
        }
        Log.e("@@@ VOTES",Integer.toString(totalPartyVotes)+" @@@");
	
		LinkedHashMap<String, String> conceptNumbers = db_adapter
				.getConceptsCountPreferential();
		Log.e("CHECK ACTIVITY","**************************");
		Log.e("concepts: ", conceptNumbers.toString());
		conceptNumbers.remove("PAPELETAS RECIBIDAS");
		conceptNumbers.remove("PREFERENTIAL ELECTION ID");
		conceptNumbers.remove("JRV");

		int qtyTotalEscrutadas = ah.parseInt(conceptNumbers.get("TOTAL PAPELETAS ESCRUTADAS"), 0);
		int qtyEntregadas = ah.parseInt(conceptNumbers.get("PAPELETAS ENTREGADAS"), 0);
		int qtyFaltantes = ah.parseInt(conceptNumbers.get("PAPELETAS FALTANTES"), 0);
		int qtySobrantes = ah.parseInt(conceptNumbers.get("SOBRANTES"), 0);
		int qtyInutilizadas = ah.parseInt(conceptNumbers.get("INUTILIZADAS"), 0);
		int qtyImpugnados = ah.parseInt(conceptNumbers.get("IMPUGNADOS"), 0);
		int qtyNulos = ah.parseInt(conceptNumbers.get("NULOS"), 0);
		int qtyAbstenciones = ah.parseInt(conceptNumbers.get("ABSTENCIONES"), 0);
		int qtyOfPapeletasInicio = ah.parseInt(escrudata.getPapeletasInicio(), 0);
		int qtyOfPapeletasFinal = ah.parseInt(escrudata.getPapeletasFinal(), 0);
		int TOTAL_PAPELETAS = (qtyOfPapeletasFinal- qtyOfPapeletasInicio)+1;

		// loop over the set using an entry set
		int  sumOfAllConcepts = 0;
		for (Map.Entry<String, String> entry : conceptNumbers.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			Log.e("$$$&$&$&$&","$(*(*(*(*(*(*(**)*)*)*)*)*)*)*)**)$%$%%$%$%");
			Log.e(key,val);

			sumOfAllConcepts += Integer.parseInt(val);
		}
		sumOfAllConcepts += totalPartyVotes;
		// CARLOS: 2014-08-21
		boolean directVotes = getResources().getString(R.string.voteType).equals("DIRECT");
		int cross_votes = 0;
		if(!directVotes) cross_votes = ah.loadPreferences(Consts.VOTO_CRUZADO);


		int qtyRealSumOfAllConcepts = (sumOfAllConcepts - (qtyTotalEscrutadas + qtyEntregadas)+cross_votes);
		int qtyJustPartyVotes = totalPartyVotes;
		escrudata.setVotosValidos(String.valueOf(totalPartyVotes));
		Log.e("Sum of sumOfAllConcepts", " : " + String.valueOf(qtyRealSumOfAllConcepts));

		//--------------------------------------------------------------------------------------
        /* ERROR #1 2014-09-16 Updated by Charles
         *          2016-05-24 second update
         * El calculo de Votos Validos no coincide con el numero de boletas
         */
		// Changes:
		//- (qtyImpugnados + qtyNulos + qtyAbstenciones) - qtyFaltantes)) {
		Log.e("partyVotes", Integer.toString(qtyJustPartyVotes));
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
		Log.e("Votantes",Integer.toString(votantes));
		if (votantes > TOTAL_PAPELETAS) {
			escrudata.setErrorTypeTwo("true");
			Log.i("ChckList Error type TWO", "True");
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
			Log.i("ChckList Error ", "type THREE: True");
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
			Log.i("ChkList Error", "type FOUR: True");
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
			Log.i("CheckList Error", " type FIVE: True");
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
			Log.i("CheckList Error", "type SIX: true");
		} else {
			escrudata.setErrorTypeSix("false");
			Log.i("CheckList Error", "type SIX: false");
		}
	}

    private void calculateErrorsHon() {
        //BR: "check errors"
        List<PreferentialPartyVotes> partyVotes;
        int totalPartyVotes = 0;
        int qtyPartyVotes = 0;
        int initialVotes = 0;
        partyVotes = db_adapter.getPartiesPreferentialVotes();
        for (PreferentialPartyVotes party: partyVotes ){
            //patch:
            boolean pass = true;
            int partyBoleats = 0;
            try{
                partyBoleats =Integer.parseInt( party.getParty_boletas());
            }catch (NumberFormatException nfe){
                pass = false;
            }
            if(!pass){
                partyBoleats = (int) Float.parseFloat(party.getParty_boletas());
            }
            totalPartyVotes += partyBoleats;
            qtyPartyVotes += partyBoleats;
            initialVotes +=  party.getParty_votes();
            Log.e(" CHECKLIST","******************************************");

            Log.e("*******","*********************************************");
        }
        Log.e("@@@ VOTES",Integer.toString(totalPartyVotes)+" @@@");

        LinkedHashMap<String, String> conceptNumbers = db_adapter.getConceptsCountPreferential();
        conceptNumbers.remove("PREFERENTIAL ELECTION ID");
        conceptNumbers.remove("JRV");

        int qtyPapRec = ah.parseInt(conceptNumbers.get("PAPELETAS RECIBIDAS"), 0);
        int qtyUtil = ah.parseInt(conceptNumbers.get("UTILIZADAS"), 0);
        int qtyTotVoters = ah.parseInt(conceptNumbers.get("MER"), 0) + ah.parseInt(conceptNumbers.get("CIUDADANOS"), 0);

        int qtyValid = ah.parseInt(conceptNumbers.get("VOTOS VALIDOS"), 0);
        int qtyBlanco = ah.parseInt(conceptNumbers.get("EN BLANCO"), 0);
        int qtyNul = ah.parseInt(conceptNumbers.get("NULOS"), 0);
        int qtyTotal = ah.parseInt(conceptNumbers.get("GRAN TOTAL"), 0);

        int qtyTotalEscrutadas = ah.parseInt(conceptNumbers.get("TOTAL PAPELETAS ESCRUTADAS"), 0);
        int qtyEntregadas = ah.parseInt(conceptNumbers.get("PAPELETAS ENTREGADAS"), 0);
        int qtyFaltantes = ah.parseInt(conceptNumbers.get("PAPELETAS FALTANTES"), 0);
        int qtySobrantes = ah.parseInt(conceptNumbers.get("SOBRANTES"), 0);
        int qtyInutilizadas = ah.parseInt(conceptNumbers.get("INUTILIZADAS"), 0);
        int qtyImpugnados = ah.parseInt(conceptNumbers.get("IMPUGNADOS"), 0);
        int qtyNulos = ah.parseInt(conceptNumbers.get("NULOS"), 0);
        int qtyAbstenciones = ah.parseInt(conceptNumbers.get("ABSTENCIONES"), 0);
        int qtyOfPapeletasInicio = ah.parseInt(escrudata.getPapeletasInicio(), 0);
        int qtyOfPapeletasFinal = ah.parseInt(escrudata.getPapeletasFinal(), 0);
        int TOTAL_PAPELETAS = (qtyOfPapeletasFinal- qtyOfPapeletasInicio)+1;

        // loop over the set using an entry set
        int  sumOfAllConcepts = 0;
        for (Map.Entry<String, String> entry : conceptNumbers
                .entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            Log.e("$$$&$&$&$&","$(*(*(*(*(*(*(**)*)*)*)*)*)*)*)**)$%$%%$%$%");
            Log.e(key,val);

            sumOfAllConcepts += Integer.parseInt(val);
        }
        sumOfAllConcepts += totalPartyVotes;
        // CARLOS: 2014-08-21
        boolean directVotes = getResources().getString(R.string.voteType).equals("DIRECT");
        int cross_votes = 0;
        if(!directVotes) cross_votes = ah.loadPreferences(Consts.VOTO_CRUZADO);


        int qtyRealSumOfAllConcepts = (sumOfAllConcepts - (qtyTotalEscrutadas + qtyEntregadas)+cross_votes);
        int qtyJustPartyVotes = totalPartyVotes;
        escrudata.setVotosValidos(String.valueOf(totalPartyVotes));
        Log.e("Sum of sumOfAllConcepts", " : " + String.valueOf(qtyRealSumOfAllConcepts));

        String voteType = getResources().getString(R.string.voteType);
        if(voteType.contains("PREF")){
            qtyPartyVotes = qtyValid;
        }
        //--------------------------------------------------------------------------------------
        /* ERROR #1 2014-09-16 Updated by Charles
         *          2016-05-24 second update
         * El calculo de Votos Validos no coincide con el numero de boletas
         */
        // Changes:
        //- (qtyImpugnados + qtyNulos + qtyAbstenciones) - qtyFaltantes)) {
        Log.e("partyVotes", Integer.toString(qtyJustPartyVotes));
        if(qtyPartyVotes > qtyPapRec) {
            escrudata.setErrorTypeOne("true");
            Log.i("Vertical Error typeONE", "True");
        } else escrudata.setErrorTypeOne("false");

        //Calculate for Error Type Two
        //		ErrorTypeTwo if the party vote count is larger than the used ballots
        if(qtyPartyVotes > qtyUtil){
            escrudata.setErrorTypeTwo("true");
            Log.i("ChckList Error typeTWO","True");
        } else escrudata.setErrorTypeTwo("false");

        //Calculate for Error Type Three
        //		ErrorTypeThree if the party vote count is larger than the total number of voters
        if(qtyPartyVotes > qtyTotVoters){
            escrudata.setErrorTypeThree("true");
            Log.i("ChckList Error typeTHRE","True");
        } else escrudata.setErrorTypeThree("false");

        //Calculate for Error Type Four
        //		ErrorTypeFour if the number of valid votes does not equal number of valid voters
        if(qtyPartyVotes != qtyValid){
            escrudata.setErrorTypeFour("true");
            Log.i("ChckList Error TypeFOUR","True");
        } else escrudata.setErrorTypeFour("false");

        //Calculate for Error Type Five
        //		ErrorTypeFive if valid votes plus invalid votes does not equal total voters
        if((qtyPartyVotes + qtyBlanco + qtyNul) != qtyTotVoters){
            escrudata.setErrorTypeFive("true");
            Log.i("ChckList Error TypeFIVE","True");
        } else escrudata.setErrorTypeFive("false");

        //Calculate for Error Type Six
        // 		ErrorTypeSix if valid votes plus invalid votes does not equal grand total
        if((qtyUtil != qtyTotVoters)||(qtyUtil != qtyTotal)||(qtyTotVoters != qtyTotal)){
            escrudata.setErrorTypeSix("true");
            Log.i("ChckList Error TypeSIX","True");
        } else escrudata.setErrorTypeSix("false");
    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus){
        if(!hasfocus){
            findViewById(R.id.button_layout).setVisibility(View.INVISIBLE);
        }else{
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
        }

    }

}
