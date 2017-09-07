package com.afilon.mayor.v11.model;

/**
 * Created by ccano on 7/6/16.
 */
public class CandidateCrossVote {
    private String mCandidateName;
    private String mCandidateId;
    private int    mCandidatePicture;
    private String mCandidateParty;
    private String mCandidatePrefElecId;
    private String mPartyElectionId;
    private int    mCandidateSortIndex;
    private boolean mIsMarked;
    private float mCandidateVote;
    private boolean mismatch=false;


    public CandidateCrossVote(int candidateSortIndex, String candidateName, String candidateId,
                              int candidatePicture, String candidateParty, String candidatePrefElecId,
                              boolean isMarked, float candidateVote) {
        this.mCandidateSortIndex = candidateSortIndex;
        this.mCandidateName = candidateName;
        this.mCandidateId = candidateId;
        this.mCandidatePicture = candidatePicture;
        this.mCandidateParty = candidateParty;
        this.mCandidatePrefElecId = candidatePrefElecId;
        this.mIsMarked = isMarked;
        this.mCandidateVote = candidateVote;
    }

    public void setMismatch(boolean mismatch){
        this.mismatch = mismatch;
    }
    public boolean isMismatch(){
        return this.mismatch;
    }

    public String getPartyElectionId(){
        return mPartyElectionId;
    }

    public void setPartyElectionId(String partyId){
        this.mPartyElectionId = partyId;
    }

    public String getCandidateName() { return mCandidateName;}
    public void setCandidateName(String candidateName) { this.mCandidateName = candidateName;}

    public String getCandidateId() { return mCandidateId;}
    public void setCandidateId(String candidateId) { this.mCandidateId = candidateId;}

    public int getCandidatePicture() { return mCandidatePicture;}
    public void setCandidatePicture(int candidatePicture) { this.mCandidatePicture = candidatePicture;}

    public String getCandidateParty() { return mCandidateParty;}
    public void setCandidateParty(String candidateParty) { this.mCandidateParty = candidateParty;}

    public String getCandidatePrefElecId() { return mCandidatePrefElecId;}
    public void setCandidatePrefElecId(String candidatePrefElecId) { this.mCandidatePrefElecId = candidatePrefElecId;}

    public int getCandidateSortIndex() { return mCandidateSortIndex;}

    public void setCandidateSortIndex(int candidateSortIndex) { this.mCandidateSortIndex = candidateSortIndex;}

    public boolean isMarked() { return mIsMarked; }
    public void setMark(boolean mark) { this.mIsMarked = mark;}

    public float getCandidateVote() { return mCandidateVote; }
    public void setCandidateVote(float vote) {this.mCandidateVote = vote;}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidateCrossVote)) return false;

        CandidateCrossVote that = (CandidateCrossVote) o;

        if (mCandidatePicture != that.mCandidatePicture) return false;
        if (mCandidateSortIndex != that.mCandidateSortIndex) return false;
        if (mIsMarked != that.mIsMarked) return false;
        if (Float.compare(that.mCandidateVote, mCandidateVote) != 0) return false;
        if (!mCandidateName.equals(that.mCandidateName)) return false;
        if (!mCandidateId.equals(that.mCandidateId)) return false;
        if (!mCandidateParty.equals(that.mCandidateParty)) return false;
        return mCandidatePrefElecId.equals(that.mCandidatePrefElecId);

    }

    @Override
    public int hashCode() {
        int result = mCandidateName.hashCode();
        result = 31 * result + mCandidateId.hashCode();
        result = 31 * result + mCandidatePicture;
        result = 31 * result + mCandidateParty.hashCode();
        result = 31 * result + mCandidatePrefElecId.hashCode();
        result = 31 * result + mCandidateSortIndex;
        result = 31 * result + (mIsMarked ? 1 : 0);
        result = 31 * result + (mCandidateVote != +0.0f ? Float.floatToIntBits(mCandidateVote) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CandidateCrossVote{" +
                "mCandidateName='" + mCandidateName + '\'' +
                ", mCandidateId='" + mCandidateId + '\'' +
                ", mCandidatePicture=" + mCandidatePicture +
                ", mCandidateParty='" + mCandidateParty + '\'' +
                ", mCandidatePrefElecId='" + mCandidatePrefElecId + '\'' +
                ", mCandidateSortIndex=" + mCandidateSortIndex +
                ", mIsMarked=" + mIsMarked +
                ", mCandidateVote=" + mCandidateVote +
                '}';
    }
}
