package com.afilon.mayor.v11.utils;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns;
import com.afilon.mayor.v11.fragments.ThreeButtonFragment;
import com.afilon.mayor.v11.fragments.drawSignature;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns.DuiChallengeListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogEditTextFragment4Boletas;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.fragments.TwoButtonDialogToConfirmAddBoletasFragment;
import com.afilon.mayor.v11.model.CustomKeyboard;

import java.util.HashMap;

/**
 * Created by BReinosa on 3/9/2017.
 */
public class ChallengeHelper {
    protected Context mContext;
    private String DUI_A;
    private String DUI_B;
    private int[] keyboardIds;
    private CustomKeyboard presidenteKeyboard;
    private CustomKeyboard secretaryKeyboard;
    private boolean challengeHelperReady;
    private static final int PRESIDENT_VERIFICATION = 556;
    private static final int SECRETARY_VERIFICATION = 558;
    private static final int DUI_CONFIRMATION_REQUEST= 554;
    private int USERS_VERIFIED_ROUTINE;
    private String currentDui;
    private DatabaseAdapterParlacen db_adapter;
    private Utilities ah;
    private HashMap<Integer,OnApprove> routineSet;
    private OnApprove routine;



    public ChallengeHelper(Context context) {
        mContext = context;
        routineSet = new HashMap<>();
    }

    TwoButtonDialogFragment twoBtnDialogFragment;





//    DialogToConfirmDuiTwoBtns dialogToConfirmDuiTwoBtns;
    //------------------PUBLIC METHODS: ------------------------------------------------------------

    public void createDialogEditText(String msg, int routineID) {
        TwoButtonDialogEditTextFragment4Boletas editTextFragment = new TwoButtonDialogEditTextFragment4Boletas();
        FragmentManager fm = ((Activity)mContext).getFragmentManager();
        editTextFragment.setOnButtonsClickedListenerOne(ballotListener);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", routineID);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        bndl.putString("invisible", "invisible");
        editTextFragment.setArguments(bndl);
        editTextFragment.show(fm, "new triage dialog");

    }

   public void createThreeButtonMenu(String btnLabel1, String btnLabel2, String btnLabel3, ThreeButtonFragment.ThreeButtonListener listener){
       FragmentManager fm = ((Activity)mContext).getFragmentManager();
       ThreeButtonFragment menuFrag = new ThreeButtonFragment();
       menuFrag.setOnButtonsClickedListenerOne(listener);
       Bundle bnld = new Bundle();
       bnld.putString(Consts.FIRST_BTN,btnLabel1);
       bnld.putString(Consts.SECOND_BTN, btnLabel2);
       bnld.putString(Consts.THRID_BTN,btnLabel3);
       menuFrag.setArguments(bnld);
       menuFrag.show(fm,"three_menu");
   }



    public void createDialog(String msg, int routineID) {
        FragmentManager fm = ((Activity)mContext).getFragmentManager();
        twoBtnDialogFragment = new TwoButtonDialogFragment();
        twoBtnDialogFragment.setOnButtonsClickedListenerOne(plainChallengeListener);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Si");
        bndl.putInt("yesIndex", routineID);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        bndl.putString("invisible", "visible");
        twoBtnDialogFragment.setArguments(bndl);
        twoBtnDialogFragment.show(fm, "new triage dialog");
    }
    /** createDuiChallenge flow goes : plain challenge --> request DUI 1 --> request DUI2 -->
     *  final approval routine.  message is the message displayed during plain challenge */
    public void createDuiChallenge(String message, int routineID){
        /** double dui challenge (back to back requests) */
        isHelperReady();
        USERS_VERIFIED_ROUTINE = routineID;
        createDialog(message,DUI_CONFIRMATION_REQUEST);
    }

    private void isHelperReady(){
        if(!challengeHelperReady)  throw new RuntimeException("THE CUSTOM KEYBOARDS WERE NOT REGISTERED: Use " +
                "Method addCustomKeyboards(int keyboard1, int keyboard2) ");
        if(db_adapter==null || ah ==null)  throw new RuntimeException("DATABASE ADAPTER OR UTILITIES WERE NOT REGISTERED TO CHALLENGE HELPER: Use " +
                "Method setTools() ");
    }

    public void createDuiChallenge(int routineID){
        isHelperReady();
        /** double dui challenge (back to back requests)  with out a message*/
        USERS_VERIFIED_ROUTINE = routineID;
        routine = routineSet.get(DUI_CONFIRMATION_REQUEST);
        routine.approved();

    }

    public void createSingleDuiChallenge(String message, int routineID){
        //TODO: THIS method is not complete 4/12/17
        isHelperReady();
        USERS_VERIFIED_ROUTINE = routineID;
//        createDialogToConfirmDui(message,SECRETARY_VERIFICATION,presidenteKeyboard);
    }

    public void createSingleDuiChallenge(int routineID){
        isHelperReady();
        USERS_VERIFIED_ROUTINE = routineID;
        createDialogToConfirmDui("Ingrese su DUI",-1,singleDuiChallengeListener);


    }

    public void createSingleDuiChallenge(String message, int routineID, String Id, int i){
        isHelperReady();
        DUI_A = Id;
        createDialogToConfirmDui(message,routineID,compareDuisListeners);
    }

    public void createSingleDuiChallenge(String message, int routineID, String Id){
        isHelperReady();
        DUI_A = Id;
        createDialogToConfirmDui(message,routineID,compareDuisListeners);
    }

    //TODO: CREATE CHALLGE FOR CASES WHERE WE NEED ONE AT A TIME:
    public void setTools(Utilities utilities, DatabaseAdapterParlacen adapter){
        ah = utilities;
        db_adapter = adapter;
    }

    public void setTools(DatabaseAdapterParlacen adapter){
        ah= new Utilities(mContext);
        db_adapter = adapter;
    }

    public void setTools(Utilities utilities){
        ah = utilities;
        db_adapter = new DatabaseAdapterParlacen(mContext);
    }

    public void setTools(){
        setDefaultTools();
    }
    /** register the custom keyboards by providing the view Id of the keyboards
     * - the keyboard views need to be added to the activity xml layout file */
    @Deprecated
    public void addCustomKeyBoards(int presidentKeyBoardViewID, int secretaryKeyboardViewID){
        //NOT DYNAMIC, SHOULD ONLY HOLD TWO KEYBOARDS.
        keyboardIds = new int[2];
        keyboardIds[0]=presidentKeyBoardViewID;
        keyboardIds[1]=secretaryKeyboardViewID;
        initiateCustomKeyboards();
    }

    public void addCustomKeyBoard(int presidentKeyBoardViewID){
        keyboardIds = new int[1];
        keyboardIds[0]=presidentKeyBoardViewID;
        intitiateKeyboard();
    }

    public interface OnApprove {
        public void approved();

    }

    OnTwoButtonDialogFragmentListener plainChallengeListener = new OnTwoButtonDialogFragmentListener() {

        @Override
        public void onYesButtonForTwoButtonDialogClicked(int caseWhichLaunchedFragment) {
            /** caseWhichLaunchedFragment is the yestIndex assigned when create dialog was created*/
            routine = routineSet.get(caseWhichLaunchedFragment);
            routine.approved();
        }

        @Override
        public void onNoButtonForTwoButtonDialogClickedX() {
            routine = routineSet.get(-1);
            if(!(routine==null)) routine.approved();
            twoBtnDialogFragment.dismiss();
        }

    };

    public void addRoutine(int routineID,OnApprove routine) {
        routineSet.put(routineID,routine);
    }

    private void setDefaultTools(){
        ah = new Utilities(mContext);
        db_adapter = new DatabaseAdapterParlacen(mContext);
    }

    @Deprecated
    private void initiateCustomKeyboards(){
        presidenteKeyboard = new CustomKeyboard((Activity)mContext,keyboardIds[0], R.xml.tenhexkbd);
        secretaryKeyboard = new CustomKeyboard((Activity)mContext,keyboardIds[1], R.xml.tenhexkbd);
        addRoutine(DUI_CONFIRMATION_REQUEST,duiConfirmationCall);
        addRoutine(SECRETARY_VERIFICATION,secretaryConfirmation);
        addRoutine(PRESIDENT_VERIFICATION,presidentConfirmation);
        challengeHelperReady=true;
        secretaryKeyboard.hideCustomKeyboard();
    }

    private void intitiateKeyboard(){
        presidenteKeyboard = new CustomKeyboard((Activity)mContext,keyboardIds[0], R.xml.tenhexkbd);
        addRoutine(DUI_CONFIRMATION_REQUEST,duiConfirmationRequest);
        addRoutine(SECRETARY_VERIFICATION,secretaryConfirmationRequest);
        addRoutine(PRESIDENT_VERIFICATION,presidentConfirmationRequest);
        challengeHelperReady=true;
    }

    //--------------------------- DUI CHALLENGE ----------------------------------------------------
    private void createDialogToConfirmDui(String msg, int yesIndex, DuiChallengeListener listener) {
//        if(mContext instanceof FragmentActivity){
//            android.support.v4.app.FragmentManager fm =((FragmentActivity)mContext).getSupportFragmentManager();
//        }
        android.app.FragmentManager fm =((Activity)mContext).getFragmentManager();
        DialogToConfirmDuiTwoBtns dialogToConfirmDuiTwoBtns = new DialogToConfirmDuiTwoBtns();
        dialogToConfirmDuiTwoBtns.setOnDuiChallengerListener(listener);
        dialogToConfirmDuiTwoBtns.setCustomKeyboard(presidenteKeyboard);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "Cancelar");
        bndl.putString("question", msg);
        bndl.putString("invisible", "No");//set invisible to hide 'Cancelar' btn
        dialogToConfirmDuiTwoBtns.setArguments(bndl);
        dialogToConfirmDuiTwoBtns.show(fm,msg);

    }

    public void signaturePad(drawSignature.drawSignatureListener sigList,String electionID, String jrv, String name, String title, String dui){
        android.app.FragmentManager fm = ((Activity)mContext).getFragmentManager();
        drawSignature drawSig = new drawSignature();
        String prompt = name + " , " + title + "\nFirma";
        Bundle bndl = new Bundle();
        drawSig.setOnClickedListener(sigList);
        bndl.putString("jrv", jrv);
        bndl.putString("title",title);
        bndl.putString("name", name);
        bndl.putString("dui", dui);
        bndl.putString("electionID", electionID);
        bndl.putString("yesButtonText", "Continuar");
        bndl.putString("noButtonText", "Cancelar");
        bndl.putString("question", prompt);
        drawSig.setArguments(bndl);
        drawSig.show(fm,prompt);
    }

    /**
     * @param msg
     * @param yesIndex
     * @param keyboard
     */
    @Deprecated
    private void createDialogToConfirmDui(String msg, int yesIndex, CustomKeyboard keyboard) {
        android.app.FragmentManager fm =((Activity)mContext).getFragmentManager();
        DialogToConfirmDuiTwoBtns dialogToConfirmDuiTwoBtns = new DialogToConfirmDuiTwoBtns();
        dialogToConfirmDuiTwoBtns.setOnDuiChallengerListener(duiChallengeListener);
        dialogToConfirmDuiTwoBtns.setCustomKeyboard(keyboard);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "Cancelar");
        bndl.putString("question", msg);
        bndl.putString("invisible", "No");//set invisible to hide 'Cancelar' btn
        dialogToConfirmDuiTwoBtns.setArguments(bndl);
        dialogToConfirmDuiTwoBtns.show(fm,msg);
    }
    //---------------------- Fragment Activity Dui Challenge --------------------------------------

    private boolean confirmPresident(String duiNumber){
        String jrv = ah.loadPreferencesString(mContext.getResources().getString(R.string.jrvNumber));
        boolean isDuiVerified;
        try {
            Log.e("OPEN DB : ", "true");
            db_adapter.open();
            isDuiVerified = db_adapter.verifyDui(DatabaseAdapterParlacen.PRESIDENT, duiNumber, jrv);
//            isDuiVerified = db_adapter.verifyDui("Presidente", duiNumber, jrv);
            db_adapter.close();
        } catch (Exception e) {
            Log.e("DUI REST ERROR: ", e.getMessage());
            isDuiVerified = false;
        }
        return isDuiVerified;
    }

    private boolean confirmSecretary(String duiNumber){
        String jrv = ah.loadPreferencesString(mContext.getResources().getString(R.string.jrvNumber));
        boolean isDuiVerified;
        try {
            db_adapter.open();
            isDuiVerified= db_adapter.verifyDui(DatabaseAdapterParlacen.SECRETARIO, duiNumber, jrv);
//            isDuiVerified= db_adapter.verifyDui("Secretario", duiNumber, jrv);
            db_adapter.close();
        } catch (Exception e) {
            Log.e("DUI REST ERROR: ", e.getMessage());
            isDuiVerified = false;
        }
        return isDuiVerified;

    }

    private void rejectVerification(String failedMessage){
        ah.createCustomToast(failedMessage);
    }

    private void proceedToFinalApprovalRoutine(){
        routine = routineSet.get(USERS_VERIFIED_ROUTINE);
        routine.approved();
    }

    DuiChallengeListener duiChallengeListener = new DuiChallengeListener() {
        @Override
        public void onYesButtonClicked(String duiNumber, int routineID) {
            currentDui = duiNumber;
            routine = routineSet.get(routineID);
            routine.approved();
        }

        @Override
        public void onNoButtonClicked() {
            if(secretaryKeyboard!=null){
                secretaryKeyboard.hideCustomKeyboard();
            }

        }
    };

    DuiChallengeListener singleDuiChallengeListener = new DuiChallengeListener() {
        @Override
        public void onYesButtonClicked(String duiNumber, int routineID) {
            currentDui = duiNumber;
            boolean validDui = confirmPresident(currentDui);
            if(!validDui){
                validDui = confirmSecretary(currentDui);
            }
            if(validDui){
                proceedToFinalApprovalRoutine();
                return;
            }
            rejectVerification(mContext.getResources().getString(R.string.failedConfirmation));
        }

        @Override
        public void onNoButtonClicked() {

        }
    };

    DuiChallengeListener compareDuisListeners = new DuiChallengeListener() {
        @Override
        public void onYesButtonClicked(String duiNumber, int routineID) {
            if(duiNumber.equals(DUI_A)){
                routine = routineSet.get(routineID);
                routine.approved();
            }else rejectVerification("Datos Incorrectos");
        }

        @Override
        public void onNoButtonClicked() {
            routine = routineSet.get(-1);
            routine.approved();

        }
    };

    TwoButtonDialogEditTextFragment4Boletas.OnTwoButtonBoletaDialogFragmentListener ballotListener =
            new TwoButtonDialogEditTextFragment4Boletas.OnTwoButtonBoletaDialogFragmentListener() {
        @Override
        public void onYesButtonBoletaDialogClicked(String numberOfPics) {

        }

        @Override
        public void onNoButtonBoletaDialogClicked() {

        }
    };


    //------------------------ DEFAULT ROUTINES ----------------------------------------------------

    private OnApprove duiConfirmationRequest = new OnApprove() {
        @Override
        public void approved() {
            createDialogToConfirmDui(mContext.getResources().getString(R.string.dui1Input),PRESIDENT_VERIFICATION,duiChallengeListener);
        }
    };

    private OnApprove secretaryConfirmationRequest = new OnApprove() {
        @Override
        public void approved() {
            DUI_B = currentDui;
            if (!DUI_A.contains(currentDui)) {
                if (!confirmSecretary(currentDui)) {
                    rejectVerification(mContext.getResources().getString(R.string.failedConfirmation));
                } else {
                    ah.createCustomToast(mContext.getResources().getString(R.string.dui2Confirmed));
                    proceedToFinalApprovalRoutine();
                }
            } else {
                rejectVerification(mContext.getResources().getString(R.string.duiplicateDui));
            }
        }
    };

    private OnApprove presidentConfirmationRequest  = new OnApprove() {
        @Override
        public void approved() {
            DUI_A = currentDui;
            if (!confirmPresident(currentDui)) {
                rejectVerification(mContext.getResources().getString(R.string.failedConfirmation));
            } else {
                ah.createCustomToast(mContext.getResources().getString(R.string.dui1Confirmed));
                createDialogToConfirmDui(mContext.getResources().getString(R.string.dui2Input),SECRETARY_VERIFICATION,duiChallengeListener);
            }
        }
    };
    //--------------------------------------------------------------------------------------------------------------------------------------
    // Deprecated callbacks
    private OnApprove duiConfirmationCall = new OnApprove() {
        @Override
        public void approved() {
            createDialogToConfirmDui(mContext.getResources().getString(R.string.dui1Input), PRESIDENT_VERIFICATION,presidenteKeyboard);
        }
    };

    private OnApprove secretaryConfirmation = new OnApprove() {
        @Override
        public void approved() {
            secretaryKeyboard.hideCustomKeyboard();
            DUI_B = currentDui;
            if (!DUI_A.contains(currentDui)) {
                if (!confirmSecretary(currentDui)) {
                    rejectVerification(mContext.getResources().getString(R.string.failedConfirmation));
                } else {
                    ah.createCustomToast(mContext.getResources().getString(R.string.dui2Confirmed));
                    proceedToFinalApprovalRoutine();
                }
            } else {
                rejectVerification(mContext.getResources().getString(R.string.duiplicateDui));
            }
        }
    };

    private OnApprove presidentConfirmation  = new OnApprove() {
        @Override
        public void approved() {
            DUI_A = currentDui;
            if (!confirmPresident(currentDui)) {
                rejectVerification(mContext.getResources().getString(R.string.failedConfirmation));
            } else {
                ah.createCustomToast(mContext.getResources().getString(R.string.dui1Confirmed));
//                secretaryKeyboard.showCustomKeyboard(null);
//                secretaryKeyboard = new CustomKeyboard((Activity)mContext,keyboardIds[1], R.xml.tenhexkbd);
                createDialogToConfirmDui(mContext.getResources().getString(R.string.dui2Input), SECRETARY_VERIFICATION,secretaryKeyboard);
            }
        }
    };


    private OnApprove onNoDefault =  new OnApprove() {
        @Override
        public void approved() {
            return;
        }
    };


}
