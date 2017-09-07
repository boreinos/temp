package com.afilon.mayor.v11.model;

import com.afilon.mayor.v11.R;

/**
 * Created by ccano on 7/1/16.
 */
public class CandidateFlagsGrid {
    private int mCandidateFlag;
    private String mFlagName;


    public CandidateFlagsGrid(int candidateFlag, String flagName) {
        this.mCandidateFlag =  candidateFlag;
        this.mFlagName =  flagName;
    }

    public int getFlagPicture() {
        return mCandidateFlag;
    }

    public String getFlagName() {
        return mFlagName;
    }


    //Let's render 6 parties
    public static  CandidateFlagsGrid[] ITEMS = {
            new CandidateFlagsGrid(R.drawable.amaro, "AMARO"),
            new CandidateFlagsGrid(R.drawable.cele, "CELE"),
            new CandidateFlagsGrid(R.drawable.indpt, "INDPT"),
            new CandidateFlagsGrid(R.drawable.lila, "LILA"),
            new CandidateFlagsGrid(R.drawable.mora, "MORA"),
            new CandidateFlagsGrid(R.drawable.nara, "NARA"),

    };

    public  CandidateFlagsGrid getItem(int id) {
        for (CandidateFlagsGrid item: ITEMS) {
            if (item.getFlagPicture() == id) {
                return item;
            }
        }
        return null;
    }
}