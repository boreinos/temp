package com.afilon.mayor.v11.model;

/**
 * Created by ccano on 7/12/16.
 */
public class CrossVoteCount {
    private String mCandidateName;
    private String mCandidateId;
    private String mCandidateParty;
    private String mCandidatePrefElecId; //Candidate Preferential Election ID field
    private float  mCandidateAssignedVote;

    public CrossVoteCount(String candidateName, String candidateId, String candidateParty,
                          String candidatePrefElecId, float candidateAssignedVote) {
        this.mCandidateName = candidateName;
        this.mCandidateId = candidateId;
        this.mCandidateParty = candidateParty;
        this.mCandidatePrefElecId = candidatePrefElecId;
        this.mCandidateAssignedVote = candidateAssignedVote;
    }

    public String getCandidateName() { return mCandidateName; }
    public void setCandidateName(String candidateName) { this.mCandidateName = candidateName; }

    public String getCandidateId() { return mCandidateId; }
    public void setCandidateId(String candidateId) { this.mCandidateId = candidateId; }

    public String getCandidateParty() { return mCandidateParty; }
    public void setCandidateParty(String candidateParty) { this.mCandidateParty = candidateParty; }

    public String getCandidatePrefElecId() { return mCandidatePrefElecId; }
    public void setCandidatePrefElecId(String candidatePrefElecId) { mCandidatePrefElecId = candidatePrefElecId; }

    public float getCandidateAssignedVote() { return mCandidateAssignedVote; }
    public void setCandidateAssignedVote(float candidateAssignedVote) { mCandidateAssignedVote = candidateAssignedVote; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrossVoteCount)) return false;

        CrossVoteCount that = (CrossVoteCount) o;

        if (Float.compare(that.mCandidateAssignedVote, mCandidateAssignedVote) != 0) return false;
        if (!mCandidateName.equals(that.mCandidateName)) return false;
        if (!mCandidateId.equals(that.mCandidateId)) return false;
        if (!mCandidateParty.equals(that.mCandidateParty)) return false;
        return mCandidatePrefElecId.equals(that.mCandidatePrefElecId);

    }

    @Override
    public int hashCode() {
        int result = mCandidateName.hashCode();
        result = 31 * result + mCandidateId.hashCode();
        result = 31 * result + mCandidateParty.hashCode();
        result = 31 * result + mCandidatePrefElecId.hashCode();
        result = 31 * result + (mCandidateAssignedVote != +0.0f ? Float.floatToIntBits(mCandidateAssignedVote) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CrossVoteCount{" +
                "mCandidateName='" + mCandidateName + '\'' +
                ", mCandidateId='" + mCandidateId + '\'' +
                ", mCandidateParty='" + mCandidateParty + '\'' +
                ", mCandidatePrefElecId='" + mCandidatePrefElecId + '\'' +
                ", mCandidateAssignedVote=" + mCandidateAssignedVote +
                '}';
    }
}
