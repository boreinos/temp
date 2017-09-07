package com.afilon.mayor.v11.activities;

//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
//import android.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.DialogToConfirmDui;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns;
import com.afilon.mayor.v11.fragments.PartyFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.model.AppLog;
import com.afilon.mayor.v11.model.Candidate;
import com.afilon.mayor.v11.model.CandidateMarks;
import com.afilon.mayor.v11.model.CrossVoteBundle;
import com.afilon.mayor.v11.model.CrossVoteCount;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.PreferentialCandidateVotes;
import com.afilon.mayor.v11.model.PreferentialVotoBanderas;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CrossVoteSummaryActivity extends AfilonFragmentActivity
        implements PartyFragment.OnListFragmentInteractionListener{

    private ArrayList<Party> partyArrayList;
    private DatabaseAdapterParlacen db_adapter;
    private Escrudata escrudata;
    private VotingCenter vc;
    private String title;
    private Utilities ah = new Utilities(this);
    private ChallengeHelper challengeHelper;

    private final static int RECHAZAR = 3;
    private final static int ACEPTAR = 4;

    private AppLog applog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ah.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cross_vote_summary);

        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
                CrossVoteSummaryActivity.this));

        db_adapter = new DatabaseAdapterParlacen(this);
        //-------------------SET UP CHALLENGE HELPER -----------------------------------------------
        challengeHelper = new ChallengeHelper(this);
        challengeHelper.setTools(ah,db_adapter);
        challengeHelper.addCustomKeyBoards(R.id.keyboardview,R.id.keyboardview2);
        challengeHelper.addRoutine(RECHAZAR,rejectRoutine);
        challengeHelper.addRoutine(ACEPTAR,continueRoutine);
        //------------------------------------------------------------------------------------
        //load info from bundle:
        Bundle b = getIntent().getExtras();
        vc = b.getParcelable("com.afilon.tse.votingcenter");
        escrudata = b.getParcelable("com.afilon.tse.escrudata");

        ah.saveCurrentScreen(this.getClass(),b);

        if(Consts.LOCALE.contains("HON")){
            title = Consts.TOTAL_VOTE_SUMMARY;
        }else title = escrudata.getPageTitle();// b.getString("title");

        if (title == null) {
            throw new RuntimeException(this.toString()
                    + "     Title must be passed through bundle to the activity");
        }
        //------------------------------------------------------------------------------------

        //load headers:
        ((TextView) findViewById(R.id.vote_center)).setText(title);
        ((TextView) findViewById(R.id.textView13)).setText(vc.getMunicipioString());
        ((TextView) findViewById(R.id.textView15)).setText(vc.getDepartamentoString());
        ((TextView) findViewById(R.id.textView25)).setText(vc.getJrvString());
        ((TextView) findViewById(R.id.textView23)).setText(ah.loadPreferencesString("barcodeSaved"));

        //--------------------------------------------------------------------------------------
        loadDataSQLite();

        (findViewById(R.id.rechezar_btn)).setOnClickListener(rechazar());
        (findViewById(R.id.aceptar_btn)).setOnClickListener(nextActivity());
        findViewById(R.id.horizontal_sv).setOnTouchListener(clearTouchSource());

        applog = new AppLog(vc.getJrvString());

    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
//        if (!db_adapter.isOpen()) db_adapter.open();
        //progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Dispatch onResume() to fragments.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        //progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db_adapter.isOpen()) db_adapter.close();
    }

    List<android.support.v4.app.Fragment> fragments;
    private float total_marks=0f;

    private void loadDataSQLite() {
        db_adapter.open();
        partyArrayList = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());//"10022");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for (Party party : partyArrayList) {
            ArrayList<Candidate> candidateList = new ArrayList<>();
            //get all the candidates of that party:
            if (title.equals(Consts.CROSS_VOTE_SUMMARY)) {
                candidateList = db_adapter.getCandidatesWithMarks(party.getParty_preferential_election_id());
                candidateList = db_adapter.getCandidateCrossVotes(candidateList, party.getParty_preferential_election_id());
                party.setParty_cross_votes(db_adapter.getPartyCrossVotes(party.getParty_preferential_election_id()));
            } else {
                candidateList = db_adapter.getCandidatesWithTotalMarks(party.getParty_preferential_election_id());
                candidateList = db_adapter.getCandidateCrossVotes(candidateList, party.getParty_preferential_election_id());
                candidateList = db_adapter.getCandidateTotalVotes(candidateList, party.getParty_preferential_election_id());
                party.setParty_cross_votes(db_adapter.getTotalPartyVotes(party.getParty_preferential_election_id()));
            }
            party.setCandidateList(candidateList);
            total_marks+= party.getParty_cross_votes();
            ((LinearLayout) findViewById(R.id.headers)).addView(addHeader(party.getParty_name(), party.getParty_cross_votes()));
            PartyFragment partyone = PartyFragment.newInstance(candidateList, title);
            fragmentTransaction.add(R.id.fragmentContainer, partyone);
        }
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        fragments = fragmentManager.getFragments();
        db_adapter.close();
        if(Consts.LOCALE.equals(Consts.HONDURAS)){
            findViewById(R.id.mark_view).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.total_marks)).setText(String.format(Locale.US,getResources().getString(R.string.floatFormating),total_marks));
        }

    }

    private View addHeader(String name, float votes) {
        TextView tv = new TextView(this);
        int width = (int) convertDpToPixel(423f, this);
        int marging = (int) convertDpToPixel(17f, this);
        //int txtSize = (int) convertDpToPixel(25f, this);
        String message = name + " "+getResources().getString(R.string.voto)+"   " + String.format(Locale.US, getResources().getString(R.string.floatFormating), votes);

        if(Consts.LOCALE.contains("HON")){
            message = name+ " "+getResources().getString(R.string.voto)+"   " + String.format(Locale.US, getResources().getString(R.string.floatFormating), votes);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(marging, 0, 0, marging);
        tv.setLayoutParams(params);
        tv.setText(message);
//        tv.setTextSize(txtSize);
        tv.setTextSize(25f);
//        tv.setPadding(2,2,2,2);
        tv.setTypeface(null, Typeface.BOLD);
        //     tv.setGravity(Gravity.CENTER);

//        Log.e("WIDTH IN DP: ", String.valueOf(convertPixelsToDp(16, this)));

        return tv;
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }


    private View.OnClickListener rechazar() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                challengeHelper.createDialog(getResources().getString(R.string.rejectChallenge), RECHAZAR);
            }
        };
    }


    private View.OnClickListener nextActivity() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                challengeHelper.createDuiChallenge(getResources().getString(R.string.aceptChallenge), ACEPTAR);
            }
        };
    }


    private void updateCrossVoteValues() {
        db_adapter.open();
        //todo update party totals:
        for (Party party : partyArrayList) {
            float cross_votes = party.getParty_cross_votes();
            db_adapter.updatePartyCrossVotes(cross_votes, party.getParty_preferential_election_id());
        }
        db_adapter.close();

    }

    private void updateCandidateFinalVotes() {
        db_adapter.open();
        for (Party party : partyArrayList) {
            ArrayList<Candidate> candidates = party.candidates;
            for (Candidate candidate : candidates) {
                db_adapter.updateCandidateFinalVote(candidate);
            }
        }
    }

    private LinearLayout addPartyInformation(String partyName) {
        return new LinearLayout(this, null);
    }

    //    @Override
//    public void onListFragmentInteraction(Candidate item) {
//        Log.e(item.getCandidate_name(), item.getCandidate_order());
//
//    }
    View touchSource;
    RecyclerView.OnItemTouchListener listener = new RecyclerView.SimpleOnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }
    };

    @Override
    public void onListFragmentTouchEvent(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && ((RecyclerView) v).getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            touchSource = null;
            for (android.support.v4.app.Fragment fg : fragments) {
                ((RecyclerView) fg.getView()).removeOnItemTouchListener(listener);
            }
            return;
        }
        if (touchSource == null) {
            touchSource = v;
            for (android.support.v4.app.Fragment fg : fragments) {
                if (fg.getView().equals(v)) continue;
                ((RecyclerView) fg.getView()).addOnItemTouchListener(listener);
            }
        }
    }

    @Override
    public void onListFragmentScroll(RecyclerView v, int dx, int dy) {
        if (!v.equals(touchSource)) {
            return;
        }
        for (android.support.v4.app.Fragment fg : fragments) {
            if (fg.getView().equals(v)) {
                continue;
            }
            fg.getView().scrollBy(dx, dy);
        }
    }


    private void proceedToNextPage() {
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        Bundle bc = new Bundle();
        bc.putParcelable("com.afilon.tse.votingcenter", vc);
        bc.putParcelable("com.afilon.tse.escrudata", escrudata);
        bc.putParcelable("com.afilon.mayor.applog", applog);
        Intent intent;
        switch (title) {
            case Consts.TOTAL_VOTE_SUMMARY:
                //CARLOS: 2016-10-19
                updateCandidateFinalVotes();
                intent = new Intent(CrossVoteSummaryActivity.this, Consts.PREFSUMACT);
                intent.putExtras(bc);
                startActivity(intent);
                finish();
                break;
            case Consts.CROSS_VOTE_SUMMARY:
                //Go to the Total:
                updateCrossVoteValues();
                escrudata.setPageTitle(Consts.TOTAL_VOTE_SUMMARY);
//                bc.putString("title", Consts.TOTAL_VOTE_SUMMARY);
                intent = new Intent(CrossVoteSummaryActivity.this, Consts.SUMCROSSACT);
                intent.putExtras(bc);
                startActivity(intent);
                finish();
                break;
        }

    }

    private void rejectResults(){
        Bundle bc = new Bundle();
        bc.putParcelable("com.afilon.tse.votingcenter", vc);
        bc.putParcelable("com.afilon.tse.escrudata", escrudata);
        Intent intent;
        /** WHICH SCREEN IS BEING VIEWED */
        switch (title) {
            case Consts.CROSS_VOTE_SUMMARY:
                //Todo: clean databse from cross votes:
                ah.savePreferences("HowManyBallotSoFar", 0);
                db_adapter.open();
                db_adapter.deleteAllCandidateCrossVote();
                db_adapter.close();
                intent = new Intent(CrossVoteSummaryActivity.this,
                        Consts.CROSSVOTEACT);
                intent.putExtras(bc);
                startActivity(intent);
                break;
            case Consts.TOTAL_VOTE_SUMMARY:
                db_adapter.open();
                db_adapter.deletePreferentialVotoBanderas();
                db_adapter.deletePartiesPreferentialVotes();
                db_adapter.deleteAllPreferentialCandidateVotes();
                db_adapter.deleteAllCandidateCrossVote();
                db_adapter.deleteTemp();
                db_adapter.deleteAllCandidateMarks();
                db_adapter.close();

                ah.savePreferences("rechazada", true);
                ah.savePreferences("firstScreen", true);
                AppLog applog = new AppLog(vc.getJrvString());
                bc.putParcelable("com.afilon.mayor.applog", applog);
                intent = new Intent(CrossVoteSummaryActivity.this,
                        Consts.EMPTYTABLEACT);
                intent.putExtras(bc);
                startActivity(intent);
                break;

        }
        finish();
    }

    private ChallengeHelper.OnApprove continueRoutine = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            proceedToNextPage();
        }
    };

    private ChallengeHelper.OnApprove rejectRoutine = new ChallengeHelper.OnApprove() {
        @Override
        public void approved() {
            rejectResults();
        }
    };

    private View.OnTouchListener clearTouchSource() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.e("FRAGMET LISTENR", "TOUCH SOURCE IS NULL NOW");
                    touchSource = null;
                    for (android.support.v4.app.Fragment fg : fragments) {
                        ((RecyclerView) fg.getView()).removeOnItemTouchListener(listener);
                    }
                }
                return false;
            }
        };
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
