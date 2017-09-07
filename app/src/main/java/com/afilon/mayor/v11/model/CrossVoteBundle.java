package com.afilon.mayor.v11.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ccano on 7/22/16.
 */
public class CrossVoteBundle implements Parcelable {
    private String mJrv;
    private String mPrefElecId;
    private String mPartyElecId;
    private String mCandidateElecId;
    private float mVote;
    private int bandMarks;
    private String mBoletaNo;

    public CrossVoteBundle(String jrv, String prefElecId, String partyElecId,
                           String candidateElecId, float vote, String boletaNo) {
        this.mJrv = jrv;
        this.mPrefElecId = prefElecId;
        this.mPartyElecId = partyElecId;
        this.mCandidateElecId = candidateElecId;
        this.mVote = vote;
        this.mBoletaNo = boletaNo;
    }

    public CrossVoteBundle() {
    }

    public String getJrv() { return mJrv; }
    public void setJrv(String jrv) { this.mJrv = jrv;}

    public String getPrefElecId() { return mPrefElecId; }
    public void setPrefElecId(String prefElecId) { this.mPrefElecId = prefElecId; }

    public String getPartyPrefElecId() { return mPartyElecId; }
    public void setPartyPrefElecId(String partyPrefElecId) { this.mPartyElecId = partyPrefElecId; }

    public String getCandidatePrefElecId() { return mCandidateElecId; }
    public void setCandidatePrefElecId(String candidatePrefElecId) { this.mCandidateElecId = candidatePrefElecId; }

    public int getBandMarks(){return bandMarks; }
    public void setBandMarks(int marks){this.bandMarks = marks; }

    public float getVote() { return mVote; }
    public void setVote(float vote) { this.mVote = vote; }

    public String getBoletaNo() { return mBoletaNo; }
    public void setBoletaNo(String boletaNo) { mBoletaNo = boletaNo; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrossVoteBundle)) return false;

        CrossVoteBundle that = (CrossVoteBundle) o;

        if (Float.compare(that.mVote, mVote) != 0) return false;
        if (!mJrv.equals(that.mJrv)) return false;
        if (!mPrefElecId.equals(that.mPrefElecId)) return false;
        if (!mPartyElecId.equals(that.mPartyElecId)) return false;
        if (!mCandidateElecId.equals(that.mCandidateElecId)) return false;
        return mBoletaNo.equals(that.mBoletaNo);

    }

    @Override
    public int hashCode() {
        int result = mJrv.hashCode();
        result = 31 * result + mPrefElecId.hashCode();
        result = 31 * result + mPartyElecId.hashCode();
        result = 31 * result + mCandidateElecId.hashCode();
        result = 31 * result + (mVote != +0.0f ? Float.floatToIntBits(mVote) : 0);
        result = 31 * result + mBoletaNo.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CrossVoteBundle{" +
                "mJrv='" + mJrv + '\'' +
                ", mPrefElecId='" + mPrefElecId + '\'' +
                ", mPartyElecId='" + mPartyElecId + '\'' +
                ", mCandidateElecId='" + mCandidateElecId + '\'' +
                ", mVote=" + mVote +
                ", mBoletaNo='" + mBoletaNo + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mJrv);
        dest.writeString(this.mPrefElecId);
        dest.writeString(this.mPartyElecId);
        dest.writeString(this.mCandidateElecId);
        dest.writeFloat(this.mVote);
        dest.writeString(this.mBoletaNo);
    }

    protected CrossVoteBundle(Parcel in) {
        this.mJrv = in.readString();
        this.mPrefElecId = in.readString();
        this.mPartyElecId = in.readString();
        this.mCandidateElecId = in.readString();
        this.mVote = in.readFloat();
        this.mBoletaNo = in.readString();
    }

    public static final Creator<CrossVoteBundle> CREATOR = new Creator<CrossVoteBundle>() {
        @Override
        public CrossVoteBundle createFromParcel(Parcel source) {
            return new CrossVoteBundle(source);
        }

        @Override
        public CrossVoteBundle[] newArray(int size) {
            return new CrossVoteBundle[size];
        }
    };
}