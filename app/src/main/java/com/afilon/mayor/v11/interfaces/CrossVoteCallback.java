package com.afilon.mayor.v11.interfaces;

import com.afilon.mayor.v11.model.CandidateCrossVote;
import com.afilon.mayor.v11.model.CandidateMarks;
import com.afilon.mayor.v11.model.CrossVoteBundle;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by ccano on 7/15/16.
 */
public interface CrossVoteCallback {
    public static final int TOTAL_PARTY_A = 19;
    public static final int TOTAL_PARTY_B = 12;
    public static final int TOTAL_PARTY_C = 24;
    @Deprecated
    void getTotalPartyA(int totalPartyA);
    @Deprecated
    void getTotalMarked(int totalMarked);
    @Deprecated
    void getCandidates(ArrayList<CandidateCrossVote> candidateWithMark);
    @Deprecated
    void forceFlagAdapterRefresh();
    @Deprecated
    void getCandidateVotes(LinkedHashMap<String, String> CandidatesVotes);
    @Deprecated
    void getMarks(ArrayList<CandidateMarks> Marks);
    @Deprecated
    void getCurrentBallotMarks(int currentBallotMarks);
    @Deprecated
    void aceptarAllowed(boolean allowed);


}
