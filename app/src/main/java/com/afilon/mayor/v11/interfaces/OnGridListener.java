package com.afilon.mayor.v11.interfaces;

import com.afilon.mayor.v11.model.CandidateCrossVote;
import com.afilon.mayor.v11.model.Party;

/**
 * Created by BReinosa on 6/21/2017.
 */
public interface OnGridListener {

    void onCandidateGridEvent(CandidateCrossVote candidate);

    void onPartyGridEvent(Party party);
}
