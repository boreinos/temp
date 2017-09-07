package com.afilon.mayor.v11.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ccano on 8/10/16.
 */
public class CandidateMarks implements Parcelable {
    private String mJrv;
    private String mCandidateId;
    private String mPartyId;
    private String mPrefElecId;
    private String mElecType;
    private int mTotalMarks;
    //----------------------------------------------
    private int candidate_preferential_marcas;
    private int candidate_cross_marcas;
    private int candidate_plancha_marcas;
    private int candidate_marcas;
    public int getCandidate_preferential_marcas(){
        return candidate_preferential_marcas;
    }
    public void setCandidate_preferential_marcas(int marks){
        candidate_preferential_marcas = marks;
    }
    public void setCandidate_plancha_marcas(int marks){
        this.candidate_plancha_marcas = marks;
    }
    public int getCandidate_cross_marcas(){return candidate_cross_marcas;}
    public void setCandidate_cross_marcas(int marks){
        candidate_cross_marcas = marks;
    }

    public CandidateMarks(String jrv, String candidateId, String partyId, String electionId,
                          int crossMarks, int preferentialMarks, int totalMarks){
        this.mJrv = jrv;
        this.mCandidateId = candidateId;
        this.mPartyId = partyId;
        this.mPrefElecId = electionId;
        this.candidate_cross_marcas = crossMarks;
        this.candidate_preferential_marcas = preferentialMarks;
        this.candidate_marcas = totalMarks;
    }

    public CandidateMarks(String mJrv, String mPrefElecId, String mCandidateId,
                          String mPartyId,
                          String mElecType, int mTotalMarks) {
        this.mJrv = mJrv;
        this.mCandidateId = mCandidateId;
        this.mPartyId = mPartyId;
        this.mPrefElecId = mPrefElecId;
        this.mElecType = mElecType;
        this.mTotalMarks = mTotalMarks;
    }


    public String getmJrv() {
        return mJrv;
    }

    public void setmJrv(String mJrv) {
        this.mJrv = mJrv;
    }

    public String getmCandidateId() {
        return mCandidateId;
    }

    public void setmCandidateId(String mCandidateId) {
        this.mCandidateId = mCandidateId;
    }

    public String getmPartyId() {
        return mPartyId;
    }

    public void setmPartyId(String mPartyId) {
        this.mPartyId = mPartyId;
    }

    public String getmPrefElecId() {
        return mPrefElecId;
    }

    public void setmPrefElecId(String mPrefElecId) {
        this.mPrefElecId = mPrefElecId;
    }

    public String getmElecType() {
        return mElecType;
    }

    public void setmElecType(String mElecType) {
        this.mElecType = mElecType;
    }

    public int getmTotalMarks() {
        return mTotalMarks;
    }

    public void setmTotalMarks(int mTotalMarks) {
        this.mTotalMarks = mTotalMarks;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidateMarks)) return false;

        CandidateMarks that = (CandidateMarks) o;

        if (getmTotalMarks() != that.getmTotalMarks()) return false;
        if (!getmJrv().equals(that.getmJrv())) return false;
        if (!getmCandidateId().equals(that.getmCandidateId())) return false;
        if (!getmPartyId().equals(that.getmPartyId())) return false;
        if (!getmPrefElecId().equals(that.getmPrefElecId())) return false;
        return getmElecType().equals(that.getmElecType());

    }

    @Override
    public int hashCode() {
        int result = getmJrv().hashCode();
        result = 31 * result + getmCandidateId().hashCode();
        result = 31 * result + getmPartyId().hashCode();
        result = 31 * result + getmPrefElecId().hashCode();
        result = 31 * result + getmElecType().hashCode();
        result = 31 * result + getmTotalMarks();
        return result;
    }

    @Override
    public String toString() {
        return "CandidateMarks{" +
                "mJrv='" + mJrv + '\'' +
                ", mCandidateId='" + mCandidateId + '\'' +
                ", mPartyId='" + mPartyId + '\'' +
                ", mPrefElecId='" + mPrefElecId + '\'' +
                ", mElecType='" + mElecType + '\'' +
                ", mTotalMarks=" + mTotalMarks +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mJrv);
        dest.writeString(this.mCandidateId);
        dest.writeString(this.mPartyId);
        dest.writeString(this.mPrefElecId);
        dest.writeString(this.mElecType);
        dest.writeInt(this.mTotalMarks);
    }

    protected CandidateMarks(Parcel in) {
        this.mJrv = in.readString();
        this.mCandidateId = in.readString();
        this.mPartyId = in.readString();
        this.mPrefElecId = in.readString();
        this.mElecType = in.readString();
        this.mTotalMarks = in.readInt();
    }

    public static final Parcelable.Creator<CandidateMarks> CREATOR = new Parcelable.Creator<CandidateMarks>() {
        @Override
        public CandidateMarks createFromParcel(Parcel source) {
            return new CandidateMarks(source);
        }

        @Override
        public CandidateMarks[] newArray(int size) {
            return new CandidateMarks[size];
        }
    };
}
