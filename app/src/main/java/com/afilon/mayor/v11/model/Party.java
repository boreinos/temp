package com.afilon.mayor.v11.model;

import java.util.ArrayList;

public class Party {

	private String id;
	private String pref_election_id;
	private String party_preferential_election_id;
	private String party_name;
	private String party_order;
	private String party_votes="0";
	private String party_votes_two="0";
	private String party_boletas;
	private float votes_per_ballot =0f;
	private float cross_votes = 0f;
	private int party_drawableId;
	private int candidateTotal;
	private   PreferentialVotoBanderas voteBreakdown;
	public  ArrayList<Candidate> candidates;
	private int party_marcas=0;
	private int mismatchQty=0;

	public void addMismatch(int mismatch){
		mismatchQty+=mismatch;
	}
	public int getMismatchQty(){
		return mismatchQty;
	}
	public void clearMismatches(){
		mismatchQty = 0;
	}

	public Party(String id, String event_locality_id,
			String party_preferential_election_id, String party_name,
			String party_order) {
		this.id = id;
		this.pref_election_id = event_locality_id;
		this.party_preferential_election_id = party_preferential_election_id;
		this.party_name = party_name;
		this.party_order = party_order;

	}

	public Party() {
		// TODO Auto-generated constructor stub
	}
	public void setParty_cross_votes(float votes){
		cross_votes = votes;
	}
	public float getParty_cross_votes(){
		return cross_votes;
	}
	public void setPartyMarks(int marcas){
		this.party_marcas = marcas;
	}
	public int getPartyMarks(){
		return party_marcas;
	}
    public void setBallotVotes(float cVotes){
		this.votes_per_ballot = cVotes;
	}
	public float getBallotVotes(){
		return votes_per_ballot;
	}
	public int getPartyDrawableId() {
		return this.party_drawableId;
	}
	public void setCandidateTotal(int total){
		this.candidateTotal = total;
	}
	public int getCandidateTotal(){
		return candidateTotal;
	}

	public void setPartyDrawableId(int drawableId) {
		this.party_drawableId =  drawableId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPref_election_id() {
		return pref_election_id;
	}

	public void setPref_Election_id(String event_locality_id) {
		this.pref_election_id = event_locality_id;
	}

	public String getParty_preferential_election_id() {
		return party_preferential_election_id;
	}

	public void setParty_preferential_election_id(String party_event_locality_id) {
		this.party_preferential_election_id = party_event_locality_id;
	}

	public String getParty_name() {
		return party_name;
	}

	public void setParty_name(String party_name) {
		this.party_name = party_name;
	}

	public String getParty_order() {
		return party_order;
	}

	public void setParty_order(String party_order) {
		this.party_order = party_order;
	}

	public String getParty_votes() {
		return party_votes;
	}

	public void setParty_votes(String party_votes) {
		this.party_votes = party_votes;
	}

	public String getParty_boletas() {
		return party_boletas;
	}

	public void setParty_boletas(String party_boletas) {
		this.party_boletas = party_boletas;
	}

	public String getParty_votes_two() {
		return party_votes_two;
	}

	public void setParty_votes_two(String party_votes_two) {
		this.party_votes_two = party_votes_two;
	}

	public void createPartyVotes(){
		voteBreakdown = new PreferentialVotoBanderas();
		voteBreakdown.setParty_preferential_election_id(party_preferential_election_id);
		voteBreakdown.setPreferential_election_id(pref_election_id);
	}
	public void addPreferentialVotes(float votes){

		voteBreakdown.setParty_preferential_votes(votes+voteBreakdown.getParty_preferential_votes());
	}
	public void addPlanchaVote(float votes){
		voteBreakdown.setParty_votes(votes+voteBreakdown.getParty_votes());
	}
	public void addCrossVote(float votes){
		voteBreakdown.setParty_cross_votes(votes+voteBreakdown.getParty_cross_votes());
	}

	public void setVoteBreakdown(PreferentialVotoBanderas votes){
		this.voteBreakdown = votes;
	}

	public void setCandidateList(ArrayList<Candidate> candidateList){
		candidates = candidateList;
	}

	public PreferentialVotoBanderas getVoteBreakdown(){
		return voteBreakdown;
	}

}
