package com.afilon.mayor.v11.model;

import com.afilon.mayor.v11.R;

/**
 * Created by ccano on 7/4/16.
 */
public class CandidateFlagsGridTest {
    private int mCandidateFlag;
    private String mFlagName;


    public CandidateFlagsGridTest(int candidateFlag, String flagName) {
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
    public static  CandidateFlagsGridTest[] ITEMS = {
            new CandidateFlagsGridTest(R.drawable.amaro, "AMARO"),
            new CandidateFlagsGridTest(R.drawable.cele, "CELE"),
            new CandidateFlagsGridTest(R.drawable.indpt, "INDPT"),
            new CandidateFlagsGridTest(R.drawable.lila, "LILA"),
            new CandidateFlagsGridTest(R.drawable.mora, "MORA"),
            new CandidateFlagsGridTest(R.drawable.nara, "NARA"),

    };

    public  CandidateFlagsGridTest getItem(int id) {
        for (CandidateFlagsGridTest item: ITEMS) {
            if (item.getFlagPicture() == id) {
                return item;
            }
        }
        return null;
    }
}
