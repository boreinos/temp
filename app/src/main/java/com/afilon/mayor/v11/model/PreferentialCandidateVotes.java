package com.afilon.mayor.v11.model;

public class PreferentialCandidateVotes {

	private int jrv;
	private String preferential_election_id;
	private String party_preferential_election_id;
	private String candidate_preferential_election_id;
	private float candidate_votes;
	private float candidate_bandera_votes;
    private float candidate_preferential_votes;
	private float candidate_cross_votes=0f;

	public int getJrv() {
		return jrv;
	}

	public void setJrv(int jrv) {
		this.jrv = jrv;
	}

	public String getPreferential_election_id() {
		return preferential_election_id;
	}

	public void setPreferential_election_id(String preferential_election_id) {
		this.preferential_election_id = preferential_election_id;
	}

	public String getParty_preferential_election_id() {
		return party_preferential_election_id;
	}

	public void setParty_preferential_election_id(
			String party_preferential_election_id) {
		this.party_preferential_election_id = party_preferential_election_id;
	}

	public String getCandidate_preferential_election_id() {
		return candidate_preferential_election_id;
	}

	public void setCandidate_preferential_election_id(
			String candidate_preferential_election_id) {
		this.candidate_preferential_election_id = candidate_preferential_election_id;
	}

	public float getCandidate_votes() {
		return candidate_votes;
	}

	public void setCandidate_votes(float candidate_votes) {
		this.candidate_votes = candidate_votes;
	}
    public float getCandidate_bandera_votes(){ return candidate_bandera_votes;}
    public void setCandidate_bandera_votes(float votes){this.candidate_bandera_votes = votes;}
    public float getCandidate_preferential_votes(){return candidate_preferential_votes;}
    public void setCandidate_preferential_votes(float votes){this.candidate_preferential_votes=votes;}
	public void setCandidate_cross_votes(float votes){
		this.candidate_cross_votes = votes;
	}
	public float getCandidate_cross_votes(){
		return candidate_cross_votes;
	}

}