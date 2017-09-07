package com.afilon.mayor.v11.model;

public class PreferentialVotoBanderas {

	private String jrv;

	private String preferential_election_id;
	private String bandera_preferential_election_id;
	private String party_preferential_election_id;

	private String party_boletas;

	private float party_preferential_votes=0f;
	private float party_cross_votes=0f;
	private float party_votes=0f;



	public String getJrv() {
		return jrv;
	}

	public void setJrv(String jrv) {
		this.jrv = jrv;
	}

	public String getPreferential_election_id() {
		return preferential_election_id;
	}

	public void setPreferential_election_id(String preferential_election_id) {
		this.preferential_election_id = preferential_election_id;
	}

	public String getBandera_preferential_election_id() {
		return bandera_preferential_election_id;
	}

	public void setBandera_preferential_election_id(
			String bandera_preferential_election_id) {
		this.bandera_preferential_election_id = bandera_preferential_election_id;
	}

	public String getParty_preferential_election_id() {
		return party_preferential_election_id;
	}

	public void setParty_preferential_election_id(
			String party_preferential_election_id) {
		this.party_preferential_election_id = party_preferential_election_id;
	}

	public float getParty_votes() {
		return party_votes;
	}

	public void setParty_votes(float party_votes) {
		this.party_votes = party_votes;
	}
    public void setParty_preferential_votes(float votes){this.party_preferential_votes = votes;}
    public float getParty_preferential_votes(){return party_preferential_votes;}
	public void setParty_cross_votes(float votes){this.party_cross_votes= votes;}
	public float getParty_cross_votes(){return party_cross_votes;}

	public String getParty_boletas() {
		return party_boletas;
	}

	public void setParty_boletas(String party_boletas) {
		this.party_boletas = party_boletas;
	}
	public float getPartyTotals(){
		return party_preferential_votes+party_cross_votes+ party_votes;
	}

}