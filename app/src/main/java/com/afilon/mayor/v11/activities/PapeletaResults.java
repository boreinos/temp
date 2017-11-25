package com.afilon.mayor.v11.activities;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.model.BallotResults;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class PapeletaResults extends AfilonActivity {
    Utilities util;
    DatabaseAdapterParlacen db_adapter;
    VotingCenter vc;
    Escrudata escrudata;
    String formatting = "%.5f";
    Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = new Utilities(this);
        util.tabletConfiguration(Build.MODEL,this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(PapeletaResults.this));
        setContentView(R.layout.activity_papeleta_results);
        //----------- end window set up ------------------------
        b = getIntent().getExtras();
        vc = b.getParcelable("com.afilon.tse.votingcenter");
        escrudata = b.getParcelable("com.afilon.tse.escrudata");

        util.saveCurrentScreen(this.getClass(),b); // persist bundle;
        //------------ end getting bundle -----------------------
        db_adapter = new DatabaseAdapterParlacen(this);
        db_adapter.open();
        ArrayList<BallotResults> Ballots = db_adapter.getBallotResults();
        Log.e("Narrator","we've got the ballot results");
        TableLayout table = (TableLayout) findViewById(R.id.result_table);
        LinearLayout headers = (LinearLayout) findViewById(R.id.header);
        Button continueBtn = (Button)findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Intent search = new Intent(this, PapeletaResults.class);
               nextActivity();

            }
        });
        util.setButtonColorGreen(continueBtn);
        ArrayList<Party> parties = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());
        Log.e("Narrator", "now we have the list of parties");
        int marcas = 0;
        int old_ballot = 0;
        float votes = 0f;
        LinearLayout header = new LinearLayout(this);
        TableRow subheader = new TableRow(this);
        headers.addView(header);
        //table.addView(header);
        table.addView(subheader);
        // jrv header & sub header
        TextView papeletaHspce = new TextView(this);
        TextView papeletaSubHspce = new TextView(this);
        TextView labelJrv = new TextView(this);
        TextView marcasJrv = new TextView(this);
        TextView votesJrv = new TextView(this);
        header.addView(papeletaHspce);
        header.addView(labelJrv);
        subheader.addView(papeletaSubHspce);
        subheader.addView(marcasJrv);
        subheader.addView(votesJrv);

        labelJrv.setText("JRV: "+vc.getJRV());
        marcasJrv.setText("Marcas");
        votesJrv.setText("Votos");



        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) labelJrv.getLayoutParams();
        LinearLayout.LayoutParams subparams = (LinearLayout.LayoutParams) marcasJrv.getLayoutParams();
        //params.height = getResources().getDimension(R.dimen.outcome_head_height);
        params.width = (int) getResources().getDimension(R.dimen.outcome_head_width);
        subparams.width = (int) getResources().getDimension(R.dimen.outcome_sub_width);

        marcasJrv.setLayoutParams(subparams);
        marcasJrv.setTextSize(getResources().getDimension(R.dimen.outcome));
        votesJrv.setLayoutParams(subparams);
        votesJrv.setTextSize(getResources().getDimension(R.dimen.outcome));

        labelJrv.setLayoutParams(params);
        papeletaHspce.setLayoutParams(subparams);

        labelJrv.setTextSize(getResources().getDimension(R.dimen.outcome_head));
        labelJrv.setGravity(Gravity.CENTER);
        marcasJrv.setGravity(Gravity.CENTER);
        votesJrv.setGravity(Gravity.CENTER);

        for(Party party: parties){
            //partyOrder.put(party.getParty_name(),i);
            TextView labelTv = new TextView(this);
            TextView marcasTv = new TextView(this);
            TextView votesTv = new TextView(this);
            header.addView(labelTv);
            subheader.addView(marcasTv);
            subheader.addView(votesTv);
            labelTv.setText(party.getParty_name());
            labelTv.setLayoutParams(params);
            labelTv.setTextSize(getResources().getDimension(R.dimen.outcome_head));
            labelTv.setGravity(Gravity.CENTER);
            marcasTv.setText("Marcas");
            votesTv.setText("Votos");

            marcasTv.setGravity(Gravity.CENTER);
            votesTv.setGravity(Gravity.CENTER);

            marcasTv.setLayoutParams(subparams);
            votesTv.setLayoutParams(subparams);

            marcasTv.setTextSize(getResources().getDimension(R.dimen.outcome));
            votesTv.setTextSize(getResources().getDimension(R.dimen.outcome));
        }

        Log.e("Narrator","header and subheader rows are built");
        int rowNumber=0;
        TableRow currentRow = createTotalRow(parties,subparams);
        table.addView(currentRow);
        View v = new View(this);
        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        v.setBackgroundColor(getResources().getColor(R.color.divisor));
        table.addView(v);
        for(BallotResults ballot: Ballots){

            if(old_ballot==ballot.ballotNumber()){
                //jrv marcas and votes:
                marcas += ballot.getMarcas();
                votes  += ballot.getVotes();


                String partyName = ballot.getParty();
                // update row:
                ((TextView)currentRow.findViewById(getRowViewId("Jrv-Marcas"))).setText(String.valueOf(marcas));
                ((TextView)currentRow.findViewById(getRowViewId("Jrv-Votes"))).setText(String.format(Locale.US, formatting, votes)); //String.valueOf(votes));
                ((TextView)currentRow.findViewById(getRowViewId(ballot.getParty()+"-Marcas"))).setText(String.valueOf(ballot.getMarcas()));
                ((TextView)currentRow.findViewById(getRowViewId(ballot.getParty()+"-Votes"))).setText(String.format(Locale.US, formatting, ballot.getVotes())); //String.valueOf(ballot.getVotes()));
                // get totals:
                // get the current totals:
                String currentJrvMarcas = ((TextView)table.findViewById(getRowViewId("Jrv-total-Marcas"))).getText().toString();
                String currentJrvVotes =((TextView)table.findViewById(getRowViewId("Jrv-total-Votes"))).getText().toString();
                String currentPartyMarcas = ((TextView)table.findViewById(getRowViewId(partyName+"-total-Marcas"))).getText().toString();
                String currentPartyVotes = ((TextView)table.findViewById(getRowViewId(partyName+"-total-Votes"))).getText().toString();
                // sum values up:
                int totalJrvMarcas    = util.parseInt(currentJrvMarcas,0)   + ballot.getMarcas();
                float totalJrvVotes   = util.parseFloat(currentJrvVotes,0)  + ballot.getVotes();
                int totalPartyMarcas  = util.parseInt(currentPartyMarcas,0) + ballot.getMarcas();
                float totalPartyVotes = util.parseFloat(currentPartyVotes,0)+ ballot.getVotes();
                // update textboxes:
                ((TextView)table.findViewById(getRowViewId("Jrv-total-Marcas"))).setText(String.valueOf(totalJrvMarcas));
                ((TextView)table.findViewById(getRowViewId("Jrv-total-Votes"))).setText(String.format(Locale.US, formatting, totalJrvVotes)); //String.valueOf(totalJrvVotes));
                ((TextView)table.findViewById(getRowViewId(partyName+"-total-Marcas"))).setText(String.valueOf(totalPartyMarcas));
                ((TextView)table.findViewById(getRowViewId(partyName+"-total-Votes"))).setText(String.format(Locale.US, formatting, totalPartyVotes)); //String.valueOf(totalPartyVotes));


            }else{
                rowNumber++;
                Log.e("Row Number",String.valueOf(rowNumber));
                currentRow = createNewRow(parties,subparams);
                marcas=ballot.getMarcas();
                votes= ballot.getVotes();
                String partyName = ballot.getParty();
 //               currentRow = row;
                table.addView(currentRow);
                ((TextView)currentRow.findViewById(getRowViewId("papeleta"))).setText(String.valueOf(ballot.ballotNumber()));
                ((TextView)currentRow.findViewById(getRowViewId("Jrv-Marcas"))).setText(String.valueOf(marcas));
                ((TextView)currentRow.findViewById(getRowViewId("Jrv-Votes"))).setText(String.format(Locale.US, formatting, votes));//String.valueOf(votes));
                ((TextView)currentRow.findViewById(getRowViewId(partyName+"-Marcas"))).setText(String.valueOf(marcas));
                ((TextView)currentRow.findViewById(getRowViewId(partyName+"-Votes"))).setText(String.format(Locale.US, formatting, votes));//String.valueOf(votes));
                // add to the sum:
                // get the current totals:
                String currentJrvMarcas = ((TextView)table.findViewById(getRowViewId("Jrv-total-Marcas"))).getText().toString();
                String currentJrvVotes =((TextView)table.findViewById(getRowViewId("Jrv-total-Votes"))).getText().toString();
                String currentPartyMarcas = ((TextView)table.findViewById(getRowViewId(partyName+"-total-Marcas"))).getText().toString();
                String currentPartyVotes = ((TextView)table.findViewById(getRowViewId(partyName+"-total-Votes"))).getText().toString();
                // sum values up:
                int totalJrvMarcas = util.parseInt(currentJrvMarcas,0)+marcas;
                float totalJrvVotes  = util.parseFloat(currentJrvVotes,0) +votes;
                int totalPartyMarcas = util.parseInt(currentPartyMarcas,0)+marcas;
                float totalPartyVotes = util.parseFloat(currentPartyVotes,0)+votes;
                // update textboxes:
                ((TextView)table.findViewById(getRowViewId("Jrv-total-Marcas"))).setText(String.valueOf(totalJrvMarcas));
                ((TextView)table.findViewById(getRowViewId("Jrv-total-Votes"))).setText(String.format(Locale.US, formatting, totalJrvVotes)); // String.valueOf(totalJrvVotes));
                ((TextView)table.findViewById(getRowViewId(partyName+"-total-Marcas"))).setText(String.valueOf(totalPartyMarcas));
                ((TextView)table.findViewById(getRowViewId(partyName+"-total-Votes"))).setText(String.format(Locale.US, formatting, totalPartyVotes)); //String.valueOf(totalPartyVotes));
            }
            old_ballot= ballot.ballotNumber();
        }
    }
    private int getRowViewId(String item) {
        return item.hashCode() & 0xfffffff;
    }

    private TableRow createNewRow(ArrayList<Party> partyArrayList, LinearLayout.LayoutParams txtparams){
        TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.trow, null);
        // add the jrv views:
        TextView papeleta = new TextView(this);
        TextView jrvMarcas = new TextView(this);
        TextView jrvVotes = new TextView(this);
        papeleta.setId(getRowViewId("papeleta"));
        jrvMarcas.setId(getRowViewId("Jrv-Marcas"));
        jrvVotes.setId(getRowViewId("Jrv-Votes"));
        // dimension constraints:
        papeleta.setLayoutParams(txtparams);
        jrvMarcas.setLayoutParams(txtparams);
        jrvVotes.setLayoutParams(txtparams);
        papeleta.setTextSize(getResources().getDimension(R.dimen.outcome));
        jrvMarcas.setTextSize(getResources().getDimension(R.dimen.outcome));
        jrvVotes.setTextSize(getResources().getDimension(R.dimen.outcome));
        jrvMarcas.setGravity(Gravity.CENTER);
        papeleta.setGravity(Gravity.CENTER);
        row.addView(papeleta);
        row.addView(jrvMarcas);
        row.addView(jrvVotes);
        for(Party party: partyArrayList){
            TextView partyMarcas = new TextView(this);
            TextView partyVotes = new TextView(this);
            partyMarcas.setId(getRowViewId(party.getParty_name()+"-Marcas"));
            partyVotes.setId(getRowViewId(party.getParty_name()+"-Votes"));
            partyMarcas.setLayoutParams(txtparams);
            partyVotes.setLayoutParams(txtparams);
            partyMarcas.setTextSize(getResources().getDimension(R.dimen.outcome));
            partyVotes.setTextSize(getResources().getDimension(R.dimen.outcome));
            partyMarcas.setGravity(Gravity.CENTER);
            row.addView(partyMarcas);
            row.addView(partyVotes);
        }
        return row;
    }
    private TableRow createTotalRow(ArrayList<Party> partyArrayList, LinearLayout.LayoutParams txtparams){
        TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.trow, null);
        // add the jrv views:
        TextView papeleta = new TextView(this);
        TextView jrvMarcas = new TextView(this);
        TextView jrvVotes = new TextView(this);
        //papeleta.setId(getRowViewId("papeleta"));
        jrvMarcas.setId(getRowViewId("Jrv-total-Marcas"));
        jrvVotes.setId(getRowViewId("Jrv-total-Votes"));

        papeleta.setGravity(Gravity.CENTER);
        papeleta.setTextSize(getResources().getDimension(R.dimen.outcome));
        papeleta.setText("Papeleta");

        // dimension constraints:
        jrvMarcas.setLayoutParams(txtparams);
        jrvVotes.setLayoutParams(txtparams);
        papeleta.setLayoutParams(txtparams);
        jrvMarcas.setTextSize(getResources().getDimension(R.dimen.outcome));
        jrvVotes.setTextSize(getResources().getDimension(R.dimen.outcome));
        jrvMarcas.setGravity(Gravity.CENTER);
        row.addView(papeleta);
        row.addView(jrvMarcas);
        row.addView(jrvVotes);
        for(Party party: partyArrayList){
            TextView partyMarcas = new TextView(this);
            TextView partyVotes = new TextView(this);
            partyMarcas.setId(getRowViewId(party.getParty_name()+"-total-Marcas"));
            partyVotes.setId(getRowViewId(party.getParty_name()+"-total-Votes"));
            partyMarcas.setLayoutParams(txtparams);
            partyVotes.setLayoutParams(txtparams);
            partyMarcas.setTextSize(getResources().getDimension(R.dimen.outcome));
            partyVotes.setTextSize(getResources().getDimension(R.dimen.outcome));
            partyMarcas.setGravity(Gravity.CENTER);
            row.addView(partyMarcas);
            row.addView(partyVotes);
        }
        return row;
    }
    private void nextActivity(){
        Intent search = new Intent(this, CrossVoteSummaryActivity.class);
        search.putExtras(b);
        startActivity(search);
        finish();
    }





}
