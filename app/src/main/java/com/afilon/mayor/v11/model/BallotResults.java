package com.afilon.mayor.v11.model;

/**
 * Created by Boris on 11/12/2017.
 */

public class BallotResults{
    private int ballotCount;
    private int marcas;
    private float votes;
    private String partyName;
    private String Jrv;
    private String Preferential_Election_ID;
    private String Party_Preferential_Election_ID;

    public BallotResults(int count, int marks, float vote, String name, String jrv){
        this.ballotCount=count;
        this.marcas=marks;
        this.votes=vote;
        this.partyName=name;
        this.Jrv=jrv;
    }
    public void setPreferential_Election_ID(String election_id){
        this.Preferential_Election_ID = election_id;
    }
    public void setParty_Preferential_Election_ID(String party_election_id){
        this.Party_Preferential_Election_ID = party_election_id;
    }
    public int ballotNumber(){
        return this.ballotCount;
    }
    public float getVotes(){
        return this.votes;
    }
    public int getMarcas(){
        return this.marcas;
    }
    public String getParty(){
        return this.partyName;
    }


}
