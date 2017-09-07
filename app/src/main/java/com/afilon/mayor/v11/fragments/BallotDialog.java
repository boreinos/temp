package com.afilon.mayor.v11.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.activities.CrossedVoteActivity;

/**
 * Created by BReinosa on 8/9/2017.
 */
public class BallotDialog extends DialogFragment {
    public interface BallotDialogListener{
        public void onDialogValidClick(DialogFragment dialog);
        public void onDialogNullClick(DialogFragment dialog);
        public void onDialogBlankClick(DialogFragment dialog);
    }

    BallotDialogListener mListener;
    String header="";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Use the Builder class for convenient dialog construction
        final TextView myView = (TextView) getActivity().getLayoutInflater().inflate(R.layout.alert_header,null); //  new TextView(getActivity());
//        myView.setText(R.string.bdmessage);
        myView.setText(header);
        myView.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle(R.string.bdmessage)
                builder.setCustomTitle(myView)
                .setPositiveButton(R.string.blankVote, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogBlankClick(BallotDialog.this);

                    }
                })
                .setNegativeButton(R.string.nullVotes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // add to null votes and move to next ballot
                        mListener.onDialogNullClick(BallotDialog.this);
                    }
                }).setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //  add to valid votes and enter data
                mListener.onDialogValidClick(BallotDialog.this);
            }
        });

//        builder.setView()
        // Create the AlertDialog object and return it
        builder.setCancelable(false);

        final AlertDialog alert = builder.create();
        alert.show();
        alert.getWindow().getAttributes();
        alert.setCanceledOnTouchOutside(false);
        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(30f);
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(30f);
        alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(30f);

        alert.getWindow().setLayout(1200,300);
//        TextView textView = (TextView) alert.findViewById(R.id.alertTitle);
//        if(textView!=null){
//            Log.e("BALLOT DIALOG","WTF?");
//            textView.setTextSize(40f);
//        }

//                ((TextView)alert.getWindow().findViewById(android.R.id.title)).setTextSize(40f);


//        alert.setOnShowListener(new DialogInterface.OnShowListener(){
//            @Override
//            public void onShow(DialogInterface dialog) {
//
//
//
//            }
//        });
//        return builder.create();
        return alert;
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        //verify this is a crossvote activity:
        CrossedVoteActivity act;
        try{
          act = (CrossedVoteActivity) activity;
          mListener = act.getDialogListener();
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+"must be a crossed vote activity");
        }

    }
    public void setHeader(String message){
        header=message;
    }

}
