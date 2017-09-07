package com.afilon.mayor.v11.model;

import android.util.Log;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.ContextHandler;
import com.thoughtworks.xstream.InitializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by BReinosa on 6/27/2017.
 */
public class Ballot {
    private HashMap<String, CandidateCrossVote> List1, List2, matches, mismatch; // happy?
    private int maxBallotSize;
    private HashMap<String, Party> partyList1, partyList2;
    private boolean isConfirmed = false;
    private boolean firstEntry = true;
    private ArrayList<Party> partyArrayList;
    private int ballotNumber;
    private String jrv, preferentialId;
    //-----------------------------------
    private int Marks=0;
    //-----------------------------------
    private final int PLANCHA = 2;
    private final int PREFERENTIAL = 1;
    private final int CROSS = 3;
    public static final int PREF_MARK= 4;
    public static final int PLAN_MARK=5;
    public static final int CROS_MARK=6;


    public Ballot(int maxBallotSize) {
        this.maxBallotSize = maxBallotSize;
        List1 = new HashMap<>();
        List2 = new HashMap<>();
        partyList1 = new HashMap<>();
    }

    public void setLocation(String jrv, String preferentialId){
        this.jrv = jrv;
        this.preferentialId = preferentialId;
    }

    public HashMap<String, CandidateCrossVote> getFirstMarks(){return List1;}

    public HashMap<String, CandidateCrossVote> getSecondMarks(){
        return List2;
    }

    public void setMarks(int marks){
        Marks = marks;
    }

    private void resetMarks(){
        setMarks(0);
    }

    public boolean verifyMarks(){
        return Marks==getVotes();
    }

    public void setFirstEntry(boolean isFirstEntry) {
        this.firstEntry = isFirstEntry;
    }

    public void addCandidate(CandidateCrossVote candidate) {
        if (firstEntry) {
            CandidateCrossVote guy = List1.put(candidate.getCandidatePrefElecId(), candidate);
            if (guy != null) {
                Log.e("CROSSVOTE/BR", "OLD MEMBER REPLACED " + guy.getCandidateName());
            }
            List1 = updateCandidateVote(List1);
        } else {
            List2.put(candidate.getCandidatePrefElecId(), candidate);
            List2 = updateCandidateVote(List2);
        }
        updatePartyList(firstEntry);
    }

    public void removeCandidate(CandidateCrossVote candidate) {
        if (firstEntry) {
            List1.remove(candidate.getCandidatePrefElecId());
            List1 = updateCandidateVote(List1);
        } else {
            List2.remove(candidate.getCandidatePrefElecId());
            List2 = updateCandidateVote(List2);
        }
        updatePartyList(firstEntry);
    }

    public boolean isBallotFull() {
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        return list.size() == maxBallotSize;
    }
    public boolean partyHasVotes(String partyId){
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        for(Map.Entry<String, CandidateCrossVote> entry: list.entrySet()){
            if(entry.getValue().getPartyElectionId().equals(partyId)) return true;
        }
        return false;
    }

    public int getMaxBallotSize(){
        return maxBallotSize;
    }

    public HashMap<String, CandidateCrossVote> confirmBallotEntries() {
        initializeMatches();
        if (List1.size() < List2.size()) {
            // get a list of matches and mismatches, then compare the second list to matches
            // in order to append all mismatches to the mismatch list.
            compare(List1, List2);
            compare(List2, matches);
        } else {
            compare(List2, List1);
            compare(List1, matches);
        }
        popMismatches();
        findMismatchParty();
        return mismatch;
    }

    private void resetMismatchParty(){
        for(int i=0; i<partyArrayList.size(); i++){
            String partyId = partyArrayList.get(i).getParty_preferential_election_id();
            partyList2.get(partyId).clearMismatches();
            partyArrayList.get(i).clearMismatches();
        }
    }

    private void findMismatchParty(){
        resetMismatchParty();
        if (mismatch.size()>0){
            Log.e("MISMATCHES",String.valueOf(mismatch.size()));
            for(Map.Entry<String, CandidateCrossVote> entry: mismatch.entrySet()){
                partyList1.get(entry.getValue().getPartyElectionId()).addMismatch(1);
            }
            updatePartyArrayList(partyList1);
        }
    }

    public boolean hasMismatches(){
        return mismatch.size()>0;
    }

    private HashMap<String, CandidateCrossVote> updateCandidateVote(HashMap<String, CandidateCrossVote> list){
        if(Consts.LOCALE.contains("ELSA")){
            return updateCandidateVoteSV(list);
        }
        return updateCandidateVoteHN(list);
    }

    private HashMap<String, CandidateCrossVote> updateCandidateVoteSV(HashMap<String, CandidateCrossVote> list) {
        HashMap<String, CandidateCrossVote> tempList = new HashMap<>();
        for (Map.Entry<String, CandidateCrossVote> entry : list.entrySet()) {
            CandidateCrossVote candidate = entry.getValue();
            candidate.setCandidateVote(1f / list.size());
            Log.e("BR/CROSS","CANDIDATE VOTE: "+String.valueOf(candidate.getCandidateVote()));
            tempList.put(candidate.getCandidatePrefElecId(), candidate);
        }
        return tempList;
    }

    private HashMap<String, CandidateCrossVote> updateCandidateVoteHN(HashMap<String, CandidateCrossVote> list) {
        HashMap<String, CandidateCrossVote> tempList = new HashMap<>();
        for (Map.Entry<String, CandidateCrossVote> entry : list.entrySet()) {
            CandidateCrossVote candidate = entry.getValue();
            candidate.setCandidateVote(1f);
            tempList.put(candidate.getCandidatePrefElecId(), candidate);
        }
        return tempList;
    }

    private void compare(HashMap<String, CandidateCrossVote> firstList, HashMap<String, CandidateCrossVote> secondList) {
        //firstList is always greater
        CandidateCrossVote tempCandidate;
        for (Map.Entry<String, CandidateCrossVote> entry : firstList.entrySet()) {
            String candidateId = entry.getKey();
            tempCandidate = secondList.get(candidateId);
            if (tempCandidate != null) {
                matches.put(candidateId, tempCandidate);
            } else {
                mismatch.put(candidateId, entry.getValue());
            }
        }
    }

    private void initializeMatches() {
        matches = new HashMap<>();
        mismatch = new HashMap<>();
    }

    private void clearMatches() {
        matches = null;
        mismatch = null;
    }

    private void initializePartyList() {
        for (Party party : partyArrayList) {
            partyList1.put(party.getParty_preferential_election_id(), party);
        }
    }

    public int getVotes(){
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        return list.size();
    }

    public String getSize() {
//            HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
//            return String.valueOf(list.size());
        return String.valueOf(getVotes());
    }

    private void resetBallot() {
        //todo: find resetBallot
        //counted marks:
        resetMarks();
        // candidate votes
        List1 = List2 = null;
        List1 = new HashMap<>();
        List2 = new HashMap<>();
        //party votes
        partyList1 = partyList2 = null;
        partyList1 = new HashMap<>();
        partyList2 = new HashMap<>();
        //update partyArrayList:
        for (int k = 0; k < partyArrayList.size(); k++) {
            partyArrayList.get(k).setBallotVotes(0f);
            partyArrayList.get(k).setPartyMarks(0);
            partyArrayList.get(k).clearMismatches();
            partyList1.put(partyArrayList.get(k).getParty_preferential_election_id(), partyArrayList.get(k));
        }
        clearMatches();
    }

    public void newBallot(int ballotNumber){
        this.ballotNumber = ballotNumber;
        resetBallot();
    }

    public void setupNextEntry() {
        partyList2 = new HashMap<>();
        for (int k = 0; k < partyArrayList.size(); k++) {
            partyArrayList.get(k).setBallotVotes(0f);
            partyArrayList.get(k).setPartyMarks(0);
            partyList2.put(partyArrayList.get(k).getParty_preferential_election_id(), partyArrayList.get(k));
        }
    }

    private void updatePartyList(boolean firstIteration) {
        if (firstIteration) {
            partyList1 = clearVotes(partyList1);
            partyList1 = updatePartyVote(List1, partyList1);
        } else {
            partyList2 = clearVotes(partyList2);
            partyList2 = updatePartyVote(List2, partyList2);
        }

    }

    public HashMap<String, Party> updatePartyVote(HashMap<String, CandidateCrossVote> list, HashMap<String, Party> partyList) {
        for (Map.Entry<String, CandidateCrossVote> entry : list.entrySet()) {
            String partyId = entry.getValue().getPartyElectionId();
            Party party = partyList.get(partyId);
            Log.e("BR/CROSS","Candidate Vote in List"+String.valueOf(entry.getValue().getCandidateVote()));
            float partyVotes = entry.getValue().getCandidateVote() + party.getBallotVotes();
            int partyMarks = 1 + party.getPartyMarks();
            party.setBallotVotes(partyVotes);
            party.setPartyMarks(partyMarks);
            partyList.put(partyId, party);
        }
        updatePartyArrayList(partyList);
        return partyList;
    }

    private void updatePartyArrayList(HashMap<String, Party> partyList){
        for (int i = 0; i < partyArrayList.size(); i++) {
            String partyId = partyArrayList.get(i).getParty_preferential_election_id();
            Party party = partyList.get(partyId);
            partyArrayList.set(i, party);
        }
    }

    public HashMap<String, Party> clearVotes(HashMap<String, Party> partyList) {
        HashMap<String, Party> tempParty = new HashMap<>();
        for (Map.Entry<String, Party> entry : partyList.entrySet()) {
            Party party = entry.getValue();
            party.setBallotVotes(0f);
            party.setPartyMarks(0);
            tempParty.put(party.getParty_preferential_election_id(), party);
        }
        return tempParty;
    }

    public ArrayList<Party> getPartyArrayList(){
        return partyArrayList;
    }

    public void setPartyArrayList(ArrayList<Party> partylist){
        partyArrayList = partylist;
        initializePartyList();
    }

    public ArrayList<CrossVoteBundle> buildVoteBundle() {
        //            if(!isConfirmed){            } //todo: determine if confirmed.
        ArrayList<CrossVoteBundle> CVBundle = new ArrayList<CrossVoteBundle>();
        for (Map.Entry<String, CandidateCrossVote> entry : List2.entrySet()) {
            CandidateCrossVote candidate = entry.getValue();
            CVBundle.add(new CrossVoteBundle(
                    jrv,
                    preferentialId,
                    candidate.getPartyElectionId(),
                    candidate.getCandidatePrefElecId(),
                    candidate.getCandidateVote(),
                    String.valueOf(ballotNumber)
            ));
        }
        return CVBundle;
    }

    public String voteMultiplier(){
        if(Consts.LOCALE.contains("HON")){
            return "1";
        }
        return voteMultiplierSV();
    }

    public String voteMultiplierSV() {
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        if (list.size() != 0) {
            return String.format(Locale.US,"%.4f",(1f / list.size()));
//            return Float.toString((1f / list.size()));
        }
        return "0";
    }

    public boolean isReady(ArrayList<String> partyListIds){
        if (Consts.LOCALE.contains("HON")) {
            return !isBallotEmpty();
        }
        return isCrossVote(partyListIds);
    }

    public boolean isCrossVote(ArrayList<String> partyListIds) {
        HashMap<String, Party> list = firstEntry ? partyList1 : partyList2;
        if(list.isEmpty()) return false;

        int count = 0;
        for (String partyId : partyListIds) {
            if (list.get(partyId).getBallotVotes() != 0) {
                count++;
                if(count>1){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBallotEmpty(){
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        return list.isEmpty();
    }

//    public void markCandidatesFrom(String partyElectionId) {
//        if (firstEntry) {
//            List1 = selectCandidatesFrom(partyElectionId);
//        } else {
//            List2 = selectCandidatesFrom(partyElectionId);
//        }
//        updatePartyList(firstEntry);
//    }

    public void markCandidatesFrom(ArrayList<Candidate> candidates) {
        if (firstEntry) {
            List1 = selectCandidatesFrom(candidates);
        } else {
            List2 = selectCandidatesFrom(candidates);
        }
        updatePartyList(firstEntry);
    }

    public void clearCandidates(){
        if (firstEntry) {
            List1 = new HashMap<>();
        } else {
            List2 = new HashMap<>();
        }
        updatePartyList(firstEntry);
    }

    private HashMap<String, CandidateCrossVote> selectCandidatesFrom(ArrayList<Candidate> candidates) {
        HashMap<String, CandidateCrossVote> list = new HashMap<>();
        //todo: move out of here:
        //todo-------------------------------------
        float singleVote;
        if(Consts.LOCALE.contains("ELSA")){
            singleVote = 1f / candidates.size();
        }else{
            singleVote = 1f;
        }
        // todo------------------------------------
        for (Candidate candidate : candidates) {

            CandidateCrossVote crossVote = new CandidateCrossVote(0, candidate.getCandidate_name(),
                    candidate.getCandidateID(), Integer.valueOf(candidate.getCandidate_image()), // might not need this garbage
                    candidate.getPartyName(), candidate.getCandidatePreferentialElectionID(),
                    true, singleVote);

            crossVote.setPartyElectionId(candidate.getPartyPreferentialElectionID());
            list.put(candidate.getCandidatePreferentialElectionID(), crossVote);
        }
        return list;
    }

//    private HashMap<String, CandidateCrossVote> selectCandidatesFrom(String partyElectionID) {
//        HashMap<String, CandidateCrossVote> list = new HashMap<>();
//        ArrayList<Candidate> candidates = db_adapter.getParlacenCandidatesArrayList(partyElectionID);
//        //todo: move out of here:
//        //todo-------------------------------------
//        float singleVote;
//        if(Consts.LOCALE.contains("ELSA")){
//            singleVote = 1f / candidates.size();
//        }else{
//            singleVote = 1f;
//        }
//        // todo------------------------------------
//        for (Candidate candidate : candidates) {
//
//            candidate.setCandidate_image(String.valueOf(getResources().getIdentifier("pic" +
//                            candidate.getCandidatePreferentialElectionID().toLowerCase(), "drawable",
//                    getApplicationContext().getPackageName())));
//
//            CandidateCrossVote crossVote = new CandidateCrossVote(0, candidate.getCandidate_name(),
//                    candidate.getCandidateID(), Integer.valueOf(candidate.getCandidate_image()), // might not need this garbage
//                    candidate.getPartyName(), candidate.getCandidatePreferentialElectionID(),
//                    true, singleVote);
//            crossVote.setPartyElectionId(partyElectionID);
//            list.put(candidate.getCandidatePreferentialElectionID(), crossVote);
//        }
//        return list;
//    }

    private void popMismatches() {
        for (Map.Entry<String, CandidateCrossVote> entry : mismatch.entrySet()) {
            String candidateId = entry.getKey();
            List1.remove(candidateId);
            List2.remove(candidateId);
        }
        List1 = updateCandidateVote(List1);
        Log.e("BR/CROSS","is it first entry?"+String.valueOf(firstEntry));
        //at this point first entry should be false, but we want to update the fist list.
        updatePartyList(!firstEntry);
    }

    public void updateSecondEntryVotes(){
        List2 = updateCandidateVote(List2);
        updatePartyList(firstEntry);
    }

    public int getVoteType(ArrayList<String> partyListIds) {
        if(isCrossVote(partyListIds)){
            return CROSS;
        }
        // so is not cross vote, then if it is full then it is plancha
        if(isBallotFull()){
            return PLANCHA;
        }
        // otherwise it must be preferential
        return PREFERENTIAL;
    }

    public int getMarkType(ArrayList<String> partyListIds){
        if(isCrossVote(partyListIds)){
            return CROS_MARK;
        }
        // so is not cross vote, then if it is full then it is plancha
        if(isBallotFull()){
            return PLAN_MARK;
        }
        // otherwise it must be preferential
        return PREF_MARK;
    }

    public void addPreferentialPartyVote(){
        for(int i = 0; i<partyArrayList.size(); i++){
            String partyId = partyArrayList.get(i).getParty_preferential_election_id();
            partyArrayList.get(i).addPreferentialVotes(partyList2.get(partyId).getBallotVotes());
        }
    }

    public void addPlanchaPartyVote(){
        for(int i = 0; i<partyArrayList.size(); i++){
            String partyId = partyArrayList.get(i).getParty_preferential_election_id();
            partyArrayList.get(i).addPlanchaVote(partyList2.get(partyId).getBallotVotes());
        }
    }

    public void addCrossParyVote(){
        for(int i = 0; i<partyArrayList.size(); i++){
            String partyId = partyArrayList.get(i).getParty_preferential_election_id();
            Log.e("Ballot Votes", String.valueOf(partyList2.get(partyId).getBallotVotes()));
            partyArrayList.get(i).addCrossVote(partyList2.get(partyId).getBallotVotes());
        }
    }
    public BallotType createType(int type){
        return  new BallotType(type);
    }
    public class BallotType{
        public final static int VALID_VOTE = 1;
        public final static int NULL_VOTE = 2;
        public final static int EMPTY_VOTE = 3;
        private String ballotType;
        private int count;
        public BallotType(int type) {
            switch (type){
                case VALID_VOTE:
                    ballotType= ContextHandler.getElectionContext().getResources().getString(R.string.validCount);
                    break;
                case NULL_VOTE:
                    ballotType = ContextHandler.getElectionContext().getResources().getString(R.string.nullCount);
                    break;
                case EMPTY_VOTE:
                    ballotType = ContextHandler.getElectionContext().getResources().getString(R.string.blankCount);
                    break;
                default:
                    throw new InitializationException("NOT A VALID VOTE TYPE");
            }
        }
        public void setCount(int currentCount){
            this.count = currentCount;
        }
        public int getCount(){
            return count;
        }
        public int addToCount(){
            return  ++count;
        }
        public String getBallotType(){
            return ballotType;
        }

    }

}