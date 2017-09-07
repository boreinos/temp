package com.afilon.mayor.v11.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.model.CandidateMarks;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.PreferentialCandidateVotes;
import com.afilon.mayor.v11.model.PreferentialPartyVotes;
import com.afilon.mayor.v11.model.PreferentialVotoBanderas;
import com.afilon.mayor.v11.model.PresidenteStaff;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.webservice.WebServiceActaImageTask;
import com.afilon.mayor.v11.webservice.WebServiceRestTask;
import com.afilon.mayor.v11.webservice.WebServiceActaImageTask.SendImageResponseCallback;
import com.afilon.mayor.v11.webservice.WebServiceRestTask.DataResponseCallback;


import com.google.gson.Gson;

public class LastActivity extends AfilonActivity implements
        OnTwoButtonDialogFragmentListener, DataResponseCallback,
        SendImageResponseCallback {

    private Button sendData_btn;
    private Escrudata escrudata;
    private Utilities ah;
    private String jrvNumber;
    private String electionId;
    private DatabaseAdapterParlacen db_adapter;
    private TwoButtonDialogFragment twoBtnDialogFragment;
    private TextView webservice_confirmationTv;
    private ProgressBar progressBar;
    private Button sendImage_btn;
    private Results results;
    private Button weblink_btn;
    private String actaSigCount;
    private int i = 0;
    private boolean isDirectVote;
    private List<File> signatureFiles;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ah = new Utilities(LastActivity.this);
        ah.tabletConfiguration(Build.MODEL, this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_last);

        // // // Trap unexpected error
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
                LastActivity.this));
        isDirectVote = getResources().getString(R.string.voteType).equals("DIRECT");



        final Bundle extras = getIntent().getExtras();


        if (extras != null) {
            escrudata = extras.getParcelable("com.afilon.tse.escrudata");
            jrvNumber = escrudata.getJrv();
            actaSigCount = extras.getString("actaSignatureCount");
            electionId = escrudata.getActaImageLink();
            Log.e("Last Activity Election ID", electionId);

        }// todo otherwise crash?
        ah.saveCurrentScreen(this.getClass(),extras);

        signatureFiles = getSigFiles(new File(Environment.getExternalStorageDirectory() + File.separator));

        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();

        results = new Results(escrudata,db_adapter);

        webservice_confirmationTv = (TextView) findViewById(R.id.imageconfirmation_textView);
        TextView jrv_numberTv = (TextView) findViewById(R.id.jrv_number);
        String jrvLabel = getResources().getText(R.string.precint)+": " + jrvNumber;
        jrv_numberTv.setText(jrvLabel);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        Button cerrarBtn = (Button) findViewById(R.id.cerrar_btn);
        ah.setButtonColorGreen(cerrarBtn);
        cerrarBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Boolean isSDPresent = android.os.Environment
                        .getExternalStorageState().equals(
                                android.os.Environment.MEDIA_MOUNTED);
                if (!isSDPresent) {
                    File dir = getFilesDir();
                    File file = new File(dir, jrvNumber);
                    file.delete();
                }
                createDialog("  \u00BFDESEA CERRAR?  ", 3);
            }
        });

        sendData_btn = (Button) findViewById(R.id.enviar_btn);
        ah.setButtonColorGreen(sendData_btn);
        sendData_btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);

                // change button color to red
                ah.setButtonColorRed(sendData_btn);
                // disable data button
                sendData_btn.setEnabled(false);
                sendJsonTask(results.getConceptUri(),results.getConcepts(),4);
                sendJsonTask(results.getErrorUri(),results.getErrors(),9);
                sendJsonTask(results.getPartyVoteUri(),results.getPartyVotes(),2);
                sendJsonTask(results.getChecklistUri(),results.getCheckList(),7);
                sendJsonTask(results.getIsprocessableUri(),results.getCheckList(),34);
                if (!isDirectVote) {
                    sendJsonTask(results.getUriCandidates(),results.getCandidateVotes(),3);
                    sendJsonTask(results.getUriBanderas(),results.getBanderaVotes(),5);
                    sendJsonTask(results.getUriMarks(),results.getCandidateMarks(), 17);

                }

            }
        });

        sendImage_btn = (Button) findViewById(R.id.enviar_image_btn);
        ah.setButtonColorRed(sendImage_btn);

        sendImage_btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                ah.setButtonColorRed(sendImage_btn);


                webservice_confirmationTv.setText("");
                sendImagesTasks();
            }
        });

        weblink_btn = (Button) findViewById(R.id.enlacedeweb_btn);
        ah.setButtonColorRed(weblink_btn);
        weblink_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (webservice_confirmationTv.getText().toString()
                        .equals("TRANSMISION: POSITIVA")) {
                    Intent intent = new Intent(LastActivity.this,
                            Consts.WEBACT);
//                    intent.putExtra("jrv", escrudata.getJrv());
//                    intent.putExtra("preferential_election_id", electionId);
                    intent.putExtras(extras);
                    startActivity(intent);
                    finish();

                } else {
                    ah.createCustomToast("NO HAY CONFIRMACION, REENVIAR",
                            "Si No Funciona, \n Llamar Administrador");
                }
            }
        });

        boolean dataSent = ah.loadPreferencesUpdate("dsent");
        boolean imageSent = ah.loadPreferencesUpdate("isent");
        if(dataSent){
            ah.setButtonColorRed(sendData_btn);
            if(imageSent){
                ah.setButtonColorRed(sendImage_btn);
                ah.setButtonColorGreen(weblink_btn);
                webservice_confirmationTv.setText("TRANSMISION: POSITIVA");
            }else {
                ah.setButtonColorGreen(sendImage_btn);
            }
        }else{
            ah.setButtonColorGreen(sendData_btn);
        }
    }

    private void sendJsonTask(String uri, String jsonString, int task_case) {
        try {

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("content-type", "application/json");

            HttpEntity entity;

            StringEntity s = new StringEntity(jsonString);
            entity = s;
            httpPost.setEntity(entity);

            WebServiceRestTask task = new WebServiceRestTask(task_case);
            task.setResponseDataCallback(LastActivity.this);
            task.execute(httpPost);

            Log.e("LastActivity", "CommonTask JSON" + task_case+ jsonString);

        } catch (Exception e) {
            Log.e("LastActivity", "CommonTask ERROR"+ e.getMessage());
        }
    }

    private void sendImagesTasks() {

        WebServiceActaImageTask uploadActaImageTaskOne;

        if (!webservice_confirmationTv.getText().toString().equals("TRANSMISION: POSITIVA")) {

            if (ah.isOnline(LastActivity.this)) {

                int totalNumberOfImagePics = ah.loadPreferences("GrandTotalOfPictures");
//                int numSigPics = Integer.parseInt(actaSigCount); //TODO determine number of signature images
//                Log.e("Num Signature Pics = ", Integer.toString(numSigPics));
                String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                char[] alphabetArray = alphabet.toCharArray();

                for (int j = 0; j < totalNumberOfImagePics; j++) {

                    uploadActaImageTaskOne = new WebServiceActaImageTask();
                    if (j == totalNumberOfImagePics - 1) {
                        uploadActaImageTaskOne
                                .setResponseCallback(LastActivity.this);
                    }

                    uploadActaImageTaskOne.postData(LastActivity.this, Consts.PREF_ELECTION_IMAGE_URL, jrvNumber + getResources().getString(R.string.imageType) + alphabetArray[j], electionId);
//                    ah.createCustomToast(Consts.PREF_ELECTION_IMAGE_URL, jrvNumber + getResources().getString(R.string.imageType)
//                            + alphabetArray[j]);
                }

//                if(false) {
                    //TODO send signature images
                    for (File file : signatureFiles) {
                        String delims = "[_]+";
                        String[] candInfo;
                        do{
                            candInfo = file.getName().split(delims);
                            for(int i = 0 ; i < candInfo.length ; i++){
                                Log.e("candInfor : " , candInfo[i]);
                            }
                        }while (candInfo.length<4);
//                        String[] candInfo = file.getName().split("_");
                        String dui = candInfo[2];
                        String title = candInfo[3];
                        String delims2 = "[.]+";
                        String[] titleInfo = title.split(delims2);
//                        String[] titleInfo = title.split(".");
                        title = titleInfo[0];
                        WebServiceActaImageTask uploadActaSigTaskOne;
                        uploadActaSigTaskOne = new WebServiceActaImageTask();
//				public void postSig(Context context, String serviceUrl, String fileName, String dui, String title)
                        uploadActaSigTaskOne.postSig(this, Consts.PREF_ELECTION_SIG_URL, file.getName(), dui, title, jrvNumber);
                    }
//                }

            } else {
                ah.createCustomToast("No hay connecion", "accesible.");
            }

        } else {
            ah.createCustomToast("Los Datos de Esta", "JRV Ya Existen.");
        }
    }

    private void sendSigTask(){

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.jrv, menu);
        return true;
    }

    public void onBackPressed() {
        Log.d("Last Activity", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    @Override
    public void onYesButtonForTwoButtonDialogClicked(int x) {

//        db_adapter = new DatabaseAdapterParlacen(this);// why the fuck do you keep making a new reference?
        db_adapter.open();
        db_adapter.deletePreferentialVotoBanderas();
        db_adapter.deletePartiesPreferentialVotes();
        db_adapter.deleteAllPreferentialCandidateVotes();

        switch (x) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
                break;
            default:
                finish();
                break;
        }

    }

    @Override
    public void onNoButtonForTwoButtonDialogClickedX() {
        // TODO Auto-generated method stub

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
    protected void onResume(){
        super.onResume();
        if(!db_adapter.isOpen()){
            db_adapter.open();
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onRequestDataSuccess(String response) {
        // a success reply come from server for data
        webservice_confirmationTv.setText("TRANSMISION: POSITIVA");
        webservice_confirmationTv.setTextColor(Color.GREEN);
        ah.setButtonColorGreen(sendImage_btn);
        Log.e("LastActivity Data Success response : ", response);

        if (i < 1) {
            ah.setButtonColorGreen(sendImage_btn);
            sendImage_btn.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            i++;
            ah.savePreferences("dsent",true);
        }

        if(!db_adapter.isOpen()){
            db_adapter.open();
        }
    }

    @Override
    public void onRequestDataError(Exception error) {
        // an error reply come from server
        Log.e("LastAct DataFailure:", error.getMessage());

        progressBar.setVisibility(View.GONE);
        sendData_btn.setText("RE-ENVIAR DATOS");
        ah.setButtonColorRed(sendImage_btn);
        // change data button to green color
        ah.setButtonColorGreen(sendData_btn);
        // enable data button again
        sendData_btn.setEnabled(true);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onRequestSendImageSuccess(String response) {
        ah.setButtonColorRed(sendImage_btn);
        // a success reply come from server for image
        webservice_confirmationTv.setText("TRANSMISION: POSITIVA");
        webservice_confirmationTv.setTextColor(Color.GREEN);

        if (!sendData_btn.isEnabled() && (!sendImage_btn.isEnabled())) {
            ah.setButtonColorGreen(weblink_btn);
            weblink_btn.setEnabled(true);
            ah.savePreferences("isent",true);
        }

        Log.e("LastActivity Image Success: ", response);
        progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onRequestSendImageError(Exception error) {
        // //an error reply come from server
        Log.e("LastActivity Image Failure :", error.getMessage());
        webservice_confirmationTv.setText("TRANSMISION: NEGATIVA");
        webservice_confirmationTv.setTextColor(Color.RED);

        progressBar.setVisibility(View.GONE);
        // enable image send button
        sendImage_btn.setText("RE-ENVIAR IMAGEN");
        sendImage_btn.setEnabled(true);
        ah.setButtonColorGreen(sendImage_btn);
    }

    public List<File> getSigFiles(File directory){
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        for(File file: files){
            if(file.getName().contains(jrvNumber) && file.getName().contains(electionId)){
                Log.e("NEW FILE ADDED : ", file.getName());
                inFiles.add(file);
            }
        }
        return inFiles;
    }


    private class Results {
        private List<PreferentialPartyVotes> partyVotes;
        private HashMap<String,String> party_candidate;
        private List<PreferentialVotoBanderas> votoBanderasArrayList;
        private List<PreferentialCandidateVotes> candidatesListSelectedArrayListTwo;
        private List<CandidateMarks> candidateMarksList;
        HashMap<String, String> conceptNumbers;
        private Gson gson = new Gson();
        private String electionId;
        private String jrv;
        private Escrudata escrutada;
        private DatabaseAdapterParlacen db_adapter;
        private String conceptUri = Consts.PREF_ELECTION_URL + "/Concepts";
        private String errorUri = Consts.PREF_ELECTION_URL + "/Errors";
        private String partyVoteUri = Consts.PREF_ELECTION_URL + "/PartyVotes";
        private String checklistUri =  Consts.PREF_ELECTION_URL + "/ActaQuality";
        private String isprocessableUri = Consts.PREF_ELECTION_JRV_PROCESSABLE_URL;
        private String uriCandidates = Consts.PREF_ELECTION_URL + "/CandidateVotes";
        private String uriBanderas = Consts.PREF_ELECTION_URL + "/BanderaVotes";
        private String uriMarks = Consts.PREF_ELECTION_URL + "/CandidateMarcas";
        private String checkList;


        public Results(Escrudata escrudata, DatabaseAdapterParlacen database){
            this.electionId = escrudata.getActaImageLink();
            this.jrv = escrudata.getJrv();
            this.escrutada = escrudata;
            this.db_adapter = database;

            partyVotes = db_adapter.getPartiesPreferentialVotes();
            checkList = compileList();
            conceptNumbers = db_adapter.getConceptsCountPreferential();
            conceptNumbers.put("JRV", escrudata.getJrv());
            conceptNumbers.put("CLOSURE TIME", escrudata.getHoraCierre());
            conceptNumbers.put("INITIAL PAPELETAS", escrudata.getPapeletasInicio());
            conceptNumbers.put("FINAL PAPELETAS", escrudata.getPapeletasFinal());
            conceptNumbers.put("PAPELETAS RECIBIDAS", escrudata.getPapeletasTotal());
            conceptNumbers.put("PREFERENTIAL ELECTION ID", escrudata.getActaImageLink());
            conceptNumbers.put("VOTOSVALIDOS", escrudata.getVotosValidos());
            conceptNumbers.put("OBSERVACIONES", escrudata.getReclamos() + " ");
            conceptNumbers.put("VOTOSCRUZADOS", String.valueOf(ah.loadPreferences(Consts.VOTO_CRUZADO)));

        }

        public String getPartyVotes(){
            if(Consts.LOCALE.contains("ELSA")){//todo:  really it should be direct only.
                Gson gson = new Gson();
                return gson.toJson(partyVotes);
            }else{
                return getDirectPartyVotes();
            }
        }

        public String getPartyVoteUri(){
            return partyVoteUri;
        }

        private String getDirectPartyVotes(){
            keepDbOpen();
            //----------------------------------------------------------------------------------
            //ADD CANDIDATE ID: for direct votes only:
            party_candidate = db_adapter.getPartiesCandidatesIDs(electionId);
            for(PreferentialPartyVotes vote: partyVotes){
                vote.setCandidate_direct_election_id(party_candidate.get(vote.getParty_preferential_election_id()));
            }
            //----------------------------------------------------------------------------------
           return gson.toJson(partyVotes);
        }

        public String getCandidateVotes(){
            if(Consts.LOCALE.contains("ELSA")){
                return getCandidateVotesSV();
            }
            return getCandidateMarksHN();
        }

        public String getCandidateVotesSV(){
            keepDbOpen();
            candidatesListSelectedArrayListTwo = db_adapter.getPreferentialElectionCandidateVotes();
            return gson.toJson(candidatesListSelectedArrayListTwo);
        }

        public String getUriCandidates(){
            return uriCandidates;
        }

        public String getBanderaVotes(){
            keepDbOpen();
            votoBanderasArrayList = db_adapter.getBanderaVotesPreferential();
            return gson.toJson(votoBanderasArrayList);
        }

        public String getUriBanderas(){
            return uriBanderas;
        }

        public String getConcepts(){
            return gson.toJson(conceptNumbers);

        }

        public String getConceptUri(){
            return conceptUri;
        }

        public String getCandidateMarks(){
            if(Consts.LOCALE.contains("ELSA")){
                return getCandidateMarksSV();
            }
            return getCandidateMarksHN();
        }

        public String getCandidateMarksSV(){
            keepDbOpen();
            candidateMarksList = db_adapter.getMarksArrayListToSend(electionId, jrv);
            return gson.toJson(candidateMarksList);
        }
        public String getCandidateMarksHN(){
            keepDbOpen();
            candidateMarksList = db_adapter.getTotalMarksArrayList(electionId, jrv);
            return gson.toJson(candidateMarksList);
        }

        public String getUriMarks(){
            return uriMarks;
        }

        public String getErrors(){
            HashMap<String, String> errorsAdapter =  new HashMap<>();
            errorsAdapter.put("JRV", escrutada.getJrv());
            errorsAdapter.put("PREFERENTIAL ELECTION ID", escrutada.getActaImageLink());
            errorsAdapter.put("error_typeone", escrutada.getErrorTypeOne());
            errorsAdapter.put("error_typetwo", escrutada.getErrorTypeTwo());
            errorsAdapter.put("error_typethree", escrutada.getErrorTypeThree());
            errorsAdapter.put("error_typefour", escrutada.getErrorTypeFour());
            errorsAdapter.put("error_typefive", escrutada.getErrorTypeFive());
            errorsAdapter.put("error_typesix", escrutada.getErrorTypeSix());
            return gson.toJson(errorsAdapter);
        }

        public String getErrorUri(){
            return errorUri;
        }
        public String getChecklistUri(){
            return checklistUri;
        }
        public String getIsprocessableUri(){
            return isprocessableUri;
        }
        public String getCheckList(){
            return checkList;
        }

        private String compileList(){
            LinkedHashMap<String, String> signaturesMap = new LinkedHashMap<>();
            String jsonCL = ah.loadPreferencesString("jsonCheckList");
            try {
                JSONObject jsonObj = new JSONObject(jsonCL);
                jsonObj.put("IMAGEQTY", ah.loadPreferences("GrandTotalOfPictures"));
                Iterator<String> iter = jsonObj.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    signaturesMap.put(key, jsonObj.getString(key));
                }
                Gson gson = new Gson();

                jsonCL = gson.toJson(signaturesMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonCL;
        }

        private void keepDbOpen(){
            if(!db_adapter.isOpen())
                db_adapter.open();
        }

    }

}