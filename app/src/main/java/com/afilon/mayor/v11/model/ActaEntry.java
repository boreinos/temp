package com.afilon.mayor.v11.model;

/**
 * Created by BReinosa on 8/7/2017.
 * This Class is a simple container to hold data for Acta
 * */
public class ActaEntry {
    String entryName="";
    String totalVotes="";
    String totalMarcas="";
    String planchaVotes="";
    String planchaMarcas="";
    String parcialVotes="";
    String parcialMarcas="";
    String cruzadoVotes="";
    String cruzadoMarcas="";

    public ActaEntry(String entryName){
        this.entryName = entryName;
    }

    public void setMarcas(String totalMarcas, String planchaMarcas, String parcialMarcas, String cruzadoMarcas){
        this.totalMarcas = totalMarcas;
        this.planchaMarcas = planchaMarcas;
        this.parcialMarcas = parcialMarcas;
        this.cruzadoMarcas = cruzadoMarcas;
    }
    public void setVotes(String totalVotes, String planchaVotes, String parcialVotes, String cruzadoVotes){
        this.totalVotes = totalVotes;
        this.planchaVotes = planchaVotes;
        this.parcialVotes = parcialVotes;
        this.cruzadoVotes = cruzadoVotes;
    }
    //----------------------------------------------------------------------------------------------
    /**
     * getter votes:
     * */

    public String getTotalVotes(){
        return totalVotes;
    }
    public String getPlanchaVotes(){
        return planchaVotes;
    }
    public String getParcialVotes(){
        return parcialVotes;
    }
    public String getCruzadoVotes(){
        return cruzadoVotes;
    }
    //----------------------------------------------------------------------------------------------
    /**
     * getter marcas:
     * */
    public String getTotalMarcas(){
        return totalMarcas;
    }
    public String getPlanchaMarcas(){
        return planchaMarcas;
    }
    public String getParcialMarcas(){
        return parcialMarcas;
    }
    public String getCruzadoMarcas(){
        return cruzadoMarcas;
    }
    //----------------------------------------------------------------------------------------------
    public String getEntryName(){
        return entryName;
    }
    //----------------------------------------------------------------------------------------------
    /**
     * setters votes and marks:
     */
    public void setTotalVotes(String totalVotes)
    {
        this.totalVotes = totalVotes;
    }
    public void setTotalMarcas(String totalMarcas){
        this.totalMarcas = totalMarcas;
    }
    public void setPlanchaVotes(String planchaVotes){
        this.planchaVotes = planchaVotes;
    }
    public void setPlanchaMarcas(String planchaMarcas){
        this.planchaMarcas = planchaMarcas;
    }
    public void setParcialVotes(String parcialVotes){
        this.parcialVotes = parcialVotes;
    }
    public void setParcialMarcas(String parcialMarcas){
        this.parcialMarcas = parcialMarcas;
    }
    public void setCruzadoVotes(String cruzadoVotes){
        this.cruzadoVotes = cruzadoVotes;
    }
    public void setCruzadoMarcas(String cruzadoMarcas){
        this.cruzadoMarcas = cruzadoMarcas;
    }
}
