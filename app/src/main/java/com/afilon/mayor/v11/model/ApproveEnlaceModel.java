package com.afilon.mayor.v11.model;

public class ApproveEnlaceModel {

	private int jrv;
	private String preferential_election_id;
	private String provisional_accepted;
	private String accept_string;

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

	public String getProvisional_accepted() {
		return provisional_accepted;
	}

	public void setProvisional_accepted(
			String provisional_accepted) {
		this.provisional_accepted = provisional_accepted;
	}

	public String getApproval() {
		return accept_string;
	}

	public void setApproval(String accept) {
		this.accept_string = accept;
	}

}
