package com.afilon.mayor.v11.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Candidate implements Parcelable {

	private String candidateID;
	private String party_name;
	private String candidate_order;
	private String candidate_name;
	private String candidate_image;
	private String candidatePreferentialElectionID;
	private String partyPreferentialElectionID;

	private float votesNumber=0f;
	private float preferentialVotes=0f;
	private float banderaNumbers=0f;
	private float crossVote = 0f;
	private int marcas = 0;
	private int mMarksQty = 0;
	private int mBanderaMarks = 0;
	private int mPreferentialMarks = 0;

	private boolean cbOneSelected;
	private boolean cbTwoSelected;

	public Candidate(String name, String number, String partyid,
					 String candidateElectionId, String candidate_order) {

		this.candidate_name = name;
		this.party_name = partyid;
		this.candidateID = number; // candidate index
		this.candidatePreferentialElectionID = candidateElectionId;
		this.candidate_order = candidate_order;

	}

	public Candidate(String candidateName, String partyName, String candidateID, String partyID){
		this.candidate_name=candidateName;
		this.party_name=partyName;
		this.candidatePreferentialElectionID = candidateID;
		this.partyPreferentialElectionID = partyID;
	}

	public String getCandidateID() {
		return candidateID;
	}

	public void setCandidateID(String candidateID) {
		this.candidateID = candidateID;
	}

	public String getPartyName() {
		return party_name;
	}

	public void setPartyID(String partyID) {
		this.party_name = partyID;
	}

	public String getCandidate_order() {
		return candidate_order;
	}

	public void setCandidate_order(String candidate_order) {
		this.candidate_order = candidate_order;
	}

	public String getCandidate_name() {
		return candidate_name;
	}

	public void setCandidate_name(String candidate_name) {
		this.candidate_name = candidate_name;
	}

	public String getCandidate_image() {
		return candidate_image;
	}

	public void setCandidate_image(String candidate_image) {
		this.candidate_image = candidate_image;
	}

	public String getCandidatePreferentialElectionID() {
		return candidatePreferentialElectionID;
	}

	public void setCandidatePreferentialElectionID(String eventID) {
		candidatePreferentialElectionID = eventID;
	}

	public String getPartyPreferentialElectionID() {
		return partyPreferentialElectionID;
	}

	public void setPartyPreferentialElectionID(
			String cand_party_event_locality_id) {
		this.partyPreferentialElectionID = cand_party_event_locality_id;
	}

	public float getVotesNumber() {
		return votesNumber;
	}
	public float getCrossVote(){return crossVote;}
	public void setCrossVote(float vote){this.crossVote =vote; }

	public void setVotesNumber(float numberOfChecked) {
		this.votesNumber = numberOfChecked;
	}

	public float getPreferentialVotes(){return preferentialVotes;}
	public void setPreferentialVotes(float votes){this.preferentialVotes = votes;}
	public float getBanderaNumbers(){return banderaNumbers;}
	public void setBanderaNumber(float votes){this.banderaNumbers = votes; }


	public boolean isCbOneSelected() {
		return cbOneSelected;
	}

	public void setCbOneSelected(boolean cbOneSelected) {
		this.cbOneSelected = cbOneSelected;
	}

	public boolean isCbTwoSelected() {
		return cbTwoSelected;
	}

	public void setCbTwoSelected(boolean cbTwoSelected) {
		this.cbTwoSelected = cbTwoSelected;
	}
	public void updateTotalVotes(){
		this.votesNumber = banderaNumbers+crossVote+preferentialVotes;
	}

	public Candidate() {
		// TODO Auto-generated constructor stub
	}


	public void setTotalMarks(int pMark, int cMark) {
		this.mMarksQty = pMark + cMark;
	}

	public int getMarksQty() { return this.mMarksQty; }

	public void setMarcas(int marcas){
		this.marcas = marcas;
	}
	public int getMarcas(){
		return marcas;
	}

	public void setBanderaMarks(int marcas) { this.mBanderaMarks = marcas; }
	public int getBanderaMarks() { return this.mBanderaMarks; }

	public void setPreferentialMarks(int marcas) { this.mPreferentialMarks = marcas; }
	public int getPreferentialMarks() {return this.mPreferentialMarks; }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.candidateID);
		dest.writeString(this.party_name);
		dest.writeString(this.candidate_order);
		dest.writeString(this.candidate_name);
		dest.writeString(this.candidate_image);
		dest.writeString(this.candidatePreferentialElectionID);
		dest.writeString(this.partyPreferentialElectionID);
		dest.writeFloat(this.votesNumber);
		dest.writeFloat(this.preferentialVotes);
		dest.writeFloat(this.banderaNumbers);
		dest.writeFloat(this.crossVote);
		dest.writeInt(this.marcas);
		dest.writeByte(this.cbOneSelected ? (byte) 1 : (byte) 0);
		dest.writeByte(this.cbTwoSelected ? (byte) 1 : (byte) 0);
	}

	protected Candidate(Parcel in) {
		this.candidateID = in.readString();
		this.party_name = in.readString();
		this.candidate_order = in.readString();
		this.candidate_name = in.readString();
		this.candidate_image = in.readString();
		this.candidatePreferentialElectionID = in.readString();
		this.partyPreferentialElectionID = in.readString();
		this.votesNumber = in.readFloat();
		this.preferentialVotes = in.readFloat();
		this.banderaNumbers = in.readFloat();
		this.crossVote = in.readFloat();
		this.marcas = in.readInt();
		this.cbOneSelected = in.readByte() != 0;
		this.cbTwoSelected = in.readByte() != 0;
	}

	public static final Parcelable.Creator<Candidate> CREATOR = new Parcelable.Creator<Candidate>() {
		@Override
		public Candidate createFromParcel(Parcel source) {
			return new Candidate(source);
		}

		@Override
		public Candidate[] newArray(int size) {
			return new Candidate[size];
		}
	};
}