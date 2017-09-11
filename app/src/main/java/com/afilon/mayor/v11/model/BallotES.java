package com.afilon.mayor.v11.model;

import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BReinosa on 9/6/2017.
 */
public class BallotES {

    //    private ArrayList<Party> partiesSelected;
//    private ArrayList<Candidate> candidatesSelected;
    private HashMap<String, String> firstPartyList, secondPartyList, matchParties, mismatchParties;
    private HashMap<String, String> firstCandidateList, secondCandidateList, matchCandidate, mismatchCandidate;

    private int voteType;
    private float partyVote;
    private int partyMark;
    private float candidateVote;
    private int candidateMark;


    private int maxSelection;
    private int BallotNumber;
    private int electionID;
    private String JRV;

    DatabaseAdapterParlacen db_adapter;

    private static final int PARTY_MATCH = 1,
            PARTY_MISMATCH = 2,
            CANDIDATE_MATCH = 3,
            CANDIDATE_MISMATCH = 4,
            PARTY_LIST = 5,
            CANDIDATE_LIST = 6;

    private final int PLANCHA = 2;
    private final int PREFERENTIAL = 1;
    private final int CROSS = 3;
    public static final int PREF_MARK = 4;
    public static final int PLAN_MARK = 5;
    public static final int CROS_MARK = 6;
    public static final int NULO = -1;

    // initialize ballot:
    private void createNew(int ballotNumber) {
        firstPartyList = new HashMap<>();
        secondPartyList = new HashMap<>();
        firstCandidateList = new HashMap<>();
        secondCandidateList = new HashMap<>();
        BallotNumber = ballotNumber;
        partyVote = 0f;
        partyMark = 0;
        candidateVote = 0f;
        candidateMark = 0;
    }

    //----------------------------------------------------------------------------------------------
    // selection process:
    //first selection
    private void addToFirstSelection(Party party) {
        firstPartyList.put(party.getParty_preferential_election_id(), party.getParty_name());
    }

    private void removeFromFirstSelection(Party party) {
        firstPartyList.remove(party.getParty_preferential_election_id());
    }

    private void addToFirstSelection(Candidate candidate) {
        firstCandidateList.put(candidate.getCandidatePreferentialElectionID(), candidate.getCandidate_name());
    }

    private void removeFromFirstSelection(Candidate candidate) {
        firstCandidateList.remove(candidate.getCandidatePreferentialElectionID());
    }

    // second selection:
    private void addToSecondSelection(Party party) {
        secondPartyList.put(party.getParty_preferential_election_id(), party.getParty_name());
    }

    private void addToSecondSelection(Candidate candidate) {
        secondCandidateList.put(candidate.getCandidatePreferentialElectionID(), candidate.getCandidate_name());
    }

    private void removeFromSecondSelection(Party party) {
        secondPartyList.remove(party.getParty_preferential_election_id());
    }

    private void removeFromSecondSelection(Candidate candidate) {
        secondCandidateList.remove(candidate.getCandidatePreferentialElectionID());
    }

    //  compare entries:
    private void initializeMismatches() {
        matchParties = new HashMap<>();
        mismatchParties = new HashMap<>();
        matchCandidate = new HashMap<>();
        mismatchCandidate = new HashMap<>();
    }

    private void findPartyMismatches() {
        initializeMismatches();
        // candidate list:
        String tempCandidate;
        for (Map.Entry<String, String> entry : firstCandidateList.entrySet()) {
            String candidateId = entry.getKey();
            tempCandidate = secondCandidateList.get(candidateId);
            if (tempCandidate != null) {
                matchCandidate.put(candidateId, tempCandidate);
            } else {
                mismatchCandidate.put(candidateId, entry.getValue());
            }
        }
        // party list:
        String tempParty;
        for (Map.Entry<String, String> entry : firstPartyList.entrySet()) {
            String partyId = entry.getKey();
            tempParty = secondPartyList.get(partyId);
            if (tempParty != null) {
                matchParties.put(partyId, tempParty);
            } else {
                mismatchParties.put(partyId, entry.getValue());
            }
        }
    }

    // extract matches:
    private HashMap<String, String> getList(int ID) {
        HashMap<String, String> hashmap;
        switch (ID) {
            case PARTY_MATCH:
                hashmap = matchParties;
                break;
            case PARTY_MISMATCH:
                hashmap = mismatchParties;
                break;
            case CANDIDATE_MATCH:
                hashmap = matchCandidate;
                break;
            case CANDIDATE_MISMATCH:
                hashmap = mismatchCandidate;
                break;
            case PARTY_LIST:
                hashmap = secondPartyList;
                break;
            case CANDIDATE_LIST:
                hashmap = secondCandidateList;
                break;
            default:
                hashmap = new HashMap<>();
                break;
        }
        return hashmap;
    }

    //----------------------------------------------------------------------------------------------
    // Ballot validation Process:
    private void getTalley() {
        if (secondPartyList.size() > 1){
            nullifyBallot();
        } else if (secondCandidateList.size() > maxSelection){
            nullifyBallot();
        } else if (secondPartyList.size() == 1) {
            if(!areCandidatesFromSameParty()){
                // this is the case where at least one candidate on the list doesn't belong to the party
                nullifyBallot();
            }else if(secondCandidateList.size() == maxSelection || secondCandidateList.size() ==0){
                voteType = PLANCHA;
                partyVote = 1;
                partyMark =0;
                candidateVote = 1/maxSelection;
                candidateMark = 0;
            }else if(secondCandidateList.size()<maxSelection){
                voteType = PREFERENTIAL;
                partyVote = 1;
                partyMark = 1;
                candidateVote = 1/secondCandidateList.size();
                candidateMark = 1;
            }
        } else if (secondPartyList.size() < 1) {
            if(areCandidatesFromSameParty()){
                if(secondCandidateList.size() == maxSelection){
                    voteType = PLANCHA;
                    partyVote =1;
                    partyMark = 0;
                    candidateVote = 1/maxSelection;
                    candidateMark = 1;
                } else if(secondCandidateList.size() < maxSelection){
                    voteType = PREFERENTIAL;
                    partyVote = 1;
                    partyMark = 0;
                    candidateVote = 1/secondCandidateList.size();
                    candidateMark = 1;
                }

            } else {
                //cross vote:
                voteType = CROSS;
                partyVote = -1; // party votes have to be calculate per party
                candidateVote = 1/secondPartyList.size();
                partyMark = 0;
                candidateMark = 1;
            }

        }
       //todo:  assignVotesToParty();
        //return NULO;

    }
    private boolean areCandidatesFromSameParty(){

 return false;
    }
    private void nullifyBallot(){
        voteType = NULO;
        partyVote = 0f;
        partyMark = 0;
        candidateVote = 0f;
        candidateMark = 0;
    }

}
