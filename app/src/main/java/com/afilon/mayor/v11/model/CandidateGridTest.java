package com.afilon.mayor.v11.model;

import com.afilon.mayor.v11.R;

/**
 * Created by ccano on 7/6/16.
 */
public class CandidateGridTest {
    private int mCandidatePicture;
    private String mCandidateName;
    private String mCandidateExtra;


    public CandidateGridTest(int candidatePicture, String candidateName, String candidateExtra) {
        this.mCandidatePicture =  candidatePicture;
        this.mCandidateName =  candidateName;
        this.mCandidateExtra = candidateExtra;
    }

    public int getCandidatePicture() {
        return mCandidatePicture;
    }

    public String getCandidateName() {
        return mCandidateName;
    }

    public String getCandidateExtra() {
        return mCandidateExtra;
    }

    public static  CandidateGridTest[] ITEMS = {
            new CandidateGridTest(R.drawable.candidate1, "Candidate 1 name", "Candidate 1 Extra"),
            new CandidateGridTest(R.drawable.candidate2, "Candidate 2 name", "Candidate 2 Extra"),
            new CandidateGridTest(R.drawable.candidate3, "Candidate 3 name", "Candidate 3 Extra"),
            new CandidateGridTest(R.drawable.candidate4, "Candidate 4 name", "Candidate 4 Extra"),
            new CandidateGridTest(R.drawable.candidate5, "Candidate 5 name", "Candidate 5 Extra"),
            new CandidateGridTest(R.drawable.candidate6, "Candidate 6 name", "Candidate 6 Extra"),
            new CandidateGridTest(R.drawable.candidate7, "Candidate 7 name", "Candidate 7 Extra"),
            new CandidateGridTest(R.drawable.candidate8, "Candidate 8 name", "Candidate 8 Extra"),
            new CandidateGridTest(R.drawable.candidate1, "Candidate 9 name", "Candidate 9 Extra"),
            new CandidateGridTest(R.drawable.candidate2, "Candidate 10 name", "Candidate 10 Extra"),
            new CandidateGridTest(R.drawable.candidate3, "Candidate 11 name", "Candidate 11 Extra"),
            new CandidateGridTest(R.drawable.candidate4, "Candidate 12 name", "Candidate 12 Extra"),
            new CandidateGridTest(R.drawable.candidate5, "Candidate 13 name", "Candidate 13 Extra"),
            new CandidateGridTest(R.drawable.candidate6, "Candidate 14 name", "Candidate 14 Extra"),
            new CandidateGridTest(R.drawable.candidate7, "Candidate 15 name", "Candidate 15 Extra"),
            new CandidateGridTest(R.drawable.candidate8, "Candidate 16 name", "Candidate 16 Extra"),
            new CandidateGridTest(R.drawable.candidate1, "Candidate 17 name", "Candidate 17 Extra"),
            new CandidateGridTest(R.drawable.candidate2, "Candidate 18 name", "Candidate 18 Extra"),
            new CandidateGridTest(R.drawable.candidate3, "Candidate 19 name", "Candidate 19 Extra"),
            new CandidateGridTest(R.drawable.candidate4, "Candidate 20 name", "Candidate 20 Extra"),
            new CandidateGridTest(R.drawable.candidate5, "Candidate 21 name", "Candidate 21 Extra"),
            new CandidateGridTest(R.drawable.candidate6, "Candidate 22 name", "Candidate 22 Extra"),
            new CandidateGridTest(R.drawable.candidate7, "Candidate 23 name", "Candidate 23 Extra"),
            new CandidateGridTest(R.drawable.candidate8, "Candidate 24 name", "Candidate 24 Extra"),
            new CandidateGridTest(R.drawable.candidate1, "Candidate 25 name", "Candidate 25 Extra"),
            new CandidateGridTest(R.drawable.candidate2, "Candidate 26 name", "Candidate 26 Extra"),
            new CandidateGridTest(R.drawable.candidate3, "Candidate 27 name", "Candidate 27 Extra"),
            new CandidateGridTest(R.drawable.candidate4, "Candidate 28 name", "Candidate 28 Extra"),
            new CandidateGridTest(R.drawable.candidate5, "Candidate 29 name", "Candidate 29 Extra"),
            new CandidateGridTest(R.drawable.candidate6, "Candidate 30 name", "Candidate 30 Extra"),
            new CandidateGridTest(R.drawable.candidate7, "Candidate 31 name", "Candidate 31 Extra"),
            new CandidateGridTest(R.drawable.candidate8, "Candidate 32 name", "Candidate 32 Extra"),
            new CandidateGridTest(R.drawable.candidate1, "Candidate 33 name", "Candidate 33 Extra"),
            new CandidateGridTest(R.drawable.candidate2, "Candidate 34 name", "Candidate 34 Extra"),
            new CandidateGridTest(R.drawable.candidate3, "Candidate 35 name", "Candidate 35 Extra"),
            new CandidateGridTest(R.drawable.candidate4, "Candidate 36 name", "Candidate 36 Extra"),
            new CandidateGridTest(R.drawable.candidate5, "Candidate 37 name", "Candidate 37 Extra"),
            new CandidateGridTest(R.drawable.candidate6, "Candidate 38 name", "Candidate 38 Extra"),
            new CandidateGridTest(R.drawable.candidate7, "Candidate 39 name", "Candidate 39 Extra"),
            new CandidateGridTest(R.drawable.candidate8, "Candidate 40 name", "Candidate 40 Extra"),
            new CandidateGridTest(R.drawable.candidate1, "Candidate 41 name", "Candidate 41 Extra"),
            new CandidateGridTest(R.drawable.candidate2, "Candidate 42 name", "Candidate 42 Extra"),
            new CandidateGridTest(R.drawable.candidate3, "Candidate 43 name", "Candidate 43 Extra"),
            new CandidateGridTest(R.drawable.candidate4, "Candidate 44 name", "Candidate 44 Extra"),
            new CandidateGridTest(R.drawable.candidate5, "Candidate 45 name", "Candidate 45 Extra"),
            new CandidateGridTest(R.drawable.candidate6, "Candidate 46 name", "Candidate 46 Extra"),
            new CandidateGridTest(R.drawable.candidate7, "Candidate 48 name", "Candidate 47 Extra"),
            new CandidateGridTest(R.drawable.candidate8, "Candidate 48 name", "Candidate 48 Extra"),
    };

    public  CandidateGridTest getItem(int id) {
        for (CandidateGridTest item: ITEMS) {
            if (item.getCandidatePicture() == id) {
                return item;
            }
        }
        return null;
    }
}
