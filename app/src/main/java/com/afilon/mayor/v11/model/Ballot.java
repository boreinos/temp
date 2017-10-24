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
import java.util.Set;

/**
 * Created by BReinosa on 6/27/2017.
 */
public class Ballot {
    private HashMap<String, CandidateCrossVote> List1, List2, matches, mismatch; // happy?
    private int maxBallotSize;
    private HashMap<String, Party> partyList1, partyList2, partySelection1, partySelection2, matchParties, mismatchParties;
    private boolean isConfirmed = false;
    private boolean firstEntry = true;
    private ArrayList<Party> partyArrayList;
    private int ballotNumber;
    private String jrv, preferentialId;
    //-----------------------------------
    private int Marks = 0;
    //-----------------------------------
    private final int PLANCHA = 2;
    private final int PREFERENTIAL = 1;
    private final int CROSS = 3;
    public static final int NULO = -1;
    public static final int PREF_MARK = 4;
    public static final int PLAN_MARK = 5;
    public static final int CROS_MARK = 6;
    private int voteType = NULO;
    private float candidateVote = 0f;


    public Ballot(int maxBallotSize) {
        this.maxBallotSize = maxBallotSize;
        List1 = new HashMap<>();
        List2 = new HashMap<>();
        partySelection1 = new HashMap<>();
        partySelection2 = new HashMap<>();
        partyList1 = new HashMap<>();

    }

    public void setLocation(String jrv, String preferentialId) {
        this.jrv = jrv;
        this.preferentialId = preferentialId;
    }

    public HashMap<String, CandidateCrossVote> getFirstMarks() {
        return List1;
    }

    public HashMap<String, CandidateCrossVote> getSecondMarks() {
        return List2;
    }

    public HashMap<String, Party> getpartiesFirstmarks(){
        return partySelection1;
    }

    public HashMap<String, Party> getPartySecondMarks(){
        return partySelection2;
    }


    public void setMarks(int marks) {
        Marks = marks;
    }

    private void resetMarks() {
        setMarks(0);
    }

    public boolean verifyMarks() {
        return Marks == getVotes();
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
            calcualteMultiplier();
            List1 = updateCandidateVote(List1);
        } else {
            List2.put(candidate.getCandidatePrefElecId(), candidate);
            calcualteMultiplier();
            List2 = updateCandidateVote(List2);
        }
        updatePartyList(firstEntry);
    }
    public void checkBallot(){
        //check if ballot is valid at this point:
        if(firstEntry){
            calcualteMultiplier();
            List1 = updateCandidateVote(List1);
        }else {
            calcualteMultiplier();
            List2 = updateCandidateVote(List2);
        }
    }

    public void removeCandidateES(CandidateCrossVote candidate) {


        if (firstEntry)

        {
            Party someparty = partySelection1.get(candidate.getPartyElectionId());
            if (someparty == null) {
                // the party is not selected.
                List1.remove(candidate.getCandidatePrefElecId());
                calcualteMultiplier();
                List1 = updateCandidateVote(List1);
            }

        } else {
            Party someparty = partySelection1.get(candidate.getPartyElectionId());
            if (someparty == null) {
                List2.remove(candidate.getCandidatePrefElecId());
                calcualteMultiplier();
                List2 = updateCandidateVote(List2);
            }
        }

        updatePartyList(firstEntry);
    }

    public void removeCandidate(CandidateCrossVote candidate) {
        calcualteMultiplier();
        if (firstEntry) {
            Party someparty = partySelection1.get(candidate.getPartyElectionId());
            List1.remove(candidate.getCandidatePrefElecId());
            List1 = updateCandidateVote(List1);

        } else {
            List2.remove(candidate.getCandidatePrefElecId());
            List2 = updateCandidateVote(List2);
        }
        updatePartyList(firstEntry);
    }

    public void addParty(Party party){
        if(firstEntry){
            partySelection1.put(party.getParty_preferential_election_id(),party);
        }else{
            partySelection2.put(party.getParty_preferential_election_id(), party);
        }
        calcualteMultiplier();
        // Todo: update vote assignments
    }

    public void removeParty(Party party){
        if(firstEntry){
            partySelection1.remove(party.getParty_preferential_election_id());
        }else{
            partySelection2.remove(party.getParty_preferential_election_id());
        }
        calcualteMultiplier();
    }

    public boolean isBallotFull() {
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        return list.size() >= maxBallotSize;
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
        findPartySelectionMismatches();// pop other type of mismatches

        return mismatch;
    }

    private HashMap<String, Party> findPartySelectionMismatches(){
        initializePartyMatches();

        if (partyList1.size() < partyList2.size()) {
            // get a list of matches and mismatches, then compare the second list to matches
            // in order to append all mismatches to the mismatch list.
            compareParties(partySelection1, partySelection2);
            compareParties(partySelection2, matchParties);
        } else {
            compareParties(partySelection2, partySelection1);
            compareParties(partySelection1, matchParties);
        }
        popMismatchCandidateByParty();
        return mismatchParties; //todo: does not need to return;
    }

    private void popMismatchCandidateByParty(){
        ArrayList<String> removeCandidates = new ArrayList<>();
        for(Map.Entry<String, Party> entry : mismatchParties.entrySet()){
            for(Map.Entry<String, CandidateCrossVote> candidate : List1.entrySet()){
                if(candidate.getValue().getPartyElectionId().equals(entry.getKey())){
                    removeCandidates.add(candidate.getKey());// save id to remove candidate later.
                    mismatch.put(candidate.getKey(),candidate.getValue()); //add candidate to list
                }
            }
            for(Map.Entry<String,CandidateCrossVote> candidate: List2.entrySet()){
                if(candidate.getValue().getPartyElectionId().equals(entry.getKey())){
                    removeCandidates.add(candidate.getKey());
                    mismatch.put(candidate.getKey(),candidate.getValue());
                }
            }
            //remove party from list
            partySelection1.remove(entry.getKey());
            partySelection2.remove(entry.getKey());
        }
        // now pop them
        for(String candidateId: removeCandidates){
            List1.remove(candidateId);
            List2.remove(candidateId);
        }
    }

    public HashMap<String, Party> confirmPartySelection(){
        return mismatchParties;
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
        if(Consts.LOCALE.equals(Consts.ELSALVADOR)){
            return updateCandidateVoteSV(list);
        }
        return updateCandidateVoteHN(list);
    }

    private HashMap<String, CandidateCrossVote> updateCandidateVoteSV(HashMap<String, CandidateCrossVote> list) {
        HashMap<String, CandidateCrossVote> tempList = new HashMap<>();
        for (Map.Entry<String, CandidateCrossVote> entry : list.entrySet()) {
            CandidateCrossVote candidate = entry.getValue();
//            candidate.setCandidateVote(1f / list.size());
            float vote;
            if(voteType==NULO){
                vote = 0f;
            }else {
                vote = 1f/list.size();
            }

            candidate.setCandidateVote(vote);
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

    private void compareParties(HashMap<String, Party> firstList, HashMap<String, Party> secondList){
        Party tempCandidate;
        for (Map.Entry<String, Party> entry : firstList.entrySet()) {
            String candidateId = entry.getKey();
            tempCandidate = secondList.get(candidateId);
            if (tempCandidate != null) {
                matchParties.put(candidateId, tempCandidate);
            } else {

                mismatchParties.put(candidateId, entry.getValue());
            }
        }
    }

    private void initializePartyMatches(){
        matchParties =  new HashMap<>();
        mismatchParties = new HashMap<>();
    }

    private void initializeMatches() {
        matches = new HashMap<>();
        mismatch = new HashMap<>();

    }

    private void clearMatches() {
        matches = null;
        mismatch = null;
        matchParties = null;
        mismatchParties = null;

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
        List1 = List2  = null;
        List1 = new HashMap<>();
        List2 = new HashMap<>();
        //party votes
        partyList1 = partyList2 = null;
        partyList1 = new HashMap<>();
        partyList2 = new HashMap<>();

        partySelection1 = partySelection2 = null;
        partySelection1 = new HashMap<>();
        partySelection2 = new HashMap<>();
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
        if(Consts.LOCALE.equals(Consts.HONDURAS)){
            return "1";
        }
        return voteMultiplierSV();
    }

    public String voteMultiplierSV() {
        //TODO: DETERMINE BY VOTE VALIDITY
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        if (list.size() != 0 && voteType!=NULO) {
            return String.format(Locale.US,"%.4f",(1f / list.size()));
        }
        return "0";
    }

    public boolean isReady(ArrayList<String> partyListIds){
        return !isBallotEmpty();
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

    private boolean  isPlanchaOrPreferential(){
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        int count = 0;
        String previousID="noID";
        String currentID;
        for (Map.Entry<String, CandidateCrossVote> entry : list.entrySet()){
            currentID = entry.getValue().getPartyElectionId();
            if(!currentID.equals(previousID)){
                count++;
                if(count>1){
                    return false;
                }
            }
            previousID = currentID;
        }
        // exhusted the list
        return true;
    }

    public boolean isBallotEmpty(){
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        return list.isEmpty();
    }

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
        if(Consts.LOCALE.equals(Consts.ELSALVADOR)){
            if(voteType==NULO){
                singleVote = 0f;
            }else {
                singleVote = 1f/candidates.size();
            }

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

    public void addCandidatesFrom(ArrayList<Candidate> candidates){
        float vote;
        if(firstEntry){
            float divider = (candidates.size()+List1.size());
//            if(divider>maxBallotSize){
//                Log.e("PARTY SELECTED", "NULL VOTE??");
//                voteType=NULO;
//            }
//            if(voteType==NULO){
//                vote = 0f;
//            }else {
//                vote = 1f/divider;
//            }
            vote = 1f/divider;
            for(Candidate candidate:candidates){
                CandidateCrossVote crossVote = new CandidateCrossVote(0, candidate.getCandidate_name(),
                        candidate.getCandidateID(), Integer.valueOf(candidate.getCandidate_image()),
                        candidate.getPartyName(), candidate.getCandidatePreferentialElectionID(),
                        false, vote);
                crossVote.setPartyElectionId(candidate.getPartyPreferentialElectionID());
                CandidateCrossVote ccv = List1.put(candidate.getCandidatePreferentialElectionID(), crossVote);
                if(ccv!=null){
                    List1.put(ccv.getCandidatePrefElecId(),ccv);
                }
            }
//            if(List1.size()>maxBallotSize){
//                voteType=NULO;
//            }
            calcualteMultiplier();
            List1 = updateCandidateVote(List1);
        }else{
            float divider = (candidates.size()+List2.size());
//            if(divider>maxBallotSize){
//                voteType=NULO;
//            }
//            if(voteType==NULO){
//                vote = 0f;
//            }else {
//                vote = 1f/divider;
//            }
            vote = 1f/divider;
            for(Candidate candidate:candidates){
                CandidateCrossVote crossVote = new CandidateCrossVote(0, candidate.getCandidate_name(),
                        candidate.getCandidateID(), Integer.valueOf(candidate.getCandidate_image()), // might not need this garbage
                        candidate.getPartyName(), candidate.getCandidatePreferentialElectionID(),
                        false, vote);
                crossVote.setPartyElectionId(candidate.getPartyPreferentialElectionID());
                List2.put(candidate.getCandidatePreferentialElectionID(), crossVote);
            }
//            if(List2.size()>maxBallotSize){
//                voteType=NULO;
//            }
            calcualteMultiplier();
            List2 = updateCandidateVote(List2);
        }
        updatePartyList(firstEntry);

    }

    public void removeCandidatesFrom(ArrayList<Candidate> candidates){
        if(firstEntry){
            for(Candidate candidate: candidates){
                CandidateCrossVote ccv =  List1.remove(candidate.getCandidatePreferentialElectionID());
                if(ccv != null && ccv.isMarked()){
                    List1.put(ccv.getCandidatePrefElecId(),ccv);
                }

            }
            calcualteMultiplier();
            List1 = updateCandidateVote(List1);

        }else{
            for(Candidate candidate: candidates){
                List2.remove(candidate.getCandidatePreferentialElectionID());
            }
            calcualteMultiplier();
            List2 = updateCandidateVote(List2);
        }
        updatePartyList(firstEntry);

    }

    private void popMismatches() {
        for (Map.Entry<String, CandidateCrossVote> entry : mismatch.entrySet()) {
            String candidateId = entry.getKey();
            List1.remove(candidateId);
            List2.remove(candidateId);
        }
        calcualteMultiplier();
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

    public int getMarkTypeES(){
        int marktype;
        switch (voteType){
            case PLANCHA:
                marktype= PLAN_MARK;
                break;
            case PREFERENTIAL:
                marktype = PREF_MARK;
                break;
            case CROSS:
                marktype = CROS_MARK;
                break;
            case NULO:
                marktype = NULO;
                break;
            default:
                marktype = NULO;
                break;
        }
        return marktype;
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
        public final static int IMPUGNADO_VOTE = 4;
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
                case IMPUGNADO_VOTE:
                    ballotType = ContextHandler.getElectionContext().getResources().getString(R.string.impugnadoCount);
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

    // ----------------------------------------------------------------------------------
    private void calcualteMultiplier() {
        HashMap<String, Party> partylist = firstEntry ? partySelection1 : partySelection2;
        HashMap<String, CandidateCrossVote> candidatelist = firstEntry ? List1 : List2;
        float divider = candidatelist.size();

        if (partylist.size() > 1){
            voteType = NULO;
        } else if (candidatelist.size() > maxBallotSize){
            voteType = NULO;
        } else if (partylist.size() == 1) {
            if(!isPlanchaOrPreferential()){
                // this is the case where at least one candidate on the list doesn't belong to the party
                voteType = NULO;
            }else if(){
                //case where only plancha are selected:

            }
            else if(candidatelist.size() == maxBallotSize || candidatelist.size() ==0){
                voteType = PREFERENTIAL;
                // remove candidates that are not marked
                removeUnmarkedCandidates();
                //voteType = PLANCHA;
                // marks only when selected candidateMark = 0;
            }else if(candidatelist.size()<maxBallotSize){
                voteType = PREFERENTIAL;
            }
        } else if (partylist.size() < 1) {
            if(isPlanchaOrPreferential()){
                if(divider == maxBallotSize){
                    voteType = PLANCHA;
                } else if(divider < maxBallotSize){
                    voteType = PREFERENTIAL;
                }
            } else {
                //cross vote:
                voteType = CROSS;
            }
        }
    }
    private void removeUnmarkedCandidates(){
        HashMap<String, CandidateCrossVote> list = firstEntry ? List1 : List2;
        ArrayList<String> removeIds = new ArrayList<>();
        for (Map.Entry<String, CandidateCrossVote> entry : list.entrySet()){
            if(!entry.getValue().isMarked()){
                removeIds.add(entry.getKey());
            }
        }
        for(String id: removeIds){
            list.remove(id);
        }
    }



}