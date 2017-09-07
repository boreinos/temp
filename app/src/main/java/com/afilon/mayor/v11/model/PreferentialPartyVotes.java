package com.afilon.mayor.v11.model;

public class PreferentialPartyVotes {

	private int jrv;
	private String preferential_election_id;
	private String party_preferential_election_id;
	private float party_votes;
	private String change_boletas;
	private String party_boletas;
	private String candidate_direct_election_id;

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
	public void setCandidate_direct_election_id(String candidateID){
		this.candidate_direct_election_id = candidateID;
	}

	public float getParty_votes() {
		return party_votes;
	}

	public void setParty_votes(float party_votes) {
		this.party_votes = party_votes;
	}

	public String getChange_boletas() {
		return change_boletas;
	}

	public void setChange_boletas(String change_boletas) {
		this.change_boletas = change_boletas;
	}

	public String getParty_boletas() {
		return party_boletas;
	}

	public void setParty_boletas(String party_boletas) {
		this.party_boletas = party_boletas;
	}

}
